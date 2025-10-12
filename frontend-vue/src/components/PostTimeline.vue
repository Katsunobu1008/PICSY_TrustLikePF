<!-- frontend-vue/src/components/PostTimeline.vue -->
<!-- 役割: 最新のフィードを読み込み、PostCard を並べる。 -->
<template>
  <div class="column">
    <PostCard v-for="p in posts" :key="p.postId" :post="p" @need-refresh="refresh" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import api from '../lib/api'
import PostCard from './PostCard.vue'

const posts = ref([])

async function refresh(){
  const { data } = await api.get('/api/posts/feed', { params: { size: 50 } })
  posts.value = data
}

function onGlobalRefresh(){ refresh() }

onMounted(() => {
  refresh()
  window.addEventListener('timeline:refresh', onGlobalRefresh)
})

onUnmounted(() => {
  window.removeEventListener('timeline:refresh', onGlobalRefresh)
})
</script>

<style scoped>
.column { display: flex; flex-direction: column; gap: 12px; }
</style>
