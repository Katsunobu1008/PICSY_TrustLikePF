<!-- frontend-vue/src/views/HomeView.vue -->
<!-- 役割: 左にCompose、右にTimeline を置く。 -->
<template>
  <div class="grid">
    <section>
      <ComposePost />
    </section>
    <section>
      <Timeline />
    </section>
  </div>
</template>

<script setup>
import ComposePost from '../components/ComposePost.vue'
import Timeline from '../components/Timeline.vue'
</script>

<style scoped>
.grid { display: grid; grid-template-columns: 380px 1fr; gap: 16px; }
@media (max-width: 960px) {
  .grid { grid-template-columns: 1fr; }
}
</style>
