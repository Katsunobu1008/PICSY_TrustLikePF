// frontend-vue/vite.config.js
/* vite.config.js */
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // バックエンド Spring Boot (http://localhost:8080) へ
      '/api': {
        target: 'http://localhost:808',
        changeOrigin: true,
      },
    },
  },
})
