<!-- frontend-vue/src/components/PostCard.vue -->
<!-- 役割: 投稿カード。アクション可否取得→ いいね/引用ボタン -->
<template>
  <article class="card">
    <div class="row" style="justify-content:space-between">
      <div>
        <strong>{{ post.creatorId }}</strong>
        <div class="muted" style="font-size:12px">{{ new Date(post.createdAt).toLocaleString() }}</div>
      </div>
      <div class="muted" v-if="loading">loading...</div>
    </div>

    <p style="margin:12px 0 8px; white-space:pre-wrap;">{{ post.contentText }}</p>

    <div class="row" style="gap:8px;flex-wrap:wrap">
      <button class="btn"
        :disabled="!actions.canLike || doing"
        @click="doLike">
        👍 Like
      </button>
      <span class="muted" v-if="!actions.canLike">({{ actions.likeReason }})</span>

      <button class="btn"
        :disabled="!actions.canQuote || doing"
        @click="doQuote">
        🔁 Quote
      </button>
      <span class="muted" v-if="!actions.canQuote">({{ actions.quoteReason }})</span>
    </div>
  </article>
</template>

<script setup>
import { reactive, ref, onMounted, watch } from 'vue'
import { useActorStore } from '../stores/actor'
import { bus } from '../stores/power'
import api from '../lib/api'
import { newRequestId } from '../lib/uuid'

const props = defineProps({ post: { type: Object, required: true } })
const emit  = defineEmits(['need-refresh'])

const actor = useActorStore()
const actions = reactive({
  canLike: false, likeReason: 'loading',
  canQuote:false, quoteReason:'loading'
})
const loading = ref(false)
const doing   = ref(false)

async function fetchActions() {
  if (!actor.actorId) return
  loading.value = true
  try {
    // Affordance API（MVP標準）
    const { data } = await api.get(`/api/affordance/actors/${actor.actorId}/posts/${props.post.postId}`)
    actions.canLike = data.canLike
    actions.likeReason = data.likeReason
    actions.canQuote = data.canQuote
    actions.quoteReason = data.quoteReason
  } catch(e) {
    actions.canLike = false; actions.likeReason  = e.code || 'ERROR'
    actions.canQuote= false; actions.quoteReason = e.code || 'ERROR'
  } finally {
    loading.value = false
  }
}

async function doLike(){
  if (!actor.actorId) return
  doing.value = true
  try {
    await api.post(`/api/posts/${props.post.postId}/like`, {
      actorId: actor.actorId,
      postId:  props.post.postId,
      requestId: newRequestId()
    })
    // 取引成功 → Power 即時更新 & このカードの可否を再取得
    bus.emit('tx:done')
    await fetchActions()
    emit('need-refresh') // 好みでタイムライン全体を軽く更新
  } catch(e) {
    // GlobalExceptionHandlerの code をそのまま表示するならアラートでもOK
    alert(`${e.code || 'ERROR'}: ${e.message || ''}`)
  } finally {
    doing.value = false
  }
}

// 引用は現行APIに合わせて 2ステップ: (1) create quote(無料) → (2) quote tx(β支払い)
async function doQuote(){
  if (!actor.actorId) return
  doing.value = true
  try {
    // 1) 引用投稿を作る（無料）
    await api.post(`/api/posts/${props.post.postId}/quote`, {
      actorId: actor.actorId, postId: props.post.postId, requestId: newRequestId()
    })
    // 2) β支払い（引用取引）
    await api.post(`/api/posts/${props.post.postId}/quote/tx`, {
      actorId: actor.actorId, postId: props.post.postId, requestId: newRequestId()
    })
    bus.emit('tx:done')
    await fetchActions()
    emit('need-refresh') // 新しい引用投稿を先頭に持ってくる
  } catch(e) {
    alert(`${e.code || 'ERROR'}: ${e.message || ''}`)
  } finally {
    doing.value = false
  }
}

onMounted(fetchActions)
watch(() => actor.actorId, fetchActions)
</script>

<style scoped>
.muted { color: var(--muted); }
</style>
