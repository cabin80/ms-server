const path = require('path')
module.exports = {
  publicPath: '/',
  outputDir: 'dist',
  devServer: {
    port: 8080,
    proxy: {
      '/api': {
        target: 'http://localhost:8089',
        changeOrigin: true
      }
    }
  },
  pages: {
    index: {
      entry: 'src/main.js',
      template: 'public/index.html',
      filename: 'index.html',
      title: '音乐播放器'
    },
    v2: {
      entry: 'src/v2/main.js',
      template: 'public/index.html',
      filename: 'v2/index.html',
      title: '音乐播放器 v2'
    },
    v3: {
      entry: 'src/v3/main.js',
      template: 'public/index.html',
      filename: 'v3/index.html',
      title: '音乐播放器 v3'
    },
    v4: {
      entry: 'src/v4/main.js',
      template: 'public/index.html',
      filename: 'v4/index.html',
      title: '音乐播放器 v4'
    },
    v5: {
      entry: 'src/v5/main.js',
      template: 'public/index.html',
      filename: 'v5/index.html',
      title: '音乐播放器 v5'
    }
  }
}
