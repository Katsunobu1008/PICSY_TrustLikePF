<!-- frontend-vue/src/components/ComposePost.vue -->
<!-- 役割: 原作投稿フォーム（ロイヤリティ必須）。引用は PostCard 側で実施。 -->
<template>
  <form class="compose" @submit.prevent="submit">
    <textarea v-model="content" placeholder="Share something..."></textarea>
    <div class="row">
      <label>royalty (0.0 - 1.0)</label>
      <input type="number" step="0.01" min="0" max="1" v-model.number="royalty"/>
      <button :disabled="!canSubmit">Post</button>
    </div>
    <div class="muted">Actor from URL: <code>{{ short(actor) }}</code></div>
  </form>
</template>

<script setup>
import { ref, computed } from 'vue'
import api from '../lib/api'
import { getState } from '../stores/power'  // ← bus は使わない（window イベントに統一）

const power = getState()
const actor = computed(() => power.actor)

const content = ref('')
const royalty = ref(0.7)

const canSubmit = computed(() =>
  actor.value && content.value.trim().length > 0 &&
  royalty.value >= 0 && royalty.value <= 1
)

function short(id){ return String(id||'').slice(0,8) }

async function submit(){
  if (!canSubmit.value) return
  try {
    const body = {
      creatorId: actor.value,
      contentText: content.value.trim(),
      royaltyRate: royalty.value,
      parentPostId: null,
      mediaKeys: []
    }
    await api.post('/posts', body)
    content.value = ''
    // タイムライン更新（window カスタムイベント）
    window.dispatchEvent(new CustomEvent('timeline:refresh'))
  } catch(e){
    console.error('create post failed', e)
    alert('Post failed')
  }
}
</script>

<style scoped>
.compose { border:1px solid var(--border); padding:12px; border-radius:8px; display:flex; flex-direction:column; gap:8px; }
textarea{ min-height:80px; resize:vertical; padding:8px; }
.row { display:flex; gap:10px; align-items:center; }
.muted{ color:var(--muted) }
</style>
