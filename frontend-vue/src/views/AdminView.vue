<!-- frontend-vue/src/views/AdminView.vue -->
<!-- 役割: γ設定、アクティブユーザー表、行列 -->
<template>
  <section class="panel">
    <h2>Natural Recovery γ</h2>
    <div class="row">
      <input type="number" step="0.01" min="0" max="1" v-model.number="gamma" />
      <button @click="saveGamma">Save</button>
      <button @click="runRecovery">Run Once</button>
      <button @click="recalcC">Recalc c</button>
    </div>
  </section>

  <section class="panel">
    <h2>Active Users</h2>
    <table>
      <thead><tr><th>userId</th><th>name</th></tr></thead>
      <tbody>
        <tr v-for="u in users" :key="u.userId">
          <td>{{ u.userId }}</td>
          <td>{{ u.name }}</td>
        </tr>
      </tbody>
    </table>
  </section>

  <section class="panel">
    <h2>Evaluation Matrix (Active × Active)</h2>
    <MatrixHeatmap />
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../lib/api'
import MatrixHeatmap from '../components/admin/MatrixHeatmap.vue'

const gamma = ref(0.03)
const users = ref([])

async function load(){
  try {
    const g = await api.get('/api/admin/settings/recovery-gamma')
    gamma.value = g.data.value
  } catch {            // 変数未使用で no-unused-vars を回避
    console.debug('recovery-gamma fetch skipped')
  }
  try {
    const a = await api.get('/api/v1/dashboard/active-users')
    users.value = a.data.users || []
  } catch {
    console.debug('active-users fetch skipped')
  }
}

async function saveGamma(){
  await api.put('/api/admin/settings/recovery-gamma', { value: Number(gamma.value) })
  alert('Saved')
}
async function runRecovery(){
  await api.post('/api/admin/recover')
  alert('Started')
}
async function recalcC(){
  await api.post('/api/admin/recalc-c')
  alert('Started')
}

onMounted(load)
</script>

<style scoped>
.panel{ border:1px solid var(--border); border-radius:8px; padding:10px; margin-bottom:16px; }
.row{ display:flex; align-items:center; gap:8px; }
table{ border-collapse: collapse; }
th,td{ border:1px solid var(--border); padding:4px 6px; }
</style>
