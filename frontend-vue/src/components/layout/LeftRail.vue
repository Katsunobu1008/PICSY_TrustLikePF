<!-- frontend-vue/src/components/layout/LeftRail.vue -->
<!-- 役割: 左サイドのナビゲーションとショートカットをまとめた固定パネル -->
<template>
  <nav class="space-y-6 text-sm">
    <div class="space-y-2">
      <div class="px-4 text-xs font-semibold uppercase tracking-wider text-muted">ナビゲーション</div>
      <router-link
        v-for="item in navItems"
        :key="item.to"
        :to="item.to"
        class="flex items-center gap-3 rounded-full px-4 py-2 font-medium transition duration-150 ease-soft"
        :class="isActive(item.to)
          ? 'bg-brand/10 text-brand shadow-subtle'
          : 'text-slate-700 hover:bg-slate-200/70'
        "
      >
        <span class="text-lg">{{ item.icon }}</span>
        <span>{{ item.label }}</span>
      </router-link>
    </div>

    <div class="space-y-3">
      <div class="px-4 text-xs font-semibold uppercase tracking-wider text-muted">ショートカット</div>
      <ul class="space-y-1 px-2">
        <li v-for="shortcut in shortcuts" :key="shortcut.label">
          <a
            :href="shortcut.href"
            class="flex items-center gap-3 rounded-xl px-3 py-2 text-slate-600 transition duration-150 ease-soft hover:bg-slate-200/70"
          >
            <span class="text-base">{{ shortcut.icon }}</span>
            <span>{{ shortcut.label }}</span>
          </a>
        </li>
      </ul>
    </div>

    <div class="rounded-card bg-surface px-4 py-5 shadow-card">
      <h3 class="text-sm font-semibold text-slate-800">サポート</h3>
      <p class="mt-2 text-xs leading-relaxed text-muted">
        PICSY • TrustLike の新UIに関するフィードバックをお待ちしています。改善提案はダッシュボードの
        <span class="font-semibold text-brand">Feedback</span> カードから送信できます。
      </p>
    </div>
  </nav>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()

const navItems = [
  { to: '/', label: 'ホーム', icon: '🏠' },
  { to: '/dashboard', label: 'ダッシュボード', icon: '📊' },
  { to: '/admin', label: '管理コンソール', icon: '🛠' },
]

const shortcuts = [
  { label: 'ヘルプセンター', icon: '❓', href: '#' },
  { label: 'リリースノート', icon: '🗒', href: '#' },
  { label: '開発者ブログ', icon: '📰', href: '#' },
]

  const isActive = (to) => route.path === to
</script>
