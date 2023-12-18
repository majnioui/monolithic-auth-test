package com.monolithicauthtest.app.web.rest;

import com.monolithicauthtest.app.domain.Gitrep;
import com.monolithicauthtest.app.repository.GitrepRepository;
import com.monolithicauthtest.app.service.GitHubService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GitHubOAuthController {

    @Value("${github.client-id}")
    private String clientId;

    @Value("${gitlab.client-id}")
    private String gitlabClientId;

    private final GitHubService gitHubService;
    private final GitrepRepository gitrepRepository;

    private static final Logger log = LoggerFactory.getLogger(GitHubOAuthController.class);

    public GitHubOAuthController(GitHubService gitHubService, GitrepRepository gitrepRepository) {
        this.gitHubService = gitHubService;
        this.gitrepRepository = gitrepRepository;
    }

    @GetMapping("/authorize-github")
    public void initiateAuthorization(HttpServletResponse response) throws IOException {
        String scopes = "read:user,repo";
        String redirectUrl = "https://github.com/login/oauth/authorize?client_id=" + clientId + "&scope=" + scopes;
        response.sendRedirect(redirectUrl);
    }

    // GitLab Authorization
    @GetMapping("/authorize-gitlab")
    public void initiateGitLabAuthorization(HttpServletResponse response) throws IOException {
        String scopes = "read_repository";
        String redirectUrl =
            "http://192.168.100.130/oauth/authorize?client_id=" +
            gitlabClientId +
            "&response_type=code&redirect_uri=http://localhost:8080/login/oauth2/code/gitlab";
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/login/oauth2/code/github")
    public void handleGitHubRedirect(@RequestParam("code") String code, HttpServletResponse response) {
        log.info("GitHub callback triggered with code: {}", code);
        try {
            String accessToken = gitHubService.exchangeCodeForAccessToken(code);
            log.debug("Received access token: {}", accessToken);

            if (accessToken != null) {
                // Update Gitrep with the new access token
                String clientId = "1001"; // Hardcoded client ID for testing only
                gitHubService.updateAccessToken(clientId, accessToken);
                log.info("Access token updated successfully in Gitrep entity");

                // Redirect to the test-github page aka back to the autorize/refresh page
                response.sendRedirect("/test-github");
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
            String accessToken = gitHubService.exchangeCodeForGitLabAccessToken(code);
            log.debug("Received GitLab access token: {}", accessToken);

            if (accessToken != null) {
                // Update Gitrep with the new access token
                String clientId = "1001";
                gitHubService.updateAccessToken(clientId, accessToken);
                log.info("GitLab access token updated successfully in Gitrep entity");

                // Redirect to an appropriate page for GitLab
                response.sendRedirect("/test-github");
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

    @GetMapping("/user/repositories")
    public ResponseEntity<List<Map<String, Object>>> getUserRepositories() {
        Optional<Gitrep> latestGitrep = gitrepRepository.findFirstByOrderByCreatedAtDesc();
        if (!latestGitrep.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, Object>> repositories = gitHubService.getRepositories(latestGitrep.get().getAccesstoken());
        return ResponseEntity.ok(repositories);
    }

    @GetMapping("/gitlab/repositories")
    public ResponseEntity<List<Map<String, Object>>> getGitLabRepositories() {
        List<Map<String, Object>> repositories = gitHubService.getGitLabRepositories();
        return ResponseEntity.ok(repositories);
    }
}
