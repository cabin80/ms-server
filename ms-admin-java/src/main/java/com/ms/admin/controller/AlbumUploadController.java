package com.ms.admin.controller;

import com.ms.admin.config.Result;
import com.ms.admin.entity.Album;
import com.ms.admin.entity.Song;
import com.ms.admin.service.AdminService;
import com.ms.admin.service.CosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AlbumUploadController {

    private static final Logger log = LoggerFactory.getLogger(AlbumUploadController.class);

    private final AdminService adminService;
    private final CosService cosService;

    public AlbumUploadController(AdminService adminService, CosService cosService) {
        this.adminService = adminService;
        this.cosService = cosService;
    }

    @PostMapping("/upload-album")
    public Map<String, Object> uploadAlbum(
            @RequestParam("album_name") String albumName,
            @RequestParam("artist_id") Integer artistId,
            @RequestParam("artist_name") String artistName,
            @RequestParam(value = "description", defaultValue = "") String description,
            @RequestParam(value = "cover", required = false) MultipartFile coverFile,
            @RequestParam("songs") MultipartFile[] songFiles) {

        log.info("上传专辑: name={}, artistId={}, songs={}", albumName, artistId, songFiles.length);

        // 1. 上传封面（如果有）
        String coverUrl = "";
        if (coverFile != null && !coverFile.isEmpty()) {
            try {
                coverUrl = cosService.uploadCover(coverFile);
            } catch (IOException e) {
                log.error("封面上传失败", e);
                return Result.error(500, "封面上传失败: " + e.getMessage());
            }
        }

        // 2. 创建专辑记录
        Album album = new Album();
        album.setName(albumName);
        album.setArtistId(artistId);
        album.setDescription(description);
        album.setCover(coverUrl);
        Album createdAlbum = adminService.createAlbum(album);
        Integer albumId = createdAlbum.getId();

        // 3. 上传歌曲文件并创建歌曲记录
        int songIndex = 0;
        for (MultipartFile songFile : songFiles) {
            if (songFile.isEmpty()) continue;
            songIndex++;

            String songUrl;
            try {
                songUrl = cosService.uploadSong(songFile);
            } catch (IOException e) {
                log.error("歌曲上传失败: {}", songFile.getOriginalFilename(), e);
                continue; // 跳过失败的歌曲，继续上传其他
            }

            // 从文件名推测歌曲名称（去掉扩展名）
            String originalName = songFile.getOriginalFilename();
            String songName = originalName;
            if (originalName != null && originalName.contains(".")) {
                songName = originalName.substring(0, originalName.lastIndexOf("."));
            }

            // 解析时长（尝试从文件名推测，如 "01-歌曲名.mp3" 或 "01. 歌曲名.mp3"）
            // 格式: "序号-歌曲名" 或 "序号. 歌曲名"
            String parsedName = songName;
            int sortOrder = songIndex;
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("^(\\d+)[.\\-\\s]+(.+)$");
            java.util.regex.Matcher m = p.matcher(songName);
            if (m.matches()) {
                try {
                    sortOrder = Integer.parseInt(m.group(1));
                    parsedName = m.group(2).trim();
                } catch (NumberFormatException ignored) {}
            }

            Song song = new Song();
            song.setName(parsedName);
            song.setArtistId(artistId);
            song.setAlbumId(albumId);
            song.setUrl(songUrl);
            song.setSortOrder(sortOrder);
            adminService.createSong(song);
        }

        log.info("专辑创建成功: id={}, name={}, songsCount={}", albumId, albumName, songIndex);
        return Result.success(Map.of("id", albumId, "songs_count", songIndex),
                String.format("专辑「%s」创建成功，共上传 %d 首歌曲", albumName, songIndex));
    }
}
