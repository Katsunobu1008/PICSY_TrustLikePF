<!-- frontend-vue/src/components/admin/MatrixHeatmap.vue -->
<!-- 役割: 評価行列（アクティブ×アクティブ）ヒートマップ（APIが無い場合は案内表示） -->
<template>
  <div v-if="error" class="muted">Matrix API not available (optional). You can add:
    <code>GET /api/v1/dashboard/active-users</code> and
    <code>GET /api/v1/dashboard/eval-matrix</code>.
  </div>

  <div v-else>
    <div class="scroll">
      <table class="matrix">
        <thead>
          <tr>
            <th>i \ j</th>
            <th v-for="u in users" :key="u.userId">{{ short(u.userId) }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in users" :key="row.userId">
            <th>{{ short(row.userId) }}</th>
            <td v-for="col in users" :key="col.userId">
              <div class="cell" :style="styleFor(row.userId,col.userId)">
                {{ val(row.userId,col.userId) }}
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div class="muted" style="margin-top:6px">Darker is higher.</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../lib/api'

const users = ref([]) // [{userId,name}]
const rows  = ref([]) // [{evaluatorId,evaluateeId,value}]
const error = ref(false)

// 値ルックアップを簡単にするため Map に
const map = ref(new Map())
function buildMap(){
  const m = new Map()
  rows.value.forEach(r => m.set(`${r.evaluatorId}|${r.evaluateeId}`, r.value))
  map.value = m
}
function val(i,j){
  const v = map.value.get(`${i}|${j}`)
  return v!=null ? v.toFixed(3) : '0.000'
}
function styleFor(i,j){
  const v = map.value.get(`${i}|${j}`) || 0
  const shade = Math.min(255, 255 - Math.round(v*255))
  return { background: `rgb(${shade},${shade},255)` }
}
function short(id){ return String(id).slice(0,8) }

async function load(){
  try {
    const a = await api.get('/api/v1/dashboard/active-users')
    users.value = a.data.users || []
    const b = await api.get('/api/v1/dashboard/eval-matrix')
    rows.value = b.data.rows || []
    buildMap()
  } catch(err) {
    console.debug('MatrixHeatmap load failed:', err) // ← これで参照される
    error.value = true
  }
}
onMounted(load)
</script>

<style scoped>
.scroll { overflow: auto; max-width: 100%; }
.matrix { border-collapse: collapse; min-width: 560px; }
th, td { border: 1px solid var(--border); padding: 4px; text-align: center; }
.cell {
  min-width: 56px; min-height: 28px; display:flex; align-items:center; justify-content:center;
}
.muted { color: var(--muted) }
</style>
