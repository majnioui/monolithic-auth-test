package com.monolithicauthtest.app.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class StatsService {

    private final String apiUrl = "https://turquoise-domino.instana.io/api/website-monitoring/config";
    private final String apiToken = "Jh_oKNVXSYuNQXTXGnQHyw";

    public String getWebsiteMonitoringConfig() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "apiToken " + apiToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }

    // method to fetch host agent details
    public String getHostAgentDetails() {
        try {
            // First, fetch the list of host agents to get snapshot IDs
            String hostAgentListUrl = "https://turquoise-domino.instana.io/api/host-agent/";
            String response = fetchFromApi(hostAgentListUrl);
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray items = jsonResponse.getJSONArray("items");
            if (items.length() > 0) {
                JSONArray snapshotIds = new JSONArray();
                for (int i = 0; i < items.length(); i++) {
                    snapshotIds.put(items.getJSONObject(i).getString("snapshotId"));
                }

                // Use the snapshot IDs to fetch detailed information
                String detailUrl = "https://turquoise-domino.instana.io/api/infrastructure-monitoring/snapshots";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "apiToken " + apiToken);
                JSONObject payload = new JSONObject();
                payload.put("snapshotIds", snapshotIds);

                HttpEntity<String> entity = new HttpEntity<>(payload.toString(), headers);
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> detailResponse = restTemplate.postForEntity(detailUrl, entity, String.class);

                if (detailResponse.getStatusCode() == HttpStatus.OK) {
                    return detailResponse.getBody();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{}"; // Return empty JSON in case of failure
    }

    private String fetchFromApi(String url) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "apiToken " + apiToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }
}
