import Vue from 'vue'
import Vuex from 'vuex'
import axios from 'axios'

Vue.use(Vuex)

const API_BASE = process.env.NODE_ENV === 'production' ? '/api' : '/api'

export default new Vuex.Store({
  state: {
    user: JSON.parse(localStorage.getItem('ms_user') || 'null'),
    currentSong: JSON.parse(sessionStorage.getItem('ms_current_song') || 'null'),
    playlist: JSON.parse(sessionStorage.getItem('ms_playlist') || '[]'),
    currentIndex: parseInt(sessionStorage.getItem('ms_current_index') || '-1'),
    isPlaying: false,
    showAddToPlaylist: false,
    selectedSongId: null
  },
  mutations: {
    SET_USER(state, user) {
      state.user = user
      localStorage.setItem('ms_user', JSON.stringify(user))
    },
    LOGOUT(state) {
      state.user = null
      localStorage.removeItem('ms_user')
    },
    SET_CURRENT_SONG(state, { song, index, list }) {
      state.currentSong = song
      state.currentIndex = index
      if (list) {
        state.playlist = list
        sessionStorage.setItem('ms_playlist', JSON.stringify(list))
      }
      sessionStorage.setItem('ms_current_song', JSON.stringify(song))
      sessionStorage.setItem('ms_current_index', String(index))
    },
    SET_PLAYING(state, val) {
      state.isPlaying = val
    },
    SHOW_ADD_PLAYLIST(state, songId) {
      state.selectedSongId = songId
      state.showAddToPlaylist = true
    },
    HIDE_ADD_PLAYLIST(state) {
      state.showAddToPlaylist = false
      state.selectedSongId = null
    }
  },
  actions: {
    async login({ commit }, { username, password }) {
      const res = await axios.post(`${API_BASE}/users/login`, { username, password })
      if (res.data.code === 200) {
        commit('SET_USER', res.data.data)
      }
      return res.data
    },
    async fetchSongs() {
      const res = await axios.get(`${API_BASE}/songs`)
      return res.data
    },
    async fetchArtists() {
      const res = await axios.get(`${API_BASE}/artists`)
      return res.data
    },
    async fetchAlbums() {
      const res = await axios.get(`${API_BASE}/albums`)
      return res.data
    },
    async fetchPlaylists({ state }) {
      const uid = state.user?.id || 1
      const res = await axios.get(`${API_BASE}/playlists?user_id=${uid}`)
      return res.data
    },
    async fetchPlaylistDetail(_, id) {
      const res = await axios.get(`${API_BASE}/playlists/${id}`)
      return res.data
    },
    async fetchSongsByArtist(_, artistId) {
      const res = await axios.get(`${API_BASE}/songs/by-artist/${artistId}`)
      return res.data
    },
    async fetchSongsByAlbum(_, albumId) {
      const res = await axios.get(`${API_BASE}/songs/by-album/${albumId}`)
      return res.data
    },
    async toggleFavorite(_, { user_id, song_id }) {
      const res = await axios.post(`${API_BASE}/favorites/toggle`, { user_id, song_id })
      return res.data
    },
    async checkFavorite(_, { user_id, song_id }) {
      const res = await axios.get(`${API_BASE}/favorites/check/${song_id}?user_id=${user_id}`)
      return res.data
    },
    async addToPlaylist(_, { playlist_id, song_id }) {
      const res = await axios.post(`${API_BASE}/playlists/add-song`, { playlist_id, song_id })
      return res.data
    }
  },
  getters: {
    isLoggedIn: state => !!state.user
  }
})
