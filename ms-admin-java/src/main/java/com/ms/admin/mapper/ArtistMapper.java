package com.ms.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ms.admin.entity.Artist;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface ArtistMapper extends BaseMapper<Artist> {

    @Select("SELECT a.*, (SELECT COUNT(*) FROM songs WHERE artist_id = a.id) as song_count " +
            "FROM artists a ORDER BY a.name ASC")
    List<Map<String, Object>> selectAllWithCount();

    @Select("SELECT a.*, (SELECT COUNT(*) FROM songs WHERE artist_id = a.id) as song_count " +
            "FROM artists a WHERE a.id = #{id}")
    Map<String, Object> selectByIdWithCount(@Param("id") Integer id);
}
