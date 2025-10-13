# server-node/Dockerfile
# --- build UI (frontend-vue) ---
FROM node:20-alpine AS ui
WORKDIR /ui
# lock 不整合の影響を避けるため、ci が失敗したら install にフォールバック
COPY frontend-vue/package*.json ./
RUN npm ci || npm install
COPY frontend-vue/ ./
# プロダクション向け API ベースは /api（BFF 経由）
# .env.production があればそれが使われます。無い場合でも VITE_API_BASE=/api 既定でOK。
RUN npm run build   # → /ui/dist

# --- build BFF (server-node) ---
FROM node:20-alpine AS bff
WORKDIR /app
ENV NODE_ENV=production

COPY server-node/package*.json ./
# lock を同期済みなので本来は ci がGood。万一のため install にしておくと現場で安定。
RUN npm install --omit=dev
COPY server-node/src ./src

# UI の成果物を BFF の公開フォルダへ配置
COPY --from=ui /ui/dist ./public

EXPOSE 3000
# compose から BACKEND_API_URL=http://backend:8080 を注入します
CMD ["node", "src/server.js"]
