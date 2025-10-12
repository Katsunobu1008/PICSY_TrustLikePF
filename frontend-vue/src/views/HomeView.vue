<!-- frontend-vue/src/views/HomeView.vue -->
<!-- 役割: 投稿フォーム + タイムラインを同一ページに -->
<template>
  <div class="stack">
    <ComposePost />
    <PostTimeline />
  </div>
</template>

<script setup>
import ComposePost from '../components/ComposePost.vue'
import PostTimeline from '../components/PostTimeline.vue'
</script>

<style scoped>
.stack{ display:flex; flex-direction:column; gap:16px; }
</style>
