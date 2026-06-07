<template>
  <div class="home-layout">
    <!-- 左侧边栏 -->
    <aside class="sidebar">
      <div class="logo" @click="goHome">
        <span class="logo-icon">♪</span>
        <span class="logo-text">MS Music</span>
      </div>

      <!-- 用户区域 -->
      <div class="user-section">
        <div v-if="isLoggedIn" class="user-info" @click="$router.push('/login')">
          <div class="avatar">{{ user.nickname?.[0] || 'U' }}</div>
          <span>{{ user.nickname }}</span>
        </div>
        <button v-else class="login-btn" @click="$router.push('/login')">登录</button>
      </div>

      <!-- 歌手列表 -->
      <div class="list-section">
        <div class="section-header">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
          <span>歌手</span>
        </div>
        <div class="list-items">
          <div v-for="artist in artists" :key="artist.id"
               class="list-item"
               :class="{ active: activeView === 'artist' && activeId === artist.id }"
               @click="selectArtist(artist)">
            <img :src="artist.cover || getDefaultCover()" class="item-cover" />
            <div class="item-info">
              <span class="item-name">{{ artist.name }}</span>
              <span class="item-sub">{{ artist.song_count }}首歌曲</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 我的歌单 -->
      <div class="list-section">
        <div class="section-header">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"/></svg>
          <span>我的歌单</span>
        </div>
        <div class="list-items">
          <div v-for="pl in playlists" :key="pl.id"
               class="list-item"
               :class="{ active: activeView === 'playlist' && activeId === pl.id }"
               @click="selectPlaylist(pl)">
            <img :src="pl.cover || getDefaultCover()" class="item-cover" />
            <div class="item-info">
              <span class="item-name">{{ pl.name }}</span>
              <span class="item-sub">{{ pl.song_count }}首</span>
            </div>
          </div>
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <div v-if="activeView === 'home'" class="home-view">
        <h1 class="greeting">音乐</h1>
        <p class="subtitle">选择左侧的歌手或歌单开始播放</p>
        <div class="home-cards">
          <div class="home-card" v-for="artist in artists.slice(0,6)" :key="artist.id" @click="selectArtist(artist)">
            <img :src="artist.cover || getDefaultCover()" />
            <span>{{ artist.name }}</span>
          </div>
        </div>
      </div>

      <div v-else-if="activeView === 'songs'" class="songs-view">
        <div class="songs-header">
          <h2>{{ viewTitle }}</h2>
          <span class="song-count">{{ songs.length }}首歌曲</span>
        </div>
        <div class="songs-list">
          <div v-for="(song, idx) in songs" :key="song.id"
               class="song-item"
               :class="{ active: song.id === currentSong?.id }"
               @dblclick="playSong(song, idx)">
            <div class="song-index">{{ idx + 1 }}</div>
            <img :src="song.album_cover || song.artist_cover || getDefaultCover()" class="song-cover" />
            <div class="song-info">
              <span class="song-name">{{ song.name }}</span>
              <div class="song-meta">
                <span class="song-artist">{{ song.artist_name }}</span>
                <span class="song-album">{{ song.album_name }}</span>
              </div>
            </div>
            <span class="song-duration">{{ song.duration_text }}</span>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      artists: [],
      playlists: [],
      songs: [],
      activeView: 'home',
      activeId: null,
      viewTitle: ''
    }
  },
  computed: {
    user() { return this.$store.state.user },
    isLoggedIn() { return this.$store.getters.isLoggedIn },
    currentSong() { return this.$store.state.currentSong }
  },
  async created() {
    await this.loadData()
    // handle query params
    if (this.$route.query.artist) {
      this.loadArtistSongs(this.$route.query.artist)
    } else if (this.$route.query.album) {
      this.loadAlbumSongs(this.$route.query.album)
    }
  },
  methods: {
    getDefaultCover() {
      return 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="64" height="64"><circle cx="12" cy="12" r="10" fill="%23302b63"/><path d="M12 8.5c-1.93 0-3.5 1.57-3.5 3.5s1.57 3.5 3.5 3.5 3.5-1.57 3.5-3.5S13.93 8.5 12 8.5z" fill="%237a9aff"/></svg>'
    },
    async loadData() {
      const [artistsRes, playlistsRes] = await Promise.all([
        this.$store.dispatch('fetchArtists'),
        this.$store.dispatch('fetchPlaylists')
      ])
      this.artists = artistsRes.data || []
      this.playlists = playlistsRes.data || []
    },
    goHome() {
      this.activeView = 'home'
      this.activeId = null
      this.songs = []
    },
    async selectArtist(artist) {
      this.activeView = 'songs'
      this.activeId = artist.id
      this.viewTitle = artist.name
      const res = await this.$store.dispatch('fetchSongsByArtist', artist.id)
      this.songs = res.data || []
    },
    async selectPlaylist(pl) {
      this.activeView = 'songs'
      this.activeId = pl.id
      this.viewTitle = pl.name
      const res = await this.$store.dispatch('fetchPlaylistDetail', pl.id)
      this.songs = res.data?.songs || []
    },
    async loadArtistSongs(artistId) {
      const res = await this.$store.dispatch('fetchSongsByArtist', artistId)
      this.songs = res.data || []
      this.activeView = 'songs'
      this.viewTitle = '歌手'
    },
    async loadAlbumSongs(albumId) {
      const res = await this.$store.dispatch('fetchSongsByAlbum', albumId)
      this.songs = res.data || []
      this.activeView = 'songs'
      this.viewTitle = '专辑'
    },
    playSong(song, index) {
      this.$store.commit('SET_CURRENT_SONG', { song, index, list: this.songs })
    }
  }
}
</script>

