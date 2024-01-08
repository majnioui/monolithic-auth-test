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

    public void cloneRepositoryForUser(String repoName, String userLogin) throws GitAPIException {
        log.debug("Starting cloneRepositoryForUser with repoName: {} and userLogin: {}", repoName, userLogin);

        // Retrieve user ID by login
        String userId = getUserIdByLogin(userLogin);
        if (userId == null) {
            log.error("User not found for login: {}", userLogin);
            throw new IllegalStateException("User not found for login: " + userLogin);
        }
        log.debug("User ID retrieved for login {}: {}", userLogin, userId);

        // Retrieve GitHub username using the userId
        String username = getGithubUsername(userId);
        log.debug("GitHub username retrieved: {}", username);

        // Retrieve access token
        String accessToken = authorizationService.retrieveAccessToken(Gitrep.PlatformType.GITHUB, userId);
        if (accessToken == null) {
            log.error("No access token available for GitHub for userId: {}", userId);
            throw new IllegalStateException("No access token available for GitHub");
        }

        String repoUrl = getGithubRepoCloneUrl(username, repoName, accessToken);

        String localPath = System.getProperty("user.dir");
        log.debug("Cloning repository from URL: {} to local path: {}", repoUrl, localPath);

        cloneRepository(repoUrl, repoName);
        log.info("Repository cloning completed for {}", repoName);
    }

    private String getGithubRepoCloneUrl(String username, String repoName, String accessToken) {
        log.debug("Fetching GitHub repo clone URL for repoName: {} and username: {}", repoName, username);

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

    private void cloneRepository(String repoUrl, String repoName) throws GitAPIException {
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
        Git.cloneRepository().setURI(repoUrl).setDirectory(repoDir).call();
        log.info("Repository cloned successfully into {}", repoDirPath);
    }

    private String getUserIdByLogin(String login) {
        Optional<User> userOpt = userRepository.findOneByLogin(login);
        return userOpt.map(User::getId).map(String::valueOf).orElse(null);
    }

    private String getGithubUsername(String userId) {
        log.debug("Retrieving GitHub username for userId: {}", userId);
        Optional<Gitrep> gitrepOptional = gitrepRepository.findByClientidAndPlatformType(userId, Gitrep.PlatformType.GITHUB);

        if (!gitrepOptional.isPresent()) {
            log.error("Gitrep not found for userId: {}", userId);
            throw new IllegalStateException("Gitrep not found for userId: " + userId);
        }

        Gitrep gitrep = gitrepOptional.get();
        log.debug("Found Gitrep for userId: {} with username: {}", userId, gitrep.getUsername());
        return gitrep.getUsername();
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
