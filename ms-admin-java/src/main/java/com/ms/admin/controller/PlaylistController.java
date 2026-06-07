package com.ms.admin.controller;

import com.ms.admin.config.Result;
import com.ms.admin.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final AdminService adminService;

    public PlaylistController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public Map<String, Object> getPlaylists(@RequestParam(defaultValue = "1") Integer user_id) {
        return Result.success(adminService.getUserPlaylists(user_id));
    }

    @GetMapping("/{id}")
    public Map<String, Object> getPlaylistDetail(@PathVariable Integer id) {
        Map<String, Object> detail = adminService.getPlaylistDetail(id);
        if (detail == null) return Result.error(404, "Not found");
        return Result.success(detail);
    }

    @PostMapping("/add-song")
    public Map<String, Object> addSong(@RequestBody Map<String, Object> body) {
        Integer playlistId = (Integer) body.get("playlist_id");
        Integer songId = (Integer) body.get("song_id");
        if (playlistId == null || songId == null) {
            return Result.error(400, "playlist_id and song_id are required");
        }
        adminService.addSongToPlaylist(playlistId, songId);
        return Result.success(null, "已添加到歌单");
    }
}
