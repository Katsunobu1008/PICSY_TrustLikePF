<!-- frontend-vue/src/components/ComposePost.vue -->
<!-- 役割: 原作投稿フォーム（ロイヤリティ必須）。成功/失敗のフィードバック付き -->
<template>
  <form class="compose" @submit.prevent="submit">
    <h3>New Post</h3>
    <textarea v-model="content" placeholder="Share something..."></textarea>
    <div class="row">
      <label>royalty (0.00 - 1.00)</label>
      <input type="number" step="0.01" min="0" max="1" v-model.number="royalty"/>
      <button :disabled="!canSubmit || busy">{{ busy ? 'Posting...' : 'Post' }}</button>
    </div>
    <p class="muted">Actor: <code>{{ short(actor) || '(unset)' }}</code></p>
    <p v-if="msg" :class="ok ? 'ok':'err'">{{ msg }}</p>
  </form>
</template>

<script setup>
import { ref, computed } from 'vue'
import api from '../lib/api'
import { getState } from '../stores/power'

const st = getState()
const actor = computed(() => st.actor)

const content = ref('')
const royalty = ref(0.7)
const busy = ref(false)
const ok = ref(false)
const msg = ref('')

const canSubmit = computed(() =>
  actor.value && content.value.trim().length>0 &&
  Number.isFinite(royalty.value) && royalty.value>=0 && royalty.value<=1
)

function short(id){ return String(id||'').slice(0,8) }

async function submit(){
  if (!canSubmit.value) return
  try{
    busy.value = true
    msg.value = ''
    const body = {
      creatorId: actor.value,
      contentText: content.value.trim(),
      royaltyRate: Number(royalty.value),
      parentPostId: null,
      mediaKeys: []
    }
    await api.post('/posts', body)
    ok.value = true; msg.value = 'Posted!'
    content.value = ''
    // TL を更新
    window.dispatchEvent(new CustomEvent('timeline:refresh'))
  }catch(e){
    ok.value = false
    const detail = e?.response?.data?.message || e?.message || 'unknown'
    msg.value = `Post failed: ${detail}`
  }finally{
    busy.value = false
  }
}
</script>

<style scoped>
.compose{ border:1px solid var(--border); padding:14px; border-radius:10px; display:flex; flex-direction:column; gap:10px; background:#fff }
h3{ margin:0 0 6px; }
textarea{ min-height:96px; resize:vertical; padding:10px; border:1px solid var(--border); border-radius:8px; }
.row{ display:flex; gap:10px; align-items:center }
input{ width:120px; padding:6px 8px; border:1px solid var(--border); border-radius:6px; }
button{ padding:8px 12px; border:1px solid var(--border); border-radius:8px; background:#111827; color:#fff; }
.muted{ color:var(--muted); margin:0 }
.ok{ color:#065f46 }
.err{ color:#b91c1c }
</style>
