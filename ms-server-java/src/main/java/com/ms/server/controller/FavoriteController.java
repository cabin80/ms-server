package com.ms.server.controller;

import com.ms.server.config.Result;
import com.ms.server.service.MusicService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final MusicService musicService;

    public FavoriteController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping
    public Map<String, Object> getAll(@RequestParam(defaultValue = "1") Integer userId) {
        return Result.success(musicService.getFavorites(userId));
    }

    @PostMapping("/toggle")
    public Map<String, Object> toggle(@RequestBody Map<String, Integer> body) {
        Integer uid = body.getOrDefault("user_id", 1);
        var result = musicService.toggleFavorite(uid, body.get("song_id"));
        String msg = result.get("favorited").equals(true) ? "已收藏" : "已取消收藏";
        return Result.success(result, msg);
    }

    @GetMapping("/check/{songId}")
    public Map<String, Object> check(@PathVariable Integer songId,
                                     @RequestParam(defaultValue = "1") Integer userId) {
        boolean favorited = musicService.checkFavorite(userId, songId);
        return Result.success(Map.of("favorited", favorited));
    }
}
