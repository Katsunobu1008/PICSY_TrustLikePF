import express from 'express';
import { createProxyMiddleware } from 'http-proxy-middleware';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = 3000; // コンテナ内部のポート
const BACKEND_URL = process.env.BACKEND_API_URL || 'http://localhost:8081';

// APIプロキシ: /api で始まるリクエストをJavaバックエンドへ転送
app.use(
  '/api',
  createProxyMiddleware({
    target: BACKEND_URL,
    changeOrigin: true,
  })
);

// 静的ファイル配信: frontend-vueのビルド成果物(dist)を配信する設定
// publicディレクトリをプロジェクトルートからの相対パスで指定
const frontendDistPath = path.join(
  __dirname,
  '..',
  '..',
  'frontend-vue',
  'dist'
);
app.use(express.static(frontendDistPath));

// SPAフォールバック: どのURLにアクセスされてもindex.htmlを返す
app.get('*', (req, res) => {
  res.sendFile(path.join(frontendDistPath, 'index.html'));
});

app.listen(PORT, () => {
  console.log(`✅ BFF Server is running on port ${PORT}`);
});
