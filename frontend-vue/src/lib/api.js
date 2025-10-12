// frontend-vue/src/lib/api.js
// 役割: axiosインスタンス。Viteの /api プロキシを使う想定。
// エラー時は共通の整形を行い、呼び出し側でトーストなどに使える形にする。
import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 15000,
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const status = err?.response?.status
    const data = err?.response?.data
    const code = data?.code || data?.error || 'UNKNOWN'
    const message = data?.message || err.message
    return Promise.reject({ status, code, message })
  },
)

export default api
