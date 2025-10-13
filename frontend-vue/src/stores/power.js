// frontend-vue/src/stores/power.js
// 役割: アクター(?actor=UUID)を保持し、購買力(Eii*c)をポーリング更新するグローバルストア。
//      Vueのリアクティブを使うことで、参照側は自動でUIが更新される。
// 依存: src/lib/api.js

import { reactive } from 'vue'
import api from '../lib/api'

const state = reactive({
  actor: null, // 選択アクターのUUID（URLクエリに同期する）
  eii: 1.0, // 自己ループ（行自己）Eii
  c: 1.0, // 貢献度 c
  power: 1.0, // 購買力 = Eii * c
  polling: null, // setInterval のハンドル
})

export function getState() {
  return state
}

export function setActor(actorId) {
  state.actor = actorId || null
  if (state.actor) {
    refreshOnce()
      // タイムラインを再取得したい場合は window イベントで十分（bus不要）
      .finally(() => window.dispatchEvent(new CustomEvent('timeline:refresh')))
  }
}

// 交易直後など、UIを即時に気持ちよくするための楽観的消費（後でポーリングで正へ収束）
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
    // 通信不調時はログに留め、UIは次回ポーリングに委ねる
    console.warn('power refresh failed', e)
  }
}

export function startPolling() {
  stopPolling()
  const ms = Number(import.meta.env.VITE_POLL_MS || 30000)
  state.polling = setInterval(refreshOnce, ms)
  // すぐ1回
  refreshOnce()
}

export function stopPolling() {
  if (state.polling) clearInterval(state.polling)
  state.polling = null
}
