package com.ms.server.controller;

import com.ms.server.config.Result;
import com.ms.server.entity.Album;
import com.ms.server.service.MusicService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final MusicService musicService;

    public AlbumController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping
    public Map<String, Object> getAll() {
        return Result.success(musicService.getAllAlbums());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Integer id) {
        var album = musicService.getAlbumById(id);
        if (album == null) return Result.error(404, "Not found");
        return Result.success(album);
    }

    @GetMapping("/by-artist/{artistId}")
    public Map<String, Object> getByArtist(@PathVariable Integer artistId) {
        return Result.success(musicService.getAlbumsByArtist(artistId));
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Album album) {
        Album created = musicService.createAlbum(album);
        return Result.success(Map.of("id", created.getId()), "创建成功");
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Integer id, @RequestBody Album album) {
        Album updated = musicService.updateAlbum(id, album);
        if (updated == null) return Result.error(404, "Not found");
        return Result.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        musicService.deleteAlbum(id);
        return Result.success(null, "删除成功");
    }
}
