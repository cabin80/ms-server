package com.ms.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping
public class SpaFallbackController {

    @Autowired
    private ResourceLoader resourceLoader;

    @GetMapping(value = "/admin", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> adminPage() {
        try {
            var resource = resourceLoader.getResource("classpath:/static/admin.html");
            var content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok().body(content);
        } catch (Exception e) {
            return ResponseEntity.ok()
                    .header("Content-Type", "text/html; charset=utf-8")
                    .body("<!DOCTYPE html><html><body><h1>Admin Page Error</h1><p>" + e.getMessage() + "</p></body></html>");
        }
    }

    @GetMapping(value = "/v2", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> v2Page() {
        try {
            var resource = resourceLoader.getResource("classpath:/static/v2/index.html");
            var content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok().body(content);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("v2 page not found");
        }
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
