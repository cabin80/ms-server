package com.ms.server.controller;

import com.ms.server.config.Result;
import com.ms.server.entity.Song;
import com.ms.server.service.MusicService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final MusicService musicService;

    public SongController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping
    public Map<String, Object> getAll() {
        return Result.success(musicService.getAllSongs());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Integer id) {
        Song song = musicService.getSongById(id);
        if (song == null) return Result.error(404, "Not found");
        return Result.success(song);
    }

    @GetMapping("/by-artist/{artistId}")
    public Map<String, Object> getByArtist(@PathVariable Integer artistId) {
        return Result.success(musicService.getSongsByArtist(artistId));
    }

    @GetMapping("/by-album/{albumId}")
    public Map<String, Object> getByAlbum(@PathVariable Integer albumId) {
        return Result.success(musicService.getSongsByAlbum(albumId));
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Song song) {
        Song created = musicService.createSong(song);
        return Result.success(Map.of("id", created.getId()), "创建成功");
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Integer id, @RequestBody Song song) {
        Song updated = musicService.updateSong(id, song);
        if (updated == null) return Result.error(404, "Not found");
        return Result.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        musicService.deleteSong(id);
        return Result.success(null, "删除成功");
    }
}
