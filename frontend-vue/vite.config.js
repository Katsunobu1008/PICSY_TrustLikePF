// frontend-vue/vite.config.js
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  server: {
    // ローカル開発サーバーのポートを3001に設定（BFFの3000と区別するため）
    port: 3001,
    proxy: {
      // '/api' で始まるリクエストをBFF (Node.jsサーバー) へ転送
      '/api': {
        target: 'http://localhost:8080', // docker-composeで公開したwebサービスのポート
        changeOrigin: true,
      },
    },
  },
});
