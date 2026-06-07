<template>
  <div id="app" :class="{ 'has-player': !!currentSong }">
    <div class="player-container">
      <div class="main-content">
        <!-- 左侧列表 -->
        <div class="sidebar">
          <div class="sidebar-group">
            <div class="group-title">歌手</div>
            <div class="singer-list">
              <div v-for="artist in artists" :key="artist.id"
                   class="sidebar-item"
                   :class="{ active: activeView === 'artist' && activeId === artist.id }"
                   @click="selectArtist(artist)">
                {{ artist.name }}
              </div>
            </div>
          </div>

          <div class="sidebar-group">
            <div class="group-title">我的歌单</div>
            <div class="playlist-list">
              <div v-for="pl in playlists" :key="pl.id"
                   class="sidebar-item"
                   :class="{ active: activeView === 'playlist' && activeId === pl.id }"
                   @click="selectPlaylist(pl)">
                {{ pl.name }}
              </div>
            </div>
          </div>
        </div>

        <div class="divider"></div>

        <!-- 中间：封面 + 歌词 -->
        <div class="center-wrap">
          <div class="cover-section">
            <div class="cover-box" :class="{ rotating: isPlaying }" @click="togglePlay">
              <!-- 封面图（居中正方形，透过黑胶孔显示） -->
              <div class="cover-container">
                <img :src="currentSong?.albumCover || currentSong?.artistCover || currentSong?.album_cover || currentSong?.artist_cover || DEFAULT_COVER" alt="封面" />
              </div>
              <!-- 黑胶唱片 SVG 覆盖层（整体旋转） -->
              <svg class="record-svg" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 500 500" width="340" height="340">
                <defs>
                  <mask id="centerHole">
                    <rect width="500" height="500" fill="white" />
                    <circle cx="250" cy="250" r="80" fill="black" />
                  </mask>
                  <radialGradient id="recordSurface" cx="50%" cy="50%" r="50%">
                    <stop offset="0%" stop-color="#2a2a2a" />
                    <stop offset="12%" stop-color="#1c1c1c" />
                    <stop offset="30%" stop-color="#141414" />
                    <stop offset="60%" stop-color="#0d0d0d" />
                    <stop offset="85%" stop-color="#111" />
                    <stop offset="100%" stop-color="#1a1a1a" />
                  </radialGradient>
                  <linearGradient id="highlightBeam" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="15%" stop-color="rgba(255,255,255,0)" />
                    <stop offset="28%" stop-color="rgba(255,255,255,0)" />
                    <stop offset="35%" stop-color="rgba(255,255,255,0.03)" />
                    <stop offset="40%" stop-color="rgba(255,255,255,0.06)" />
                    <stop offset="45%" stop-color="rgba(255,255,255,0.12)" />
                    <stop offset="48%" stop-color="rgba(255,255,255,0.18)" />
                    <stop offset="50%" stop-color="rgba(255,255,255,0.22)" />
                    <stop offset="52%" stop-color="rgba(255,255,255,0.18)" />
                    <stop offset="55%" stop-color="rgba(255,255,255,0.12)" />
                    <stop offset="60%" stop-color="rgba(255,255,255,0.06)" />
                    <stop offset="65%" stop-color="rgba(255,255,255,0.03)" />
                    <stop offset="72%" stop-color="rgba(255,255,255,0)" />
                    <stop offset="85%" stop-color="rgba(255,255,255,0)" />
                  </linearGradient>
                  <radialGradient id="outerGlow" cx="50%" cy="50%" r="50%">
                    <stop offset="80%" stop-color="rgba(0,0,0,0)" />
                    <stop offset="90%" stop-color="rgba(180,180,180,0.04)" />
                    <stop offset="95%" stop-color="rgba(180,180,180,0.10)" />
                    <stop offset="98%" stop-color="rgba(180,180,180,0.18)" />
                    <stop offset="100%" stop-color="rgba(180,180,180,0.28)" />
                  </radialGradient>
                  <radialGradient id="recordShadow" cx="50%" cy="50%" r="50%">
                    <stop offset="90%" stop-color="rgba(0,0,0,0)" />
                    <stop offset="96%" stop-color="rgba(0,0,0,0.15)" />
                    <stop offset="100%" stop-color="rgba(0,0,0,0.3)" />
                  </radialGradient>
                </defs>
                <circle cx="250" cy="250" r="178" fill="url(#recordShadow)" />
                <circle cx="250" cy="250" r="176" fill="url(#outerGlow)" />
                <g mask="url(#centerHole)">
                  <circle cx="250" cy="250" r="170" fill="url(#recordSurface)" />
                  <g stroke="#1a1a1a" stroke-width="0.5" fill="none" opacity="0.4">
                    <circle cx="250" cy="250" r="166" /><circle cx="250" cy="250" r="163" />
                    <circle cx="250" cy="250" r="159" /><circle cx="250" cy="250" r="155" />
                    <circle cx="250" cy="250" r="151" /><circle cx="250" cy="250" r="148" />
                    <circle cx="250" cy="250" r="144" /><circle cx="250" cy="250" r="140" />
                    <circle cx="250" cy="250" r="137" /><circle cx="250" cy="250" r="133" />
                    <circle cx="250" cy="250" r="129" /><circle cx="250" cy="250" r="126" />
                    <circle cx="250" cy="250" r="122" /><circle cx="250" cy="250" r="118" />
                    <circle cx="250" cy="250" r="114" /><circle cx="250" cy="250" r="111" />
                    <circle cx="250" cy="250" r="107" /><circle cx="250" cy="250" r="103" />
                    <circle cx="250" cy="250" r="100" /><circle cx="250" cy="250" r="96" />
                  </g>
                  <circle cx="250" cy="250" r="170" fill="url(#highlightBeam)" />
                </g>
              </svg>
            </div>
            <div class="song-info-row">
              <div class="song-text">
                <h2>{{ currentSong ? currentSong.name : '未播放' }}</h2>
                <p>{{ currentSong ? currentSong.artist_name : '选择歌曲开始播放' }}</p>
              </div>
              <button class="like-btn" :class="{ active: isFavorited }" @click="toggleFav" v-if="currentSong">
                {{ isFavorited ? '♥' : '♡' }}
              </button>
            </div>
          </div>

          <div class="lyrics-box" v-show="!lyricsHidden">
            <div class="lyrics-title">歌词</div>
            <div class="lyrics-list" ref="lyricsList">
              <p v-if="!currentSong || !currentSong.lyrics" style="color:#666;">暂无歌词</p>
              <template v-else>
                <p v-for="(line, i) in lyricsLines" :key="i"
                   :class="{ active: activeLyricIndex === i }"
                   :ref="el => { if (activeLyricIndex === i) activeLyricEl = el }">
                  {{ line }}
                </p>
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部控制条 -->
      <div class="ctrl-bar">
        <div class="left-group">
          <div class="tooltip-wrap">
            <button class="ctrl-btn" @click="showAddDialog">+♪</button>
            <div class="tooltip">添加到歌单</div>
          </div>
          <button class="ctrl-btn">⋮</button>
        </div>

        <div class="progress-area">
          <div class="progress-bar" ref="progressBar" @mousedown="onProgressDragStart" @click="onProgressClick">
            <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
          </div>
          <div class="time-text">{{ currentTimeText }} / {{ durationText }}</div>
        </div>

        <div class="play-control">
          <button class="ctrl-btn" @click="prevSong">⏮</button>
          <button class="play-btn" @click="togglePlay">{{ isPlaying ? '❚❚' : '▶' }}</button>
          <button class="ctrl-btn" @click="nextSong">⏭</button>
        </div>

        <div class="right-group">
          <div class="tooltip-wrap">
            <button class="quality-btn" @click="toggleLyrics">词</button>
            <div class="tooltip">切换歌词显示</div>
          </div>
          <div class="tooltip-wrap">
            <button class="small-btn" @click="toggleRepeat" :class="{ 'active': repeatMode }">⟳</button>
            <div class="tooltip">单曲循环</div>
          </div>
          <div class="volume-wrap" @mouseenter="volumeEnter" @mouseleave="volumeLeave">
            <button class="small-btn" @click="toggleMute">{{ isMuted ? '🔇' : '🔊' }}</button>
            <div class="volume-popup" v-show="showVolume">
              <div class="volume-slider" ref="volumeSlider" @mousedown="onVolumeDragStart" @click="onVolumeClick">
                <div class="volume-fill" :style="{ height: volumePercent + '%' }"></div>
                <div class="volume-thumb" :style="{ bottom: volumeThumbBottom }"></div>
              </div>
            </div>
          </div>
          <button class="small-btn">☰</button>
        </div>
      </div>
    </div>

    <!-- 音频 -->
    <!-- MP3 / 非 FLAC 使用原生 audio -->
    <audio ref="audio" @timeupdate="onTimeUpdate" @loadedmetadata="onLoaded" @ended="nextSong" @play="isPlaying = true" @pause="isPlaying = false"></audio>
    <!-- FLAC 使用 Web Audio API，隐藏 audio 元素用于 timeupdate 事件（可选） -->

    <!-- 添加到歌单弹窗 -->
    <div class="modal-overlay" v-if="showAddModal" @click.self="showAddModal = false">
      <div class="modal-card">
        <div class="modal-header">
          <h3>添加到歌单</h3>
          <span class="modal-header-btn" @click="showNewPlaylistInput = !showNewPlaylistInput" v-if="!showNewPlaylistInput">+ 新歌单</span>
        </div>
        <div class="modal-new-line" v-if="showNewPlaylistInput">
          <input class="new-input" v-model="newPlaylistName" placeholder="新歌单名称" @keyup.enter="createAndAdd" />
          <button class="new-btn" @click="createAndAdd">添加</button>
        </div>
        <div class="modal-list">
          <div v-for="pl in playlists" :key="pl.id" class="modal-item" @click="addToPlaylist(pl.id)">
            {{ pl.name }}
          </div>
        </div>
        <button class="modal-close" @click="showAddModal = false">关闭</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

