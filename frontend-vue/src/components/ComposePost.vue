<!-- frontend-vue/src/components/ComposePost.vue -->
<!-- 役割: フィードの投稿導線。モーダルを開いて投稿体験を開始する -->
<template>
  <section class="rounded-card bg-surface p-5 shadow-card">
    <header class="flex items-start justify-between gap-4">
      <div class="flex items-start gap-3">
        <div class="flex h-12 w-12 items-center justify-center rounded-full bg-brand/10 text-base font-semibold text-brand">
          {{ actorInitials }}
        </div>
        <div>
          <p class="text-sm font-semibold text-slate-900">
            {{ actor ? `Actor ${short(actor)}` : 'アクター未設定' }}
          </p>
          <p class="text-xs text-muted">
            {{ actor ? '今のアイデアを共有するとフィードが即時に更新されます。' : 'トップバーからアクターを設定すると投稿できます。' }}
          </p>
        </div>
      </div>
      <button
        type="button"
        class="rounded-full bg-brand px-4 py-2 text-sm font-semibold text-white transition hover:bg-brand/90 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand"
        :class="{ 'opacity-60': !actor }"
        :disabled="!actor"
        @click="openComposer"
      >
        投稿を作成
      </button>
    </header>

    <button
      type="button"
      class="mt-5 flex w-full items-center gap-3 rounded-full border border-outline bg-white px-5 py-3 text-left text-sm text-slate-600 transition hover:bg-slate-100"
      :class="{ 'cursor-not-allowed opacity-60': !actor }"
      :disabled="!actor"
      @click="openComposer"
    >
      <span class="text-muted">{{ actor ? '今どんなことを共有しますか？' : '投稿するにはアクターを設定してください' }}</span>
    </button>

    <div class="mt-4 flex flex-wrap items-center gap-2 text-xs text-muted">
      <button
        type="button"
        class="inline-flex items-center gap-2 rounded-full border border-outline px-3 py-1.5 transition hover:bg-slate-100"
        :disabled="!actor"
      >
        ✏️ テキスト
      </button>
      <button
        type="button"
        class="inline-flex items-center gap-2 rounded-full border border-outline px-3 py-1.5 transition hover:bg-slate-100"
        :disabled="!actor"
      >
        🖼 メディア
      </button>
      <button
        type="button"
        class="inline-flex items-center gap-2 rounded-full border border-outline px-3 py-1.5 transition hover:bg-slate-100"
        :disabled="!actor"
      >
        📎 添付
      </button>
    </div>

    <p v-if="toast" class="mt-5 rounded-full bg-emerald-50 px-4 py-2 text-center text-xs font-semibold text-emerald-600">
      {{ toast }}
    </p>

    <PostComposerModal
      v-if="isModalOpen"
      :actor="actor"
      :default-royalty="DEFAULT_ROYALTY"
      @close="closeComposer"
      @posted="handlePosted"
      @needs-actor="handleNeedsActor"
    />
  </section>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import PostComposerModal from './compose/PostComposerModal.vue'
import { getState } from '../stores/power'

const state = getState()
const actor = computed(() => state.actor)
const actorInitials = computed(() => (actor.value ? String(actor.value).slice(0, 2).toUpperCase() : '?'))

const isModalOpen = ref(false)
const toast = ref('')
const DEFAULT_ROYALTY = 0.7
let toastTimer = null

function short(id) {
  return id ? String(id).slice(0, 8) : ''
}

function openComposer() {
  if (!actor.value) {
    handleNeedsActor()
    return
  }
  isModalOpen.value = true
}

function closeComposer() {
  isModalOpen.value = false
}

function setToast(message) {
  toast.value = message
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    toast.value = ''
  }, 3000)
}

function handlePosted() {
  setToast('フィードに投稿しました。')
  window.dispatchEvent(new CustomEvent('timeline:refresh'))
  closeComposer()
}

function handleNeedsActor() {
  if (!actor.value) setToast('投稿するにはアクターを設定してください。')
}

function handleGlobalCompose() {
  openComposer()
}

onMounted(() => {
  window.addEventListener('compose:open', handleGlobalCompose)
})

onUnmounted(() => {
  window.removeEventListener('compose:open', handleGlobalCompose)
  if (toastTimer) clearTimeout(toastTimer)
})
</script>
