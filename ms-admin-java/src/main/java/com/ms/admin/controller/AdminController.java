package com.ms.admin.controller;

import com.ms.admin.config.Result;
import com.ms.admin.entity.*;
import com.ms.admin.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ===== Stats =====
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return Result.success(adminService.getStats());
    }

    // ===== Songs =====
    @GetMapping("/songs")
    public Map<String, Object> getSongs() {
        return Result.success(adminService.getAllSongs());
    }

    @GetMapping("/songs/search")
    public Map<String, Object> searchSongs(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String artist_name,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String first_letter) {
        return Result.success(adminService.searchSongs(name, artist_name, region, first_letter));
    }

    @GetMapping("/songs/{id}")
    public Map<String, Object> getSong(@PathVariable Integer id) {
        Song s = adminService.getSongById(id);
        if (s == null) return Result.error(404, "Not found");
        return Result.success(s);
    }

    @PostMapping("/songs")
    public Map<String, Object> createSong(@RequestBody Song song) {
        Song created = adminService.createSong(song);
        return Result.success(Map.of("id", created.getId()), "创建成功");
    }

    @PutMapping("/songs/{id}")
    public Map<String, Object> updateSong(@PathVariable Integer id, @RequestBody Song song) {
        Song updated = adminService.updateSong(id, song);
        if (updated == null) return Result.error(404, "Not found");
        return Result.success(null, "更新成功");
    }

    @DeleteMapping("/songs/{id}")
    public Map<String, Object> deleteSong(@PathVariable Integer id) {
        adminService.deleteSong(id);
        return Result.success(null, "删除成功");
    }

    // ===== Artists =====
    @GetMapping("/artists")
    public Map<String, Object> getArtists() {
        return Result.success(adminService.getAllArtists());
    }

    @GetMapping("/artists/{id}")
    public Map<String, Object> getArtist(@PathVariable Integer id) {
        Map<String, Object> artist = adminService.getArtistById(id);
        if (artist == null) return Result.error(404, "Not found");
        return Result.success(artist);
    }

    @PostMapping("/artists")
    public Map<String, Object> createArtist(@RequestBody Artist artist) {
        Artist created = adminService.createArtist(artist);
        return Result.success(Map.of("id", created.getId()), "创建成功");
    }

    @PutMapping("/artists/{id}")
    public Map<String, Object> updateArtist(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        adminService.updateArtist(id, body);
        return Result.success(null, "更新成功");
    }

    @DeleteMapping("/artists/{id}")
    public Map<String, Object> deleteArtist(@PathVariable Integer id) {
        adminService.deleteArtist(id);
        return Result.success(null, "删除成功");
    }

    @GetMapping("/artists/{artistId}/direct-songs")
    public Map<String, Object> getDirectSongs(@PathVariable Integer artistId) {
        return Result.success(adminService.getDirectSongs(artistId));
    }

    // ===== Albums =====
    @GetMapping("/albums")
    public Map<String, Object> getAlbums() {
        return Result.success(adminService.getAllAlbums());
    }

    @GetMapping("/albums/by-artist/{artistId}")
    public Map<String, Object> getAlbumsByArtist(@PathVariable Integer artistId) {
        return Result.success(adminService.getAlbumsByArtist(artistId));
    }

    @PostMapping("/albums")
    public Map<String, Object> createAlbum(@RequestBody Album album) {
        Album created = adminService.createAlbum(album);
        return Result.success(Map.of("id", created.getId()), "创建成功");
    }

    @DeleteMapping("/albums/{id}")
    public Map<String, Object> deleteAlbum(@PathVariable Integer id) {
        adminService.deleteAlbum(id);
        return Result.success(null, "删除成功");
    }

    // ===== Playlists =====
    @GetMapping("/playlists")
    public Map<String, Object> getPlaylists() {
        return Result.success(adminService.getAllPlaylists());
    }

    @PostMapping("/playlists/batch-add-songs")
    public Map<String, Object> batchAddSongs(@RequestBody Map<String, Object> body) {
        Integer playlistId = (Integer) body.get("playlist_id");
        @SuppressWarnings("unchecked")
        List<Integer> songIds = (List<Integer>) body.get("song_ids");
        if (playlistId == null || songIds == null || songIds.isEmpty()) {
            return Result.error(400, "参数错误");
        }
        int added = adminService.batchAddSongsToPlaylist(playlistId, songIds);
        return Result.success(null, "已添加 " + added + " 首歌曲");
    }

    // ===== Users =====
    @GetMapping("/users")
    public Map<String, Object> getUsers(@RequestParam(required = false) String keyword) {
        return Result.success(adminService.getUsers(keyword));
    }

    @GetMapping("/users/{userId}/playlists")
    public Map<String, Object> getUserPlaylists(@PathVariable Integer userId) {
        return Result.success(adminService.getUserPlaylists(userId));
    }

    // ===== Access Logs =====
    @GetMapping("/access-logs")
    public Map<String, Object> getAccessLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(adminService.getAccessLogs(page, size));
    }
}
