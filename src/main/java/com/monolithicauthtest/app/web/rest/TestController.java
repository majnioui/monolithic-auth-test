package com.monolithicauthtest.app.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/testgit")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Test endpoint response");
    }
}