const API_BASE = process.env.NODE_ENV === 'production' ? '/api' : '/api'
const DEFAULT_COVER = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="100" height="100"><circle cx="12" cy="12" r="10" fill="%23302b63"/><path d="M12 8.5c-1.93 0-3.5 1.57-3.5 3.5s1.57 3.5 3.5 3.5 3.5-1.57 3.5-3.5S13.93 8.5 12 8.5z" fill="%237a9aff"/></svg>'

export default {
  name: 'App',
  data() {
    return {
      artists: [],
      playlists: [],
      songs: [],
      activeView: 'home',
      activeId: null,
      currentSong: null,
      isPlaying: false,
      isFavorited: false,
      currentTime: 0,
      duration: 0,
      isMuted: false,
      volume: 0.7,
      volumeBeforeMute: 0.7,
      showVolume: false,
      volumeHideTimer: null,
      activeLyricIndex: -1,
      activeLyricEl: null,
      showAddModal: false,
      showNewPlaylistInput: false,
      newPlaylistName: '',
      selectedSongId: null,
      progressDragging: false,
      volumeDragging: false,
      repeatMode: false,
      DEFAULT_COVER,
      // FLAC 解码相关
      audioContext: null,
      flacSource: null,
      flacStartTime: 0,
      flacDuration: 0,
      isFlac: false,
      flacTimerId: null,
      flacPauseOffset: 0
    }
  },
  computed: {
    /**
     * 解析歌词文本，返回 [{ time: 秒数, text: 歌词文本 }] 数组
     * 支持 LRC 格式：[mm:ss.xx]歌词（来自 LRCLIB 的 syncedLyrics）
     * 也支持纯文本歌词（无时间戳）
     */
    parsedLyrics() {
      if (!this.currentSong || !this.currentSong.lyrics) return []
      const raw = this.currentSong.lyrics
      const lines = raw.split('\n').filter(l => l.trim())
      const result = []

      // 检查是否为 LRC 格式（包含 [mm:ss 模式）
      const lrcRegex = /\[(\d{2}):(\d{2})(?:\.(\d{2,3}))?\]/g
      const hasLrc = lines.some(l => lrcRegex.test(l))

      if (hasLrc) {
        // LRC 格式：解析时间戳
        for (const line of lines) {
          // 重置正则（全局匹配需要重置 lastIndex）
          lrcRegex.lastIndex = 0
          let match
          let time = null
          let text = line
          while ((match = lrcRegex.exec(line)) !== null) {
            const min = parseInt(match[1])
            const sec = parseInt(match[2])
            const ms = match[3] ? (match[3].length === 2 ? parseInt(match[3]) * 10 : parseInt(match[3])) : 0
            time = min * 60 + sec + ms / 1000
            // 移除时间戳部分
            text = text.replace(match[0], '').trim()
          }
          if (time !== null && text) {
            result.push({ time, text })
          }
        }
        // 按时间排序
        result.sort((a, b) => a.time - b.time)
      } else {
        // 纯文本歌词：显示所有行，但不支持时间同步
        for (const line of lines) {
          if (line) result.push({ time: -1, text: line })
        }
      }
      return result
    },
    /**
     * 纯文本行数组（用于 v-for 渲染）
     */
    lyricsLines() {
      return this.parsedLyrics.map(l => l.text)
    },
    progressPercent() {
      if (this.duration <= 0) return 0
      return (this.currentTime / this.duration) * 100
    },
    currentTimeText() {
      const m = Math.floor(this.currentTime / 60)
      const s = Math.floor(this.currentTime % 60)
      return `${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`
    },
    durationText() {
      if (this.duration <= 0) return '00:00'
      const m = Math.floor(this.duration / 60)
      const s = Math.floor(this.duration % 60)
      return `${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`
    },
    volumePercent() {
      return Math.round((this.isMuted ? 0 : this.volume) * 100)
    },
    volumeThumbBottom() {
      // 减去半个球高度(7px)使球心对齐点击位置
      return `calc(${this.isMuted ? 0 : Math.round(this.volume * 100)}% - 7px)`
    }
  },
  async created() {
    await this.loadData()
    // 初始化 AudioContext（用户交互时再 resume）
    this.audioContext = new (window.AudioContext || window.webkitAudioContext)()
    // 初始化音量
    this.$nextTick(() => {
      const audio = this.$refs.audio
      if (audio) audio.volume = this.volume
    })
    // 自动选中第一首歌
    if (this.songs.length > 0) {
      this.playSong(this.songs[0])
    }
  },
  methods: {
    getDefaultCover() { return DEFAULT_COVER },

    async loadData() {
      try {
        const [songsRes, artistsRes, playlistsRes] = await Promise.all([
          axios.get(`${API_BASE}/songs`),
          axios.get(`${API_BASE}/artists`),
          axios.get(`${API_BASE}/playlists?user_id=1`)
        ])
        this.songs = songsRes.data?.data || []
        this.artists = artistsRes.data?.data || []
        this.playlists = playlistsRes.data?.data || []
      } catch(e) {
        console.error('Load data error:', e)
      }
    },

    async selectArtist(artist) {
      this.activeView = 'artist'
      this.activeId = artist.id
      try {
        const res = await axios.get(`${API_BASE}/songs/by-artist/${artist.id}`)
        this.songs = res.data?.data || []
        if (this.songs.length > 0) this.playSong(this.songs[0])
      } catch(e) {}
    },

    async selectPlaylist(pl) {
      this.activeView = 'playlist'
      this.activeId = pl.id
      try {
        const res = await axios.get(`${API_BASE}/playlists/${pl.id}`)
        this.songs = res.data?.data?.songs || []
        if (this.songs.length > 0) this.playSong(this.songs[0])
      } catch(e) {}
    },

    /**
     * 判断 URL 是否为 FLAC 文件
     */
    isFlacUrl(url) {
      if (!url) return false
      const lower = url.toLowerCase()
      return lower.endsWith('.flac') || lower.includes('.flac?')
    },

    /**
     * 停止 FLAC 播放并清理资源
     */
    stopFlacPlayback() {
      if (this.flacTimerId) {
        clearInterval(this.flacTimerId)
        this.flacTimerId = null
      }
      if (this.flacSource) {
        try { this.flacSource.stop() } catch(e) {}
        this.flacSource = null
      }
      this.flacDuration = 0
      this.flacStartTime = 0
      this.flacPauseOffset = 0
      this.isFlac = false
    },

    /**
     * 通过 Web Audio API 解码并播放 FLAC
     */
    async playFlacAudio(url) {
      this.stopFlacPlayback()
      this.isFlac = true

      try {
        // 确保 AudioContext 处于 running 状态
        if (this.audioContext.state === 'suspended') {
          await this.audioContext.resume()
        }

        // 使用前端代理获取音频文件（绕过 CORS 问题）
        const proxyUrl = `/api/proxy/raw?url=${encodeURIComponent(url)}`
        const response = await fetch(proxyUrl)
        const arrayBuffer = await response.arrayBuffer()

        // 解码 FLAC
        const audioBuffer = await this.audioContext.decodeAudioData(arrayBuffer)
        this.flacDuration = audioBuffer.duration
        this.duration = audioBuffer.duration

        // 创建 BufferSource 并连接
        const source = this.audioContext.createBufferSource()
        source.buffer = audioBuffer

        const gainNode = this.audioContext.createGain()
        gainNode.gain.value = this.isMuted ? 0 : this.volume
        this._flacGainNode = gainNode

        source.connect(gainNode)
        gainNode.connect(this.audioContext.destination)

        this.flacSource = source
        this.flacStartTime = this.audioContext.currentTime
        this.flacPauseOffset = 0

        source.start(0)
        this.isPlaying = true

        // 启动 timeupdate 定时器（替代 audio 元素的 timeupdate 事件）
        if (this.flacTimerId) clearInterval(this.flacTimerId)
        this.flacTimerId = setInterval(() => {
          if (!this.isPlaying) return
          const elapsed = this.audioContext.currentTime - this.flacStartTime + this.flacPauseOffset
          this.currentTime = Math.min(elapsed, this.flacDuration)
          this.syncLyrics()

          // 歌曲结束
          if (this.currentTime >= this.flacDuration - 0.05) {
            clearInterval(this.flacTimerId)
            this.flacTimerId = null
            this.isPlaying = false
            if (this.repeatMode) {
              // 单曲循环：重新播放当前曲目
              const song = this.currentSong
              this.currentSong = null
              this.playSong(song)
            } else {
              this.nextSong()
            }
          }
        }, 250)

        // 播放结束后清理
        source.onended = () => {
          // onended 在 stop() 和自然结束都会触发，判断是否自然结束
        }

      } catch (e) {
        console.error('FLAC 解码失败:', e)
        this.isFlac = false
        this.isPlaying = false
        // fallback: 尝试用 native audio 播放
        this.playMp3Audio(url)
      }
    },

    /**
     * 使用原生 audio 元素播放 MP3
     */
    playMp3Audio(url) {
      const audio = this.$refs.audio
      if (!audio) return
      audio.src = url
      audio.load()
      audio.play().then(() => {
        this.isPlaying = true
      }).catch((e) => {
        console.error('MP3 播放失败:', e)
      })
    },

    playSong(song) {
      // 停止当前播放
      this.stopFlacPlayback()
      const audio = this.$refs.audio
      if (audio) {
        audio.pause()
        audio.src = ''
      }

      this.currentSong = song
      this.isPlaying = false
      this.currentTime = 0
      this.duration = 0
      this.activeLyricIndex = -1
      this.checkFav()

      this.$nextTick(() => {
        if (this.isFlacUrl(song.url)) {
          this.playFlacAudio(song.url)
        } else {
          this.playMp3Audio(song.url)
        }
      })
    },

    prevSong() {
      this.stopFlacPlayback()
      const idx = this.songs.findIndex(s => s.id === this.currentSong?.id)
      if (idx > 0) this.playSong(this.songs[idx - 1])
      else if (this.songs.length > 0) this.playSong(this.songs[this.songs.length - 1])
    },

    nextSong() {
      this.stopFlacPlayback()
      const idx = this.songs.findIndex(s => s.id === this.currentSong?.id)
      if (idx < this.songs.length - 1) this.playSong(this.songs[idx + 1])
      else if (this.songs.length > 0) this.playSong(this.songs[0])
    },

    togglePlay() {
      if (!this.currentSong) return

      if (this.isFlac) {
        if (this.isPlaying) {
          // 暂停 FLAC
          if (this.flacSource) {
            try {
              this.flacSource.stop()
            } catch(e) {}
          }
          if (this.flacTimerId) {
            clearInterval(this.flacTimerId)
            this.flacTimerId = null
          }
          this.flacPauseOffset = this.currentTime
          this.isPlaying = false
        } else {
          // 恢复 FLAC 播放（从上次暂停位置）
          this.resumeFlacPlayback()
        }
      } else {
        const audio = this.$refs.audio
        if (!audio) return
        if (this.isPlaying) {
          audio.pause()
        } else {
          audio.play().catch(() => {})
        }
      }
    },

    /**
     * 从上一次暂停位置恢复 FLAC 播放
     */
    resumeFlacPlayback() {
      if (!this.currentSong) return

      // FLAC 不支持从中间 seek 后继续 — 需要重新解码
      // 采用简单方案：重新从 URL fetch 解码，seek 到暂停位置
      const seekTo = this.flacPauseOffset

      this.stopFlacPlayback()
      this.isFlac = true
      this.flacPauseOffset = 0

      const url = this.currentSong.url
      if (!url) return

      const proxyUrl = `/api/proxy/raw?url=${encodeURIComponent(url)}`
      fetch(proxyUrl)
        .then(r => r.arrayBuffer())
        .then(buf => this.audioContext.decodeAudioData(buf))
        .then(audioBuffer => {
          this.flacDuration = audioBuffer.duration
          this.duration = audioBuffer.duration

          const source = this.audioContext.createBufferSource()
          source.buffer = audioBuffer

          const gainNode = this.audioContext.createGain()
          gainNode.gain.value = this.isMuted ? 0 : this.volume
          this._flacGainNode = gainNode

          source.connect(gainNode)
          gainNode.connect(this.audioContext.destination)

          this.flacSource = source

          // seek 到指定位置
          const startOffset = Math.min(seekTo, audioBuffer.duration - 0.1)
          source.start(0, startOffset)
          this.flacStartTime = this.audioContext.currentTime
          this.flacPauseOffset = startOffset
          this.isPlaying = true

          if (this.flacTimerId) clearInterval(this.flacTimerId)
          this.flacTimerId = setInterval(() => {
            if (!this.isPlaying) return
            const elapsed = this.flacPauseOffset + (this.audioContext.currentTime - this.flacStartTime)
            this.currentTime = Math.min(elapsed, this.flacDuration)
            this.syncLyrics()
            if (this.currentTime >= this.flacDuration - 0.05) {
              clearInterval(this.flacTimerId)
              this.flacTimerId = null
              this.isPlaying = false
              if (this.repeatMode) {
                const song = this.currentSong
                this.currentSong = null
                this.playSong(song)
              } else {
                this.nextSong()
              }
            }
          }, 250)
        })
        .catch(e => {
          console.error('FLAC 恢复播放失败:', e)
          this.isFlac = false
          this.isPlaying = false
        })
    },

    onTimeUpdate(e) {
      if (this.progressDragging) return
      // FLAC 模式使用自己的定时器，不从这里获取时间
      if (this.isFlac) return
      this.currentTime = e.target.currentTime
      this.syncLyrics()
    },

    onLoaded(e) {
      this.duration = e.target.duration || 0
    },

    /**
     * 根据当前播放时间同步歌词高亮行
     * 使用 parsedLyrics 中的精确时间戳匹配
     */
    syncLyrics() {
      if (!this.parsedLyrics.length) return
      const lyrics = this.parsedLyrics
      const time = this.currentTime

      // 从后往前找，找到第一个时间 <= 当前时间的行
      let idx = -1
      for (let i = lyrics.length - 1; i >= 0; i--) {
        if (lyrics[i].time >= 0 && lyrics[i].time <= time) {
          idx = i
          break
        }
      }

      if (idx !== this.activeLyricIndex) {
        this.activeLyricIndex = idx
        this.$nextTick(() => {
          if (this.$refs.lyricsList) {
            const el = this.$refs.lyricsList.querySelector('.active')
            if (el) el.scrollIntoView({ behavior: 'smooth', block: 'center' })
          }
        })
      }
    },

    async checkFav() {
      if (!this.currentSong) return
      try {
        const res = await axios.get(`${API_BASE}/favorites/check/${this.currentSong.id}?user_id=1`)
        this.isFavorited = res.data.data.favorited
      } catch(e) {}
    },

    async toggleFav() {
      if (!this.currentSong) return
      const res = await axios.post(`${API_BASE}/favorites/toggle`, { user_id: 1, song_id: this.currentSong.id })
      this.isFavorited = res.data.data.favorited
    },

    async createAndAdd() {
      const name = this.newPlaylistName.trim()
      if (!name) return
      try {
        // 1. 创建新歌单
        const createRes = await axios.post(`${API_BASE}/playlists`, { name, user_id: 1 })
        const newId = createRes.data?.data?.id
        // 2. 将当前歌曲加入该歌单
        if (newId && this.selectedSongId) {
          await axios.post(`${API_BASE}/playlists/add-song`, { playlist_id: newId, song_id: this.selectedSongId })
        }
        // 3. 刷新歌单列表
        const listRes = await axios.get(`${API_BASE}/playlists?user_id=1`)
        this.playlists = listRes.data?.data || []
        this.newPlaylistName = ''
      } catch (e) {
        console.error('创建歌单失败:', e)
      }
    },

    showAddDialog() {
      if (this.currentSong) {
        this.selectedSongId = this.currentSong.id
        this.showAddModal = true
      }
    },

    showPlaylistDialog() {
      // 简单功能：显示当前播放列表
      alert('播放列表：' + this.songs.map(s => s.name).join('\n'))
    },

    async addToPlaylist(playlistId) {
      await axios.post(`${API_BASE}/playlists/add-song`, { playlist_id: playlistId, song_id: this.selectedSongId })
      this.showAddModal = false
    },

    onProgressDragStart(e) {
      e.preventDefault()
      this.progressDragging = true
      this.setProgress(e)
      const onMove = (ev) => { ev.preventDefault(); this.setProgress(ev) }
      const onUp = () => {
        this.progressDragging = false
        window.removeEventListener('mousemove', onMove)
        window.removeEventListener('mouseup', onUp)
      }
      window.addEventListener('mousemove', onMove)
      window.addEventListener('mouseup', onUp)
    },

    onProgressClick(e) {
      if (!this.progressDragging) this.setProgress(e)
    },

    setProgress(e) {
      const rect = this.$refs.progressBar.getBoundingClientRect()
      const x = e.clientX - rect.left
      const pct = Math.min(1, Math.max(0, x / rect.width))

      if (this.isFlac) {
        // FLAC seek: 保存目标时间，下次 resume 时从该位置播放
        const targetTime = pct * this.flacDuration
        this.flacPauseOffset = targetTime
        this.currentTime = targetTime

        if (this.isPlaying) {
          // 正在播放时 seek：停止当前，从新位置恢复
          if (this.flacTimerId) {
            clearInterval(this.flacTimerId)
            this.flacTimerId = null
          }
          if (this.flacSource) {
            try { this.flacSource.stop() } catch(e) {}
          }
          // 从新位置恢复播放
          this.isPlaying = false
          this.resumeFlacPlayback()
        }
      } else {
        const audio = this.$refs.audio
        if (audio && this.duration > 0) {
          audio.currentTime = pct * this.duration
          this.currentTime = audio.currentTime
        }
      }
    },

    volumeEnter() {
      if (this.volumeHideTimer) {
        clearTimeout(this.volumeHideTimer)
        this.volumeHideTimer = null
      }
      this.showVolume = true
    },

    volumeLeave() {
      this.volumeHideTimer = setTimeout(() => {
        this.showVolume = false
      }, 300)
    },

    toggleMute() {
      if (this.isFlac) {
        // FLAC 用 gain node 控制音量
        if (this.isMuted) {
          this.volume = this.volumeBeforeMute
          this.isMuted = false
        } else {
          this.volumeBeforeMute = this.volume
          this.volume = 0
          this.isMuted = true
        }
        if (this._flacGainNode) {
          this._flacGainNode.gain.value = this.isMuted ? 0 : this.volume
        }
      } else {
        const audio = this.$refs.audio
        if (audio) {
          if (this.isMuted) {
            this.volume = this.volumeBeforeMute
            audio.muted = false
            audio.volume = this.volume
            this.isMuted = false
          } else {
            this.volumeBeforeMute = this.volume
            this.volume = 0
            audio.muted = true
            audio.volume = 0
            this.isMuted = true
          }
        }
      }
    },

    setVolume(v) {
      this.volume = Math.max(0, Math.min(1, v))

      if (this.isFlac) {
        if (this._flacGainNode) {
          this._flacGainNode.gain.value = this.volume
        }
        if (this.volume > 0 && this.isMuted) {
          this.isMuted = false
        } else if (this.volume <= 0) {
          this.isMuted = true
        }
      } else {
        const audio = this.$refs.audio
        if (audio) {
          audio.volume = this.volume
          if (this.volume > 0 && this.isMuted) {
            this.isMuted = false
            audio.muted = false
          } else if (this.volume <= 0) {
            this.isMuted = true
            audio.muted = true
          }
        }
      }
    },

    onVolumeDragStart(e) {
      e.preventDefault()
      this.volumeDragging = true
      this.setVolumeFromEvent(e)
      const onMove = (ev) => { ev.preventDefault(); this.setVolumeFromEvent(ev) }
      const onUp = () => {
        this.volumeDragging = false
        window.removeEventListener('mousemove', onMove)
        window.removeEventListener('mouseup', onUp)
      }
      window.addEventListener('mousemove', onMove)
      window.addEventListener('mouseup', onUp)
    },

    onVolumeClick(e) {
      if (!this.volumeDragging) this.setVolumeFromEvent(e)
    },

    setVolumeFromEvent(e) {
      const slider = this.$refs.volumeSlider
      if (!slider) return
      const rect = slider.getBoundingClientRect()
      const y = e.clientY - rect.top
      const pct = Math.min(1, Math.max(0, 1 - y / rect.height))
      this.setVolume(pct)
    },

    toggleRepeat() {
      this.repeatMode = !this.repeatMode
      if (this.$refs.audio && !this.isFlac) {
        this.$refs.audio.loop = this.repeatMode
      }
      // FLAC 循环在 nextSong 中判断 repeatMode
    },

    toggleLyrics() {
      this.lyricsHidden = !this.lyricsHidden
    }
  }
}
</script>

