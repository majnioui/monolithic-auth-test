package com.monolithicauthtest.app.web.rest;

import com.monolithicauthtest.app.service.BuildService;
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
    public ResponseEntity<String> suggestBuildpack(@RequestParam String repoName) {
        String suggestedBuildpack = buildService.suggestBuildpack(repoName);
        return ResponseEntity.ok(suggestedBuildpack);
    }
}
