package com.ms.admin.controller;

import com.ms.admin.config.Result;
import com.ms.admin.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final AdminService adminService;

    public AlbumController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public Map<String, Object> getAll() {
        return Result.success(adminService.getAllAlbums());
    }

    @GetMapping("/by-artist/{artistId}")
    public Map<String, Object> getByArtist(@PathVariable Integer artistId) {
        return Result.success(adminService.getAlbumsByArtist(artistId));
    }
}
