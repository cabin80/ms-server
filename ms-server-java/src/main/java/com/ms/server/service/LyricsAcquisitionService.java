package com.ms.server.service;

import com.ms.server.entity.Song;
import com.ms.server.mapper.SongMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 歌词获取服务 —— 独立的歌词获取模块，admin 和 server 均可调用。
 *
 * 触发时机：
 *   1. admin 上传歌曲时触发（上传完成立即获取，写入 DB，不影响上传响应时间）
 *   2. server 返回歌曲列表/详情时触发（歌词为空时异步获取，返回已有信息）
 *
 * 歌词源链：
 *   LRCLIB (#1) → Meting-API / 网易云 (#2) → 兜底
 */
@Service
public class LyricsAcquisitionService {

    private static final Logger log = LoggerFactory.getLogger(LyricsAcquisitionService.class);

    private final LyricsService lyricsService;
    private final SongMapper songMapper;

    public LyricsAcquisitionService(LyricsService lyricsService, SongMapper songMapper) {
        this.lyricsService = lyricsService;
        this.songMapper = songMapper;
    }

    /**
     * 尝试为歌曲获取歌词并保存到数据库。
     * 如果 lyric 字段已存在且不为空，则跳过。
     * 幂等，可重复调用无副作用。
     *
     * @param song 歌曲对象（必须包含 name、artistName、duration 等信息）
     * @return 获取到的歌词字符串，或 null（未获取到/已存在）
     */
    public String acquireIfMissing(Song song) {
        if (song == null) return null;
        // 已有歌词则跳过
        if (song.getLyrics() != null && !song.getLyrics().isBlank()) {
            return song.getLyrics();
        }
        // 临时调试：记录传入的 song 内容
        if (song.getName() == null || song.getName().isBlank()) {
            log.warn("[DEBUG] acquireIfMissing: song.id={} name is BLANK! all fields: id={} artistName={} albumName={}",
                    song.getId(), song.getId(), song.getArtistName(), song.getAlbumName());
        }
        return doAcquire(song);
    }

    /**
     * 强制为歌曲获取歌词（即使已有也重新获取）。
     * 用于 admin 手动触发重新抓取。
     *
     * @param song 歌曲对象
     * @return 获取到的歌词字符串，或 null
     */
    public String forceAcquire(Song song) {
        if (song == null) return null;
        return doAcquire(song);
    }

    /**
     * 根据歌曲 ID 获取歌词并保存。
     *
     * @param songId 歌曲 ID
     * @return 获取到的歌词字符串，或 null
     */
    public String acquireById(Integer songId) {
        Song song = songMapper.selectById(songId);
        if (song == null) return null;
        return acquireIfMissing(song);
    }

    /**
     * 核心获取逻辑
     */
    private String doAcquire(Song song) {
        try {
            if (song.getName() == null || song.getName().isBlank()) {
                log.warn("歌词获取跳过: 歌曲 name 为空 (songId={})", song.getId());
                return null;
            }
            // 解析时长
            int duration = parseDuration(song);
            log.info("歌词获取开始: {} -- {} ({}s)", song.getName(), song.getArtistName(), duration);
            // 多源获取
            Map<String, Object> resp = lyricsService.fetchLyrics(
                    song.getName(),
                    song.getArtistName(),
                    song.getAlbumName(),
                    duration
            );
            String lyrics = lyricsService.extractLyrics(resp);
            if (lyrics != null) {
                // 只更新 lyrics 字段，不修改其他字段
                songMapper.updateLyricsById(song.getId(), lyrics);
                song.setLyrics(lyrics);
                log.info("歌词获取成功: {} -- {} ({} chars)",
                        song.getName(), song.getArtistName(), lyrics.length());
            } else {
                log.info("歌词未找到: {} -- {}", song.getName(), song.getArtistName());
            }
            return lyrics;
        } catch (Exception e) {
            log.warn("歌词获取异常: {} - {}: {}", song.getName(), song.getArtistName(), e.getMessage());
            return null;
        }
    }

    /**
     * 从 durationText（如 "03:34"）或 duration（秒数）解析秒
     */
    public static int parseDuration(Song song) {
        if (song.getDuration() != null && song.getDuration() > 0) {
            return song.getDuration();
        }
        if (song.getDurationText() != null && song.getDurationText().contains(":")) {
            String[] parts = song.getDurationText().split(":");
            try {
                return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }
}
