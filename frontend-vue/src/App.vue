<!-- frontend-vue/src/App.vue -->
<!-- 役割: ルートレイアウト。上部に TopNav を配置。 -->
<template>
  <div class="min-h-screen bg-background font-sans text-slate-900">
    <TopNav />
    <main class="mx-auto flex w-full max-w-[1200px] flex-1 px-4 pb-12 pt-6 sm:px-6">
      <div class="flex w-full flex-col gap-6 xl:grid xl:grid-cols-[280px_minmax(0,1fr)_320px] xl:gap-6">
        <aside class="hidden xl:block">
          <LeftRail />
        </aside>

        <section class="w-full xl:px-0">
          <RouterView v-slot="{ Component }">
            <component :is="Component" />
          </RouterView>
        </section>

        <aside class="hidden xl:block">
          <RightRail />
        </aside>
      </div>
    </main>
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router'
import TopNav from './components/TopNav.vue'
import LeftRail from './components/layout/LeftRail.vue'
import RightRail from './components/layout/RightRail.vue'
</script>
