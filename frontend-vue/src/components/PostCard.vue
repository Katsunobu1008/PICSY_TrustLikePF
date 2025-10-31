<!-- frontend-vue/src/components/PostCard.vue -->
<!-- 役割: 投稿カード。ハート/リツイートで取引実行。 -->
<template>
  <article class="bg-white border border-gray-200 rounded-xl p-4 shadow-sm transition hover:shadow-md">
    <header class="flex items-center justify-between gap-2 text-sm text-gray-500">
      <div class="font-bold text-gray-800">👤 {{ short(post.creatorId) }}</div>
      <div class="flex items-center gap-3">
        <span v-if="post.royaltyRate != null" class="text-purple-600 font-semibold">ρ {{ Number(post.royaltyRate).toFixed(2) }}</span>
        <span class="text-gray-400">{{ new Date(post.createdAt).toLocaleString() }}</span>
      </div>
    </header>

    <p class="my-3 text-gray-700 whitespace-pre-wrap">{{ post.contentText }}</p>

    <footer class="flex gap-2">
      <button
        class="flex items-center justify-center w-12 h-10 rounded-lg text-xl transition"
        :class="{
          'text-gray-400 bg-gray-100 cursor-not-allowed': !afford.canLike || loading.like,
          'text-red-500 bg-red-100 hover:bg-red-200': afford.canLike && !loading.like
        }"
        :disabled="!afford.canLike || loading.like"
        :title="afford.likeReason"
        @click="like"
      >
        <span v-if="loading.like" class="animate-spin">⏳</span>
        <span v-else>❤️</span>
      </button>

      <button
        class="flex items-center justify-center w-12 h-10 rounded-lg text-xl transition"
        :class="{
          'text-gray-400 bg-gray-100 cursor-not-allowed': !afford.canQuote || loading.quote,
          'text-green-500 bg-green-100 hover:bg-green-200': afford.canQuote && !loading.quote
        }"
        :disabled="!afford.canQuote || loading.quote"
        :title="afford.quoteReason"
        @click="quote"
      >
        <span v-if="loading.quote" class="animate-spin">⏳</span>
        <span v-else>🔁</span>
      </button>
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
/* Tailwind CSS will handle styling through utility classes in the template. */
/* This style block can be removed or kept for component-specific overrides. */
</style>
