package com.ms.server.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
public class SpaFallbackController {

    @RequestMapping("/admin")
    public ResponseEntity<Void> adminPage() {
        return redirect("/admin.html");
    }

    @RequestMapping("/v2")
    public ResponseEntity<Void> v2Page() {
        return redirect("/v2/index.html");
    }

    /**
     * /v3 → 重定向到 /v3/index.html
     * /v3/ → 直接返回 /v3/index.html 内容
     */
    @RequestMapping("/v3")
    public ResponseEntity<Void> v3Page() {
        return redirect("/v3/index.html");
    }

    @RequestMapping("/v3/")
    @ResponseBody
    public ResponseEntity<byte[]> v3TrailingSlash() {
        return serveStatic("/v3/index.html");
    }

    @RequestMapping("/v4")
    public ResponseEntity<Void> v4Page() {
        return redirect("/v4/index.html");
    }

    @RequestMapping("/v5")
    public ResponseEntity<Void> v5Page() {
        return redirect("/v5/index.html");
    }

    private ResponseEntity<Void> redirect(String path) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", path)
                .build();
    }

    private ResponseEntity<byte[]> serveStatic(String path) {
        try {
            ClassPathResource resource = new ClassPathResource("static" + path);
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(resource.getInputStream().readAllBytes());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
