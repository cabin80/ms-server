package com.ms.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("songs")
public class Song {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    @TableField("artist_id")
    private Integer artistId;
    @TableField("album_id")
    private Integer albumId;
    private String url;
    private Integer duration;
    @TableField("duration_text")
    private String durationText;
    private String lyrics;
    @TableField("sort_order")
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String artistName;
    @TableField(exist = false)
    private String artistCover;
    @TableField(exist = false)
    private String albumName;
    @TableField(exist = false)
    private String albumCover;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getArtistId() { return artistId; }
    public void setArtistId(Integer artistId) { this.artistId = artistId; }
    public Integer getAlbumId() { return albumId; }
    public void setAlbumId(Integer albumId) { this.albumId = albumId; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public String getDurationText() { return durationText; }
    public void setDurationText(String durationText) { this.durationText = durationText; }
    public String getLyrics() { return lyrics; }
    public void setLyrics(String lyrics) { this.lyrics = lyrics; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public String getArtistCover() { return artistCover; }
    public void setArtistCover(String artistCover) { this.artistCover = artistCover; }
    public String getAlbumName() { return albumName; }
    public void setAlbumName(String albumName) { this.albumName = albumName; }
    public String getAlbumCover() { return albumCover; }
    public void setAlbumCover(String albumCover) { this.albumCover = albumCover; }
}
