<!-- frontend-vue/src/components/admin/FreezeUnfreezeForm.vue -->
<!-- 役割: ユーザーID指定で凍結/解除 -->
<template>
  <div class="row">
    <input class="input" v-model="userId" placeholder="user UUID" />
    <button class="btn" :disabled="pending" @click="freeze">Freeze</button>
    <button class="btn" :disabled="pending" @click="reactivate">Reactivate</button>
    <span class="muted" v-if="msg">{{ msg }}</span>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import api from '../../lib/api'

const userId = ref('')
const msg    = ref('')
const pending= ref(false)

async function freeze(){
  if (!userId.value) return
  if (!confirm('Freeze this user?')) return
  pending.value = true; msg.value=''
  try {
    await api.post(`/api/admin/users/${userId.value}/freeze`)
    msg.value = 'Frozen.'
  } catch(e) {
    msg.value = `${e.code||'ERROR'}: ${e.message||''}`
  } finally { pending.value = false }
}
async function reactivate(){
  if (!userId.value) return
  if (!confirm('Reactivate this user?')) return
  pending.value = true; msg.value=''
  try {
    await api.post(`/api/admin/users/${userId.value}/reactivate`)
    msg.value = 'Reactivated.'
  } catch(e) {
    msg.value = `${e.code||'ERROR'}: ${e.message||''}`
  } finally { pending.value = false }
}
</script>

<style scoped>
.muted { color: var(--muted) }
</style>