<style scoped>
.home-layout {
  display: flex;
  height: calc(100vh - 80px);
  overflow: hidden;
}

/* Sidebar */
.sidebar {
  width: 280px;
  min-width: 280px;
  background: rgba(15,12,41,0.8);
  border-right: 1px solid rgba(255,255,255,0.06);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  padding: 0 0 16px 0;
  scrollbar-width: thin;
  scrollbar-color: rgba(122,154,255,0.2) transparent;
}
.sidebar::-webkit-scrollbar { width: 4px; }
.sidebar::-webkit-scrollbar-thumb { background: rgba(122,154,255,0.2); border-radius: 2px; }

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 20px 12px;
  cursor: pointer;
}
.logo-icon {
  font-size: 28px;
  color: #7a9aff;
  text-shadow: 0 0 20px rgba(122,154,255,0.5);
}
.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
}

.user-section {
  padding: 12px 20px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px;
  border-radius: 8px;
  transition: background 0.2s;
}
.user-info:hover { background: rgba(122,154,255,0.08); }
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7a9aff, #302b63);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: #fff;
}
.login-btn {
  width: 100%;
  padding: 8px;
  background: rgba(122,154,255,0.15);
  border: 1px solid rgba(122,154,255,0.25);
  border-radius: 8px;
  color: #7a9aff;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}
.login-btn:hover { background: rgba(122,154,255,0.25); }

.list-section {
  padding: 12px 0;
}
.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  color: #8a8aaa;
  font-size: 12px;
  letter-spacing: 2px;
  text-transform: uppercase;
}
.list-items {
  display: flex;
  flex-direction: column;
}
.list-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 20px;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 3px solid transparent;
}
.list-item:hover { background: rgba(122,154,255,0.06); }
.list-item.active {
  background: rgba(122,154,255,0.1);
  border-left-color: #7a9aff;
}
.item-cover {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  object-fit: cover;
}
.item-info {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.item-name {
  color: #d0d0e0;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.item-sub {
  color: #6a6a8a;
  font-size: 11px;
}

/* Main Content */
.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 32px 40px;
  scrollbar-width: thin;
  scrollbar-color: rgba(122,154,255,0.2) transparent;
}
.main-content::-webkit-scrollbar { width: 6px; }
.main-content::-webkit-scrollbar-thumb { background: rgba(122,154,255,0.2); border-radius: 3px; }

.greeting {
  font-size: 42px;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 0 40px rgba(122,154,255,0.2);
  margin-bottom: 8px;
}
.subtitle {
  color: #6a6a8a;
  font-size: 14px;
  margin-bottom: 32px;
}
.home-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}
.home-card {
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.06);
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}
.home-card:hover {
  background: rgba(122,154,255,0.08);
  border-color: rgba(122,154,255,0.2);
  transform: translateY(-4px);
}
.home-card img {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 8px;
  object-fit: cover;
  margin-bottom: 10px;
}
.home-card span {
  font-size: 13px;
  color: #d0d0e0;
}

/* Songs View */
.songs-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 24px;
}
.songs-header h2 {
  color: #fff;
  font-size: 28px;
  text-shadow: 0 0 30px rgba(122,154,255,0.2);
}
.song-count { color: #6a6a8a; font-size: 13px; }
.song-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.song-item:hover { background: rgba(122,154,255,0.06); }
.song-item.active { background: rgba(122,154,255,0.12); }
.song-index {
  width: 24px;
  text-align: center;
  color: #6a6a8a;
  font-size: 12px;
}
.song-cover {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  object-fit: cover;
}
.song-info { flex: 1; overflow: hidden; }
.song-name {
  display: block;
  color: #e0e0f0;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.song-meta {
  display: flex;
  gap: 8px;
  margin-top: 2px;
}
.song-artist, .song-album {
  color: #6a6a8a;
  font-size: 12px;
}
.song-duration { color: #6a6a8a; font-size: 12px; }
</style>
