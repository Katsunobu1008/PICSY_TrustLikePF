<!-- frontend-vue/src/App.vue -->
<!-- 役割: ルートレイアウト。上部に TopNav を配置。 -->
<template>
  <div class="shell">
    <TopNav />
    <main class="page">
      <router-view/>
    </main>
  </div>
</template>

<script setup>
import TopNav from './components/TopNav.vue'
</script>

<style>
:root{ --border:#e5e7eb; --muted:#6b7280 }
*{ box-sizing:border-box }
html,body,#app{ height:100%; margin:0 }
.shell{ min-height:100%; display:flex; flex-direction:column }
.page{ flex:1; max-width: 920px; width:100%; margin:0 auto; padding:16px }
</style>
