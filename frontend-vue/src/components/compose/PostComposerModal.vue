<!-- frontend-vue/src/components/compose/PostComposerModal.vue -->
<!-- 役割: 投稿モーダル。アクター情報を表示しつつ本文とロイヤリティを設定する -->
<template>
  <Teleport to="body">
    <div class="fixed inset-0 z-50 flex items-start justify-center overflow-y-auto px-4 py-10">
      <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" @click="requestClose('backdrop')" />
      <div
        ref="dialogRef"
        class="relative z-10 w-full max-w-xl rounded-3xl bg-white p-6 shadow-2xl"
        role="dialog"
        aria-modal="true"
      >
        <header class="flex items-start justify-between gap-3">
          <div>
            <p class="text-lg font-semibold text-slate-900">投稿を作成</p>
            <p class="text-xs text-muted">ロイヤリティを設定してフィードへ共有します。</p>
          </div>
          <button
            type="button"
            class="rounded-full p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
            :disabled="busy"
            aria-label="モーダルを閉じる"
            @click="requestClose('button')"
          >
            ✕
          </button>
        </header>

        <form class="mt-5 space-y-5" @submit.prevent="submit">
          <div class="flex items-center gap-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-full bg-brand/10 text-sm font-semibold text-brand">
              {{ actorInitials }}
            </div>
            <div>
              <p class="text-sm font-semibold text-slate-900">
                {{ actor ? `Actor ${short(actor)}` : 'アクター未設定' }}
              </p>
              <p class="text-xs text-muted">
                {{ actor ? '投稿は即座にフィードへ反映されます。' : '投稿するにはアクターを設定してください。' }}
              </p>
            </div>
          </div>

          <textarea
            ref="textareaRef"
            v-model="content"
            class="h-36 w-full rounded-2xl border border-outline px-4 py-3 text-base text-slate-800 placeholder:text-muted focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/40"
            placeholder="今どんなアイデアを共有しますか？"
          />

          <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
            <label class="flex flex-col gap-2 text-xs font-semibold uppercase tracking-wide text-muted">
              ロイヤリティ (0.00 - 1.00)
              <div class="flex items-center gap-3 text-slate-600">
                <input
                  v-model.number="royalty"
                  type="range"
                  min="0"
                  max="1"
                  step="0.01"
                  class="w-40 accent-brand"
                />
                <input
                  v-model.number="royalty"
                  type="number"
                  min="0"
                  max="1"
                  step="0.01"
                  class="w-20 rounded-xl border border-outline px-3 py-2 text-sm text-slate-800 focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/30"
                />
                <span class="text-[11px] text-muted">推奨値 0.70</span>
              </div>
            </label>
            <p class="text-xs text-muted">ロイヤリティは Like / Quote で自動精算されます。</p>
          </div>

          <div class="flex items-center gap-3 text-xs">
            <div class="relative h-2 flex-1 overflow-hidden rounded-full bg-slate-200">
              <div
                class="absolute inset-y-0 left-0 rounded-full transition-all duration-200"
                :class="progressClass"
                :style="progressStyle"
              />
            </div>
            <span :class="charClass">{{ charCount }} / {{ SUGGESTED_LENGTH }}</span>
          </div>

          <div class="flex justify-end gap-3 pt-2">
            <button
              type="button"
              class="rounded-full border border-outline px-4 py-2 text-sm font-semibold text-slate-600 transition hover:bg-slate-100"
              :disabled="busy"
              @click="requestClose('cancel')"
            >
              キャンセル
            </button>
            <button
              type="submit"
              class="inline-flex items-center justify-center rounded-full bg-brand px-5 py-2 text-sm font-semibold text-white transition hover:bg-brand/90 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand"
              :class="{ 'opacity-60': !canSubmit || busy }"
              :disabled="!canSubmit || busy"
            >
              <svg
                v-if="busy"
                class="mr-2 h-4 w-4 animate-spin"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <circle class="opacity-20" cx="12" cy="12" r="10" />
                <path d="M12 2a10 10 0 0 1 10 10" class="opacity-70" />
              </svg>
              <span>{{ busy ? '投稿中…' : '投稿する' }}</span>
            </button>
          </div>

          <p v-if="msg" :class="statusClass" class="text-sm font-semibold">
            {{ msg }}
          </p>
        </form>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import api from '../../lib/api'

const props = defineProps({
  actor: { type: String, default: null },
  defaultRoyalty: { type: Number, default: 0.7 },
})

const emit = defineEmits(['close', 'posted', 'needs-actor'])

const content = ref('')
const royalty = ref(props.defaultRoyalty)
const busy = ref(false)
const ok = ref(false)
const msg = ref('')
const textareaRef = ref(null)
const dialogRef = ref(null)
let closeTimer = null

const SUGGESTED_LENGTH = 280

const trimmedContent = computed(() => content.value.trim())
const charCount = computed(() => trimmedContent.value.length)
const charClass = computed(() => {
  if (charCount.value <= 160) return 'text-brand'
  if (charCount.value <= SUGGESTED_LENGTH) return 'text-amber-500'
  return 'text-rose-500'
})
const progressClass = computed(() => {
  if (charCount.value <= 160) return 'bg-brand'
  if (charCount.value <= SUGGESTED_LENGTH) return 'bg-amber-400'
  return 'bg-rose-400'
})
const progressStyle = computed(() => ({ width: `${Math.min(100, (charCount.value / SUGGESTED_LENGTH) * 100)}%` }))

const canSubmit = computed(
  () => Boolean(props.actor) && trimmedContent.value.length > 0 && Number.isFinite(royalty.value) && royalty.value >= 0 && royalty.value <= 1
)

const statusClass = computed(() => (ok.value ? 'text-emerald-600' : 'text-rose-600'))
const actorInitials = computed(() => (props.actor ? String(props.actor).slice(0, 2).toUpperCase() : '?'))

watch(
  () => props.defaultRoyalty,
  (val) => {
    if (Number.isFinite(val)) royalty.value = val
  }
)

watch(
  () => props.actor,
  (val) => {
    if (!val) emit('needs-actor')
  },
  { immediate: true }
)

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
  nextTick(() => {
    textareaRef.value?.focus()
  })
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
  if (closeTimer) clearTimeout(closeTimer)
})

function handleKeydown(event) {
  if (event.key === 'Escape') requestClose('escape')
}

function short(id) {
  return id ? String(id).slice(0, 8) : ''
}

function requestClose(reason) {
  if (busy.value) return
  emit('close', { reason })
}

async function submit() {
  if (!canSubmit.value) {
    ok.value = false
    msg.value = '投稿するにはアクターと本文が必要です。'
    emit('needs-actor')
    return
  }

  try {
    busy.value = true
    msg.value = ''
    const body = {
      creatorId: props.actor,
      contentText: trimmedContent.value,
      royaltyRate: Number(royalty.value),
      parentPostId: null,
      mediaKeys: [],
    }
    await api.post('/posts', body)
    ok.value = true
    msg.value = '投稿が完了しました。'
    content.value = ''
    emit('posted')
    closeTimer = setTimeout(() => requestClose('success'), 900)
  } catch (error) {
    ok.value = false
    const detail = error?.response?.data?.message || error?.message || '理由不明のエラー'
    msg.value = `投稿に失敗しました: ${detail}`
  } finally {
    busy.value = false
  }
}
</script>
