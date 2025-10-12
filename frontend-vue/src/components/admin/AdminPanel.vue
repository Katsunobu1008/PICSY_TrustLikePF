<!-- frontend-vue/src/components/admin/AdminPanel.vue -->
<!-- 役割: 管理ダッシュボード。γの表示/変更、回収実行、c再計算、行列ヒートマップ。 -->
<template>
  <div class="grid">
    <section class="card">
      <h3>Recovery γ</h3>
      <div class="row">
        <input type="number" step="0.01" min="0" max="1" v-model.number="gamma"/>
        <button @click="saveGamma">Save</button>
        <button @click="runRecovery">Run recovery now</button>
        <button @click="recalcC">Recalc c now</button>
      </div>
      <div class="muted">Current γ: {{ current.toFixed(2) }}</div>
    </section>

    <section class="card">
      <h3>Eval Matrix (Active × Active)</h3>
      <MatrixHeatmap/>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../lib/api'
import MatrixHeatmap from './MatrixHeatmap.vue'

const gamma   = ref(0.03)
const current = ref(0.03)

async function loadGamma(){
  const { data } = await api.get('/api/admin/settings/recovery-gamma')
  gamma.value = data.value
  current.value = data.value
}
async function saveGamma(){
  await api.put('/api/admin/settings/recovery-gamma', { value: gamma.value })
  await loadGamma()
}
async function runRecovery(){
  await api.post('/api/admin/recover')
}
async function recalcC(){
  await api.post('/api/admin/recalc-c')
}

onMounted(loadGamma)
</script>

<style scoped>
.grid{ display:grid; grid-template-columns: 1fr; gap:16px; }
.card{ border:1px solid var(--border); border-radius:8px; padding:12px; }
.row{ display:flex; gap:8px; align-items:center; }
.muted{ color:var(--muted) }
</style>
