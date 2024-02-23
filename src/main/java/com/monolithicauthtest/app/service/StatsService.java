package com.monolithicauthtest.app.service;

import com.monolithicauthtest.app.domain.InstanaApiToken;
import com.monolithicauthtest.app.repository.InstanaApiTokenRepository;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private RestTemplate restTemplate;

    private String apiToken = "";
    private String baseUrl = "";
    private static final Logger log = LoggerFactory.getLogger(StatsService.class);

    @Value("${settings.hcl.path:/home}")
    private String settingsHclSearchPath;

    @Autowired
    private InstanaApiTokenRepository instanaApiTokenRepository;

    @PostConstruct
    public void init() {
        // Retrieve the latest InstanaApiToken from the database
        InstanaApiToken instanaApiToken = instanaApiTokenRepository.findTopByOrderByIdDesc();
        if (instanaApiToken != null) {
            this.apiToken = instanaApiToken.getToken();
        } else {
            log.warn("No InstanaApiToken found.");
        }

        // Find settings.hcl file and extract baseUrl (aka our host url)
        String settingsFilePath = findSettingsHclFilePath();
        if (settingsFilePath == null || settingsFilePath.isEmpty()) {
            log.error("Settings file path is empty or not found.");
            // We can add here a default location in case the search fails
        } else {
            this.baseUrl = extractBaseUrlFromSettingsHcl(settingsFilePath);
        }
    }

    private String findSettingsHclFilePath() {
        try {
            Process process = new ProcessBuilder(
                "/bin/sh",
                "-c",
                "find " +
                settingsHclSearchPath +
                " -type f -name \"settings.hcl\" 2>/dev/null | awk '{print length, $0}' | sort -n | cut -d\" \" -f2- | head -n 1"
            )
                .start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                return reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String extractBaseUrlFromSettingsHcl(String filePath) {
        try {
            Process process = new ProcessBuilder("/bin/sh", "-c", "grep 'host_name' " + filePath + " | cut -d'=' -f2 | tr -d '\" '")
                .start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                return reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // General method for making GET requests
    private String makeGetRequest(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "apiToken " + this.apiToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(this.baseUrl + endpoint, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            return "{}"; // Return empty JSON in case of errors
        }
    }

    // Method for making POST requests
    private String makePostRequest(String endpoint, String jsonPayload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "apiToken " + this.apiToken);
        HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(this.baseUrl + endpoint, entity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            return "{}"; // Return empty JSON in case of errors
        }
    }

    public String getWebsiteMonitoringConfig() {
        String endpoint = "/api/website-monitoring/config";
        return makeGetRequest(endpoint);
    }

    // method to fetch host agent details
    public String getHostAgentDetails() {
        try {
            String hostAgentListUrl = "/api/host-agent/";
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
                String detailUrl = "/api/infrastructure-monitoring/snapshots";
                // Use makePostRequest to send the POST request
                return makePostRequest(detailUrl, payload.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{}";
    }

    public String getInstalledSoftware() {
        String endpoint = "/api/infrastructure-monitoring/software/versions";
        return makeGetRequest(endpoint);
    }

    public String getInfrastructureTopology() {
        String endpoint = "/api/infrastructure-monitoring/topology";
        return makeGetRequest(endpoint);
    }

    public String getAllEvents() {
        String endpoint = "/api/events?windowSize=86400000";
        return makeGetRequest(endpoint);
    }
}
