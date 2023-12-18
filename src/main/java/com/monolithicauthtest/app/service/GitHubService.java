package com.monolithicauthtest.app.service;

import com.monolithicauthtest.app.domain.Gitrep;
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
public class GitHubService {

    private final Logger log = LoggerFactory.getLogger(GitHubService.class);
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

    public GitHubService(
        GitrepRepository gitrepRepository,
        @Value("${github.client-id}") String clientId,
        @Value("${github.client-secret}") String clientSecret
    ) {
        this.restTemplate = new RestTemplate();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.gitrepRepository = gitrepRepository;

        // Log to confirm values are loaded
        log.info("GitHub Client ID: {}", clientId);
        log.info("GitHub Client Secret: {}", clientSecret);
    }

    @Transactional
    public void updateAccessToken(String clientId, String accessToken) {
        // Delete existing tokens for the client
        gitrepRepository.deleteByClientid(clientId);

        // Save the new token
        Gitrep gitrep = new Gitrep();
        gitrep.setClientid(clientId);
        gitrep.setAccesstoken(accessToken);
        gitrepRepository.save(gitrep);
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
            log.error("Response body: {}", e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("Error while exchanging code for GitLab access token", e);
            return null;
        }
    }

    public List<Map<String, Object>> getRepositories(String accessToken) {
        String uri = "https://api.github.com/user/repos";
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
            log.error("Service: Error fetching repositories ", e);
            return Collections.emptyList();
        }
    }

    // Method to fetch repositories from GitLab
    public List<Map<String, Object>> getGitLabRepositories() {
        String accessToken = retrieveAccessToken();
        if (accessToken == null) {
            return Collections.emptyList();
        }

        String uri = "http://192.168.100.130/api/v4/projects"; // GitLab API endpoint
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

    public String retrieveAccessToken() {
        Optional<Gitrep> latestGitrep = gitrepRepository.findFirstByOrderByCreatedAtDesc();
        if (latestGitrep.isPresent()) {
            return latestGitrep.get().getAccesstoken();
        }
        return null;
    }
}
