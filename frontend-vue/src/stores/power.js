// frontend-vue/src/stores/power.js
// 役割: アクター（?actor=UUID）を保持し、購買力をポーリング/更新するシンプルなストア。
// 依存: src/lib/api.js

import api from '../lib/api'

// 超簡易イベントバス（mitt等の依存を増やさないMVP）
export const bus = {
  _h: {},
  on(evt, fn) {
    ;(this._h[evt] ||= []).push(fn)
  },
  off(evt, fn) {
    this._h[evt] = (this._h[evt] || []).filter((f) => f !== fn)
  },
  emit(evt, ...args) {
    ;(this._h[evt] || []).forEach((f) => f(...args))
  },
}

const state = {
  actor: null, // UUID
  eii: 1,
  c: 1,
  purchasingPower: 1,
  polling: null,
}

export function getState() {
  return state
}

export function setActor(uuid) {
  state.actor = uuid || null
  // actor 変更時に即時購買力フェッチ & TL更新合図
  if (state.actor) {
    fetchPower().finally(() => bus.emit('timeline:refresh'))
  }
}

export async function fetchPower() {
  if (!state.actor) return
  try {
    const { data } = await api.get(`/api/users/${state.actor}/power`)
    state.eii = data.eii
    state.c = data.c
    state.purchasingPower = data.purchasingPower
    bus.emit('power:updated', { ...data })
  } catch (e) {
    console.error('fetchPower failed', e)
  }
}

// Tx直後などに一時的に“見かけ上”の購買力を減らしておく（楽観的UI）
export function optimisticSpend(delta) {
  state.purchasingPower = Math.max(0, state.purchasingPower - delta)
}

// ポーリング開始
export function startPolling() {
  const ms = Number(import.meta.env.VITE_POLL_MS || 30000)
  if (state.polling) clearInterval(state.polling)
  state.polling = setInterval(fetchPower, ms)
  fetchPower()
}

// ポーリング停止
export function stopPolling() {
  if (state.polling) clearInterval(state.polling)
  state.polling = null
}
