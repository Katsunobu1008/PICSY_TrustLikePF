<!-- frontend-vue/src/components/PostTimeline.vue -->
<!-- 役割: 最新フィード。ロード/空/エラーの状態表示も追加。 -->
<template>
  <div class="flex flex-col gap-5">
    <template v-if="loading">
      <div
        v-for="n in 3"
        :key="n"
        class="h-36 rounded-3xl border border-slate-800/70 bg-slate-900/70 ring-1 ring-white/5 shadow-inner shadow-slate-950/50 animate-pulse"
      />
    </template>
    <div
      v-else-if="error"
      class="rounded-3xl border border-rose-500/20 bg-rose-500/10 px-5 py-4 text-sm font-medium text-rose-100"
    >
      Failed to load timeline.
    </div>
    <div
      v-else-if="!posts.length"
      class="rounded-card border border-outline bg-surface px-5 py-6 text-center text-sm text-slate-600"
    >
      <div class="space-y-3">
        <p class="font-semibold">まだ投稿がありません。</p>
        <p class="text-sm text-muted">最初の投稿をしてコミュニティを盛り上げましょう。</p>
        <div class="mt-3 flex justify-center">
          <button
            type="button"
            class="rounded-full bg-brand px-4 py-2 text-sm font-semibold text-white hover:bg-brand/90"
            @click="$emit('open-composer') || window.dispatchEvent(new CustomEvent('compose:open'))"
          >
            投稿を作成
          </button>
        </div>
      </div>
    </div>
    <template v-else>
      <PostCard v-for="p in posts" :key="p.postId" :post="p" @need-refresh="refresh" />
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import api from '../lib/api'
import PostCard from './PostCard.vue'

const posts = ref([])
const loading = ref(true)
const error = ref(false)

async function refresh(){
  try{
    loading.value = true; error.value = false
    const { data } = await api.get('/posts/feed', { params: { size: 50 } })
    posts.value = data
  }catch{
    error.value = true
  }finally{
    loading.value = false
  }
}

function onGlobalRefresh(){ refresh() }

onMounted(() => {
  refresh()
  window.addEventListener('timeline:refresh', onGlobalRefresh)
})
onUnmounted(() => window.removeEventListener('timeline:refresh', onGlobalRefresh))
</script>
