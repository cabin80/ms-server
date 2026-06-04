package com.ms.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * 歌词服务 - 多源歌词获取
 *
 * 歌词源优先级：
 *   1. LRCLIB /api/get (精确匹配)
 *   2. LRCLIB /api/search (模糊搜索)
 *   3. Meting-API (api.injahow.cn) — 通过网易云 ID 获取 LRC 歌词
 *      Meting-API 需要先通过本机网易云 API (localhost:3000) 搜索拿到歌曲 ID
 *
 * Meting-API 优势：可以获取中文歌曲、韩文歌曲的 LRC 同步歌词
 *   使用：type=lrc&id={song_id}&server=netease
 *   返回 raw LRC 文本
 */
@Service
public class LyricsService {

    private static final Logger log = LoggerFactory.getLogger(LyricsService.class);

    private static final String LRCLIB_BASE = "https://lrclib.net";
    private static final String METING_BASE = "https://api.injahow.cn";
    private static final String NETEASE_API_BASE = "http://localhost:3000";

    private final WebClient webClient;
    private final WebClient metingClient;
    private final WebClient neteaseClient;

    public LyricsService() {
        this.webClient = WebClient.builder()
                .baseUrl(LRCLIB_BASE)
                .defaultHeader("User-Agent", "MS-Music-Server/1.0 (music player)")
                .build();
        this.metingClient = WebClient.builder()
                .baseUrl(METING_BASE)
                .defaultHeader("User-Agent", "MS-Music-Server/1.0")
                .build();
        this.neteaseClient = WebClient.builder()
                .baseUrl(NETEASE_API_BASE)
                .defaultHeader("User-Agent", "MS-Music-Server/1.0")
                .build();
    }

