package com.ms.server.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URI;

@Controller
public class SpaFallbackController {

    @GetMapping("/admin")
    public ResponseEntity<Void> adminPage() {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/admin.html"));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
