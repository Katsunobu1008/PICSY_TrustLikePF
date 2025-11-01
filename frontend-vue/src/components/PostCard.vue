<!-- frontend-vue/src/components/PostCard.vue -->
<!-- 役割: 投稿カード。ハート/リツイートで取引実行。 -->
<template>
  <article
    class="group rounded-card border border-outline bg-surface px-6 py-5 shadow-card transition duration-200 hover:-translate-y-0.5 hover:shadow-2xl"
  >
    <header class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div class="flex items-center gap-4">
        <div
          class="flex h-12 w-12 items-center justify-center rounded-2xl text-lg font-semibold text-white shadow-sm"
          :style="{ background: accentColor }"
        >
          {{ initials }}
        </div>
        <div>
          <p class="text-sm font-semibold text-slate-900">Actor {{ shortId }}</p>
          <p class="text-xs text-muted">Posted {{ formattedTimestamp }}</p>
        </div>
      </div>
      <div class="flex items-center gap-3 text-sm">
        <span
          v-if="post.royaltyRate != null"
          class="inline-flex items-center gap-2 rounded-full border border-purple-200 bg-purple-50 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-purple-700"
        >
          ρ {{ Number(post.royaltyRate).toFixed(2) }}
        </span>
      </div>
    </header>
    <p class="mt-4 whitespace-pre-wrap text-base leading-relaxed text-slate-900">
      {{ post.contentText }}
    </p>

    <footer class="mt-6 flex items-center gap-3">
      <button
        type="button"
        class="flex h-11 w-12 items-center justify-center rounded-2xl border text-xl transition duration-200 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand"
        :class="actionClasses('like')"
        :disabled="!afford.canLike || loading.like"
        :title="afford.likeReason"
        aria-label="Like"
        @click="like"
      >
        <svg
          v-if="loading.like"
          class="h-5 w-5 animate-spin"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
        >
          <circle class="opacity-20" cx="12" cy="12" r="10" />
          <path d="M12 2a10 10 0 0 1 10 10" class="opacity-70" />
        </svg>
        <span v-else>❤️</span>
      </button>

      <button
        type="button"
        class="flex h-11 w-12 items-center justify-center rounded-2xl border text-xl transition duration-200 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand"
        :class="actionClasses('quote')"
        :disabled="!afford.canQuote || loading.quote"
        :title="afford.quoteReason"
        aria-label="Quote"
        @click="quote"
      >
        <svg
          v-if="loading.quote"
          class="h-5 w-5 animate-spin"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
        >
          <circle class="opacity-20" cx="12" cy="12" r="10" />
          <path d="M12 2a10 10 0 0 1 10 10" class="opacity-70" />
        </svg>
        <span v-else>🔁</span>
      </button>
    </footer>
  </article>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import api from '../lib/api'
import { getState, optimisticSpend } from '../stores/power'
import { uuidv4 } from '../lib/uuid'

const props = defineProps({ post: { type:Object, required:true } })
const emit = defineEmits(['need-refresh'])

const afford = ref({ canLike:false, likeReason:'', canQuote:false, quoteReason:'' })
const loading = ref({ like:false, quote:false })
const state = getState()

const shortId = computed(() => short(props.post.creatorId))
const initials = computed(() => shortId.value.slice(0, 2).toUpperCase())
const accentColor = computed(() => accentFromId(props.post.creatorId))
const formattedTimestamp = computed(() => formatTimestamp(props.post.createdAt))

function short(id){ return String(id).slice(0,8) }

function accentFromId(id){
  const text = String(id || '')
  let hash = 0
  for (let i = 0; i < text.length; i += 1) hash = (hash + text.charCodeAt(i) * 31) % 360
  return `hsl(${hash}, 90%, 70%)`
}

function formatTimestamp(value){
  if (!value) return 'just now'
  const dt = new Date(value)
  if (Number.isNaN(dt.getTime())) return 'just now'
  const diffMs = Date.now() - dt.getTime()
  const diffMin = Math.round(diffMs / 60000)
  if (diffMin < 1) return 'just now'
  if (diffMin < 60) return `${diffMin}m ago`
  const diffHr = Math.round(diffMin / 60)
  if (diffHr < 24) return `${diffHr}h ago`
  const diffDay = Math.round(diffHr / 24)
  if (diffDay < 7) return `${diffDay}d ago`
  return dt.toLocaleString()
}

function actionClasses(kind){
  const loadingKey = kind === 'like' ? 'like' : 'quote'
  const affordKey = kind === 'like' ? 'canLike' : 'canQuote'
  const isLoading = loading.value[loadingKey]
  const can = afford.value[affordKey]
  if (!can || isLoading) {
    return 'cursor-not-allowed border-slate-800 bg-slate-800 text-slate-500'
  }
  return kind === 'like'
    ? 'border-rose-500/40 bg-rose-500/10 text-rose-300 hover:bg-rose-500/20'
    : 'border-emerald-500/40 bg-emerald-500/10 text-emerald-300 hover:bg-emerald-500/20'
}

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
