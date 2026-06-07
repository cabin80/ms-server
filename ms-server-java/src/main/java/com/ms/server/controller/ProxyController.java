package com.ms.server.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 * 代理控制器
 * 前端 fetch 音频文件时解决 CORS 限制
 * bucket 公有读，直接 HTTP GET 即可
 */
@Controller
@RequestMapping("/api/proxy")
public class ProxyController {

    private static final Logger log = LoggerFactory.getLogger(ProxyController.class);

    @GetMapping("/raw")
    @ResponseBody
    public void proxyRaw(String url, HttpServletResponse response) {
        HttpURLConnection conn = null;
        try {
            URI uri = new URI(url);
            conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            conn.setRequestProperty("User-Agent", "MS-Music/1.0");

            int statusCode = conn.getResponseCode();
            if (statusCode != 200) {
                log.warn("代理请求失败: {} status={}", url, statusCode);
                response.sendError(statusCode, "上游返回 " + statusCode);
                return;
            }

            String contentType = conn.getContentType();
            response.setContentType(contentType != null ? contentType : "audio/flac");

            long contentLength = conn.getContentLengthLong();
            if (contentLength > 0) {
                response.setContentLengthLong(contentLength);
            }

            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Cache-Control", "public, max-age=86400");
            response.setHeader("Access-Control-Allow-Origin", "*");

            try (InputStream is = conn.getInputStream();
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[65536];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            log.error("代理文件失败: {}", url, e);
            if (!response.isCommitted()) {
                try {
                    response.sendError(502, "代理失败: " + e.getMessage());
                } catch (Exception ignored) {}
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
