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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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

    // Bitbucket
    @Value("${bitbucket.client-id}")
    private String bitbucketClientId;

    @Value("${bitbucket.client-secret}")
    private String bitbucketClientSecret;

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
        log.info("Updating Gitrep for clientId: {}, platformType: {}", clientId, platformType);
        gitrepRepository.save(gitrep);
    }

    public String getGitLabUsername(String accessToken, String userId) {
        Optional<Gitrep> gitrep = gitrepRepository.findByClientidAndPlatformType(userId, Gitrep.PlatformType.GITLAB);
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

    public String getGitHubUsername(String accessToken, String userId) {
        Optional<Gitrep> gitrep = gitrepRepository.findByClientidAndPlatformType(userId, Gitrep.PlatformType.GITHUB);
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

    public String getBitbucketUsername(String accessToken, String userId) {
        String uri = "https://api.bitbucket.org/2.0/user";

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
            log.error("Error while fetching Bitbucket user information", e);
            return null;
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
        String uri = "http://192.168.100.130/oauth/token"; // CHANGE THIS

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> params = new HashMap<>();
        params.put("client_id", gitlabClientId);
        params.put("client_secret", gitlabClientSecret);
        params.put("code", code);
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", "http://localhost:8080/login/oauth2/code/gitlab"); // CHANGE THIS

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

    public String exchangeCodeForBitbucketAccessToken(String code) {
        String uri = "https://bitbucket.org/site/oauth2/access_token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", bitbucketClientId);
        params.add("client_secret", bitbucketClientSecret);
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", "http://localhost:8080/login/oauth2/code/bitbucket"); // CHANGE THIS

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

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
                log.error("Bitbucket access token not found in the response");
                return null;
            }
        } catch (Exception e) {
            log.error("Error while exchanging code for Bitbucket access token", e);
            return null;
        }
    }

    public List<Map<String, Object>> getRepositories(String userId) {
        String accessToken = retrieveAccessToken(Gitrep.PlatformType.GITHUB, userId);
        if (accessToken == null) {
            return Collections.emptyList();
        }

        Optional<Gitrep> gitrep = gitrepRepository.findByClientidAndPlatformType(userId, Gitrep.PlatformType.GITHUB);
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
            log.error("Service: Error fetching GitHub repositories", e);
            return Collections.emptyList();
        }
    }

    // Method to fetch repositories from GitLab
    public List<Map<String, Object>> getGitLabRepositories(String userId) {
        String accessToken = retrieveAccessToken(Gitrep.PlatformType.GITLAB, userId);
        if (accessToken == null) {
            return Collections.emptyList();
        }

        Optional<Gitrep> gitrep = gitrepRepository.findByClientidAndPlatformType(userId, Gitrep.PlatformType.GITLAB);
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

    // Method to fetch repositories from Bitbucket
    public List<Map<String, Object>> getBitbucketRepositories(String userId) {
        String accessToken = retrieveAccessToken(Gitrep.PlatformType.BITBUCKET, userId);
        if (accessToken == null) {
            return Collections.emptyList();
        }

        Optional<Gitrep> gitrep = gitrepRepository.findByClientidAndPlatformType(userId, Gitrep.PlatformType.BITBUCKET);
        String baseUrl = gitrep.map(Gitrep::getClientUrl).orElse("https://api.bitbucket.org/2.0/repositories/");
        String uri = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";

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
            if (responseBody != null && responseBody.containsKey("values")) {
                Object valuesObj = responseBody.get("values");
                if (valuesObj instanceof List<?>) {
                    List<?> tempList = (List<?>) valuesObj;
                    if (!tempList.isEmpty() && tempList.get(0) instanceof Map<?, ?>) {
                        // Safe to cast
                        return (List<Map<String, Object>>) tempList;
                    }
                }
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Service: Error fetching Bitbucket repositories", e);
            return Collections.emptyList();
        }
    }

    public String retrieveAccessToken(PlatformType platformType, String userId) {
        Optional<Gitrep> latestGitrep = gitrepRepository.findByClientidAndPlatformType(userId, platformType);
        return latestGitrep.map(Gitrep::getAccesstoken).orElse(null);
    }
}
