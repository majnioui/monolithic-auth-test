package com.monolithicauthtest.app.service;

import org.springframework.stereotype.Service;

@Service
public class BuildService {

    public String suggestBuildpack(String repoName) {
        // Placeholder implementation
        // Here you would have logic to determine the buildpack based on the repoName
        // For example, if the repoName suggests a Java project, return a Java buildpack
        return "default-java-buildpack"; // Replace with actual logic
    }
}
