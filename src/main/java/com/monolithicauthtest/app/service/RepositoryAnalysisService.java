package com.monolithicauthtest.app.service;

import com.monolithicauthtest.app.domain.Gitrep;
import com.monolithicauthtest.app.repository.GitrepRepository;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

@Service
public class RepositoryAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(RepositoryAnalysisService.class);

    private final AuthorizationService authorizationService;
    private final RestTemplate restTemplate;
    private final GitrepRepository gitrepRepository;

    @Autowired
    public RepositoryAnalysisService(AuthorizationService authorizationService, GitrepRepository gitrepRepository) {
        this.authorizationService = authorizationService;
        this.gitrepRepository = gitrepRepository;
        this.restTemplate = new RestTemplate();
    }

    public String getJavaVersionFromGithubRepo(String repoName, String userId) {
        String accessToken = authorizationService.retrieveAccessToken(Gitrep.PlatformType.GITHUB, userId);
        if (accessToken == null) {
            throw new IllegalStateException("No access token available for GitHub");
        }

        Gitrep.PlatformType platformType = Gitrep.PlatformType.GITHUB;
        Gitrep gitrep = gitrepRepository
            .findByClientidAndPlatformType(userId, platformType)
            .orElseThrow(() -> new IllegalStateException("Gitrep not found for userId: " + userId + " and platform: " + platformType));
        String username = gitrep.getUsername();

        String fileContent = fetchFileContentFromGitHub(username, repoName, "pom.xml", accessToken);
        return extractJavaVersionFromPom(fileContent);
    }

    private String fetchFileContentFromGitHub(String ownerName, String repoName, String filePath, String accessToken) {
        String fileApiUrl = "https://api.github.com/repos/" + ownerName + "/" + repoName + "/contents/" + filePath;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                fileApiUrl,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("content")) {
                String contentBase64 = (String) responseBody.get("content");
                // Remove any potential newline or carriage return characters
                contentBase64 = contentBase64.replace("\n", "").replace("\r", "").trim();

                // Log the sanitized Base64 content
                log.debug("Sanitized Base64 Content from GitHub: {}", contentBase64);

                if (contentBase64 != null && !contentBase64.isEmpty()) {
                    return new String(Base64.getDecoder().decode(contentBase64), StandardCharsets.UTF_8);
                } else {
                    log.error("No content or empty content received from GitHub");
                }
            }
        } catch (HttpClientErrorException e) {
            log.error("HTTP error fetching file from GitHub: {}", e.getStatusCode());
        } catch (IllegalArgumentException e) {
            log.error("Error decoding Base64 content", e);
        } catch (Exception e) {
            log.error("Error fetching file from GitHub", e);
        }
        return null;
    }

    private String extractJavaVersionFromPom(String pomXmlContent) {
        if (pomXmlContent == null) {
            log.error("POM XML content is null");
            return null;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new ByteArrayInputStream(pomXmlContent.getBytes(StandardCharsets.UTF_8))));

            XPath xPath = XPathFactory.newInstance().newXPath();
            String javaVersion = (String) xPath.evaluate("/project/properties/java.version", document, XPathConstants.STRING);

            return javaVersion;
        } catch (Exception e) {
            log.error("Error parsing pom.xml content", e);
            return null;
        }
    }
}
