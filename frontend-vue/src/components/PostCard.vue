<!-- frontend-vue/src/components/PostCard.vue -->
<!-- 役割: 投稿カード。ハート/リツイートで取引実行。 -->
<template>
  <article class="card">
    <header class="head">
      <div class="author">👤 {{ short(post.creatorId) }}</div>
      <div class="meta">
        <span v-if="post.royaltyRate != null">ρ {{ Number(post.royaltyRate).toFixed(2) }}</span>
        <span class="muted">{{ new Date(post.createdAt).toLocaleString() }}</span>
      </div>
    </header>

    <p class="body">{{ post.contentText }}</p>

    <footer class="actions">
      <button
        class="icon"
        :disabled="!afford.canLike || loading.like"
        :title="afford.likeReason"
        @click="like"
      >{{ loading.like ? '…' : '❤️' }}</button>

      <button
        class="icon"
        :disabled="!afford.canQuote || loading.quote"
        :title="afford.quoteReason"
        @click="quote"
      >{{ loading.quote ? '…' : '🔁' }}</button>
    </footer>
  </article>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import api from '../lib/api'
import { getState, optimisticSpend } from '../stores/power'
import { uuidv4 } from '../lib/uuid'

const props = defineProps({ post: { type:Object, required:true } })
const emit = defineEmits(['need-refresh'])

const afford = ref({ canLike:false, likeReason:'', canQuote:false, quoteReason:'' })
const loading = ref({ like:false, quote:false })
const state = getState()

function short(id){ return String(id).slice(0,8) }

async function loadAffordance(){
  if (!state.actor) {
    afford.value = { canLike:false, likeReason:'NO_ACTOR', canQuote:false, quoteReason:'NO_ACTOR' }
    return
  }
  // ✅ 新パス：/posts/{id}/actions?actor=...
  const { data } = await api.get(`/posts/${props.post.postId}/actions`, { params: { actor: state.actor } })
  afford.value = data
}

async function like(){
  try{
    loading.value.like = true
    const body = { actorId: state.actor, postId: props.post.postId, requestId: uuidv4() }
    await api.post(`/posts/${props.post.postId}/like`, body)
    optimisticSpend(0.05)
    await loadAffordance()
    emit('need-refresh')
  }catch(e){
    alert(`Like failed: ${e?.response?.data?.message || e.message}`)
  }finally{ loading.value.like = false }
}

async function quote(){
  try{
    loading.value.quote = true
    const reqId = uuidv4()
    const body = { actorId: state.actor, postId: props.post.postId, requestId: reqId }
    await api.post(`/posts/${props.post.postId}/quote`, body)     // 投稿生成（無料）
    await api.post(`/posts/${props.post.postId}/quote/tx`, body)  // β 取引
    optimisticSpend(0.12)
    await loadAffordance()
    emit('need-refresh')
  }catch(e){
    alert(`Quote failed: ${e?.response?.data?.message || e.message}`)
  }finally{ loading.value.quote = false }
}

onMounted(loadAffordance)
watch(() => [state.actor, props.post.postId], loadAffordance)
</script>

<style scoped>
.card{ border:1px solid var(--border); border-radius:12px; padding:12px; background:#fff }
.head{ display:flex; align-items:center; justify-content:space-between; gap:10px }
.author{ font-weight:700 }
.meta{ display:flex; gap:10px; align-items:center }
.muted{ color:var(--muted) }
.body{ margin:8px 0 10px; white-space:pre-wrap; }
.actions{ display:flex; gap:8px }
.icon{ width:40px; height:36px; border-radius:10px; border:1px solid var(--border); background:#fff; font-size:18px; }
.icon:disabled{ opacity:.45; cursor:not-allowed }
</style>
