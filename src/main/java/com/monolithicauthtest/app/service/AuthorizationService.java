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
    public void updateAccessToken(String clientId, String accessToken, PlatformType platformType) {
        // Try to find an existing Gitrep entity
        Optional<Gitrep> existingGitrep = gitrepRepository.findByClientidAndPlatformType(clientId, platformType);
        Gitrep gitrep;
        if (existingGitrep.isPresent()) {
            // Update the existing entity
            gitrep = existingGitrep.get();
            gitrep.setAccesstoken(accessToken);
        } else {
            // Create a new entity if it doesn't exist
            gitrep = new Gitrep();
            gitrep.setClientid(clientId);
            gitrep.setAccesstoken(accessToken);
            gitrep.setPlatformType(platformType);
        }
        // Save the Gitrep entity
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
