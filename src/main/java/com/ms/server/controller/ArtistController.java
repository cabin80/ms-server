package com.ms.server.controller;

import com.ms.server.config.Result;
import com.ms.server.entity.Artist;
import com.ms.server.entity.Song;
import com.ms.server.service.MusicService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    private final MusicService musicService;

    public ArtistController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping
    public Map<String, Object> getAll() {
        return Result.success(musicService.getAllArtists());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Integer id) {
        Artist artist = musicService.getArtistById(id);
        if (artist == null) return Result.error(404, "Not found");
        Map<String, Object> result = new HashMap<>();
        result.put("id", artist.getId());
        result.put("name", artist.getName());
        result.put("cover", artist.getCover());
        result.put("description", artist.getDescription());
        result.put("created_at", artist.getCreatedAt());
        result.put("songs", musicService.getSongsByArtist(id));
        return Result.success(result);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Artist artist) {
        Artist created = musicService.createArtist(artist);
        return Result.success(Map.of("id", created.getId()), "创建成功");
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Integer id, @RequestBody Artist artist) {
        Artist updated = musicService.updateArtist(id, artist);
        if (updated == null) return Result.error(404, "Not found");
        return Result.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        musicService.deleteArtist(id);
        return Result.success(null, "删除成功");
    }
}
