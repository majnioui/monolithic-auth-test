package com.monolithicauthtest.app.service;

import com.monolithicauthtest.app.domain.Docker;
import com.monolithicauthtest.app.domain.Gitrep;
import com.monolithicauthtest.app.domain.User;
import com.monolithicauthtest.app.repository.DockerRepository;
import com.monolithicauthtest.app.repository.GitrepRepository;
import com.monolithicauthtest.app.repository.UserRepository;
import com.monolithicauthtest.app.security.SecurityUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@SuppressWarnings("unchecked")
public class BuildService {

    private static final Logger log = LoggerFactory.getLogger(BuildService.class);

    private final RestTemplate restTemplate;
    private final AuthorizationService authorizationService;
    private final UserRepository userRepository;
    private final GitrepRepository gitrepRepository;
    private final DockerRepository dockerRepository;

    public BuildService(
        AuthorizationService authorizationService,
        UserRepository userRepository,
        GitrepRepository gitrepRepository,
        DockerRepository dockerRepository
    ) {
        this.authorizationService = authorizationService;
        this.userRepository = userRepository;
        this.gitrepRepository = gitrepRepository;
        this.restTemplate = new RestTemplate();
        this.dockerRepository = dockerRepository;
    }

    // Method to suggest the pack builders
    public String suggestBuildpack(String repoName, String userLogin, Gitrep.PlatformType platformType)
        throws GitAPIException, InterruptedException, IOException {
        // Clone the repository first, if already done it will be skipped
        cloneRepositoryForUser(repoName, userLogin, platformType);

        // Define the path to the cloned repository
        String repoPath = System.getProperty("user.dir") + File.separator + repoName;

        // Execute 'pack builder suggest' in the cloned repository's directory
        List<String> builders = new ArrayList<>();
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(repoPath));
        processBuilder.command("bash", "-c", "pack builder suggest");

        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            builders.add(line);
            log.debug("Parsed builder: {}", line);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("pack builder suggest command exited with error code: {}", exitCode);
        }

        // Format the builders into a readable string
        String formattedBuilders = builders.stream().filter(s -> !s.trim().isEmpty()).collect(Collectors.joining("\n"));
        return formattedBuilders;
    }

    public void cloneRepositoryForUser(String repoName, String userLogin, Gitrep.PlatformType platformType) throws GitAPIException {
        log.debug(
            "Checking if repository already exists for repoName: {}, userLogin: {}, platformType: {}",
            repoName,
            userLogin,
            platformType
        );

        String repoDirPath = System.getProperty("user.dir") + File.separator + repoName;
        File repoDir = new File(repoDirPath);

        if (repoDir.exists() && repoDir.isDirectory()) {
            log.info("Repository already exists at: {}. Skipping cloning.", repoDirPath);
            return; // Skip cloning if the directory already exists (to avoid duplication)
        }

        String accessToken = authorizationService.retrieveAccessToken(platformType, getUserIdByLogin(userLogin));
        if (accessToken == null) {
            throw new IllegalStateException("No access token available for " + platformType);
        }

        String username = getUsernameFromGitrep(getUserIdByLogin(userLogin), platformType);
        String repoUrl = getRepoCloneUrl(platformType, username, repoName, accessToken);

        cloneRepository(repoUrl, repoName, accessToken, platformType);
        log.info("Repository cloned successfully into {}", repoDirPath);
    }

    public void executeCustomBuildCommand(String repoName, String userLogin, Gitrep.PlatformType platformType, String command)
        throws GitAPIException, InterruptedException, IOException {
        // Ensure the repository is cloned first
        cloneRepositoryForUser(repoName, userLogin, platformType);

        // Define the path to the cloned repository
        String repoPath = System.getProperty("user.dir") + File.separator + repoName;

        // Format the date in the desired format
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());

        // Construct the full command with the hardcoded part and the variable part
        String fullCommand = "sudo pack build rkube-" + date + " --builder " + command;

        // Execute the custom build command in the cloned repository's directory
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(repoPath));
        processBuilder.command("bash", "-c", fullCommand);

        Process process = processBuilder.start();

        // Read standard output and error streams
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))
        ) {
            // Reading standard output
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("Command output: {}", line);
            }

            // Reading standard error
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                log.error("Command error: {}", errorLine);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("Custom build command exited with error code: {}", exitCode);
            throw new IllegalStateException("Custom build command failed with error code: " + exitCode);
        }

        log.info("Custom build command executed successfully in {}", repoPath);

        // Define the path for the .tar file
        String tarFilePath = System.getProperty("user.dir") + File.separator + "rkube-" + date + ".tar";

        // Save the image as a .tar file
        String saveCommand = "sudo docker save -o " + tarFilePath + " rkube-" + date;
        processBuilder.command("bash", "-c", saveCommand);
        Process saveProcess = processBuilder.start();

        // Read the output and error streams for the save process
        try (
            BufferedReader saveReader = new BufferedReader(new InputStreamReader(saveProcess.getInputStream()));
            BufferedReader saveErrorReader = new BufferedReader(new InputStreamReader(saveProcess.getErrorStream()))
        ) {
            // Reading save command output
            String saveLine;
            while ((saveLine = saveReader.readLine()) != null) {
                log.info("Save command output: {}", saveLine);
            }

            // Reading save command error
            String saveErrorLine;
            while ((saveErrorLine = saveErrorReader.readLine()) != null) {
                log.error("Save command error: {}", saveErrorLine);
            }
        }

        int saveExitCode = saveProcess.waitFor();
        if (saveExitCode != 0) {
            log.error("Failed to save the image as a .tar file with error code: {}", saveExitCode);
            throw new IllegalStateException("Failed to save the image with error code: " + saveExitCode);
        }
        log.info("Image saved successfully as a .tar file in {}", tarFilePath);
    }

    // Method to fetch all Docker entities
    public List<Docker> getAllDockerEntities() {
        return dockerRepository.findAll();
    }

    public void pushImageToRegistry(String imageName, String username, String repositoryName, String registryType)
        throws IOException, InterruptedException {
        imageName = imageName.toLowerCase();

        // Use different separator based on registry type
        String separator = registryType.equals("quay") ? "/" : ":";
        String taggedImageName = (registryType.equals("quay") ? "quay.io/" : "") + username + "/" + repositoryName + separator + imageName;

        log.info("Starting to tag the image: {}", imageName);

        // Tag the image
        ProcessBuilder tagProcessBuilder = new ProcessBuilder();
        tagProcessBuilder.command("bash", "-c", "sudo docker tag " + imageName + " " + taggedImageName);
        tagProcessBuilder.redirectErrorStream(true);
        Process tagProcess = tagProcessBuilder.start();

        // Read the output from the command
        BufferedReader reader = new BufferedReader(new InputStreamReader(tagProcess.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            log.info(line);
        }

        int tagExitCode = tagProcess.waitFor();
        if (tagExitCode != 0) {
            log.error("Failed to tag the image. Exit code: {}", tagExitCode);
            throw new IllegalStateException("Failed to tag the image with error code: " + tagExitCode);
        } else {
            log.info("Image tagged successfully with name: {}", taggedImageName);
        }

        // Push the image
        ProcessBuilder pushProcessBuilder = new ProcessBuilder();
        String pushCommand = registryType.equals("quay") ? "sudo podman push " : "sudo docker push ";
        pushProcessBuilder.command("bash", "-c", pushCommand + taggedImageName);
        Process pushProcess = pushProcessBuilder.start();
        int pushExitCode = pushProcess.waitFor();
        if (pushExitCode != 0) {
            log.error("Failed to push the image. Exit code: {}", pushExitCode);
            throw new IllegalStateException("Failed to push the image with error code: " + pushExitCode);
        } else {
            log.info("Image successfully pushed to {}: {}", registryType, taggedImageName);
        }

        // Save the Docker entity
        Docker docker = new Docker();
        docker.setUsername(username);
        docker.setRepoName(repositoryName);
        dockerRepository.save(docker);
    }

    private String getRepoCloneUrl(Gitrep.PlatformType platformType, String username, String repoName, String accessToken) {
        switch (platformType) {
            case GITHUB:
                return getGithubRepoCloneUrl(username, repoName, accessToken);
            case GITLAB:
                int projectId = getGitlabProjectId(username, repoName, accessToken);
                return getGitlabRepoCloneUrl(projectId, accessToken);
            case BITBUCKET:
                return getBitbucketRepoCloneUrl(username, repoName, accessToken);
            default:
                throw new IllegalStateException("Unsupported platform type: " + platformType);
        }
    }

    private String getGithubRepoCloneUrl(String username, String repoName, String accessToken) {
        // Retrieve the Gitrep entity
        String clientId = getCurrentUserId();
        Optional<Gitrep> gitrep = gitrepRepository.findByClientidAndPlatformType(clientId, Gitrep.PlatformType.GITHUB);
        // Use the URL from Gitrep or fallback to the default GitHub URL
        String baseUrl = gitrep.map(Gitrep::getClientUrl).orElse("https://api.github.com");
        String repoApiUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "repos/" + username + "/" + repoName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            repoApiUrl,
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("clone_url")) {
            String cloneUrl = (String) responseBody.get("clone_url");
            log.debug("Clone URL retrieved: {}", cloneUrl);
            return cloneUrl;
        } else {
            log.error("Unable to retrieve clone URL for the repository: {}", repoName);
            throw new IllegalStateException("Unable to retrieve clone URL for the repository");
        }
    }

    private int getGitlabProjectId(String username, String repoName, String accessToken) {
        // Retrieve the Gitrep entity
        String clientId = getCurrentUserId();
        Optional<Gitrep> gitrep = gitrepRepository.findByClientidAndPlatformType(clientId, Gitrep.PlatformType.GITLAB);
        // Use the URL from Gitrep or fallback to the default GitLab URL
        String baseUrl = gitrep.map(Gitrep::getClientUrl).orElse("http://192.168.100.130");
        String projectsApiUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "api/v4/users/" + username + "/projects";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                projectsApiUrl,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            List<Map<String, Object>> projects = response.getBody();
            if (projects != null) {
                for (Map<String, Object> project : projects) {
                    String projectName = (String) project.get("name");
                    if (repoName.equals(projectName)) {
                        return (Integer) project.get("id");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error while fetching GitLab projects for user: " + username, e);
        }

        throw new IllegalStateException("Project ID not found for repoName: " + repoName);
    }

    private String getGitlabRepoCloneUrl(int projectId, String accessToken) {
        // Retrieve the Gitrep entity
        String clientId = getCurrentUserId();
        Optional<Gitrep> gitrep = gitrepRepository.findByClientidAndPlatformType(clientId, Gitrep.PlatformType.GITLAB);
        // Use the URL from Gitrep or fallback to the default GitLab URL
        String baseUrl = gitrep.map(Gitrep::getClientUrl).orElse("http://192.168.100.130");
        String repoApiUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "api/v4/projects/" + projectId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            repoApiUrl,
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("http_url_to_repo")) {
            return (String) responseBody.get("http_url_to_repo");
        } else {
            throw new IllegalStateException("Unable to retrieve clone URL for the GitLab repository with ID: " + projectId);
        }
    }

    private String getBitbucketRepoCloneUrl(String username, String repoName, String accessToken) {
        // Retrieve the Gitrep entity
        String clientId = getCurrentUserId();
        Optional<Gitrep> gitrep = gitrepRepository.findByClientidAndPlatformType(clientId, Gitrep.PlatformType.BITBUCKET);
        // Use the URL from Gitrep or fallback to the default Bitbucket URL
        String baseUrl = gitrep.map(Gitrep::getClientUrl).orElse("https://api.bitbucket.org/2.0");
        String repoApiUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "repositories/" + username + "/" + repoName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            repoApiUrl,
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("links") && responseBody.get("links") instanceof Map) {
            Map<String, Object> links = (Map<String, Object>) responseBody.get("links");
            if (links.containsKey("clone")) {
                List<Map<String, String>> cloneLinks = (List<Map<String, String>>) links.get("clone");
                return cloneLinks
                    .stream()
                    .filter(link -> "https".equals(link.get("name")))
                    .findFirst()
                    .map(link -> link.get("href"))
                    .orElseThrow(() -> new IllegalStateException("HTTPS clone URL not found for the Bitbucket repository: " + repoName));
            }
        }
        throw new IllegalStateException("Unable to retrieve clone URL for the Bitbucket repository: " + repoName);
    }

    private String getUsernameFromGitrep(String userId, Gitrep.PlatformType platformType) {
        log.debug("Attempting to find Gitrep for userId: {} and platformType: {}", userId, platformType);
        Optional<Gitrep> gitrepOpt = gitrepRepository.findByClientidAndPlatformType(userId, platformType);
        gitrepOpt.ifPresent(gitrep -> log.debug("Found Gitrep: {}", gitrep));
        return gitrepOpt
            .map(Gitrep::getUsername)
            .orElseThrow(() -> new IllegalStateException("Username not found for userId: " + userId + " and platform: " + platformType));
    }

    // Getting the current logged in user ID directly.
    private String getCurrentUserId() {
        return SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .map(User::getId)
            .map(String::valueOf)
            .orElse(null);
    }

    private void cloneRepository(String repoUrl, String repoName, String accessToken, Gitrep.PlatformType platformType)
        throws GitAPIException {
        String currentWorkingDir = System.getProperty("user.dir");
        String repoDirPath = currentWorkingDir + File.separator + repoName;
        File repoDir = new File(repoDirPath);

        if (repoDir.exists()) {
            log.error("Repository directory already exists: {}", repoDirPath);
            throw new IllegalStateException("Repository directory already exists");
        }

        if (!repoDir.mkdir()) {
            log.error("Failed to create directory for repository: {}", repoDirPath);
            throw new IllegalStateException("Failed to create directory for repository");
        }

        log.debug("Cloning repository from URL: {} into {}", repoUrl, repoDirPath);

        CredentialsProvider credentialsProvider;
        if (platformType == Gitrep.PlatformType.BITBUCKET) {
            // For Bitbucket, use the access token as the password with a generic username
            credentialsProvider = new UsernamePasswordCredentialsProvider("x-token-auth", accessToken);
        } else {
            // For GitHub and GitLab, use the OAuth token
            credentialsProvider = new UsernamePasswordCredentialsProvider("oauth2", accessToken);
        }

        // Clone the repository
        Git.cloneRepository().setURI(repoUrl).setDirectory(repoDir).setCredentialsProvider(credentialsProvider).call();

        log.info("Repository cloned successfully into {}", repoDirPath);
    }

    private String getUserIdByLogin(String login) {
        Optional<User> userOpt = userRepository.findOneByLogin(login);
        return userOpt.map(User::getId).map(String::valueOf).orElse(null);
    }
}
