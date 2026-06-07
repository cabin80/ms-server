package com.ms.admin.controller;

import com.ms.admin.config.Result;
import com.ms.admin.entity.Song;
import com.ms.admin.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final AdminService adminService;

    public SongController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public Map<String, Object> getAll() {
        return Result.success(adminService.getAllSongs());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Integer id) {
        Song s = adminService.getSongById(id);
        if (s == null) return Result.error(404, "Not found");
        return Result.success(s);
    }

    @GetMapping("/by-artist/{artistId}")
    public Map<String, Object> getByArtist(@PathVariable Integer artistId) {
        return Result.success(adminService.getSongsByArtist(artistId));
    }

    @GetMapping("/by-album/{albumId}")
    public Map<String, Object> getByAlbum(@PathVariable Integer albumId) {
        return Result.success(adminService.getSongsByAlbum(albumId));
    }
}
