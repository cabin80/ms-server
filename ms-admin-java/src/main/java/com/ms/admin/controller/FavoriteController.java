package com.ms.admin.controller;

import com.ms.admin.config.Result;
import com.ms.admin.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final AdminService adminService;

    public FavoriteController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public Map<String, Object> getFavorites(@RequestParam(defaultValue = "1") Integer user_id) {
        return Result.success(adminService.getUserFavorites(user_id));
    }

    @PostMapping("/toggle")
    public Map<String, Object> toggle(@RequestBody Map<String, Object> body) {
        Integer userId = body.get("user_id") != null ? (Integer) body.get("user_id") : 1;
        Integer songId = (Integer) body.get("song_id");
        if (songId == null) return Result.error(400, "song_id is required");
        boolean favorited = adminService.toggleFavorite(userId, songId);
        return Result.success(Map.of("favorited", favorited), favorited ? "已收藏" : "已取消收藏");
    }

    @GetMapping("/check/{songId}")
    public Map<String, Object> check(@PathVariable Integer songId,
                                      @RequestParam(defaultValue = "1") Integer user_id) {
        boolean favorited = adminService.checkFavorite(user_id, songId);
        return Result.success(Map.of("favorited", favorited));
    }
}
