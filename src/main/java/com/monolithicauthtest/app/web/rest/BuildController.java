package com.monolithicauthtest.app.web.rest;

import com.monolithicauthtest.app.service.BuildService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BuildController {

    private final BuildService buildService;

    public BuildController(BuildService buildService) {
        this.buildService = buildService;
    }

    @GetMapping("/suggest-buildpack")
    public ResponseEntity<Map<String, String>> suggestBuildpack(@RequestParam String repoName, @RequestParam String userLogin) {
        try {
            String buildpack = buildService.suggestBuildpack(repoName, userLogin);
            Map<String, String> response = new HashMap<>();
            response.put("buildpack", buildpack);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error suggesting buildpack: " + e.getMessage()));
        }
    }
}
