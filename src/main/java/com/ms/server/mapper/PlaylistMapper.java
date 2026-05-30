package com.ms.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ms.server.entity.Playlist;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface PlaylistMapper extends BaseMapper<Playlist> {

    @Select("SELECT p.*, (SELECT COUNT(*) FROM playlist_songs WHERE playlist_id = p.id) as song_count " +
            "FROM playlists p WHERE p.user_id = #{userId} ORDER BY p.created_at DESC")
    List<Map<String, Object>> selectByUserIdWithCount(@Param("userId") Integer userId);
}
