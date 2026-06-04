package com.ms.server.controller;

import com.ms.server.config.Result;
import com.ms.server.entity.*;
import com.ms.server.mapper.*;
import com.ms.server.service.CosService;
import com.ms.server.service.MusicService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final MusicService musicService;
    private final CosService cosService;
    private final SongMapper songMapper;
    private final ArtistMapper artistMapper;
    private final AlbumMapper albumMapper;
    private final PlaylistMapper playlistMapper;
    private final UserMapper userMapper;
    private final PlaylistSongMapper playlistSongMapper;

    public AdminController(MusicService musicService, CosService cosService,
                           SongMapper songMapper, ArtistMapper artistMapper,
                           AlbumMapper albumMapper, PlaylistMapper playlistMapper,
                           UserMapper userMapper, PlaylistSongMapper playlistSongMapper) {
        this.musicService = musicService;
        this.cosService = cosService;
        this.songMapper = songMapper;
        this.artistMapper = artistMapper;
        this.albumMapper = albumMapper;
        this.playlistMapper = playlistMapper;
        this.userMapper = userMapper;
        this.playlistSongMapper = playlistSongMapper;
    }

    // ===== Stats =====
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("songs", songMapper.selectCount(null));
        stats.put("artists", artistMapper.selectCount(null));
        stats.put("albums", albumMapper.selectCount(null));
        stats.put("playlists", playlistMapper.selectCount(null));
        return Result.success(stats);
    }

    // ===== Songs =====
    @GetMapping("/songs")
    public Map<String, Object> getSongs() {
        return Result.success(songMapper.selectAllWithDetails());
    }

    @GetMapping("/songs/search")
    public Map<String, Object> searchSongs(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String artist_name,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String first_letter) {
        List<Song> all = songMapper.selectAllWithDetails();
        if (name != null && !name.isEmpty())
            all = all.stream().filter(s -> s.getName() != null && s.getName().contains(name)).toList();
        if (artist_name != null && !artist_name.isEmpty())
            all = all.stream().filter(s -> s.getArtistName() != null && s.getArtistName().contains(artist_name)).toList();
        if ((region != null && !region.isEmpty()) || (first_letter != null && !first_letter.isEmpty())) {
            List<Map<String, Object>> artists = artistMapper.selectAllWithCount();
            Set<Integer> filteredArtistIds = artists.stream()
                .filter(a -> {
                    if (region != null && !region.isEmpty() && !region.equals(a.get("region"))) return false;
                    if (first_letter != null && !first_letter.isEmpty()) {
                        String fl = (String) a.get("first_letter");
                        if ("#".equals(first_letter)) {
                            if (fl == null || !fl.matches("^[0-9]")) return false;
                        } else {
                            if (fl == null || !fl.equalsIgnoreCase(first_letter)) return false;
                        }
                    }
                    return true;
                })
                .map(a -> (Integer) a.get("id"))
                .collect(Collectors.toSet());
            all = all.stream().filter(s -> filteredArtistIds.contains(s.getArtistId())).toList();
        }
        return Result.success(all);
    }

    @GetMapping("/songs/{id}")
    public Map<String, Object> getSong(@PathVariable Integer id) {
        Song s = songMapper.selectByIdWithDetails(id);
        if (s == null) return Result.error(404, "Not found");
        return Result.success(s);
    }

    @PostMapping("/songs")
    public Map<String, Object> createSong(@RequestBody Song song) {
        Song created = musicService.createSong(song);
        return Result.success(Map.of("id", created.getId()), "创建成功");
    }

    @PutMapping("/songs/{id}")
    public Map<String, Object> updateSong(@PathVariable Integer id, @RequestBody Song song) {
        Song existing = songMapper.selectById(id);
        if (existing == null) return Result.error(404, "Not found");
        if (song.getName() != null) existing.setName(song.getName());
        if (song.getArtistId() != null) existing.setArtistId(song.getArtistId());
        if (song.getAlbumId() != null) existing.setAlbumId(song.getAlbumId());
        if (song.getUrl() != null) existing.setUrl(song.getUrl());
        if (song.getDurationText() != null) existing.setDurationText(song.getDurationText());
        if (song.getLyrics() != null) existing.setLyrics(song.getLyrics());
        if (song.getSortOrder() != null) existing.setSortOrder(song.getSortOrder());
        songMapper.updateById(existing);
        return Result.success(null, "更新成功");
    }

    @DeleteMapping("/songs/{id}")
    public Map<String, Object> deleteSong(@PathVariable Integer id) {
        musicService.deleteSong(id);
        return Result.success(null, "删除成功");
    }

    // ===== Artists =====
    @GetMapping("/artists")
    public Map<String, Object> getArtists() {
        return Result.success(artistMapper.selectAllWithCount());
    }

    @GetMapping("/artists/{id}")
    public Map<String, Object> getArtist(@PathVariable Integer id) {
        Artist artist = artistMapper.selectById(id);
        if (artist == null) return Result.error(404, "Not found");
        Map<String, Object> result = new HashMap<>();
        result.put("id", artist.getId());
        result.put("name", artist.getName());
        result.put("cover", artist.getCover());
        result.put("description", artist.getDescription());
        result.put("created_at", artist.getCreatedAt());
        result.put("songs", songMapper.selectByArtistIdWithDetails(id));
        return Result.success(result);
    }

    @PostMapping("/artists")
    public Map<String, Object> createArtist(@RequestBody Artist artist) {
        artist.setCreatedAt(LocalDateTime.now());
        artistMapper.insert(artist);
        return Result.success(Map.of("id", artist.getId()), "创建成功");
    }

    @PutMapping("/artists/{id}")
    public Map<String, Object> updateArtist(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Artist existing = artistMapper.selectById(id);
        if (existing == null) return Result.error(404, "Not found");
        if (body.containsKey("name")) existing.setName((String) body.get("name"));
        if (body.containsKey("cover")) existing.setCover((String) body.get("cover"));
        if (body.containsKey("description")) existing.setDescription((String) body.get("description"));
        artistMapper.updateById(existing);
        return Result.success(null, "更新成功");
    }

    @DeleteMapping("/artists/{id}")
    public Map<String, Object> deleteArtist(@PathVariable Integer id) {
        musicService.deleteArtist(id);
        return Result.success(null, "删除成功");
    }

    @GetMapping("/artists/{artistId}/direct-songs")
    public Map<String, Object> getDirectSongs(@PathVariable Integer artistId) {
        List<Song> songs = songMapper.selectByArtistIdWithDetails(artistId).stream()
            .filter(s -> s.getAlbumId() == null)
            .toList();
        return Result.success(songs);
    }

    // ===== Albums =====
    @GetMapping("/albums")
    public Map<String, Object> getAlbums() {
        return Result.success(albumMapper.selectAllWithDetails());
    }

    @GetMapping("/albums/by-artist/{artistId}")
    public Map<String, Object> getAlbumsByArtist(@PathVariable Integer artistId) {
        return Result.success(albumMapper.selectByArtistIdWithDetails(artistId));
    }

    @PostMapping("/albums")
    public Map<String, Object> createAlbum(@RequestBody Album album) {
        album.setCreatedAt(LocalDateTime.now());
        albumMapper.insert(album);
        return Result.success(Map.of("id", album.getId()), "创建成功");
    }

    @DeleteMapping("/albums/{id}")
    public Map<String, Object> deleteAlbum(@PathVariable Integer id) {
        musicService.deleteAlbum(id);
        return Result.success(null, "删除成功");
    }

    // ===== Playlists =====
    @GetMapping("/playlists")
    public Map<String, Object> getPlaylists() {
        List<Map<String, Object>> allPlaylists = new ArrayList<>();
        List<User> users = userMapper.selectList(null);
        for (User user : users) {
            List<Map<String, Object>> userPlaylists = playlistMapper.selectByUserIdWithCount(user.getId());
            for (Map<String, Object> pl : userPlaylists) {
                pl.put("username", user.getUsername());
                allPlaylists.add(pl);
            }
        }
        return Result.success(allPlaylists);
    }

    @PostMapping("/playlists/batch-add-songs")
    public Map<String, Object> batchAddSongs(@RequestBody Map<String, Object> body) {
        Integer playlistId = (Integer) body.get("playlist_id");
        @SuppressWarnings("unchecked")
        List<Integer> songIds = (List<Integer>) body.get("song_ids");
        if (playlistId == null || songIds == null || songIds.isEmpty()) {
            return Result.error(400, "参数错误");
        }
        int added = 0;
        for (Integer songId : songIds) {
            if (playlistSongMapper.countByPlaylistAndSong(playlistId, songId) == 0) {
                PlaylistSong ps = new PlaylistSong();
                ps.setPlaylistId(playlistId);
                ps.setSongId(songId);
                playlistSongMapper.insert(ps);
                added++;
            }
        }
        return Result.success(null, "已添加 " + added + " 首歌曲");
    }

    // ===== Users =====
    @GetMapping("/users")
    public Map<String, Object> getUsers(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> qw =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            qw.like(User::getUsername, keyword).or().like(User::getNickname, keyword);
            return Result.success(userMapper.selectList(qw));
        }
        return Result.success(userMapper.selectList(null));
    }

    @GetMapping("/users/{userId}/playlists")
    public Map<String, Object> getUserPlaylists(@PathVariable Integer userId) {
        return Result.success(playlistMapper.selectByUserIdWithCount(userId));
    }

    // ===== Access Logs =====
    @GetMapping("/access-logs")
    public Map<String, Object> getAccessLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(new ArrayList<>());
    }

    // ===== Upload =====
    @PostMapping("/upload/song")
    public Map<String, Object> uploadSong(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error(400, "文件不能为空");
        try {
            String url = cosService.uploadSong(file);
            return Result.success(Map.of("url", url), "上传成功");
        } catch (IOException e) {
            return Result.error(500, "上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload/cover")
    public Map<String, Object> uploadCover(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error(400, "文件不能为空");
        try {
            String url = cosService.uploadCover(file);
            return Result.success(Map.of("url", url), "上传成功");
        } catch (IOException e) {
            return Result.error(500, "上传失败: " + e.getMessage());
        }
    }
}
