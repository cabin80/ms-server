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
            <div class="cover-box">
              <img :src="currentSong?.album_cover || currentSong?.artist_cover || DEFAULT_COVER" alt="封面">
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

          <div class="lyrics-box">
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
          <button class="ctrl-btn" @click="showAddDialog" title="添加到歌单">♪</button>
          <button class="ctrl-btn" @click="showPlaylistDialog" title="播放列表">⋮</button>
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
          <button class="quality-btn" @click="toggleLyrics">词</button>
          <button class="small-btn" @click="toggleRepeat">⟳</button>
          <button class="small-btn" @click="toggleMute">{{ isMuted ? '🔇' : '🔊' }}</button>
          <button class="small-btn">☰</button>
        </div>
      </div>
    </div>

    <!-- 音频 -->
    <audio ref="audio" @timeupdate="onTimeUpdate" @loadedmetadata="onLoaded" @ended="nextSong" @play="isPlaying = true" @pause="isPlaying = false"></audio>

    <!-- 添加到歌单弹窗 -->
    <div class="modal-overlay" v-if="showAddModal" @click.self="showAddModal = false">
      <div class="modal-card">
        <h3>添加到歌单</h3>
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
      activeLyricIndex: -1,
      activeLyricEl: null,
      showAddModal: false,
      selectedSongId: null,
      progressDragging: false,
      repeatMode: false,
      DEFAULT_COVER
    }
  },
  computed: {
    lyricsLines() {
      if (!this.currentSong || !this.currentSong.lyrics) return []
      return this.currentSong.lyrics.split('\n').filter(l => l.trim())
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
    }
  },
  async created() {
    await this.loadData()
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

    playSong(song) {
      this.currentSong = song
      this.isPlaying = false
      this.currentTime = 0
      this.activeLyricIndex = -1
      this.checkFav()
      this.$nextTick(() => {
        const audio = this.$refs.audio
        if (audio) {
          audio.src = song.url
          audio.load()
          audio.play().then(() => {
            this.isPlaying = true
          }).catch(() => {})
        }
      })
    },

    prevSong() {
      const idx = this.songs.findIndex(s => s.id === this.currentSong?.id)
      if (idx > 0) this.playSong(this.songs[idx - 1])
      else if (this.songs.length > 0) this.playSong(this.songs[this.songs.length - 1])
    },

    nextSong() {
      const idx = this.songs.findIndex(s => s.id === this.currentSong?.id)
      if (idx < this.songs.length - 1) this.playSong(this.songs[idx + 1])
      else if (this.songs.length > 0) this.playSong(this.songs[0])
    },

    togglePlay() {
      const audio = this.$refs.audio
      if (!audio || !this.currentSong) return
      if (this.isPlaying) {
        audio.pause()
      } else {
        audio.play().catch(() => {})
      }
    },

    onTimeUpdate(e) {
      if (this.progressDragging) return
      this.currentTime = e.target.currentTime
      this.syncLyrics()
    },

    onLoaded(e) {
      this.duration = e.target.duration || 0
    },

    syncLyrics() {
      if (!this.lyricsLines.length) return
      // 简单的逐行标记：按行平均分配时间
      const totalLines = this.lyricsLines.length
      const timePerLine = this.duration / totalLines
      const idx = Math.min(Math.floor(this.currentTime / timePerLine), totalLines - 1)
      if (idx !== this.activeLyricIndex) {
        this.activeLyricIndex = idx
        this.$nextTick(() => {
          if (this.activeLyricEl && this.$refs.lyricsList) {
            // 用原生滚动
            const list = this.$refs.lyricsList
            const el = this.$refs.lyricsList.querySelector('.active')
            if (el) el.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
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
      const audio = this.$refs.audio
      if (audio && this.duration > 0) {
        audio.currentTime = pct * this.duration
        this.currentTime = audio.currentTime
      }
    },

    toggleMute() {
      const audio = this.$refs.audio
      if (audio) {
        audio.muted = !audio.muted
        this.isMuted = audio.muted
      }
    },

    toggleRepeat() {
      this.repeatMode = !this.repeatMode
      if (this.$refs.audio) {
        this.$refs.audio.loop = this.repeatMode
      }
    },

    toggleLyrics() {
      const lyricsBox = this.$refs.lyricsList?.closest('.lyrics-box')
      if (lyricsBox) {
        lyricsBox.style.display = lyricsBox.style.display === 'none' ? '' : 'none'
      }
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
 width:320px;
 height:320px;
 border-radius:20px;
 overflow:hidden;
 border:2px solid var(--glass-border);
}
.cover-box img{
 width:100%;
 height:100%;
 object-fit:cover;
 display: block;
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
 transform: scale(1.02);
 text-shadow: 0 0 3px rgba(255,255,255,0.3);
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
 margin-bottom: 16px;
 font-size: 16px;
}
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
</style>