    /**
     * 判断字符串是否包含 CJK（中日韩）字符
     */
    private boolean containsCjk(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c >= 0x4E00 && c <= 0x9FFF) ||  // 中日韩统一表意文字
                (c >= 0xAC00 && c <= 0xD7AF) ||  // 韩文音节
                (c >= 0x3040 && c <= 0x30FF)) {  // 日文假名
                return true;
            }
        }
        return false;
    }

    /**
     * 多源获取歌词
     * 策略链（CJK 歌曲优先走 Meting）：
     *   1. LRCLIB 精确匹配
     *   2. LRCLIB 纯歌名匹配（duration 过滤）
     *   3. LRCLIB search + 歌手名
     *   4. [CJK] Meting-API (网易云)
     *   5. LRCLIB search 纯歌名
     *   6. 短歌名搜索
     *   7. [非CJK] Meting-API 兜底
     */
    public Map<String, Object> fetchLyrics(String trackName, String artistName, String albumName, int duration) {
        // CJK（中/日/韩）歌曲：LRCLIB 几乎不可能有正确匹配，直接走 Meting
        // 非 CJK 歌曲：优先 LRCLIB，Meting 兜底
        boolean isCjk = containsCjk(trackName) || containsCjk(artistName);

        if (isCjk) {
            log.info("CJK歌曲→直接走 Meting: track={} artist={}", trackName, artistName);
            return tryMeting(trackName, artistName);
        }

        // 非 CJK 歌曲：LRCLIB 为主
        Map<String, Object> result = tryGet(trackName, artistName, albumName, duration);
        if (result != null) return result;
        result = tryGet(trackName, "", albumName, duration);
        if (result != null) return result;

        if (artistName != null && !artistName.isBlank()) {
            result = trySearch(trackName, artistName, duration);
            if (result != null) return result;
        }

        result = trySearch(trackName, "", duration);
        if (result != null) return result;

        if (trackName != null && trackName.contains(" ")) {
            String[] parts = trackName.split(" ");
            for (int i = parts.length - 1; i >= 1; i--) {
                StringBuilder sb = new StringBuilder();
                for (int j = i; j < parts.length; j++) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(parts[j]);
                }
                String shortName = sb.toString();
                if (!shortName.equals(trackName)) {
                    result = trySearch(shortName, "", duration);
                    if (result != null) return result;
                }
            }
        }

        return result;
    }

    private Map<String, Object> tryGet(String trackName, String artistName, String albumName, int duration) {
        try {
            Map<String, Object> result = webClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/get")
                                .queryParam("track_name", trackName != null ? trackName : "")
                                .queryParam("artist_name", artistName != null ? artistName : "");
                        if (albumName != null && !albumName.isBlank()) {
                            uriBuilder.queryParam("album_name", albumName);
                        }
                        uriBuilder.queryParam("duration", duration);
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (result != null && duration > 0) {
                Number resultDur = (Number) result.get("duration");
                if (resultDur != null && Math.abs(resultDur.intValue() - duration) > 10) {
                    log.debug("tryGet 时长不匹配: exp={} got={}", duration, resultDur.intValue());
                    return null;
                }
            }
            return result;
        } catch (Exception e) {
            log.debug("tryGet failed: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> trySearch(String trackName, String artistName, int duration) {
        try {
            List<Map<String, Object>> results = webClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/search")
                                .queryParam("track_name", trackName != null ? trackName : "");
                        if (artistName != null && !artistName.isBlank()) {
                            uriBuilder.queryParam("artist_name", artistName);
                        }
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            if (results != null && !results.isEmpty()) {
                final int TOLERANCE = 10;
                Map<String, Object> best = null;
                int bestDurDiff = Integer.MAX_VALUE;
                boolean bestHasSynced = false;

                for (Map<String, Object> item : results) {
                    String synced = (String) item.getOrDefault("syncedLyrics", "");
                    boolean hasSynced = synced != null && !synced.isBlank();
                    Number durNum = (Number) item.get("duration");
                    int durDiff = durNum != null && duration > 0
                            ? Math.abs(durNum.intValue() - duration) : Integer.MAX_VALUE;
                    if (hasSynced && durDiff <= TOLERANCE) {
                        if (!bestHasSynced || durDiff < bestDurDiff) {
                            bestDurDiff = durDiff;
                            bestHasSynced = true;
                            best = item;
                        }
                    }
                }
                if (best == null) {
                    int closest = Integer.MAX_VALUE;
                    for (Map<String, Object> item : results) {
                        Number durNum = (Number) item.get("duration");
                        int diff = durNum != null && duration > 0
                                ? Math.abs(durNum.intValue() - duration) : Integer.MAX_VALUE;
                        if (diff < closest) {
                            closest = diff;
                            best = item;
                        }
                    }
                }
                return best;
            }
        } catch (Exception e) {
            log.debug("trySearch failed: {}", e.getMessage());
        }
        return null;
    }

    private Map<String, Object> tryMeting(String trackName, String artistName) {
        try {
            if (trackName == null || trackName.isBlank()) return null;

            String searchUrl = "/search?keywords=" + java.net.URLEncoder.encode(trackName, "UTF-8") + "&limit=5";
            Map<String, Object> searchResult = neteaseClient.get()
                    .uri(searchUrl)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (searchResult == null) return null;

            Map<String, Object> resultData = (Map<String, Object>) searchResult.get("result");
            if (resultData == null) return null;

            List<Map<String, Object>> songs = (List<Map<String, Object>>) resultData.get("songs");
            if (songs == null || songs.isEmpty()) return null;

            Integer songId = null;
            String matchedArtist = "";
            Integer fallbackSongId = null;
            String fallbackArtist = "";

            for (Map<String, Object> song : songs) {
                String songName = (String) song.get("name");
                if (songName == null || songName.isBlank()) continue;

                String currentArtist = "";
                List<Map<String, Object>> artists = (List<Map<String, Object>>) song.get("artists");
                if (artists != null && !artists.isEmpty()) {
                    currentArtist = (String) artists.get(0).get("name");
                }

                boolean nameMatch = songName.equalsIgnoreCase(trackName)
                        || (trackName != null && songName.contains(trackName));
                boolean artistMatch = artistName != null && !artistName.isBlank()
                        && currentArtist != null && !currentArtist.isBlank()
                        && (artistName.toLowerCase().contains(currentArtist.toLowerCase())
                            || currentArtist.toLowerCase().contains(artistName.toLowerCase())
                            || (artistName.contains("&") && currentArtist.contains("&")));

                if (fallbackSongId == null) {
                    Object idObj = song.get("id");
                    if (idObj instanceof Number) {
                        fallbackSongId = ((Number) idObj).intValue();
                        fallbackArtist = currentArtist;
                    }
                }

                if (nameMatch && artistMatch) {
                    Object idObj = song.get("id");
                    if (idObj instanceof Number) {
                        songId = ((Number) idObj).intValue();
                        matchedArtist = currentArtist;
                        log.info("Meting 匹配: {} - {} id={}", songName, currentArtist, songId);
                        break;
                    }
                }
                if (nameMatch && songId == null) {
                    Object idObj = song.get("id");
                    if (idObj instanceof Number) {
                        songId = ((Number) idObj).intValue();
                        matchedArtist = currentArtist;
                    }
                }
            }

            if (songId == null) {
                songId = fallbackSongId;
                matchedArtist = fallbackArtist;
                log.info("Meting 兜底: id={} artist={}", songId, matchedArtist);
            }
            if (songId == null) return null;

            String lrc = metingClient.get()
                    .uri("/meting/?type=lrc&id=" + songId + "&server=netease")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (lrc != null && !lrc.isBlank()) {
                log.info("Meting 歌词获取成功: id={} len={}", songId, lrc.length());
                return Map.of("syncedLyrics", lrc, "plainLyrics", lrc, "instrumental", false);
            }
            return null;
        } catch (Exception e) {
            log.debug("tryMeting failed: {}", e.getMessage());
            return null;
        }
    }

    public String extractLyrics(Map<String, Object> response) {
        if (response == null) return null;
        if (Boolean.TRUE.equals(response.get("instrumental"))) return "[纯音乐]";
        String synced = (String) response.get("syncedLyrics");
        if (synced != null && !synced.isBlank()) return synced;
        String plain = (String) response.get("plainLyrics");
        if (plain != null && !plain.isBlank()) return plain;
        return null;
    }
}
