<!-- frontend-vue/src/components/PostTimeline.vue -->
<!-- 役割: 最新フィード。ロード/空/エラーの状態表示も追加。 -->
<template>
  <div class="flex flex-col gap-5">
    <template v-if="loading">
      <div
        v-for="n in 3"
        :key="n"
        class="rounded-card border border-outline bg-surface px-6 py-6 shadow-card"
      >
        <div class="flex items-start gap-3">
          <div class="h-12 w-12 rounded-2xl bg-slate-200 animate-pulse" />
          <div class="flex-1 space-y-3">
            <div class="h-3 w-32 rounded-full bg-slate-200 animate-pulse" />
            <div class="h-3 w-24 rounded-full bg-slate-200 animate-pulse" />
          </div>
        </div>
        <div class="mt-5 space-y-3">
          <div class="h-3 w-full rounded-full bg-slate-200/80 animate-pulse" />
          <div class="h-3 w-2/3 rounded-full bg-slate-200/80 animate-pulse" />
        </div>
        <div class="mt-6 flex gap-3">
          <div class="h-9 w-24 rounded-full bg-slate-200 animate-pulse" />
          <div class="h-9 w-32 rounded-full bg-slate-200 animate-pulse" />
        </div>
      </div>
    </template>

    <div
      v-else-if="error"
      class="rounded-card border border-rose-200 bg-rose-50 px-5 py-4 text-sm text-rose-700 shadow-card"
    >
      <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div class="space-y-1">
          <p>タイムラインの取得に失敗しました。接続を確認して再試行してください。</p>
          <p class="text-xs text-rose-500/70">{{ errorMessage }}</p>
        </div>
        <button
          type="button"
          class="inline-flex items-center justify-center rounded-full border border-rose-300 bg-white px-4 py-2 text-sm font-semibold text-rose-600 transition hover:bg-rose-100"
          @click="refresh({ forceSkeleton: false })"
        >
          再試行
        </button>
      </div>
    </div>

    <div
      v-else-if="!posts.length"
      class="rounded-card border border-outline bg-surface px-5 py-6 text-center text-sm text-slate-600 shadow-card"
    >
      <div class="space-y-3">
        <p class="font-semibold text-slate-900">まだ投稿がありません。</p>
        <p class="text-sm text-muted">最初の投稿をしてコミュニティを盛り上げましょう。</p>
        <div class="mt-3 flex justify-center">
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-full bg-brand px-4 py-2 text-sm font-semibold text-white transition hover:bg-brand/90"
            @click="openComposer"
          >
            <span>✏️</span>
            <span>投稿を作成</span>
          </button>
        </div>
      </div>
    </div>

    <div v-else class="space-y-4">
      <div
        v-if="refreshing"
        class="rounded-full bg-slate-100 px-4 py-2 text-center text-xs font-semibold text-muted"
      >
        フィードを更新しています…
      </div>
      <PostCard
        v-for="p in posts"
        :key="p.postId"
        :post="p"
        @need-refresh="refresh({ forceSkeleton: false })"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import api from '../lib/api'
import PostCard from './PostCard.vue'

const posts = ref([])
const loading = ref(true)
const error = ref(false)
const errorMessage = ref('')
const refreshing = ref(false)
const PAGE_SIZE = 20

async function refresh(options = {}){
  const forceSkeleton = options.forceSkeleton ?? posts.value.length === 0
  try{
    if (forceSkeleton) loading.value = true
    else refreshing.value = true
    error.value = false
    const { data } = await api.get('/posts/feed', { params: { size: PAGE_SIZE } })
    posts.value = data
  }catch(e){
    error.value = true
    errorMessage.value = e?.response?.data?.message || e?.message || '不明な理由で失敗しました。'
  }finally{
    loading.value = false
    refreshing.value = false
  }
}

function onGlobalRefresh(){ refresh() }

function openComposer(){
  window.dispatchEvent(new CustomEvent('compose:open'))
}

onMounted(() => {
  refresh({ forceSkeleton: true })
  window.addEventListener('timeline:refresh', onGlobalRefresh)
})
onUnmounted(() => window.removeEventListener('timeline:refresh', onGlobalRefresh))
</script>
