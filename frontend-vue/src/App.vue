<!-- frontend-vue/src/App.vue -->
<!-- 役割: フレーム。上部のヘッダバー + RouterView -->
<template>
  <div class="app">
    <HeaderBar />
    <main class="container">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import HeaderBar from './components/HeaderBar.vue'
</script>

<style>
.app { min-height: 100vh; background: var(--bg); color: var(--fg); }
.container { max-width: 1080px; margin: 0 auto; padding: 16px; }
</style>
