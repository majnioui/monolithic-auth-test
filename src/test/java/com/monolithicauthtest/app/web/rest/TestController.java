package com.monolithicauthtest.app.web.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test/working")
    public String testMethod() {
        return "It's working!";
    }
}
