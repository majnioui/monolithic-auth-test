package com.monolithicauthtest.app.web.rest;

import com.monolithicauthtest.app.domain.Gitrep;
import com.monolithicauthtest.app.repository.GitrepRepository;
import com.monolithicauthtest.app.service.AuthorizationService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthorizationController {

    @Value("${github.client-id}")
    private String clientId;

    @Value("${gitlab.client-id}")
    private String gitlabClientId;

    private final AuthorizationService authorizationService;
    private final GitrepRepository gitrepRepository;

    private static final Logger log = LoggerFactory.getLogger(AuthorizationController.class);

    public AuthorizationController(AuthorizationService authorizationService, GitrepRepository gitrepRepository) {
        this.authorizationService = authorizationService;
        this.gitrepRepository = gitrepRepository;
    }

    @PostMapping("/api/generate-pr")
    public ResponseEntity<?> generatePullRequest(@RequestBody Map<String, String> requestData) {
        String repoName = requestData.get("repoName");
        String platformType = requestData.get("platformType");
        try {
            authorizationService.createPullRequest(platformType, repoName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error generating PR", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create PR");
        }
    }

    @PostMapping("/api/save-client-url")
    public ResponseEntity<Void> saveClientUrl(@RequestBody Map<String, String> request) {
        String clientUrl = request.get("clientUrl");
        String platformType = request.get("platformType");
        final String clientId = "1001"; // Specific client ID

        // Check if clientUrl is empty or null, and return if it is
        if (clientUrl == null || clientUrl.trim().isEmpty()) {
            return ResponseEntity.ok().build(); // or use ResponseEntity.badRequest().build() to indicate a bad request
        }

        // Check if a Gitrep entity exists with the same clientId and platformType
        Optional<Gitrep> existingGitrep = gitrepRepository.findByClientidAndPlatformType(
            clientId,
            Gitrep.PlatformType.valueOf(platformType.toUpperCase())
        );

        Gitrep gitrep;
        if (existingGitrep.isPresent()) {
            // Update the existing entity
            gitrep = existingGitrep.get();
            gitrep.setClientUrl(clientUrl);
        } else {
            // Create a new entity if it doesn't exist
            gitrep = new Gitrep();
            gitrep.setClientid(clientId);
            gitrep.setAccesstoken("XXX"); // Set the access token
            gitrep.setClientUrl(clientUrl);
            gitrep.setPlatformType(Gitrep.PlatformType.valueOf(platformType.toUpperCase()));
        }

        gitrepRepository.save(gitrep);
        return ResponseEntity.ok().build();
    }

    // Github Authorization
    @GetMapping("/authorize-github")
    public void initiateAuthorization(HttpServletResponse response) throws IOException {
        String scopes = "read:user,repo";
        String redirectUrl = "https://github.com/login/oauth/authorize?client_id=" + clientId + "&scope=" + scopes;
        response.sendRedirect(redirectUrl);
    }

    // GitLab Authorization
    @GetMapping("/authorize-gitlab")
    public void initiateGitLabAuthorization(HttpServletResponse response) throws IOException {
        String redirectUrl =
            "http://192.168.100.130/oauth/authorize?client_id=" +
            gitlabClientId +
            "&response_type=code&redirect_uri=http://localhost:8080/login/oauth2/code/gitlab"; // localhost:8080 to be changed with the real path
        response.sendRedirect(redirectUrl);
    }

    // Github OAuth Callback
    @GetMapping("/login/oauth2/code/github")
    public void handleGitHubRedirect(@RequestParam("code") String code, HttpServletResponse response) {
        log.info("GitHub callback triggered with code: {}", code);
        try {
            String accessToken = authorizationService.exchangeCodeForAccessToken(code);
            log.debug("Received access token: {}", accessToken);

            if (accessToken != null) {
                // Fetch the GitHub username
                String username = authorizationService.getGitHubUsername(accessToken);

                // Update Gitrep with the new access token, platform type, and username
                String clientId = "1001"; // Hardcoded client ID for testing only
                authorizationService.updateAccessTokenAndUsername(clientId, accessToken, Gitrep.PlatformType.GITHUB, username);
                log.info("Access token and username updated successfully in Gitrep entity for GitHub");

                response.sendRedirect("/authorization");
            } else {
                log.error("Access token was null. Not saved in Gitrep entity.");
                response.sendRedirect("/error?message=Failed to obtain access token");
            }
        } catch (Exception e) {
            log.error("Error during GitHub OAuth process", e);
            try {
                response.sendRedirect("/error?message=Error during GitHub OAuth process");
            } catch (IOException ex) {
                log.error("Error during redirection to the error page", ex);
            }
        }
    }

    // GitLab OAuth Callback
    @GetMapping("/login/oauth2/code/gitlab")
    public void handleGitLabRedirect(@RequestParam("code") String code, HttpServletResponse response) {
        log.info("GitLab callback triggered with code: {}", code);
        try {
            String accessToken = authorizationService.exchangeCodeForGitLabAccessToken(code);
            log.debug("Received GitLab access token: {}", accessToken);

            if (accessToken != null) {
                // Fetch the GitLab username
                String username = authorizationService.getGitLabUsername(accessToken);
                // Update Gitrep with the new access token, platform type, and username
                String clientId = "1001"; // Example client ID
                authorizationService.updateAccessTokenAndUsername(clientId, accessToken, Gitrep.PlatformType.GITLAB, username);
                log.info("Access token and username updated successfully in Gitrep entity for GitLab");

                response.sendRedirect("/authorization");
            } else {
                log.error("GitLab access token was null. Not saved in Gitrep entity.");
                response.sendRedirect("/error?message=Failed to obtain GitLab access token");
            }
        } catch (Exception e) {
            log.error("Error during GitLab OAuth process", e);
            try {
                response.sendRedirect("/error?message=Error during GitLab OAuth process");
            } catch (IOException ex) {
                log.error("Error during redirection to the error page", ex);
            }
        }
    }

    @GetMapping("/github/repositories")
    public ResponseEntity<List<Map<String, Object>>> getGithubRepositories() {
        List<Map<String, Object>> repositories = authorizationService.getRepositories();
        if (repositories.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repositories);
    }

    @GetMapping("/gitlab/repositories")
    public ResponseEntity<List<Map<String, Object>>> getGitLabRepositories() {
        List<Map<String, Object>> repositories = authorizationService.getGitLabRepositories();
        return ResponseEntity.ok(repositories);
    }
}