<style>
:root{
 --bg-gradient: linear-gradient(135deg,#2c1b54 0%,#1a1033 100%);
 --glass-main: rgba(255,255,255,0.06);
 --glass-item: rgba(255,255,255,0.08);
 --glass-border: rgba(255,255,255,0.15);
 --text-primary: #e8e8ff;
 --text-secondary: #b0a9d6;
 --accent: #a78bfa;
 --divider: rgba(255,255,255,0.2);
 --sidebar-width: 220px;
 --ctrl-height: 90px;
}
*{
 margin:0;
 padding:0;
 box-sizing:border-box;
 user-select: none;
 -webkit-tap-highlight-color: transparent;
}
html, body {
 width: 100%;
 height: 100%;
 overflow: hidden;
 position: fixed;
}
body{
 font-family:"Segoe UI",sans-serif;
 background: var(--bg-gradient);
 height: 100vh;
 width: 100vw;
 display: flex;
 align-items: center;
 justify-content: center;
 padding: 20px;
 overflow: hidden;
 position: fixed;
 top:0;
 left:0;
 right:0;
 bottom:0;
}
#app {
 width: 100%;
 height: 100%;
 display: flex;
 align-items: center;
 justify-content: center;
}

.player-container{
 width: 100%;
 height: 100%;
 max-width: 1400px;
 max-height: 800px;
 background: var(--glass-main);
 backdrop-filter: blur(16px);
 -webkit-backdrop-filter: blur(16px);
 border: 1px solid var(--glass-border);
 border-radius: 18px;
 box-shadow: 0 8px 40px rgba(0,0,0,0.4);
 display: flex;
 flex-direction: column;
 overflow: hidden;
 position: relative;
}

