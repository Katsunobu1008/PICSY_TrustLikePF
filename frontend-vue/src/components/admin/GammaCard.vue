<!-- frontend-vue/src/components/admin/GammaCard.vue -->
<!-- 役割: γの表示/更新 -->
<template>
  <div class="row" style="align-items:flex-end">
    <div>
      <div class="muted">Current γ</div>
      <div style="font-size:24px;font-weight:700">{{ current.toFixed(3) }}</div>
    </div>
    <div style="width:140px"></div>
    <div style="max-width:260px">
      <label class="muted">Update γ (0-1)</label>
      <input class="input" type="number" min="0" max="1" step="0.01" v-model.number="next" />
    </div>
    <button class="btn primary" :disabled="saving" @click="save">Save</button>
  </div>
  <p v-if="msg" class="muted">{{ msg }}</p>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../lib/api'

const current = ref(0)
const next    = ref(0)
const msg     = ref('')
const saving  = ref(false)

async function load(){
  const { data } = await api.get('/api/admin/settings/recovery-gamma')
  current.value = data.value
  next.value    = data.value
}
async function save(){
  saving.value = true
  msg.value = ''
  try {
    await api.put('/api/admin/settings/recovery-gamma', { value: next.value })
    await load()
    msg.value = 'Saved.'
  } catch(e) {
    msg.value = `${e.code||'ERROR'}: ${e.message||''}`
  } finally {
    saving.value = false
  }
}
onMounted(load)
</script>

<style scoped>
.muted { color: var(--muted) }
</style>
