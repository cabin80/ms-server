package com.ms.server.controller;

import com.ms.server.config.Result;
import com.ms.server.service.CosService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final CosService cosService;

    public UploadController(CosService cosService) {
        this.cosService = cosService;
    }

    @PostMapping("/song")
    public Map<String, Object> uploadSong(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }
        try {
            String url = cosService.uploadSong(file);
            return Result.success(Map.of("url", url), "上传成功");
        } catch (IOException e) {
            return Result.error(500, "上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/cover")
    public Map<String, Object> uploadCover(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }
        try {
            String url = cosService.uploadCover(file);
            return Result.success(Map.of("url", url), "上传成功");
        } catch (IOException e) {
            return Result.error(500, "上传失败: " + e.getMessage());
        }
    }

    @PostMapping
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file,
                                      @RequestParam("prefix") String prefix) {
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }
        try {
            String url = cosService.upload(file, prefix);
            return Result.success(Map.of("url", url), "上传成功");
        } catch (IOException e) {
            return Result.error(500, "上传失败: " + e.getMessage());
        }
    }
}
