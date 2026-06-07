package com.ms.admin.controller;

import com.ms.admin.config.Result;
import com.ms.admin.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    private final AdminService adminService;

    public ArtistController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public Map<String, Object> getAll() {
        return Result.success(adminService.getAllArtists());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Integer id) {
        Map<String, Object> artist = adminService.getArtistById(id);
        if (artist == null) return Result.error(404, "Not found");
        return Result.success(artist);
    }
}
