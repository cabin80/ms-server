package com.ms.server.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;

@Controller
public class SpaFallbackController {

    private static final String DIST_PATH = "../ms-web/dist/";

    @GetMapping(value = {"/", "/index.html"})
    public ResponseEntity<Resource> index() {
        File file = new File(DIST_PATH + "index.html");
        if (file.exists()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(new FileSystemResource(file));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/**")
    public ResponseEntity<Resource> fallback(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/")) {
            return ResponseEntity.notFound().build();
        }
        // Clean trailing slash
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        // Try static file first
        File staticFile = new File(DIST_PATH + path);
        if (staticFile.exists() && !staticFile.isDirectory()) {
            String contentType = getContentType(path);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new FileSystemResource(staticFile));
        }
        // Try as directory with index.html
        File dirIndex = new File(DIST_PATH + path + "/index.html");
        if (dirIndex.exists()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(new FileSystemResource(dirIndex));
        }
        // SPA fallback to root index.html
        File index = new File(DIST_PATH + "index.html");
        if (index.exists()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(new FileSystemResource(index));
        }
        return ResponseEntity.notFound().build();
    }

    private String getContentType(String path) {
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".woff2")) return "font/woff2";
        if (path.endsWith(".woff")) return "font/woff";
        if (path.endsWith(".json")) return "application/json";
        return "application/octet-stream";
    }
}
