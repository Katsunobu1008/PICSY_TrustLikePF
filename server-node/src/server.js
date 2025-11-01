// server-node/src/server.js
import express from 'express';
import { createProxyMiddleware } from 'http-proxy-middleware';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = process.env.PORT || 3000;
const BACKEND = process.env.BACKEND_API_URL || 'http://localhost:8081';
const API_PREFIX = '/api';

// ✅ /api を backend にそのまま流す（pathRewriteは不要）
app.use(
  API_PREFIX,
  createProxyMiddleware({
    target: BACKEND,
    changeOrigin: true,
    pathRewrite: (path, req) => `${API_PREFIX}${path}`,
    // logLevel: 'debug',   // 困ったときだけ一時的に有効化
  })
);

// 静的配信（Dockerfile で /app/public に dist を配置）
app.use(express.static(path.join(__dirname, '..', 'public')));

// SPA なのでフォールバック
app.get('*', (_req, res) => {
  res.sendFile(path.join(__dirname, '..', 'public', 'index.html'));
});

app.listen(PORT, () => {
  console.log(`✅ BFF Server is running on port ${PORT}`);
});
