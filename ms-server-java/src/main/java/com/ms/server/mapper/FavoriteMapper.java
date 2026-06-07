package com.ms.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ms.server.entity.Favorite;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT COUNT(*) FROM favorites WHERE user_id = #{userId} AND song_id = #{songId}")
    int existsByUserIdAndSongId(@Param("userId") Integer userId, @Param("songId") Integer songId);
}
