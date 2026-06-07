package com.ms.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ms.admin.entity.*;
import com.ms.admin.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final SongMapper songMapper;
    private final ArtistMapper artistMapper;
    private final AlbumMapper albumMapper;
    private final PlaylistMapper playlistMapper;
    private final UserMapper userMapper;
    private final FavoriteMapper favoriteMapper;
    private final PlaylistSongMapper playlistSongMapper;

    public AdminService(SongMapper songMapper, ArtistMapper artistMapper, AlbumMapper albumMapper,
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

    // ====== Songs ======
    public List<Song> getAllSongs() { return songMapper.selectAllWithDetails(); }
    public Song getSongById(Integer id) { return songMapper.selectByIdWithDetails(id); }

    public List<Song> getSongsByArtist(Integer artistId) {
        return songMapper.selectByArtistIdWithDetails(artistId);
    }

    public List<Song> getSongsByAlbum(Integer albumId) {
        return songMapper.selectByAlbumIdWithDetails(albumId);
    }

    public List<Song> searchSongs(String name, String artistName, String region, String firstLetter) {
        List<Song> all = songMapper.selectAllWithDetails();
        if (name != null && !name.isEmpty())
            all = all.stream().filter(s -> s.getName() != null && s.getName().contains(name)).collect(Collectors.toList());
        if (artistName != null && !artistName.isEmpty())
            all = all.stream().filter(s -> s.getArtistName() != null && s.getArtistName().contains(artistName)).collect(Collectors.toList());
        if ((region != null && !region.isEmpty()) || (firstLetter != null && !firstLetter.isEmpty())) {
            List<Map<String, Object>> artists = artistMapper.selectAllWithCount();
            Set<Integer> filteredArtistIds = artists.stream()
                .filter(a -> {
                    if (region != null && !region.isEmpty() && !region.equals(a.get("region"))) return false;
                    if (firstLetter != null && !firstLetter.isEmpty()) {
                        String fl = (String) a.get("first_letter");
                        if (firstLetter.equals("#")) {
                            if (fl == null || !fl.matches("^[0-9]")) return false;
                        } else {
                            if (fl == null || !fl.equalsIgnoreCase(firstLetter)) return false;
                        }
                    }
                    return true;
                })
                .map(a -> (Integer) a.get("id"))
                .collect(Collectors.toSet());
            all = all.stream().filter(s -> filteredArtistIds.contains(s.getArtistId())).collect(Collectors.toList());
        }
        return all;
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
    public Long getSongCount() { return songMapper.selectCount(null); }

    // ====== Artists ======
    public List<Map<String, Object>> getAllArtists() { return artistMapper.selectAllWithCount(); }

    public Map<String, Object> getArtistById(Integer id) {
        Map<String, Object> artist = artistMapper.selectByIdWithCount(id);
        if (artist == null) return null;
        List<Song> songs = songMapper.selectByArtistIdWithDetails(id);
        artist.put("songs", songs);
        return artist;
    }

    @Transactional
    public Artist createArtist(Artist artist) {
        artist.setCreatedAt(LocalDateTime.now());
        artistMapper.insert(artist);
        return artist;
    }

    @Transactional
    public void updateArtist(Integer id, Map<String, Object> fields) {
        Artist existing = artistMapper.selectById(id);
        if (existing == null) return;
        if (fields.containsKey("status")) existing.setStatus((Integer) fields.get("status"));
        if (fields.containsKey("recommend_order")) existing.setRecommendOrder((Integer) fields.get("recommend_order"));
        if (fields.containsKey("region")) existing.setRegion((String) fields.get("region"));
        if (fields.containsKey("name")) existing.setName((String) fields.get("name"));
        if (fields.containsKey("cover")) existing.setCover((String) fields.get("cover"));
        if (fields.containsKey("description")) existing.setDescription((String) fields.get("description"));
        artistMapper.updateById(existing);
    }

    @Transactional
    public void deleteArtist(Integer id) { artistMapper.deleteById(id); }
    public Long getArtistCount() { return artistMapper.selectCount(null); }

    public List<Song> getDirectSongs(Integer artistId) {
        return songMapper.selectByArtistIdWithDetails(artistId).stream()
            .filter(s -> s.getAlbumId() == null)
            .collect(Collectors.toList());
    }

    // ====== Albums ======
    public List<Map<String, Object>> getAllAlbums() { return albumMapper.selectAllWithDetails(); }

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
    public void deleteAlbum(Integer id) { albumMapper.deleteById(id); }
    public Long getAlbumCount() { return albumMapper.selectCount(null); }

    // ====== Playlists ======
    public List<Map<String, Object>> getAllPlaylists() {
        List<User> users = userMapper.selectList(null);
        List<Map<String, Object>> allPlaylists = new ArrayList<>();
        for (User user : users) {
            List<Map<String, Object>> userPlaylists = playlistMapper.selectByUserIdWithCount(user.getId());
            for (Map<String, Object> pl : userPlaylists) {
                pl.put("username", user.getUsername());
                allPlaylists.add(pl);
            }
        }
        return allPlaylists;
    }

    public Long getPlaylistCount() { return playlistMapper.selectCount(null); }

    public List<Map<String, Object>> getUserPlaylists(Integer userId) {
        return playlistMapper.selectByUserIdWithCount(userId);
    }

    public Map<String, Object> getPlaylistDetail(Integer id) {
        // 获取歌单基本信息
        Playlist playlist = playlistMapper.selectById(id);
        if (playlist == null) return null;
        // 获取歌曲列表
        List<Song> songs = songMapper.selectByPlaylistIdWithDetails(id);
        Map<String, Object> result = new HashMap<>();
        result.put("id", playlist.getId());
        result.put("name", playlist.getName());
        result.put("cover", playlist.getCover());
        result.put("user_id", playlist.getUserId());
        result.put("description", playlist.getDescription());
        result.put("created_at", playlist.getCreatedAt());
        result.put("songs", songs);
        return result;
    }

    @Transactional
    public void addSongToPlaylist(Integer playlistId, Integer songId) {
        if (playlistSongMapper.countByPlaylistAndSong(playlistId, songId) == 0) {
            PlaylistSong ps = new PlaylistSong();
            ps.setPlaylistId(playlistId);
            ps.setSongId(songId);
            playlistSongMapper.insert(ps);
        }
    }

    @Transactional
    public int batchAddSongsToPlaylist(Integer playlistId, List<Integer> songIds) {
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
        return added;
    }

    // ====== Users ======
    public List<User> getUsers(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
            qw.like(User::getUsername, keyword).or().like(User::getNickname, keyword);
            return userMapper.selectList(qw);
        }
        return userMapper.selectList(null);
    }

    public User getUserById(Integer id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null); // 不返回密码
        }
        return user;
    }

    public User login(String username, String md5Password) {
        User user = userMapper.selectByUsernameAndPassword(username, md5Password);
        if (user != null) {
            user.setPassword(null); // 不返回密码
        }
        return user;
    }

    @Transactional
    public User register(String username, String md5Password, String nickname) {
        // 检查重名
        User existing = userMapper.selectByUsername(username);
        if (existing != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(md5Password);
        user.setNickname(nickname);
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    // ====== Favorites ======
    public List<Song> getUserFavorites(Integer userId) {
        return songMapper.selectFavoritesByUserId(userId);
    }

    @Transactional
    public boolean toggleFavorite(Integer userId, Integer songId) {
        if (favoriteMapper.existsByUserIdAndSongId(userId, songId) > 0) {
            LambdaQueryWrapper<Favorite> qw = new LambdaQueryWrapper<>();
            qw.eq(Favorite::getUserId, userId).eq(Favorite::getSongId, songId);
            favoriteMapper.delete(qw);
            return false;
        } else {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setSongId(songId);
            fav.setCreatedAt(LocalDateTime.now());
            favoriteMapper.insert(fav);
            return true;
        }
    }

    public boolean checkFavorite(Integer userId, Integer songId) {
        return favoriteMapper.existsByUserIdAndSongId(userId, songId) > 0;
    }

    // ====== Access Logs ======
    public List<Map<String, Object>> getAccessLogs(int page, int size) {
        return new ArrayList<>();
    }

    // ====== Stats ======
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("songs", getSongCount());
        stats.put("artists", getArtistCount());
        stats.put("albums", getAlbumCount());
        stats.put("playlists", getPlaylistCount());
        return stats;
    }
}
