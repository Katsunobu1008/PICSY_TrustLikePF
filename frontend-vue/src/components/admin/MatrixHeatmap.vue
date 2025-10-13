<!-- frontend-vue/src/components/admin/MatrixHeatmap.vue -->
<!-- 役割: 評価行列（アクティブ×アクティブ）ヒートマップ。 -->
<template>
  <div v-if="loading" class="skeleton" />
  <div v-else-if="error" class="muted">
    Matrix API not available (optional). You can add:
    <code>GET /v1/dashboard/active-users</code> and
    <code>GET /v1/dashboard/eval-matrix</code>.
  </div>
  <div v-else>
    <div class="legend">
      <span>0</span>
      <div class="grad"></div>
      <span>high</span>
    </div>
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
              <div class="cell" :style="styleFor(row.userId,col.userId)">{{ val(row.userId,col.userId) }}</div>
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
const loading = ref(true)
const error = ref(false)

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
  const shade = Math.max(0, 255 - Math.round(Math.min(1, v) * 255))
  return { background: `rgb(${shade},${shade},255)` }
}
function short(id){ return String(id).slice(0,8) }

async function load(){
  try {
    // ✅ baseURL=/api のため、相対パスで叩く
    users.value = (await api.get('/v1/dashboard/active-users')).data.users || []
    rows.value  = (await api.get('/v1/dashboard/eval-matrix')).data.rows || []
    buildMap()
    error.value = false
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>

<style scoped>
.skeleton{ height:180px; border:1px solid var(--border); border-radius:10px; background:linear-gradient(90deg,#f5f5f5,#fafafa,#f5f5f5); animation:p 1.2s linear infinite; }
@keyframes p{ 0%{background-position:-200px 0} 100%{background-position:200px 0} }
.scroll{ overflow:auto; max-width:100% }
.matrix{ border-collapse:collapse; min-width:560px }
th,td{ border:1px solid var(--border); padding:4px; text-align:center }
.cell{ min-width:56px; min-height:28px; display:flex; align-items:center; justify-content:center; }
.legend{ display:flex; align-items:center; gap:10px; margin:0 0 8px }
.grad{ height:8px; width:180px; background:linear-gradient(to right, rgb(255,255,255), rgb(200,200,255), rgb(150,150,255)) }
.muted{ color:var(--muted) }
</style>
