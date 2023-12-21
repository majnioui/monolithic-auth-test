package com.monolithicauthtest.app.service;

import com.monolithicauthtest.app.domain.Gitrep;
import com.monolithicauthtest.app.domain.Gitrep.PlatformType;
import com.monolithicauthtest.app.repository.GitrepRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthorizationService {

    private final Logger log = LoggerFactory.getLogger(AuthorizationService.class);
    private final RestTemplate restTemplate;
    private final GitrepRepository gitrepRepository;

    // Github
    @Value("${github.client-id}")
    private String clientId;

    @Value("${github.client-secret}")
    private String clientSecret;

    // Gitlab
    @Value("${gitlab.client-id}")
    private String gitlabClientId;

    @Value("${gitlab.client-secret}")
    private String gitlabClientSecret;

    public AuthorizationService(
        GitrepRepository gitrepRepository,
        @Value("${github.client-id}") String clientId,
        @Value("${github.client-secret}") String clientSecret
    ) {
        this.restTemplate = new RestTemplate();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.gitrepRepository = gitrepRepository;
    }

    @Transactional
    public void updateAccessTokenAndUsername(String clientId, String accessToken, PlatformType platformType, String username) {
        // Similar to updateAccessToken but also sets the username
        Optional<Gitrep> existingGitrep = gitrepRepository.findByClientidAndPlatformType(clientId, platformType);
        Gitrep gitrep;
        if (existingGitrep.isPresent()) {
            gitrep = existingGitrep.get();
            gitrep.setAccesstoken(accessToken);
            gitrep.setUsername(username);
        } else {
            gitrep = new Gitrep();
            gitrep.setClientid(clientId);
            gitrep.setAccesstoken(accessToken);
            gitrep.setUsername(username);
            gitrep.setPlatformType(platformType);
        }
        gitrepRepository.save(gitrep);
    }

    public String getGitLabUsername(String accessToken) {
        // Retrieve the Gitrep entity for GitLab
        Optional<Gitrep> gitrep = gitrepRepository.findByClientidAndPlatformType("1001", Gitrep.PlatformType.GITLAB);
        String baseUrl = gitrep.map(Gitrep::getClientUrl).orElse("http://192.168.100.130");
        String uri = baseUrl.endsWith("/") ? baseUrl + "api/v4/user" : baseUrl + "/api/v4/user";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = response.getBody();
            return responseBody != null ? (String) responseBody.get("username") : null;
        } catch (Exception e) {
            log.error("Error while fetching GitLab user information", e);
            return null;
        }
    }

    public String getGitHubUsername(String accessToken) {
        // Retrieve the Gitrep entity for GitHub
        Optional<Gitrep> gitrep = gitrepRepository.findByClientidAndPlatformType("1001", Gitrep.PlatformType.GITHUB);
        String baseUrl = gitrep.map(Gitrep::getClientUrl).orElse("https://api.github.com");
        String uri = baseUrl.endsWith("/") ? baseUrl + "user" : baseUrl + "/user";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = response.getBody();
            return responseBody != null ? (String) responseBody.get("login") : null;
        } catch (Exception e) {
            log.error("Error while fetching GitHub user information", e);
            return null;
        }
    }

    public void createPullRequest(String platformType, String repoName) throws Exception {
        Optional<Gitrep> gitrepOpt = gitrepRepository.findByClientidAndPlatformType(
            "1001",
            platformType.equals("github") ? Gitrep.PlatformType.GITHUB : Gitrep.PlatformType.GITLAB
        );

        if (gitrepOpt.isPresent()) {
            Gitrep gitrep = gitrepOpt.get();
            String accessToken = gitrep.getAccesstoken();
            String username = gitrep.getUsername();

            if (platformType.equals("github")) {
                createGitHubPullRequest(accessToken, username, repoName);
            } else if (platformType.equals("gitlab")) {
                createGitLabPullRequest(accessToken, username, repoName);
            } else {
                throw new IllegalArgumentException("Unsupported platform");
            }
        } else {
            throw new IllegalStateException("Gitrep entity not found for the given platform");
        }
    }

    private void createGitHubPullRequest(String accessToken, String username, String repoName) throws Exception {
        String url = "https://api.github.com/repos/" + username + "/" + repoName + "/pulls";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> prDetails = new HashMap<>();
        prDetails.put("title", "Testing creating PR using API");
        prDetails.put("head", "master"); // Replace with your branch name
        prDetails.put("base", "main"); // Replace with your base branch
        prDetails.put("body", "Description of the PR");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(prDetails, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new Exception("Failed to create GitHub Pull Request: " + response.getBody());
        }
    }

    private void createGitLabPullRequest(String accessToken, String username, String repoName) throws Exception {
        String projectId = username + "%2F" + repoName; // GitLab requires the project ID to be URL-encoded
        String url = "http://192.168.100.130/api/v4/projects/" + projectId + "/merge_requests";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> mrDetails = new HashMap<>();
        mrDetails.put("title", "Your MR Title");
        mrDetails.put("source_branch", "master"); // Replace with your branch name
        mrDetails.put("target_branch", "main"); // Replace with your target branch
        mrDetails.put("description", "Description of the MR");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(mrDetails, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new Exception("Failed to create GitLab Merge Request: " + response.getBody());
        }
    }

    public String exchangeCodeForAccessToken(String code) {
        String uri = "https://github.com/login/oauth/access_token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("code", code);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("access_token")) {
                return (String) responseBody.get("access_token");
            } else {
                log.error("Access token not found in the response");
                return null;
            }
        } catch (Exception e) {
            log.error("Error while exchanging code for access token", e);
            return null;
        }
    }

    public String exchangeCodeForGitLabAccessToken(String code) {
        String uri = "http://192.168.100.130/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> params = new HashMap<>();
        params.put("client_id", gitlabClientId);
        params.put("client_secret", gitlabClientSecret);
        params.put("code", code);
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", "http://localhost:8080/login/oauth2/code/gitlab");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("access_token")) {
                return (String) responseBody.get("access_token");
            } else {
                log.error("GitLab access token not found in the response");
                return null;
            }
        } catch (HttpClientErrorException e) {
            log.error("HTTP error during GitLab token exchange: {}", e.getStatusCode());
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public List<Map<String, Object>> getRepositories() {
        String accessToken = retrieveAccessToken(Gitrep.PlatformType.GITHUB);
        if (accessToken == null) {
            return Collections.emptyList();
        }

        // Retrieve the Gitrep entity for GitHub
        Optional<Gitrep> gitrep = gitrepRepository.findByClientidAndPlatformType("1001", Gitrep.PlatformType.GITHUB);
        String baseUrl = gitrep.map(Gitrep::getClientUrl).orElse("https://api.github.com");
        String uri = baseUrl.endsWith("/") ? baseUrl + "user/repos" : baseUrl + "/user/repos";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Service: Error fetching GitHub repositories ", e);
            return Collections.emptyList();
        }
    }

    // Method to fetch repositories from GitLab
    public List<Map<String, Object>> getGitLabRepositories() {
        String accessToken = retrieveAccessToken(Gitrep.PlatformType.GITLAB);
        if (accessToken == null) {
            return Collections.emptyList();
        }

        // Retrieve the Gitrep entity for GitLab
        Optional<Gitrep> gitrep = gitrepRepository.findByClientidAndPlatformType("1001", Gitrep.PlatformType.GITLAB);
        String baseUrl = gitrep.map(Gitrep::getClientUrl).orElse("http://192.168.100.130");
        String uri = baseUrl.endsWith("/") ? baseUrl + "api/v4/projects" : baseUrl + "/api/v4/projects";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Service: Error fetching GitLab repositories", e);
            return Collections.emptyList();
        }
    }

    public String retrieveAccessToken(PlatformType platformType) {
        Optional<Gitrep> latestGitrep = gitrepRepository.findFirstByPlatformTypeOrderByCreatedAtDesc(platformType);
        return latestGitrep.map(Gitrep::getAccesstoken).orElse(null);
    }
}
