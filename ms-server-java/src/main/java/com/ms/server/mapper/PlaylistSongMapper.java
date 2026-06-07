package com.ms.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ms.server.entity.PlaylistSong;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface PlaylistSongMapper extends BaseMapper<PlaylistSong> {

    @Select("SELECT COUNT(*) FROM playlist_songs WHERE playlist_id = #{playlistId} AND song_id = #{songId}")
    int countByPlaylistAndSong(@Param("playlistId") Integer playlistId, @Param("songId") Integer songId);
}
