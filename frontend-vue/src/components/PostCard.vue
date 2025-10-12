<!-- frontend-vue/src/components/PostCard.vue -->
<!-- 役割: 1件の投稿カード。アフォーダンスを見てボタン活性。Tx実行。 -->
<template>
  <div class="card">
    <div class="head">
      <div class="author">{{ short(post.creatorId) }}</div>
      <div class="meta">
        <span v-if="post.royaltyRate != null">ρ: {{ Number(post.royaltyRate).toFixed(2) }}</span>
        <span class="muted">{{ new Date(post.createdAt).toLocaleString() }}</span>
      </div>
    </div>
    <div class="body">{{ post.contentText }}</div>

    <div class="actions">
      <button
        :disabled="!afford.canLike || loading.like"
        :title="afford.likeReason"
        @click="like">
        👍 Like
      </button>
      <button
        :disabled="!afford.canQuote || loading.quote"
        :title="afford.quoteReason"
        @click="quote">
        🔁 Quote
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import api from '../lib/api'
import { getState, optimisticSpend } from '../stores/power' // ← bus を削除
import { v4 as uuidv4 } from 'uuid'

const props = defineProps({ post: { type:Object, required:true } })
const emit = defineEmits(['need-refresh'])

const afford = ref({ canLike:false, likeReason:'', canQuote:false, quoteReason:'' })
const loading = ref({ like:false, quote:false })
const power = getState()

function short(id){ return String(id).slice(0,8) }

async function loadAffordance(){
  if (!power.actor) { afford.value = { canLike:false, likeReason:'NO_ACTOR', canQuote:false, quoteReason:'NO_ACTOR' }; return }
  try {
    const { data } = await api.get(`/api/affordance/actors/${power.actor}/posts/${props.post.postId}`)
    afford.value = data
  } catch(e){
    console.error('affordance failed', e)
    afford.value = { canLike:false, likeReason:'ERR', canQuote:false, quoteReason:'ERR' }
  }
}

async function like(){
  try {
    loading.value.like = true
    const body = { actorId: power.actor, postId: props.post.postId, requestId: uuidv4() }
    await api.post(`/api/posts/${props.post.postId}/like`, body)
    // αはサーバ側の値に依存するが、MVPでは0.05で楽観更新
    optimisticSpend(0.05)
    await loadAffordance()
    emit('need-refresh')
  } catch(e){
    console.error('like failed', e)
    alert('Like failed')
  } finally {
    loading.value.like = false
  }
}

async function quote(){
  try {
    loading.value.quote = true
    const reqId = uuidv4()
    const body = { actorId: power.actor, postId: props.post.postId, requestId: reqId }

    // 1) 引用投稿を新規作成（無料）
    await api.post(`/api/posts/${props.post.postId}/quote`, body)

    // 2) β 取引（MVP: default βを想定）
    await api.post(`/api/posts/${props.post.postId}/quote/tx`, body)

    // β=0.12の楽観更新
    optimisticSpend(0.12)
    await loadAffordance()
    emit('need-refresh')
  } catch(e){
    console.error('quote failed', e)
    alert('Quote failed')
  } finally {
    loading.value.quote = false
  }
}

onMounted(loadAffordance)
watch(() => [power.actor, props.post.postId], loadAffordance)
</script>

<style scoped>
.card { border:1px solid var(--border); border-radius:8px; padding:10px; }
.head { display:flex; align-items:center; justify-content:space-between; }
.author{ font-weight:600; }
.meta { display:flex; gap:8px; align-items:center; }
.muted { color:var(--muted) }
.body { margin:8px 0 12px; white-space:pre-wrap; }
.actions { display:flex; gap:8px; }
button[disabled]{ opacity:0.5; cursor:not-allowed; }
</style>
