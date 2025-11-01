<!-- frontend-vue/src/components/PostCard.vue -->
<!-- 役割: 投稿カード。ハート/リツイートで取引実行。 -->
<template>
  <article
    class="group rounded-card border border-outline bg-surface px-6 py-6 shadow-card transition duration-200 hover:-translate-y-0.5 hover:shadow-2xl"
  >
    <header class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div class="flex items-start gap-3">
        <div
          class="flex h-12 w-12 items-center justify-center rounded-2xl text-base font-semibold text-brand shadow-sm"
          :style="{ background: accentColor }"
        >
          {{ initials }}
        </div>
        <div>
          <p class="text-sm font-semibold text-slate-900">Actor {{ shortId }}</p>
          <p class="text-xs text-muted">{{ formattedTimestamp }}</p>
        </div>
      </div>
      <div class="flex flex-col items-end gap-2 text-sm">
        <span
          v-if="post.royaltyRate != null"
          class="inline-flex items-center gap-2 rounded-full border border-purple-200 bg-purple-50 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-purple-700"
        >
          ρ {{ Number(post.royaltyRate).toFixed(2) }}
        </span>
        <div class="flex items-center gap-2 text-xs text-muted">
          <span class="inline-flex items-center gap-1 rounded-full bg-slate-100 px-2 py-1">
            <span class="h-2 w-2 rounded-full bg-emerald-400" />
            {{ postsSince }}
          </span>
        </div>
      </div>
    </header>

    <p class="mt-4 whitespace-pre-wrap text-base leading-relaxed text-slate-900">
      {{ post.contentText }}
    </p>

    <footer class="mt-6 space-y-3">
      <div class="flex flex-wrap items-center gap-3">
        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-full px-4 py-2 text-sm font-semibold transition focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand"
          :class="buttonClasses('like')"
          :disabled="!afford.canLike || loading.like"
          @click="like"
        >
          <template v-if="loading.like">
            <svg
              class="h-4 w-4 animate-spin"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <circle class="opacity-20" cx="12" cy="12" r="10" />
              <path d="M12 2a10 10 0 0 1 10 10" class="opacity-70" />
            </svg>
            <span>処理中…</span>
          </template>
          <template v-else>
            <span class="text-lg">❤️</span>
            <span>Like を実行</span>
          </template>
        </button>

        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-full px-4 py-2 text-sm font-semibold transition focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand"
          :class="buttonClasses('quote')"
          :disabled="!afford.canQuote || loading.quote"
          @click="quote"
        >
          <template v-if="loading.quote">
            <svg
              class="h-4 w-4 animate-spin"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <circle class="opacity-20" cx="12" cy="12" r="10" />
              <path d="M12 2a10 10 0 0 1 10 10" class="opacity-70" />
            </svg>
            <span>処理中…</span>
          </template>
          <template v-else>
            <span class="text-lg">🔁</span>
            <span>Quote で再投稿</span>
          </template>
        </button>
      </div>

      <div class="grid gap-2 rounded-2xl bg-slate-50 p-4 text-xs text-muted">
        <div class="flex items-center justify-between">
          <span>購買力 (Like)</span>
          <span class="font-semibold text-slate-900">{{ formatPower(afford.powerLike) }} / {{ formatPower(afford.neededLike) }}</span>
        </div>
        <div class="flex items-center justify-between">
          <span>購買力 (Quote)</span>
          <span class="font-semibold text-slate-900">{{ formatPower(afford.powerQuote) }} / {{ formatPower(afford.neededQuote) }}</span>
        </div>
      </div>

      <div class="grid gap-1 text-xs text-rose-500" aria-live="polite">
        <p v-if="reasonLabel('like') && !loading.like">Like不可: {{ reasonLabel('like') }}</p>
        <p v-if="reasonLabel('quote') && !loading.quote">Quote不可: {{ reasonLabel('quote') }}</p>
      </div>
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

const afford = ref({
  canLike:false,
  likeReason:'',
  powerLike:null,
  neededLike:null,
  canQuote:false,
  quoteReason:'',
  powerQuote:null,
  neededQuote:null,
})
const loading = ref({ like:false, quote:false })
const state = getState()

const shortId = computed(() => short(props.post.creatorId))
const initials = computed(() => shortId.value.slice(0, 2).toUpperCase())
const accentColor = computed(() => accentFromId(props.post.creatorId))
const formattedTimestamp = computed(() => formatTimestamp(props.post.createdAt))
const postsSince = computed(() => `投稿ID ${String(props.post.postId || '').slice(0, 6)}…`)

function short(id){ return String(id).slice(0,8) }

function accentFromId(id){
  const text = String(id || '')
  let hash = 0
  for (let i = 0; i < text.length; i += 1) hash = (hash + text.charCodeAt(i) * 31) % 360
  return `hsl(${hash}, 85%, 82%)`
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

function buttonClasses(kind){
  const loadingKey = kind === 'like' ? 'like' : 'quote'
  const affordKey = kind === 'like' ? 'canLike' : 'canQuote'
  const isLoading = loading.value[loadingKey]
  const can = afford.value[affordKey]
  if (isLoading) return 'cursor-wait border-brand/40 bg-brand/10 text-brand'
  if (!can) return 'cursor-not-allowed border-outline bg-slate-100 text-muted'
  return kind === 'like'
    ? 'border-rose-200 bg-rose-50 text-rose-600 hover:bg-rose-100'
    : 'border-emerald-200 bg-emerald-50 text-emerald-600 hover:bg-emerald-100'
}

function reasonLabel(kind){
  const reasonKey = kind === 'like' ? 'likeReason' : 'quoteReason'
  const reason = afford.value[reasonKey]
  if (!reason) return ''
  const dictionary = {
    NO_ACTOR: 'アクターを設定してください。',
    INSUFFICIENT_POWER: '購買力が不足しています。',
  }
  return dictionary[reason] || reason
}

function formatPower(value){
  if (value == null || Number.isNaN(Number(value))) return '--'
  return Number(value).toFixed(2)
}

async function loadAffordance(){
  if (!state.actor) {
    afford.value = {
      canLike:false,
      likeReason:'NO_ACTOR',
      powerLike:null,
      neededLike:null,
      canQuote:false,
      quoteReason:'NO_ACTOR',
      powerQuote:null,
      neededQuote:null,
    }
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
