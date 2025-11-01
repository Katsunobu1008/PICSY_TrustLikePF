<!-- frontend-vue/src/components/TopNav.vue -->
<!-- 役割: Facebookスタイルのトップバー。主要ナビとアクターPopoverを提供 -->
<template>
  <header class="sticky top-0 z-40 border-b border-outline/70 bg-surface/95 backdrop-blur shadow-subtle">
    <div class="mx-auto flex w-full max-w-[1200px] items-center gap-4 px-4 py-3 md:gap-6">
      <div class="flex items-center gap-3">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-brand/10 text-lg font-semibold text-brand">
          P
        </div>
        <div>
          <p class="text-sm font-semibold text-slate-900">PICSY • TrustLike</p>
          <p class="text-xs text-muted">Programmable royalties, captured in real-time</p>
        </div>
      </div>

      <nav class="hidden items-center gap-1 md:flex">
        <router-link
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="rounded-full px-4 py-2 text-sm font-medium transition duration-150 ease-soft"
          :class="isActiveRoute(item.to)
            ? 'bg-brand/15 text-brand'
            : 'text-slate-600 hover:bg-slate-200'
          "
          :aria-current="isActiveRoute(item.to) ? 'page' : undefined"
        >
          {{ item.label }}
        </router-link>
      </nav>

      <div class="ml-auto flex items-center gap-2 sm:gap-3">
        <div class="hidden sm:flex items-center rounded-full border border-outline bg-surface px-3 py-1.5 text-sm text-slate-600 focus-within:border-brand focus-within:ring-2 focus-within:ring-brand/30">
          <label class="sr-only" for="global-search">検索</label>
          <span class="mr-2 text-slate-400">🔍</span>
          <input
            id="global-search"
            v-model="search"
            type="search"
            placeholder="検索"
            class="w-32 bg-transparent text-sm focus:outline-none md:w-48"
            @keydown.enter.prevent="triggerSearch"
          />
        </div>

        <button
          v-for="action in quickActions"
          :key="action.key"
          type="button"
          class="hidden h-10 w-10 items-center justify-center rounded-full bg-slate-200 text-base text-slate-700 transition hover:bg-slate-300 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand md:flex"
          :title="action.label"
          @click="action.handler()"
        >
          {{ action.icon }}
        </button>

        <div class="relative">
          <button
            ref="avatarButtonRef"
            type="button"
            class="flex h-11 w-11 items-center justify-center rounded-full bg-brand text-sm font-semibold text-white shadow-subtle transition hover:brightness-95 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand"
            @click="togglePopover"
            :aria-expanded="popoverOpen"
            aria-haspopup="dialog"
          >
            {{ actorInitials }}
          </button>

          <ActorPopover
            v-if="popoverOpen"
            ref="popoverRef"
            class="absolute right-0 top-[calc(100%+0.75rem)]"
            :actor="actor"
            :local-value="local"
            :formatted-power="formattedPower"
            :formatted-eii="formattedEii"
            :formatted-c="formattedC"
            :actors="actors"
            :copy-message="copyMessage"
            @update:local-value="value => (local = value)"
            @apply="apply"
            @clear="clearActor"
            @copy="copyActor"
            @close="closePopover"
            @select="selectActor"
          />
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ActorPopover from './topnav/ActorPopover.vue'
import { getState } from '../stores/power'
import api from '../lib/api'

const route = useRoute()
const router = useRouter()
const state = getState()

const navItems = [
  { to: '/', label: 'ホーム' },
  { to: '/dashboard', label: 'ダッシュボード' },
  { to: '/admin', label: '管理' },
]

const quickActions = [
  {
    key: 'compose',
    icon: '✚',
    label: '新規投稿',
    handler: () => {
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('compose:open'))
      }
    },
  },
  {
    key: 'messages',
    icon: '💬',
    label: 'メッセージ',
    handler: () => console.info('Messages shortcut clicked'),
  },
  {
    key: 'notifications',
    icon: '🔔',
    label: '通知',
    handler: () => console.info('Notifications shortcut clicked'),
  },
]

const actor = computed(() => state.actor)
const formattedPower = computed(() => (state.power ?? 0).toFixed(6))
const formattedEii = computed(() => (state.eii ?? 0).toFixed(4))
const formattedC = computed(() => (state.c ?? 0).toFixed(4))
const actorInitials = computed(() => {
  if (!state.actor) return '?'
  return String(state.actor).slice(0, 2).toUpperCase()
})

const local = ref(route.query.actor || '')
const search = ref('')
const popoverOpen = ref(false)
const actors = ref([])
const copyMessage = ref('')
const avatarButtonRef = ref(null)
const popoverRef = ref(null)
let copyTimer = null

watch(() => route.query.actor, v => (local.value = v || ''))

watch(popoverOpen, (open) => {
  if (typeof window === 'undefined') return
  if (open) {
    document.addEventListener('click', handleOutsideClick, true)
    document.addEventListener('keydown', handleKeydown)
  } else {
    document.removeEventListener('click', handleOutsideClick, true)
    document.removeEventListener('keydown', handleKeydown)
  }
})

function isActiveRoute(to) {
  return route.path === to
}

function triggerSearch() {
  if (!search.value.trim()) return
  console.info('search keyword:', search.value.trim())
}

function togglePopover() {
  popoverOpen.value = !popoverOpen.value
}

function closePopover() {
  popoverOpen.value = false
}

function handleOutsideClick(event) {
  const target = event.target
  const popoverEl = popoverRef.value?.popover || popoverRef.value
  if (!popoverOpen.value) return
  if (popoverEl && popoverEl.contains(target)) return
  if (avatarButtonRef.value && avatarButtonRef.value.contains(target)) return
  popoverOpen.value = false
}

function handleKeydown(event) {
  if (event.key === 'Escape') popoverOpen.value = false
}

function apply() {
  const q = { ...route.query }
  if (local.value) q.actor = local.value.trim()
  else delete q.actor
  router.replace({ query: q })
  popoverOpen.value = false
}

function selectActor(id) {
  if (!id) return
  local.value = id
  apply()
}

function clearActor() {
  if (!actor.value && !local.value) return
  local.value = ''
  apply()
}

async function copyActor() {
  if (!actor.value || typeof navigator === 'undefined' || !navigator.clipboard) return
  try {
    await navigator.clipboard.writeText(actor.value)
    setCopyMessage('Copied')
  } catch (error) {
    console.warn('copy failed', error)
    setCopyMessage('Copy not available')
  }
}

function setCopyMessage(message) {
  copyMessage.value = message
  if (copyTimer) clearTimeout(copyTimer)
  copyTimer = setTimeout(() => (copyMessage.value = ''), 2000)
}

onBeforeUnmount(() => {
  if (copyTimer) clearTimeout(copyTimer)
  document.removeEventListener('click', handleOutsideClick, true)
  document.removeEventListener('keydown', handleKeydown)
})

onMounted(async () => {
  try {
    const { data } = await api.get('/v1/dashboard/active-users')
    actors.value = data?.users ?? []
  } catch (error) {
    console.warn('active users fetch failed', error)
  }
})
</script>
