package com.ms.server.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaFallbackController {

    /**
     * SPA fallback: 前端路由在/、/v2、/v3、/v4、/v5 下
     * 非 /api/ 路径都转发到对应目录的 index.html
     */
    @GetMapping(value = {"/", "/index.html"})
    public String index() {
        return "forward:/v5/index.html";
    }

    @GetMapping("/v2/**")
    public String v2Fallback() {
        return "forward:/v2/index.html";
    }

    @GetMapping("/v3/**")
    public String v3Fallback() {
        return "forward:/v3/index.html";
    }

    @GetMapping("/v4/**")
    public String v4Fallback() {
        return "forward:/v4/index.html";
    }

    @GetMapping("/v5/**")
    public String v5Fallback() {
        return "forward:/v5/index.html";
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "forward:/v5/index.html";
    }

    @GetMapping("/**")
    public String fallback(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/")) {
            return null;
        }
        return "forward:/v5/index.html";
    }
}
