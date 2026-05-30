package com.ms.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("playlist_songs")
public class PlaylistSong {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("playlist_id")
    private Integer playlistId;
    @TableField("song_id")
    private Integer songId;
    @TableField("sort_order")
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getPlaylistId() { return playlistId; }
    public void setPlaylistId(Integer playlistId) { this.playlistId = playlistId; }
    public Integer getSongId() { return songId; }
    public void setSongId(Integer songId) { this.songId = songId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
