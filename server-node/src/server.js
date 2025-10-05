const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');

const app = express();
const PORT = 3000;
const BACKEND_URL = process.env.BACKEND_API_URL || 'http://localhost:8081';

// /api/v1 へのリクエストをJavaバックエンドへプロキシ
app.use(
  '/api/v1',
  createProxyMiddleware({
    target: BACKEND_URL,
    changeOrigin: true,
  })
);

// 将来ここに静的ファイル配信のコードを追加する

app.listen(PORT, () => {
  console.log(`BFF server running on http://localhost:${PORT}`);
});
