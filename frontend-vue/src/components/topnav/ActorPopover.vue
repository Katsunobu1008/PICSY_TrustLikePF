<!-- frontend-vue/src/components/topnav/ActorPopover.vue -->
<!-- 役割: TopNav のアクター切替/ステータスPopover -->
<template>
  <section
    ref="popover"
    class="w-[320px] rounded-card border border-outline/60 bg-surface p-5 text-sm text-slate-700 shadow-card"
  >
    <header class="flex items-start justify-between gap-3">
      <div>
        <p class="text-xs font-semibold uppercase tracking-wider text-muted">Actor Context</p>
        <p class="mt-1 text-base font-semibold text-slate-900">
          {{ actor ? `Actor ${actor.slice(0, 8)}` : 'アクター未設定' }}
        </p>
      </div>
      <button
        type="button"
        class="rounded-full p-1 text-slate-400 transition hover:bg-slate-200 hover:text-slate-600"
        @click="$emit('close')"
        aria-label="Close actor menu"
      >
        ✕
      </button>
    </header>

    <div class="mt-4 space-y-2">
      <label class="text-xs font-semibold uppercase tracking-wide text-muted" for="actor-input">Actor UUID</label>
      <input
        id="actor-input"
        type="text"
        :value="localValue"
        placeholder="uuid-形式で入力"
        class="w-full rounded-2xl border border-outline bg-surface px-3 py-2 text-sm text-slate-800 placeholder:text-muted focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/40"
        @input="$emit('update:localValue', $event.target.value)"
      />
      <p class="text-xs text-muted">
        URL パラメータ:
        <code class="rounded-full bg-slate-100 px-2 py-0.5">?actor={{ actor || '(unset)' }}</code>
      </p>
    </div>

    <div v-if="actors.length" class="mt-5 space-y-2">
      <p class="text-xs font-semibold uppercase tracking-wide text-muted">サンプルアクター</p>
      <div class="grid gap-2">
        <button
          v-for="item in actors"
          :key="item.userId"
          type="button"
          class="flex items-center justify-between rounded-2xl border border-outline px-3 py-2 text-left text-xs transition hover:bg-slate-100"
          :class="item.userId === actor ? 'border-brand/60 bg-brand/10 text-brand' : ''"
          @click="$emit('select', item.userId)"
        >
          <span class="font-semibold text-slate-700">{{ item.name }}</span>
          <span class="text-[11px] text-muted">{{ item.userId.slice(0, 8) }}</span>
        </button>
      </div>
    </div>

    <div class="mt-4 grid grid-cols-3 gap-3 text-center text-xs">
      <div class="rounded-xl bg-slate-100 p-3">
        <p class="font-semibold text-slate-600">Power</p>
        <p class="mt-1 text-sm font-semibold text-slate-900">{{ formattedPower }}</p>
      </div>
      <div class="rounded-xl bg-slate-100 p-3">
        <p class="font-semibold text-slate-600">Eii</p>
        <p class="mt-1 text-sm font-semibold text-slate-900">{{ formattedEii }}</p>
      </div>
      <div class="rounded-xl bg-slate-100 p-3">
        <p class="font-semibold text-slate-600">c</p>
        <p class="mt-1 text-sm font-semibold text-slate-900">{{ formattedC }}</p>
      </div>
    </div>

    <div class="mt-5 flex flex-wrap items-center gap-2">
      <button
        type="button"
        class="inline-flex flex-1 items-center justify-center rounded-full bg-brand px-4 py-2 text-xs font-semibold text-white transition hover:bg-brand/90 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand"
        @click="$emit('apply')"
      >
        Use Actor
      </button>
      <button
        type="button"
        class="inline-flex items-center justify-center rounded-full border border-outline px-4 py-2 text-xs font-semibold text-slate-600 transition hover:bg-slate-200"
        :disabled="!actor"
        :class="{ 'cursor-not-allowed opacity-50': !actor }"
        @click="$emit('clear')"
      >
        Clear
      </button>
      <button
        type="button"
        class="inline-flex items-center justify-center rounded-full border border-outline px-4 py-2 text-xs font-semibold text-slate-600 transition hover:bg-slate-200"
        :disabled="!actor"
        :class="{ 'cursor-not-allowed opacity-50': !actor }"
        @click="$emit('copy')"
      >
        Copy
      </button>
    </div>

    <p v-if="copyMessage" class="mt-3 text-xs font-semibold text-emerald-500">
      {{ copyMessage }}
    </p>
  </section>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  actor: { type: String, default: null },
  localValue: { type: String, default: '' },
  formattedPower: { type: String, default: '0.000000' },
  formattedEii: { type: String, default: '0.0000' },
  formattedC: { type: String, default: '0.0000' },
  actors: {
    type: Array,
    default: () => [],
  },
  copyMessage: { type: String, default: '' },
})

defineEmits(['update:localValue', 'apply', 'clear', 'copy', 'close', 'select'])

const popover = ref(null)

// expose popover element to parent so it can handle outside click detection
defineExpose({ popover })
</script>
