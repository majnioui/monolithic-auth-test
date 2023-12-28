package com.monolithicauthtest.app.web.rest;

import com.monolithicauthtest.app.service.RepositoryAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RepositoryAnalysisController {

    private final RepositoryAnalysisService repositoryAnalysisService;

    @Autowired
    public RepositoryAnalysisController(RepositoryAnalysisService repositoryAnalysisService) {
        this.repositoryAnalysisService = repositoryAnalysisService;
    }

    @GetMapping("/github/java-version")
    public ResponseEntity<String> getJavaVersionFromGithubRepo(@RequestParam String repoName, @RequestParam String clientId) {
        try {
            String javaVersion = repositoryAnalysisService.getJavaVersionFromGithubRepo(repoName, clientId);
            return ResponseEntity.ok(javaVersion);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching Java version: " + e.getMessage());
        }
    }
}
