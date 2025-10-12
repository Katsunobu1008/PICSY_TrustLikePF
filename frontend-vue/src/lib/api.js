// frontend-vue/src/lib/api.js
// 役割: Vite環境変数からベースURLを読み込む薄いAPIクライアント
import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE || '/api'

const api = axios.create({
  baseURL,
  timeout: 10000,
})

export default api
