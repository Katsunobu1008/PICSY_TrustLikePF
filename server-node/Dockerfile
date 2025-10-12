# server-node/Dockerfile
# --- build UI (frontend-vue) ---
FROM node:20-alpine AS ui
WORKDIR /ui
COPY frontend-vue/package*.json ./
RUN npm ci
COPY frontend-vue/ ./
RUN npm run build      # → /ui/dist

# --- build BFF (server-node) ---
FROM node:20-alpine AS bff
WORKDIR /app
ENV NODE_ENV=production
COPY server-node/package*.json ./
RUN npm ci --omit=dev
COPY server-node/src ./src

# ビルド成果物をBFFの公開ディレクトリへ
COPY --from=ui /ui/dist ./public

# BFF が /api を backend にプロキシ、/ は ./public/index.html を返す想定
EXPOSE 3000
ENV BACKEND_API_URL=http://backend:8080
CMD ["node", "src/server.js"]
