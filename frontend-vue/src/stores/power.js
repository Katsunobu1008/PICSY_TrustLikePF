// frontend-vue/src/stores/power.js
// 役割: アクター(?actor=UUID)を保持し、購買力(Eii*c)をポーリング更新するグローバルストア。
import { reactive } from 'vue'
import api from '../lib/api'

const state = reactive({
  actor: null,
  eii: 1.0,
  c: 1.0,
  power: 1.0,
  polling: null,
})

export function getState() {
  return state
}

export function setActor(actorId) {
  state.actor = actorId || null
  if (state.actor) {
    refreshOnce().finally(() => window.dispatchEvent(new CustomEvent('timeline:refresh')))
  }
}

export function optimisticSpend(cost) {
  state.power = Math.max(0, state.power - Number(cost || 0))
}

export async function refreshOnce() {
  if (!state.actor) return
  try {
    const { data } = await api.get(`/users/${state.actor}/power`)
    state.eii = data.eii
    state.c = data.c
    state.power = data.purchasingPower
  } catch (e) {
    console.warn('power refresh failed', e)
  }
}

export function startPolling() {
  stopPolling()
  const ms = Number(import.meta.env.VITE_POLL_MS || 30000)
  state.polling = setInterval(refreshOnce, ms)
  refreshOnce()
}
export function stopPolling() {
  if (state.polling) clearInterval(state.polling)
  state.polling = null
}