.main-content{
 flex: 1;
 display: flex;
 padding: 21px;
 gap: 16px;
 overflow: hidden;
 position: relative;
 min-height: 0;
}

.sidebar{
 width: var(--sidebar-width);
 flex-shrink: 0;
 display: flex;
 flex-direction: column;
 height: 100%;
 overflow: hidden;
}

.sidebar-group{
 display: flex;
 flex-direction: column;
 gap: 6px;
 margin-bottom: 20px;
 overflow: hidden;
}
.group-title{
 font-size: 14px;
 color: var(--accent);
 margin-bottom: 8px;
 padding-left: 4px;
 flex-shrink:0;
}

.singer-list{
 max-height: 175px;
 overflow-y: auto;
 overflow-x: hidden;
 display: flex;
 flex-direction: column;
 gap: 4px;
}

.playlist-list{
 flex: 1;
 overflow-y: auto;
 overflow-x: hidden;
 display: flex;
 flex-direction: column;
 gap: 4px;
 min-height: 0;
}

::-webkit-scrollbar{width:4px;height:0;}
::-webkit-scrollbar-thumb{background:rgba(255,255,255,0.2);border-radius:2px;}

.sidebar-item{
 padding: 8px 10px;
 border-radius: 8px;
 color: var(--text-secondary);
 cursor: pointer;
 transition: 0.2s;
 font-size: 14px;
 white-space: nowrap;
}
.sidebar-item:hover,.sidebar-item.active{
 background: var(--glass-item);
 color:#fff;
}

