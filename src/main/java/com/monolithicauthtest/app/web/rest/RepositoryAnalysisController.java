package com.monolithicauthtest.app.web.rest;

import com.monolithicauthtest.app.domain.User;
import com.monolithicauthtest.app.repository.UserRepository;
import com.monolithicauthtest.app.service.RepositoryAnalysisService;
import java.util.Optional;
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
    private final UserRepository userRepository;

    @Autowired
    public RepositoryAnalysisController(RepositoryAnalysisService repositoryAnalysisService, UserRepository userRepository) {
        this.repositoryAnalysisService = repositoryAnalysisService;
        this.userRepository = userRepository;
    }

    @GetMapping("/github/java-version")
    public ResponseEntity<String> getJavaVersionFromGithubRepo(@RequestParam String repoName, @RequestParam String userLogin) {
        try {
            String userId = getUserIdByLogin(userLogin);
            String javaVersion = repositoryAnalysisService.getJavaVersionFromGithubRepo(repoName, userId);
            return ResponseEntity.ok(javaVersion);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching Java version: " + e.getMessage());
        }
    }

    private String getUserIdByLogin(String login) {
        Optional<User> userOpt = userRepository.findOneByLogin(login);
        return userOpt.map(User::getId).map(String::valueOf).orElse(null);
    }
}
