package com.monolithicauthtest.app.web.rest;

import com.monolithicauthtest.app.service.StatsService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/api/stats")
    public String getWebsiteMonitoringConfig() {
        return statsService.getWebsiteMonitoringConfig();
    }

    @GetMapping("/api/host-agent")
    public String getHostAgentDetails() throws JSONException {
        return statsService.getHostAgentDetails();
    }
}
