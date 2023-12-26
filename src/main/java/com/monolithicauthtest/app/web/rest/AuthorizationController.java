package com.monolithicauthtest.app.web.rest;

import com.monolithicauthtest.app.domain.Gitrep;
import com.monolithicauthtest.app.domain.User;
import com.monolithicauthtest.app.repository.GitrepRepository;
import com.monolithicauthtest.app.repository.UserRepository;
import com.monolithicauthtest.app.service.AuthorizationService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Value("${bitbucket.client-id}")
    private String bitbucketClientId;

    private final AuthorizationService authorizationService;
    private final GitrepRepository gitrepRepository;
    private final UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(AuthorizationController.class);

    public AuthorizationController(
        AuthorizationService authorizationService,
        GitrepRepository gitrepRepository,
        UserRepository userRepository
    ) {
        this.authorizationService = authorizationService;
        this.gitrepRepository = gitrepRepository;
        this.userRepository = userRepository;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            log.info("Authentication name: {}", authentication.getName());
            Optional<User> userOpt = userRepository.findOneByLogin(authentication.getName());
            if (userOpt.isPresent()) {
                String clientId = userOpt.map(User::getId).map(String::valueOf).orElse(null);
                log.info("Current User ID: {}", clientId);
                return clientId;
            } else {
                log.warn("No user found with login: {}", authentication.getName());
            }
        } else {
            log.warn("No authentication found in Security Context");
        }
        return null;
    }

    @PostMapping("/api/save-client-url")
    public ResponseEntity<Void> saveClientUrl(@RequestBody Map<String, String> request) {
        String clientUrl = request.get("clientUrl");
        String platformType = request.get("platformType");
        String clientId = getCurrentUserId(); // Get the current user's ID

        // Check if clientUrl is empty or null, and return if it is
        if (clientUrl == null || clientUrl.trim().isEmpty()) {
            return ResponseEntity.ok().build();
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
            gitrep.setAccesstoken("XXX"); // Just a tweak we temporary fill it with xxx it will be updated when authorizing
            gitrep.setClientUrl(clientUrl);
            gitrep.setPlatformType(Gitrep.PlatformType.valueOf(platformType.toUpperCase()));
        }

        gitrepRepository.save(gitrep);
        return ResponseEntity.ok().build();
    }

    // Github Authorization
    @GetMapping("/authorize-github")
    public void initiateAuthorization(@RequestParam(required = false) String userLogin, HttpServletResponse response) throws IOException {
        String scopes = "read:user,repo";
        String state = userLogin != null ? URLEncoder.encode(userLogin, StandardCharsets.UTF_8) : "default";
        String redirectUrl = "https://github.com/login/oauth/authorize?client_id=" + clientId + "&scope=" + scopes + "&state=" + state;
        response.sendRedirect(redirectUrl);
    }

    // GitLab Authorization
    @GetMapping("/authorize-gitlab")
    public void initiateGitLabAuthorization(HttpServletResponse response) throws IOException {
        String redirectUrl =
            "http://192.168.100.130/oauth/authorize?client_id=" + // CHANGE THIS
            gitlabClientId +
            "&response_type=code&redirect_uri=http://localhost:8080/login/oauth2/code/gitlab"; // CHANGE THIS
        response.sendRedirect(redirectUrl);
    }

    // Bitbucket Authorization
    @GetMapping("/authorize-bitbucket")
    public void initiateBitbucketAuthorization(HttpServletResponse response) throws IOException {
        String redirectUrl =
            "https://bitbucket.org/site/oauth2/authorize?client_id=" +
            bitbucketClientId +
            "&response_type=code&redirect_uri=http://localhost:8080/login/oauth2/code/bitbucket"; // CHANGE THIS
        response.sendRedirect(redirectUrl);
    }

    // Github OAuth Callback
    @GetMapping("/login/oauth2/code/github")
    public void handleGitHubRedirect(@RequestParam("code") String code, @RequestParam("state") String state, HttpServletResponse response) {
        log.info("GitHub callback triggered with code: {}", code);
        // Decode the state parameter to get the user login
        String userLogin = URLDecoder.decode(state, StandardCharsets.UTF_8);

        try {
            String accessToken = authorizationService.exchangeCodeForAccessToken(code);
            log.debug("Received access token: {}", accessToken);

            if (accessToken != null) {
                // Use userLogin to get the user ID
                String userId = getUserIdByLogin(userLogin);
                String username = authorizationService.getGitHubUsername(accessToken, userId);
                authorizationService.updateAccessTokenAndUsername(userId, accessToken, Gitrep.PlatformType.GITHUB, username);
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
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private String getUserIdByLogin(String login) {
        Optional<User> userOpt = userRepository.findOneByLogin(login);
        return userOpt.map(User::getId).map(String::valueOf).orElse(null);
    }

    // GitLab OAuth Callback
    @GetMapping("/login/oauth2/code/gitlab")
    public void handleGitLabRedirect(@RequestParam("code") String code, HttpServletResponse response) {
        log.info("GitLab callback triggered with code: {}", code);
        try {
            String accessToken = authorizationService.exchangeCodeForGitLabAccessToken(code);
            log.debug("Received GitLab access token: {}", accessToken);

            if (accessToken != null) {
                String clientId = getCurrentUserId();
                String username = authorizationService.getGitLabUsername(accessToken, clientId);
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

    // Bitbucket OAuth Callback
    @GetMapping("/login/oauth2/code/bitbucket")
    public void handleBitbucketRedirect(@RequestParam("code") String code, HttpServletResponse response) {
        log.info("Bitbucket callback triggered with code: {}", code);
        try {
            String accessToken = authorizationService.exchangeCodeForBitbucketAccessToken(code);
            log.debug("Received Bitbucket access token: {}", accessToken);

            if (accessToken != null) {
                String clientId = getCurrentUserId();
                String username = authorizationService.getBitbucketUsername(accessToken, clientId);
                authorizationService.updateAccessTokenAndUsername(clientId, accessToken, Gitrep.PlatformType.BITBUCKET, username);
                log.info("Access token and username updated successfully in Gitrep entity for Bitbucket");
                response.sendRedirect("/authorization");
            } else {
                log.error("Bitbucket access token was null. Not saved in Gitrep entity.");
                response.sendRedirect("/error?message=Failed to obtain Bitbucket access token");
            }
        } catch (IOException ex) {
            log.error("IOException during Bitbucket OAuth process", ex);
        } catch (Exception e) {
            log.error("Error during Bitbucket OAuth process", e);
            try {
                response.sendRedirect("/error?message=Error during Bitbucket OAuth process");
            } catch (IOException ex) {
                log.error("IOException during redirection to the error page", ex);
            }
        }
    }

    @GetMapping("/github/repositories")
    public ResponseEntity<List<Map<String, Object>>> getGithubRepositories() {
        String clientId = getCurrentUserId(); // Get current user's ID
        List<Map<String, Object>> repositories = authorizationService.getRepositories(clientId);
        if (repositories.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repositories);
    }

    @GetMapping("/gitlab/repositories")
    public ResponseEntity<List<Map<String, Object>>> getGitLabRepositories() {
        String clientId = getCurrentUserId(); // Get current user's ID
        List<Map<String, Object>> repositories = authorizationService.getGitLabRepositories(clientId);
        if (repositories.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repositories);
    }

    @GetMapping("/bitbucket/repositories")
    public ResponseEntity<?> getBitbucketRepositories() {
        try {
            String clientId = getCurrentUserId(); // Get current user's ID
            List<Map<String, Object>> repositories = authorizationService.getBitbucketRepositories(clientId);
            if (repositories.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(repositories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching repositories: " + e.getMessage());
        }
    }
}
