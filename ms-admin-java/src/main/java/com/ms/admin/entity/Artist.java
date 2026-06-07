package com.ms.admin.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

@TableName("artists")
public class Artist {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String cover;
    private String description;
    private String region;
    private String style;
    private String firstLetter;
    private Integer status;
    private Integer recommendOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private Long songCount;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }
    public String getFirstLetter() { return firstLetter; }
    public void setFirstLetter(String firstLetter) { this.firstLetter = firstLetter; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getRecommendOrder() { return recommendOrder; }
    public void setRecommendOrder(Integer recommendOrder) { this.recommendOrder = recommendOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getSongCount() { return songCount; }
    public void setSongCount(Long songCount) { this.songCount = songCount; }
}
