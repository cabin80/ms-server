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
        return redirect("/admin.html");
    }

    @GetMapping("/v2")
    public ResponseEntity<Void> v2Page() {
        return redirect("/v2/index.html");
    }

    @GetMapping("/v3")
    public ResponseEntity<Void> v3Page() {
        return redirect("/v3/index.html");
    }

    @GetMapping("/v4")
    public ResponseEntity<Void> v4Page() {
        return redirect("/v4/index.html");
    }

    @GetMapping("/v5")
    public ResponseEntity<Void> v5Page() {
        return redirect("/v5/index.html");
    }

    private ResponseEntity<Void> redirect(String path) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(path));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
