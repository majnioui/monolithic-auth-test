package com.monolithicauthtest.app.web.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.monolithicauthtest.app.domain.Gitrep;
import com.monolithicauthtest.app.service.BuildService;

@RestController
public class BuildController {

    private final BuildService buildService;
    private static final Logger log = LoggerFactory.getLogger(BuildService.class);

    public BuildController(BuildService buildService) {
        this.buildService = buildService;
    }

    @GetMapping("/suggest-buildpack")
    public ResponseEntity<Map<String, String>> suggestBuildpack(
        @RequestParam String repoName,
        @RequestParam String userLogin,
        @RequestParam Gitrep.PlatformType platformType
    ) {
        try {
            String buildpack = buildService.suggestBuildpack(repoName, userLogin, platformType);
            Map<String, String> response = new HashMap<>();
            response.put("buildpack", buildpack);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error suggesting buildpack: " + e.getMessage()));
        }
    }

    @PostMapping("/clone-repo")
    public ResponseEntity<?> cloneRepository(
        @RequestParam String repoName,
        @RequestParam String userLogin,
        @RequestParam String platformType
    ) {
        log.info("Received request to clone repo: {}, userLogin: {}, platformType: {}", repoName, userLogin, platformType);
        try {
            Gitrep.PlatformType platformTypeEnum = Gitrep.PlatformType.valueOf(platformType.toUpperCase());
            buildService.cloneRepositoryForUser(repoName, userLogin, platformTypeEnum);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid platform type: " + platformType);
        } catch (GitAPIException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/execute-build-command")
    public ResponseEntity<?> executeBuildCommand(
        @RequestParam String repoName,
        @RequestParam String userLogin,
        @RequestParam String platformType,
        @RequestParam String command
    ) {
        try {
            Gitrep.PlatformType platformTypeEnum = Gitrep.PlatformType.valueOf(platformType.toUpperCase());
            buildService.executeCustomBuildCommand(repoName, userLogin, platformTypeEnum, command);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | GitAPIException | InterruptedException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error executing build command: " + e.getMessage());
        }
    }

    @PostMapping("/push-to-registry")
    public ResponseEntity<?> pushToRegistry(
        @RequestParam String imageName,
        @RequestParam String username,
        @RequestParam String password,
        @RequestParam String repositoryName,
        @RequestParam String registryType
    ) {
        try {
            buildService.pushImageToRegistry(imageName, username, password, repositoryName, registryType);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error pushing image to registry: " + e.getMessage());
        }
    }

}
