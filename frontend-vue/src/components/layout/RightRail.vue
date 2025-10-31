<!-- frontend-vue/src/components/layout/RightRail.vue -->
<!-- 役割: 右サイドにステータス・推奨・トレンドカードを表示 -->
<template>
  <div class="space-y-6">
    <section class="rounded-card bg-surface p-5 shadow-card">
      <header class="flex items-center justify-between">
        <div>
          <p class="text-xs font-semibold uppercase tracking-wider text-muted">現在のアクター</p>
          <h3 class="mt-1 text-base font-semibold text-slate-900">
            {{ actorLabel }}
          </h3>
        </div>
        <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-brand/10 text-brand">
          ⚡
        </div>
      </header>
      <dl class="mt-4 space-y-3 text-sm">
        <div class="flex items-center justify-between">
          <dt class="text-muted">購買力</dt>
          <dd class="font-semibold text-slate-900">{{ formattedPower }}</dd>
        </div>
        <div class="flex items-center justify-between">
          <dt class="text-muted">Eii（影響度）</dt>
          <dd class="font-semibold text-slate-900">{{ formattedEii }}</dd>
        </div>
        <div class="flex items-center justify-between">
          <dt class="text-muted">協力度係数 c</dt>
          <dd class="font-semibold text-slate-900">{{ formattedC }}</dd>
        </div>
      </dl>
      <div class="mt-4 h-2 w-full overflow-hidden rounded-full bg-slate-200">
        <div
          class="h-full rounded-full bg-brand transition-all duration-300 ease-soft"
          :style="{ width: `${powerProgress}%` }"
        />
      </div>
      <p class="mt-2 text-xs text-muted">* 購買力は 0.00 - 1.00 スケールで表示されます。</p>
    </section>

    <section class="rounded-card bg-surface p-5 shadow-card">
      <header class="flex items-center justify-between">
        <h3 class="text-sm font-semibold text-slate-900">おすすめクリエイター</h3>
        <a href="#" class="text-xs font-semibold text-brand hover:underline">すべて表示</a>
      </header>
      <ul class="mt-4 space-y-3">
        <li
          v-for="creator in recommendedCreators"
          :key="creator.id"
          class="flex items-center justify-between gap-3"
        >
          <div class="flex items-center gap-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-full bg-slate-200 text-sm font-semibold text-slate-700">
              {{ creator.initials }}
            </div>
            <div>
              <p class="text-sm font-medium text-slate-900">{{ creator.name }}</p>
              <p class="text-xs text-muted">共鳴指数 {{ creator.index }}</p>
            </div>
          </div>
          <button class="rounded-full bg-brand px-3 py-1 text-xs font-semibold text-white transition duration-150 ease-soft hover:bg-brand/90">
            フォロー
          </button>
        </li>
      </ul>
    </section>

    <section class="rounded-card bg-surface p-5 shadow-card">
      <header class="flex items-center justify-between">
        <h3 class="text-sm font-semibold text-slate-900">トレンドメモ</h3>
        <span class="text-xs text-muted">更新: {{ trendUpdatedAt }}</span>
      </header>
      <ul class="mt-3 space-y-2 text-sm text-slate-700">
        <li v-for="trend in trends" :key="trend" class="rounded-xl bg-slate-100 px-3 py-2">
          {{ trend }}
        </li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { getState } from '../../stores/power'

const state = getState()

const actorLabel = computed(() => {
  if (!state.actor) return 'アクター未設定'
  return `Actor ${String(state.actor).slice(0, 8)}`
})

const formattedPower = computed(() => (state.power ?? 0).toFixed(6))
const formattedEii = computed(() => (state.eii ?? 0).toFixed(4))
const formattedC = computed(() => (state.c ?? 0).toFixed(4))
const powerProgress = computed(() => Math.min(100, Math.round((state.power ?? 0) * 100)))

const recommendedCreators = computed(() => [
  { id: 'alice', name: 'Alice Nakamura', initials: 'AN', index: '0.87' },
  { id: 'bob', name: 'Bob Watanabe', initials: 'BW', index: '0.79' },
  { id: 'carol', name: 'Carol Ito', initials: 'CI', index: '0.75' },
])

const trends = [
  'Like取引の平均単価が 12% 上昇',
  '引用投稿が週比 +19%',
  'ガンマ値の再調整を検討',
]

const trendUpdatedAt = computed(() => {
  const now = new Date()
  const hours = now.getHours().toString().padStart(2, '0')
  const minutes = now.getMinutes().toString().padStart(2, '0')
  return `${now.getMonth() + 1}/${now.getDate()} ${hours}:${minutes}`
})
</script>
