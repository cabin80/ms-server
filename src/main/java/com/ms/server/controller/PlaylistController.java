package com.ms.server.controller;

import com.ms.server.config.Result;
import com.ms.server.entity.Playlist;
import com.ms.server.service.MusicService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final MusicService musicService;

    public PlaylistController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping
    public Map<String, Object> getAll(@RequestParam(defaultValue = "1") Integer userId) {
        return Result.success(musicService.getPlaylistsByUser(userId));
    }

    @GetMapping("/{id}")
    public Map<String, Object> getDetail(@PathVariable Integer id) {
        var detail = musicService.getPlaylistDetail(id);
        if (detail == null) return Result.error(404, "Not found");
        return Result.success(detail);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Playlist playlist) {
        Playlist created = musicService.createPlaylist(playlist);
        return Result.success(Map.of("id", created.getId()), "创建成功");
    }

    @PostMapping("/add-song")
    public Map<String, Object> addSong(@RequestBody Map<String, Integer> body) {
        String msg = musicService.addSongToPlaylist(body.get("playlist_id"), body.get("song_id"));
        return Result.success(null, msg);
    }
}
