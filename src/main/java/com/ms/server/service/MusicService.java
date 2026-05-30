package com.ms.server.service;

import com.ms.server.entity.*;
import com.ms.server.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MusicService {

    private final SongMapper songMapper;
    private final ArtistMapper artistMapper;
    private final AlbumMapper albumMapper;
    private final PlaylistMapper playlistMapper;
    private final UserMapper userMapper;
    private final FavoriteMapper favoriteMapper;
    private final PlaylistSongMapper playlistSongMapper;

    public MusicService(SongMapper songMapper, ArtistMapper artistMapper, AlbumMapper albumMapper,
                        PlaylistMapper playlistMapper, UserMapper userMapper, FavoriteMapper favoriteMapper,
                        PlaylistSongMapper playlistSongMapper) {
        this.songMapper = songMapper;
        this.artistMapper = artistMapper;
        this.albumMapper = albumMapper;
        this.playlistMapper = playlistMapper;
        this.userMapper = userMapper;
        this.favoriteMapper = favoriteMapper;
        this.playlistSongMapper = playlistSongMapper;
    }

    // ===================== Songs ======================

    public List<Song> getAllSongs() {
        return songMapper.selectAllWithDetails();
    }

    public Song getSongById(Integer id) {
        return songMapper.selectByIdWithDetails(id);
    }

    public List<Song> getSongsByArtist(Integer artistId) {
        return songMapper.selectByArtistIdWithDetails(artistId);
    }

    public List<Song> getSongsByAlbum(Integer albumId) {
        return songMapper.selectByAlbumIdWithDetails(albumId);
    }

    public List<Song> getSongsByPlaylist(Integer playlistId) {
        return songMapper.selectByPlaylistIdWithDetails(playlistId);
    }

    @Transactional
    public Song createSong(Song song) {
        song.setCreatedAt(LocalDateTime.now());
        songMapper.insert(song);
        return song;
    }

    @Transactional
    public Song updateSong(Integer id, Song song) {
        Song existing = songMapper.selectById(id);
        if (existing == null) return null;
        if (song.getName() != null) existing.setName(song.getName());
        if (song.getArtistId() != null) existing.setArtistId(song.getArtistId());
        if (song.getAlbumId() != null) existing.setAlbumId(song.getAlbumId());
        if (song.getUrl() != null) existing.setUrl(song.getUrl());
        if (song.getDurationText() != null) existing.setDurationText(song.getDurationText());
        if (song.getLyrics() != null) existing.setLyrics(song.getLyrics());
        if (song.getSortOrder() != null) existing.setSortOrder(song.getSortOrder());
        songMapper.updateById(existing);
        return existing;
    }

    @Transactional
    public void deleteSong(Integer id) { songMapper.deleteById(id); }

    // ===================== Artists =====================

    public List<Map<String, Object>> getAllArtists() {
        return artistMapper.selectAllWithCount();
    }

    public Artist getArtistById(Integer id) { return artistMapper.selectById(id); }

    @Transactional
    public Artist createArtist(Artist artist) {
        artist.setCreatedAt(LocalDateTime.now());
        artistMapper.insert(artist);
        return artist;
    }

    @Transactional
    public Artist updateArtist(Integer id, Artist artist) {
        Artist existing = artistMapper.selectById(id);
        if (existing == null) return null;
        if (artist.getName() != null) existing.setName(artist.getName());
        if (artist.getCover() != null) existing.setCover(artist.getCover());
        if (artist.getDescription() != null) existing.setDescription(artist.getDescription());
        artistMapper.updateById(existing);
        return existing;
    }

    @Transactional
    public void deleteArtist(Integer id) { artistMapper.deleteById(id); }

    // ===================== Albums =====================

    public List<Map<String, Object>> getAllAlbums() {
        return albumMapper.selectAllWithDetails();
    }

    public Map<String, Object> getAlbumById(Integer id) {
        var album = albumMapper.selectByIdWithDetails(id);
        if (album == null) return null;
        album.put("songs", getSongsByAlbum(id));
        return album;
    }

    public List<Map<String, Object>> getAlbumsByArtist(Integer artistId) {
        return albumMapper.selectByArtistIdWithDetails(artistId);
    }

    @Transactional
    public Album createAlbum(Album album) {
        album.setCreatedAt(LocalDateTime.now());
        albumMapper.insert(album);
        return album;
    }

    @Transactional
    public Album updateAlbum(Integer id, Album album) {
        Album existing = albumMapper.selectById(id);
        if (existing == null) return null;
        if (album.getName() != null) existing.setName(album.getName());
        if (album.getCover() != null) existing.setCover(album.getCover());
        if (album.getArtistId() != null) existing.setArtistId(album.getArtistId());
        if (album.getDescription() != null) existing.setDescription(album.getDescription());
        albumMapper.updateById(existing);
        return existing;
    }

    @Transactional
    public void deleteAlbum(Integer id) { albumMapper.deleteById(id); }

    // ===================== Playlists =====================

    public List<Map<String, Object>> getPlaylistsByUser(Integer userId) {
        return playlistMapper.selectByUserIdWithCount(userId);
    }

    public Map<String, Object> getPlaylistDetail(Integer id) {
        Playlist p = playlistMapper.selectById(id);
        if (p == null) return null;
        Map<String, Object> result = new HashMap<>();
        result.put("id", p.getId());
        result.put("name", p.getName());
        result.put("cover", p.getCover());
        result.put("user_id", p.getUserId());
        result.put("description", p.getDescription());
        result.put("created_at", p.getCreatedAt());
        result.put("songs", getSongsByPlaylist(id));
        return result;
    }

    @Transactional
    public Playlist createPlaylist(Playlist playlist) {
        playlist.setCreatedAt(LocalDateTime.now());
        playlistMapper.insert(playlist);
        return playlist;
    }

    @Transactional
    public String addSongToPlaylist(Integer playlistId, Integer songId) {
        if (playlistSongMapper.countByPlaylistAndSong(playlistId, songId) > 0) {
            return "歌曲已存在歌单中";
        }
        PlaylistSong ps = new PlaylistSong();
        ps.setPlaylistId(playlistId);
        ps.setSongId(songId);
        ps.setCreatedAt(LocalDateTime.now());
        playlistSongMapper.insert(ps);
        return "已添加到歌单";
    }

    // ===================== Users =====================

    public Map<String, Object> login(String username, String password) {
        User user = userMapper.selectByUsernameAndPassword(username, md5(password));
        if (user == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("nickname", user.getNickname() != null ? user.getNickname() : "");
        map.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
        return map;
    }

    public User getUserById(Integer id) { return userMapper.selectById(id); }

    @Transactional
    public User register(String username, String password, String nickname) {
        if (userMapper.selectByUsername(username) != null) return null;
        User user = new User();
        user.setUsername(username);
        user.setPassword(md5(password));
        user.setNickname(nickname != null ? nickname : username);
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }

    // ===================== Favorites =====================

    public List<Song> getFavorites(Integer userId) {
        return songMapper.selectFavoritesByUserId(userId);
    }

    @Transactional
    public Map<String, Object> toggleFavorite(Integer userId, Integer songId) {
        Map<String, Object> result = new HashMap<>();
        Favorite existing = favoriteMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getSongId, songId));
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            result.put("favorited", false);
        } else {
            Favorite f = new Favorite();
            f.setUserId(userId);
            f.setSongId(songId);
            f.setCreatedAt(LocalDateTime.now());
            favoriteMapper.insert(f);
            result.put("favorited", true);
        }
        return result;
    }

    public boolean checkFavorite(Integer userId, Integer songId) {
        return favoriteMapper.existsByUserIdAndSongId(userId, songId) > 0;
    }

    private String md5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return input;
        }
    }
}
