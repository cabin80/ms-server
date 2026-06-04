package com.ms.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ms.server.entity.Song;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

public interface SongMapper extends BaseMapper<Song> {

    @Results(id = "songDetail", value = {
        @Result(property = "id", column = "id", id = true),
        @Result(property = "name", column = "name"),
        @Result(property = "artistId", column = "artist_id"),
        @Result(property = "albumId", column = "album_id"),
        @Result(property = "url", column = "url"),
        @Result(property = "duration", column = "duration"),
        @Result(property = "durationText", column = "duration_text"),
        @Result(property = "lyrics", column = "lyrics"),
        @Result(property = "sortOrder", column = "sort_order"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "artistName", column = "artist_name"),
        @Result(property = "artistCover", column = "artist_cover"),
        @Result(property = "albumName", column = "album_name"),
        @Result(property = "albumCover", column = "album_cover")
    })
    @Select("SELECT s.*, a.name as artist_name, a.cover as artist_cover, al.name as album_name, al.cover as album_cover " +
            "FROM songs s " +
            "LEFT JOIN artists a ON s.artist_id = a.id " +
            "LEFT JOIN albums al ON s.album_id = al.id " +
            "ORDER BY s.sort_order ASC")
    List<Song> selectAllWithDetails();

    @ResultMap("songDetail")
    @Select("SELECT s.*, a.name as artist_name, a.cover as artist_cover, al.name as album_name, al.cover as album_cover " +
            "FROM songs s " +
            "LEFT JOIN artists a ON s.artist_id = a.id " +
            "LEFT JOIN albums al ON s.album_id = al.id " +
            "WHERE s.id = #{id}")
    Song selectByIdWithDetails(@Param("id") Integer id);

    @ResultMap("songDetail")
    @Select("SELECT s.*, a.name as artist_name, a.cover as artist_cover, al.name as album_name, al.cover as album_cover " +
            "FROM songs s " +
            "LEFT JOIN artists a ON s.artist_id = a.id " +
            "LEFT JOIN albums al ON s.album_id = al.id " +
            "WHERE s.artist_id = #{artistId} ORDER BY s.sort_order ASC")
    List<Song> selectByArtistIdWithDetails(@Param("artistId") Integer artistId);

    @ResultMap("songDetail")
    @Select("SELECT s.*, a.name as artist_name, a.cover as artist_cover, al.name as album_name, al.cover as album_cover " +
            "FROM songs s " +
            "LEFT JOIN artists a ON s.artist_id = a.id " +
            "LEFT JOIN albums al ON s.album_id = al.id " +
            "WHERE s.album_id = #{albumId} ORDER BY s.sort_order ASC")
    List<Song> selectByAlbumIdWithDetails(@Param("albumId") Integer albumId);

    @ResultMap("songDetail")
    @Select("SELECT s.*, a.name as artist_name, a.cover as artist_cover, al.name as album_name, al.cover as album_cover " +
            "FROM songs s " +
            "JOIN playlist_songs ps ON s.id = ps.song_id " +
            "LEFT JOIN artists a ON s.artist_id = a.id " +
            "LEFT JOIN albums al ON s.album_id = al.id " +
            "WHERE ps.playlist_id = #{playlistId} ORDER BY ps.sort_order ASC")
    List<Song> selectByPlaylistIdWithDetails(@Param("playlistId") Integer playlistId);

    @ResultMap("songDetail")
    @Select("SELECT s.*, a.name as artist_name, a.cover as artist_cover, al.name as album_name, al.cover as album_cover " +
            "FROM favorites f " +
            "JOIN songs s ON f.song_id = s.id " +
            "LEFT JOIN artists a ON s.artist_id = a.id " +
            "LEFT JOIN albums al ON s.album_id = al.id " +
            "WHERE f.user_id = #{userId} ORDER BY f.created_at DESC")
    List<Song> selectFavoritesByUserId(@Param("userId") Integer userId);

    // Simple queries
    @Select("SELECT s.* FROM songs s WHERE s.artist_id = #{artistId} ORDER BY s.sort_order ASC")
    List<Song> selectByArtistId(@Param("artistId") Integer artistId);

    @Select("SELECT s.* FROM songs s WHERE s.album_id = #{albumId} ORDER BY s.sort_order ASC")
    List<Song> selectByAlbumId(@Param("albumId") Integer albumId);

    @Select("SELECT s.* FROM playlist_songs ps JOIN songs s ON ps.song_id = s.id WHERE ps.playlist_id = #{playlistId} ORDER BY ps.sort_order ASC")
    List<Song> selectByPlaylistId(@Param("playlistId") Integer playlistId);

    @Select("SELECT COUNT(*) FROM songs WHERE album_id = #{albumId}")
    Long countByAlbumId(@Param("albumId") Integer albumId);

    @Select("SELECT COUNT(*) FROM songs WHERE artist_id = #{artistId}")
    Long countByArtistId(@Param("artistId") Integer artistId);

    @Select("SELECT COUNT(*) FROM playlist_songs WHERE playlist_id = #{playlistId}")
    Long countByPlaylist(@Param("playlistId") Integer playlistId);

    /**
     * 仅更新歌词字段，不修改其他字段（用于歌词获取场景）
     */
    @Update("UPDATE songs SET lyrics = #{lyrics} WHERE id = #{id}")
    int updateLyricsById(@Param("id") Integer id, @Param("lyrics") String lyrics);
}
