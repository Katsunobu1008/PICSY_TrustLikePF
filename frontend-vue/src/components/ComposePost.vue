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
            {{ actorName || (actor ? `Actor ${short(actor)}` : 'アクター未設定') }}
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
        @click="openComposer"
      >
        投稿を作成
      </button>
    </header>

    <button
      type="button"
      class="mt-5 flex w-full items-center gap-3 rounded-full border border-outline bg-white px-5 py-3 text-left text-sm text-slate-600 transition hover:bg-slate-100"
      :class="{ 'opacity-60': !actor }"
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
      :actor-name="actorName"
      :default-royalty="DEFAULT_ROYALTY"
      @close="closeComposer"
      @posted="handlePosted"
      @needs-actor="handleNeedsActor"
    />
  </section>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PostComposerModal from './compose/PostComposerModal.vue'
import api from '../lib/api'
import { getState, rememberActors, setActor as setActorContext } from '../stores/power'

const state = getState()
const actor = computed(() => state.actor)
const actorName = computed(() => state.actorName || (actor.value ? state.directory?.[actor.value] : null))
const actorInitials = computed(() => {
  if (actorName.value) return actorName.value.slice(0, 2).toUpperCase()
  if (actor.value) return String(actor.value).slice(0, 2).toUpperCase()
  return '?'
})

const isModalOpen = ref(false)
const toast = ref('')
const DEFAULT_ROYALTY = 0.7
let toastTimer = null
const ensuringActor = ref(false)
const route = useRoute()
const router = useRouter()

function short(id) {
  return id ? String(id).slice(0, 8) : ''
}

async function openComposer() {
  if (!isModalOpen.value) {
    isModalOpen.value = true
    await nextTick()
  }

  if (actor.value) {
    return
  }

  const ensured = await ensureActorContext()
  if (!ensured) {
    handleNeedsActor()
  }
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

async function ensureActorContext() {
  if (actor.value || ensuringActor.value) return true

  ensuringActor.value = true
  try {
    const { data } = await api.get('/v1/dashboard/active-users')
    rememberActors(data?.users)
    const fallback = data?.users?.[0]?.userId
    const fallbackName = data?.users?.[0]?.name || null
    if (!fallback) {
      setToast('アクター候補が見つかりません。トップバーから選択してください。')
      return false
    }

    setActorContext(fallback, fallbackName)
    await router.replace({
      query: { ...route.query, actor: fallback },
    })
    setToast(`アクターを自動設定しました (${fallbackName || `Actor ${short(fallback)}`})`)
    return true
  } catch (error) {
    console.warn('auto actor assignment failed', error)
    setToast('アクターの自動設定に失敗しました。トップバーから設定してください。')
    return false
  } finally {
    ensuringActor.value = false
  }
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