.divider{
 width:1px;
 background:var(--divider);
 align-self:stretch;
 margin:10px 0;
}

.center-wrap{
 flex:1;
 display:flex;
 align-items:center;
 justify-content:flex-start;
 padding-left: 90px;
 gap:90px;
 overflow:hidden;
 position: relative;
 min-width: 0;
}

.cover-section{
 display:flex;
 flex-direction:column;
 align-items:flex-start;
 gap:16px;
 flex-shrink: 0;
}
.cover-box{
 width:340px;
 height:340px;
 border-radius:20px;
 position:relative;
 border:2px solid var(--glass-border);
 background: var(--bg-gradient);
 cursor:pointer;
 /* 整个唱片整体旋转，封面不转，SVG 转 */
}
.cover-box .cover-container{
 position:absolute;
 left:50%;
 top:50%;
 width:190px;
 height:190px;
 transform:translate(-50%,-50%);
 overflow:hidden;
 border-radius:50%;
 background:#7a9aff;
 pointer-events:none;
 z-index:1;
}
.cover-box .cover-container img{
 width:100%;
 height:100%;
 object-fit:cover;
 display:block;
}
.cover-box .record-svg{
 position:absolute;
 left:0;
 top:0;
 width:100%;
 height:100%;
 display:block;
 z-index:2;
 pointer-events:none;
 transform-origin:50% 50%;
}
.cover-box.rotating .record-svg{
 animation: spin 4s linear infinite;
}
@keyframes spin{
 from{ transform:rotate(0deg); }
 to{ transform:rotate(360deg); }
}

