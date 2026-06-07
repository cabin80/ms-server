package com.ms.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ms.server.entity.Artist;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface ArtistMapper extends BaseMapper<Artist> {

    @Select("SELECT a.*, (SELECT COUNT(*) FROM songs WHERE artist_id = a.id) as song_count " +
            "FROM artists a ORDER BY a.name ASC")
    List<Map<String, Object>> selectAllWithCount();
}
