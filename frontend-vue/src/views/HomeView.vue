<!-- frontend-vue/src/views/HomeView.vue -->
<!-- 役割: 投稿フォーム + タイムラインを同一ページに -->
<template>
  <div class="space-y-6 lg:space-y-8 xl:px-2">
    <ComposePost />
    <PostTimeline />
  </div>
</template>

<script setup>
import ComposePost from '../components/ComposePost.vue'
import PostTimeline from '../components/PostTimeline.vue'
</script>
