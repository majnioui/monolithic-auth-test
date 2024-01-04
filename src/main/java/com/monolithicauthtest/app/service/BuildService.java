package com.monolithicauthtest.app.service;

import com.monolithicauthtest.app.domain.Gitrep;
import com.monolithicauthtest.app.domain.User;
import com.monolithicauthtest.app.repository.UserRepository;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class BuildService {

    private final RestTemplate restTemplate;
    private final AuthorizationService authorizationService;
    private final UserRepository userRepository;

    public BuildService(AuthorizationService authorizationService, UserRepository userRepository) {
        this.authorizationService = authorizationService;
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
    }

    public String suggestBuildpack(String repoName, String userLogin) {
        String userId = getUserIdByLogin(userLogin);
        String accessToken = authorizationService.retrieveAccessToken(Gitrep.PlatformType.GITHUB, userId);

        if (checkFileExistsInRepo(repoName, "pom.xml", accessToken, userLogin)) {
            return "Java Buildpack";
        } else if (checkFileExistsInRepo(repoName, "package.json", accessToken, userLogin)) {
            return "Node.js Buildpack";
        } else {
            return "Default Buildpack";
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
            // If the request is successful, the file exists
            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException e) {
            // If the file doesn't exist, GitHub API will return a 404 error
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw e; // Re-throw other exceptions
        }
    }

    private String getUserIdByLogin(String login) {
        Optional<User> userOpt = userRepository.findOneByLogin(login);
        return userOpt.map(User::getId).map(String::valueOf).orElse(null);
    }
}
