package com.monolithicauthtest.app.service;

import com.monolithicauthtest.app.domain.Gitrep;
import com.monolithicauthtest.app.domain.User;
import com.monolithicauthtest.app.repository.GitrepRepository;
import com.monolithicauthtest.app.repository.UserRepository;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
public class BuildService {

    private static final Logger log = LoggerFactory.getLogger(BuildService.class);

    private final RestTemplate restTemplate;
    private final AuthorizationService authorizationService;
    private final UserRepository userRepository;
    private final GitrepRepository gitrepRepository;

    public BuildService(AuthorizationService authorizationService, UserRepository userRepository, GitrepRepository gitrepRepository) {
        this.authorizationService = authorizationService;
        this.userRepository = userRepository;
        this.gitrepRepository = gitrepRepository;
        this.restTemplate = new RestTemplate();
    }

    public String suggestBuildpack(String repoName, String userLogin) throws InterruptedException {
        List<String> suggestedBuilders;
        suggestedBuilders = getSuggestedBuilders();

        String userId = getUserIdByLogin(userLogin);
        if (userId == null) {
            throw new IllegalStateException("User not found for login: " + userLogin);
        }

        // Format the builders into a readable string
        String formattedBuilders = suggestedBuilders.stream().filter(s -> !s.trim().isEmpty()).collect(Collectors.joining("\n"));

        String builder = suggestedBuilders.isEmpty() ? "Default Builder" : formattedBuilders;
        return "Default: \n" + builder;
    }

    public void cloneRepositoryForUser(String repoName, String userLogin, Gitrep.PlatformType platformType) throws GitAPIException {
        log.debug("Starting cloneRepositoryForUser with repoName: {}, userLogin: {}, platformType: {}", repoName, userLogin, platformType);

        String userId = getUserIdByLogin(userLogin);
        if (userId == null) {
            throw new IllegalStateException("User not found for login: " + userLogin);
        }
        String accessToken = authorizationService.retrieveAccessToken(platformType, userId);
        if (accessToken == null) {
            throw new IllegalStateException("No access token available for " + platformType);
        }

        String username = getUsernameFromGitrep(userId, platformType);
        log.debug("Username retrieved for platform {}: {}", platformType, username);

        String repoUrl = getRepoCloneUrl(platformType, username, repoName, accessToken);

        cloneRepository(repoUrl, repoName, accessToken, platformType);
        log.info("Repository cloning completed for {}", repoName);
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
        String repoApiUrl = "https://api.github.com/repos/" + username + "/" + repoName;
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
        String projectsApiUrl = "http://192.168.100.130/api/v4/users/" + username + "/projects";
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
        String repoApiUrl = "http://192.168.100.130/api/v4/projects/" + projectId;
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
        String repoApiUrl = "https://api.bitbucket.org/2.0/repositories/mo-flow-test/" + repoName;
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

    private List<String> getSuggestedBuilders() {
        List<String> builders = new ArrayList<>();
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "pack builder suggest");

        try {
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
        } catch (IOException | InterruptedException e) {
            log.error("Exception in getSuggestedBuilders: {}", e.getMessage(), e);
        }

        return builders;
    }
}