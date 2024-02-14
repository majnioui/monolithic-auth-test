package com.monolithicauthtest.app.service;

import com.monolithicauthtest.app.domain.InstanaApiToken;
import com.monolithicauthtest.app.repository.InstanaApiTokenRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class StatsService {

    @Autowired
    private InstanaApiTokenRepository instanaApiTokenRepository;

    // Method to get the API token saved in our instanaApiToken entity
    private String getApiToken() {
        InstanaApiToken instanaApiToken = instanaApiTokenRepository.findTopByOrderByIdDesc();
        return instanaApiToken != null ? instanaApiToken.getToken() : "";
    }

    // General method for making GET requests
    private String makeGetRequest(String url) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "apiToken " + getApiToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            return "{}"; // Return empty JSON in case of errors
        }
    }

    // Method for making POST requests
    private String makePostRequest(String url, String jsonPayload) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "apiToken " + getApiToken());
        HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            return "{}"; // Return empty JSON in case of errors
        }
    }

    public String getWebsiteMonitoringConfig() {
        String url = "https://orchid-frata0esw3o.instana.io/api/website-monitoring/config";
        return makeGetRequest(url);
    }

    // method to fetch host agent details
    public String getHostAgentDetails() {
        try {
            String hostAgentListUrl = "https://orchid-frata0esw3o.instana.io/api/host-agent/";
            String response = makeGetRequest(hostAgentListUrl);
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray items = jsonResponse.getJSONArray("items");
            if (items.length() > 0) {
                JSONArray snapshotIds = new JSONArray();
                for (int i = 0; i < items.length(); i++) {
                    snapshotIds.put(items.getJSONObject(i).getString("snapshotId"));
                }
                // Construct JSON payload for POST request
                JSONObject payload = new JSONObject();
                payload.put("snapshotIds", snapshotIds);
                String detailUrl = "https://orchid-frata0esw3o.instana.io/api/infrastructure-monitoring/snapshots";
                // Use makePostRequest to send the POST request
                return makePostRequest(detailUrl, payload.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{}";
    }

    public String getInstalledSoftware() {
        String url = "https://orchid-frata0esw3o.instana.io/api/infrastructure-monitoring/software/versions";
        return makeGetRequest(url);
    }

    public String getInfrastructureTopology() {
        String url = "https://orchid-frata0esw3o.instana.io/api/infrastructure-monitoring/topology";
        return makeGetRequest(url);
    }

    public String getAllEvents() {
        String url = "https://orchid-frata0esw3o.instana.io/api/events?windowSize=86400000";
        return makeGetRequest(url);
    }
}
