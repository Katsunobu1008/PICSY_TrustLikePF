<!-- frontend-vue/src/components/admin/EngineControls.vue -->
<!-- 役割: 自然回収・c再計算のトリガ -->
<template>
  <div class="row" style="gap:8px">
    <button class="btn" :disabled="running" @click="recover">Run Recovery</button>
    <button class="btn" :disabled="running" @click="recalc">Recalc c</button>
    <span class="muted" v-if="msg">{{ msg }}</span>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import api from '../../lib/api'

const running = ref(false)
const msg     = ref('')

async function recover(){
  running.value = true; msg.value = ''
  try {
    await api.post('/api/admin/recover')
    msg.value = 'Recovery accepted.'
  } catch(e) {
    msg.value = `${e.code||'ERROR'}: ${e.message||''}`
  } finally { running.value = false }
}
async function recalc(){
  running.value = true; msg.value = ''
  try {
    await api.post('/api/admin/recalc-c')
    msg.value = 'Recalc accepted.'
  } catch(e) {
    msg.value = `${e.code||'ERROR'}: ${e.message||''}`
  } finally { running.value = false }
}
</script>

<style scoped>
.muted { color: var(--muted) }
</style>