.song-info-row{
 width: 100%;
 display: flex;
 justify-content: space-between;
 align-items: center;
}
.song-text{
 display: flex;
 flex-direction: column;
 gap: 4px;
}
.song-text h2{
 font-size:22px;
 color:var(--text-primary);
}
.song-text p{
 font-size:15px;
 color:var(--text-secondary);
}

.like-btn{
 background:transparent;
 border:none;
 color:var(--text-secondary);
 font-size:36px;
 width: 36px;
 height: 36px;
 display: inline-flex;
 align-items: center;
 justify-content: center;
 cursor: pointer;
 transition: color 0.2s, transform 0.05s ease;
 line-height: 1;
 padding: 0;
 outline: none;
}
.like-btn.active{
 color:#ff6b9d;
 transform: scale(1);
}
.like-btn:active {
 transform: scale(0.98);
}
.like-btn, .like-btn.active {
 font-weight: normal;
 font-family: "Segoe UI", "Apple Color Emoji", "Segoe UI Emoji", sans-serif;
}

.lyrics-box{
 flex:1;
 max-width:450px;
 height:100%;
 display:flex;
 flex-direction:column;
 justify-content:center;
 overflow: hidden;
 min-width: 0;
}
.lyrics-title{
 font-size:16px;
 color:var(--text-secondary);
 margin-bottom:16px;
 flex-shrink: 0;
}
.lyrics-list{
 overflow-y: auto;
 overflow-x: hidden;
 scroll-behavior: smooth;
 max-height: 380px;
 padding-right: 8px;
}
.lyrics-list p{
 padding:10px 0;
 color:var(--text-secondary);
 transition:0.3s;
 font-size:15px;
 white-space: nowrap;
 cursor: default;
}
.lyrics-list p.active{
 color:#fff;
 font-weight:500;
 font-size:16px;
 margin-left: -4px;
 padding-left: 4px;
 text-shadow: 0 0 8px rgba(255,255,255,0.5);
}

