// server-node/src/server.js
import express from 'express';
import { createProxyMiddleware } from 'http-proxy-middleware';
import path from 'path';
import { fileURLToPath } from 'url';

// ES Module環境で __dirname を再現するための設定
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = 3000;
const BACKEND_URL = process.env.BACKEND_API_URL || 'http://localhost:8081';

// APIリクエストのプロキシ設定
// /api で始まるリクエストをすべてJavaバックエンドに転送する
app.use(
  '/api',
  createProxyMiddleware({
    target: BACKEND_URL,
    changeOrigin: true,
    // パスを書き換えない（例：/api/v1/health -> http://backend:8080/api/v1/health）
    pathRewrite: { '^/api': '/api' },
  })
);

// Vue.jsの静的ファイル配信設定
// ビルドされたVueアプリケーションの静的ファイルが置かれるディレクトリを指定
app.use(express.static(path.join(__dirname, '..', 'public')));

// SPA (Single Page Application) のためのフォールバック設定
// API以外のすべてのリクエストに対してindex.htmlを返す
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, '..', 'public', 'index.html'));
});

app.listen(PORT, () => {
  console.log(`✅ BFF Server is running at http://localhost:${PORT}`);
});
