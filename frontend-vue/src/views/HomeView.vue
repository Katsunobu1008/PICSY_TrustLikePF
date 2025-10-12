<!-- frontend-vue/src/views/HomeView.vue -->
<template>
  <div class="grid">
    <section>
      <ComposePost />
    </section>
    <section>
      <PostTimeline /> <!-- ← ここを修正 -->
    </section>
  </div>
</template>

<script setup>
import ComposePost from '../components/ComposePost.vue'
import PostTimeline from '../components/PostTimeline.vue'  // ← ここを修正
</script>

<style scoped>
.grid { display: grid; grid-template-columns: 380px 1fr; gap: 16px; }
@media (max-width: 960px) {
  .grid { grid-template-columns: 1fr; }
}
</style>
