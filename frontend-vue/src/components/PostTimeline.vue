<!-- frontend-vue/src/components/PostTimeline.vue -->
<!-- 役割: 最新のフィードを読み込み、PostCard を並べる。 -->
<template>
  <div class="column">
    <PostCard v-for="p in posts" :key="p.postId" :post="p" @need-refresh="refresh" />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import api from '../lib/api'
import PostCard from './PostCard.vue'
import { bus } from '../stores/power'

const posts = ref([])

async function refresh(){
  const { data } = await api.get('/api/posts/feed', { params: { size: 50 } })
  posts.value = data
}
function onRefresh(){ refresh() }

onMounted(() => {
  refresh()
  bus.on('timeline:refresh', onRefresh)
})
onBeforeUnmount(() => {
  bus.off('timeline:refresh', onRefresh)
})
</script>

<style scoped>
.column { display: flex; flex-direction: column; gap: 12px; }
</style>
