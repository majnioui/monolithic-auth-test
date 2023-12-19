package com.monolithicauthtest.app.web.rest;

import com.monolithicauthtest.app.domain.Gitrep;
import com.monolithicauthtest.app.repository.GitrepRepository;
import com.monolithicauthtest.app.service.AuthorizationService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
                // Update Gitrep with the new access token and platform type
                String clientId = "1001"; // Hardcoded client ID for testing only
                authorizationService.updateAccessToken(clientId, accessToken, Gitrep.PlatformType.GITHUB);
                log.info("Access token updated successfully in Gitrep entity for GitHub");

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
                // Update Gitrep with the new access token and platform type
                String clientId = "1001"; // Example client ID
                authorizationService.updateAccessToken(clientId, accessToken, Gitrep.PlatformType.GITLAB);
                log.info("GitLab access token updated successfully in Gitrep entity for GitLab");

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
