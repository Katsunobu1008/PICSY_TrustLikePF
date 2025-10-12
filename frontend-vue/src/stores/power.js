// frontend-vue/src/stores/power.js
// 役割: 購買力の取得・ポーリング。mittの 'tx:done' イベントで即時更新。
import { defineStore } from 'pinia'
import api from '../lib/api'
import mitt from 'mitt'

export const bus = mitt()

export const usePowerStore = defineStore('power', {
  state: () => ({
    eii: 1.0,
    c: 1.0,
    power: 1.0,
    timer: null,
  }),
  actions: {
    async fetchPower(actorId) {
      if (!actorId) return
      const { data } = await api.get(`/api/users/${actorId}/power`)
      this.eii = data.eii
      this.c = data.c
      this.power = data.purchasingPower
    },
    startPolling(actorId) {
      this.stopPolling()
      const ms = Number(import.meta.env.VITE_POLL_MS || 30000)
      this.timer = setInterval(() => this.fetchPower(actorId), ms)
      // 取引成功時は即更新
      bus.off('tx:done')
      bus.on('tx:done', () => this.fetchPower(actorId))
    },
    stopPolling() {
      if (this.timer) clearInterval(this.timer)
      this.timer = null
    },
  },
})