.ctrl-bar{
 height:var(--ctrl-height);
 flex-shrink:0;
 border-top:1px solid var(--glass-border);
 display:flex;
 align-items:center;
 padding:0 41px;
 gap:12px;
 background: rgba(0,0,0,0.1);
}
.left-group{
 display:flex;
 gap:16px;
 align-items:center;
}
.ctrl-btn{
 background:transparent;
 border:none;
 color:var(--text-secondary);
 font-size:20px;
 cursor:pointer;
 transition:0.2s;
 width: 32px;
 height: 32px;
 display: inline-flex;
 align-items: center;
 justify-content: center;
}
.ctrl-btn:hover{
 color:var(--text-primary);
}

.progress-area{
 flex:1;
 display:flex;
 flex-direction:column;
 gap:6px;
 align-items:center;
 min-width: 0;
}
.progress-bar{
 width:100%;
 height:6px;
 border-radius:3px;
 background:rgba(255,255,255,0.1);
 position:relative;
 cursor:pointer;
}
.progress-fill{
 position:absolute;
 left:0;
 top:0;
 height:100%;
 background:linear-gradient(90deg,#a78bfa,#60a5fa);
 border-radius:3px;
 pointer-events: none;
}
.time-text{
 font-size:13px;
 color:var(--text-secondary);
 user-select: none;
}

.play-control{
 display:flex;
 gap:16px;
 align-items:center;
 margin:0 16px;
}
.play-btn{
 background:rgba(255,255,255,0.1);
 border:none;
 color:var(--text-primary);
 font-size:25px;
 width: 54px;
 height: 54px;
 border-radius:50%;
 display:flex;
 align-items:center;
 justify-content:center;
 cursor:pointer;
 transition:0.2s;
}
.play-btn:hover{
 background:rgba(255,255,255,0.2);
 transform: scale(0.98);
}

.right-group{
 display:flex;
 gap:16px;
 align-items:center;
}

/* Tooltip */
.tooltip-wrap{
 position:relative;
 display:flex;
}
.tooltip-wrap .tooltip{
 position:absolute;
 bottom:calc(100% + 10px);
 left:50%;
 transform:translateX(-50%);
 background:rgba(230,230,235,0.95);
 color:#222;
 font-size:12px;
 padding:6px 12px;
 border-radius:6px;
 white-space:nowrap;
 pointer-events:none;
 opacity:0;
 transition:opacity 0.18s;
 z-index:100;
}
.tooltip-wrap .tooltip::after{
 content:'';
 position:absolute;
 top:100%;
 left:50%;
 margin-left:-4px;
 border:4px solid transparent;
 border-top-color:rgba(230,230,235,0.95);
}
.tooltip-wrap:hover .tooltip{
 opacity:1;
}

/* Volume popup */
.volume-wrap{
 position:relative;
 display:flex;
}
.volume-popup{
 position:absolute;
 bottom:calc(100% + 10px);
 left:50%;
 transform:translateX(-50%);
 background:rgba(30,30,50,0.95);
 backdrop-filter:blur(12px);
 -webkit-backdrop-filter:blur(12px);
 border:1px solid rgba(255,255,255,0.12);
 border-radius:10px;
 padding:14px 10px;
 height:140px;
 display:flex;
 align-items:center;
 justify-content:center;
 z-index:100;
}
.volume-slider{
 position:relative;
 width:6px;
 height:100%;
 background:rgba(255,255,255,0.15);
 border-radius:3px;
 cursor:pointer;
}
.volume-fill{
 position:absolute;
 bottom:0;
 left:0;
 width:100%;
 background:linear-gradient(0deg,#a78bfa,#60a5fa);
 border-radius:3px;
 pointer-events:none;
}
.volume-thumb{
 position:absolute;
 left:50%;
 transform:translateX(-50%);
 width:14px;
 height:14px;
 border-radius:50%;
 background:#a78bfa;
 border:2px solid #fff;
 box-shadow:0 1px 4px rgba(0,0,0,0.3);
 pointer-events:none;
}

.small-btn.active{
 color:#a78bfa;
}
.small-btn{
 background:transparent;
 border:none;
 color:var(--text-secondary);
 font-size:18px;
 cursor:pointer;
 transition:0.2s;
 width: 32px;
 height: 32px;
 display: inline-flex;
 align-items: center;
 justify-content: center;
}
.small-btn:hover{
 color:var(--text-primary);
}
.quality-btn{
 background:rgba(255,255,255,0.1);
 color:var(--text-secondary);
 border:none;
 padding:6px 12px;
 border-radius:8px;
 font-size:14px;
 cursor:pointer;
 transition:0.2s;
}
.quality-btn:hover{
 background:rgba(255,255,255,0.2);
 color:#fff;
}

/* Modal */
.modal-overlay {
 position: fixed;
 inset: 0;
 background: rgba(0,0,0,0.6);
 display: flex;
 align-items: center;
 justify-content: center;
 z-index: 2000;
}
.modal-card {
 background: rgba(36,36,62,0.95);
 backdrop-filter: blur(20px);
 border: 1px solid rgba(255,255,255,0.1);
 border-radius: 16px;
 padding: 24px;
 min-width: 300px;
 max-height: 60vh;
 overflow-y: auto;
}
.modal-card h3 {
 color: #fff;
 margin: 0;
 font-size: 16px;
}
.modal-header {
 display: flex;
 align-items: center;
 justify-content: space-between;
 margin-bottom: 16px;
}
.modal-header-btn {
 color: #888;
 font-size: 13px;
 cursor: pointer;
 transition: 0.2s;
 padding: 2px 8px;
 border-radius: 4px;
}
.modal-header-btn:hover { color: #aaa; background: rgba(255,255,255,0.05); }
.modal-item {
 padding: 10px 12px;
 border-radius: 8px;
 color: var(--text-secondary);
 cursor: pointer;
 transition: 0.2s;
}
.modal-item:hover { background: var(--glass-item); color: #fff; }
.modal-close {
 margin-top: 16px;
 width: 100%;
 padding: 8px;
 background: rgba(255,255,255,0.06);
 border: 1px solid rgba(255,255,255,0.1);
 border-radius: 8px;
 color: #aaa;
 cursor: pointer;
}
.modal-close:hover { background: rgba(122,154,255,0.15); color: #7a9aff; }

.modal-new-line {
 display: flex;
 gap: 8px;
 margin-bottom: 12px;
}
.new-input {
 flex: 1;
 padding: 8px 12px;
 background: rgba(255,255,255,0.06);
 border: 1px solid rgba(255,255,255,0.12);
 border-radius: 8px;
 color: #ddd;
 font-size: 13px;
 outline: none;
 transition: 0.2s;
}
.new-input::placeholder { color: #666; }
.new-input:focus { border-color: rgba(122,154,255,0.5); }
.new-btn {
 padding: 8px 16px;
 background: rgba(122,154,255,0.15);
 border: 1px solid rgba(122,154,255,0.25);
 border-radius: 8px;
 color: #7a9aff;
 font-size: 13px;
 cursor: pointer;
 transition: 0.2s;
 white-space: nowrap;
}
.new-btn:hover { background: rgba(122,154,255,0.25); }
</style>
