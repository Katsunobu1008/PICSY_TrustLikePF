<!-- frontend-vue/src/components/PostTimeline.vue -->
<!-- 役割: 最新フィード。ロード/空/エラーの状態表示も追加。 -->
<template>
  <div class="column">
    <div v-if="loading" class="skeleton" />
    <div v-else-if="error" class="err">Failed to load timeline.</div>
    <div v-else-if="!posts.length" class="muted">No posts yet.</div>
    <PostCard v-for="p in posts" :key="p.postId" :post="p" @need-refresh="refresh" />
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

<style scoped>
.column{ display:flex; flex-direction:column; gap:12px }
.skeleton{ height:120px; border:1px solid var(--border); border-radius:10px; background:linear-gradient(90deg,#f5f5f5,#fafafa,#f5f5f5); animation:p 1.2s linear infinite; }
@keyframes p{ 0%{background-position:-200px 0} 100%{background-position:200px 0} }
.err{ color:#b91c1c }
.muted{ color:var(--muted) }
</style>
