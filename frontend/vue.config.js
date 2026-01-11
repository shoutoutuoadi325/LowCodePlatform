module.exports = {
  devServer: {
    port: 8080,
    proxy: {
      '/api/devices': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/scenes': {
        target: 'http://localhost:8082',
        changeOrigin: true
      }
    }
  }
}
