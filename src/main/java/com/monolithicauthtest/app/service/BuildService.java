package com.monolithicauthtest.app.service;

import com.monolithicauthtest.app.domain.Gitrep;
import com.monolithicauthtest.app.domain.User;
import com.monolithicauthtest.app.repository.GitrepRepository;
import com.monolithicauthtest.app.repository.UserRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
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
        String accessToken = authorizationService.retrieveAccessToken(Gitrep.PlatformType.GITHUB, userId);
        String githubUsername = getGithubUsername(userId);

        // Format the builders into a readable string
        String formattedBuilders = suggestedBuilders.stream().filter(s -> !s.trim().isEmpty()).collect(Collectors.joining("\n"));

        String builder = suggestedBuilders.isEmpty() ? "Default Builder" : formattedBuilders;
        if (checkFileExistsInRepo(repoName, "pom.xml", accessToken, githubUsername)) {
            return "Java: \n" + builder;
        } else if (checkFileExistsInRepo(repoName, "package.json", accessToken, githubUsername)) {
            return "NodeJs: \n" + builder;
        } else {
            return "Default: \n" + builder;
        }
    }

    private boolean checkFileExistsInRepo(String repoName, String fileName, String accessToken, String userLogin) {
        String fileApiUrl = "https://api.github.com/repos/" + userLogin + "/" + repoName + "/contents/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                fileApiUrl,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw e;
        }
    }

    private String getUserIdByLogin(String login) {
        Optional<User> userOpt = userRepository.findOneByLogin(login);
        return userOpt.map(User::getId).map(String::valueOf).orElse(null);
    }

    private String getGithubUsername(String userId) {
        Gitrep gitrep = gitrepRepository
            .findByClientidAndPlatformType(userId, Gitrep.PlatformType.GITHUB)
            .orElseThrow(() -> new IllegalStateException("Gitrep not found for userId: " + userId));
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
