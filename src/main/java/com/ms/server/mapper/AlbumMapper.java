package com.ms.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ms.server.entity.Album;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface AlbumMapper extends BaseMapper<Album> {

    @Select("SELECT al.*, ar.name as artist_name, (SELECT COUNT(*) FROM songs WHERE album_id = al.id) as song_count " +
            "FROM albums al LEFT JOIN artists ar ON al.artist_id = ar.id ORDER BY al.name ASC")
    List<Map<String, Object>> selectAllWithDetails();

    @Select("SELECT al.*, ar.name as artist_name, (SELECT COUNT(*) FROM songs WHERE album_id = al.id) as song_count " +
            "FROM albums al LEFT JOIN artists ar ON al.artist_id = ar.id WHERE al.artist_id = #{artistId} ORDER BY al.name ASC")
    List<Map<String, Object>> selectByArtistIdWithDetails(@Param("artistId") Integer artistId);

    @Select("SELECT al.*, ar.name as artist_name FROM albums al LEFT JOIN artists ar ON al.artist_id = ar.id WHERE al.id = #{id}")
    Map<String, Object> selectByIdWithDetails(@Param("id") Integer id);
}
