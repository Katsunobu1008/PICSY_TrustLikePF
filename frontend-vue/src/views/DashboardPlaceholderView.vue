<template>
  <div class="space-y-8">
    <section class="rounded-card bg-surface p-6 shadow-card">
      <header class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p class="text-xs font-semibold uppercase tracking-[0.2em] text-muted">Realtime Oversight</p>
          <h1 class="mt-1 text-2xl font-semibold text-slate-900">ダッシュボード</h1>
          <p class="mt-1 text-sm text-muted">
            アクティブなアクターの購買力・投稿状況・評価行列をリアルタイムに把握します。
          </p>
        </div>
        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-full border border-outline px-4 py-2 text-xs font-semibold text-slate-600 transition hover:bg-slate-100"
          @click="reload"
        >
          🔄 最新情報に更新
        </button>
        <button
          type="button"
          class="ml-3 inline-flex items-center gap-2 rounded-full border border-outline px-4 py-2 text-xs font-semibold text-slate-600 transition hover:bg-slate-100"
          @click="runRecovery"
        >
          ♻️ 自然回収を実行
        </button>
      </header>

      <div v-if="loading" class="mt-6 grid gap-4 sm:grid-cols-3">
        <div v-for="n in 3" :key="`placeholder-${n}`" class="h-28 rounded-2xl bg-slate-100/70 animate-pulse"></div>
      </div>

      <div
        v-else-if="error"
        class="mt-6 rounded-2xl border border-rose-200 bg-rose-50 px-5 py-4 text-sm text-rose-700"
      >
        <p class="font-semibold">ダッシュボード情報の取得に失敗しました。</p>
        <p class="mt-1 text-xs text-rose-500/70">{{ error }}</p>
      </div>

      <div v-else class="mt-6 grid gap-4 sm:grid-cols-3">
        <div class="rounded-2xl border border-outline bg-white/95 p-5">
          <p class="text-xs font-semibold uppercase tracking-widest text-muted">合計購買力</p>
          <p class="mt-3 text-2xl font-semibold text-slate-900">{{ formatNumber(totalPurchasingPower, 2) }}</p>
          <p class="mt-1 text-xs text-muted">アクティブユーザー全体の purchasing power</p>
        </div>
        <div class="rounded-2xl border border-outline bg-white/95 p-5">
          <p class="text-xs font-semibold uppercase tracking-widest text-muted">平均貢献度</p>
          <p class="mt-3 text-2xl font-semibold text-slate-900">{{ formatNumber(avgContribution, 3) }}</p>
          <p class="mt-1 text-xs text-muted">c ベクトルの平均値</p>
        </div>
        <div class="rounded-2xl border border-outline bg-white/95 p-5">
          <p class="text-xs font-semibold uppercase tracking-widest text-muted">総投稿数</p>
          <p class="mt-3 text-2xl font-semibold text-slate-900">{{ totalPosts }}</p>
          <p class="mt-1 text-xs text-muted">過去30日間の投稿合計</p>
        </div>
      </div>
    </section>

    <section v-if="!loading && !error" class="rounded-card bg-surface p-6 shadow-card">
      <header class="flex flex-col gap-1">
        <h2 class="text-xl font-semibold text-slate-900">アクティブアクター一覧</h2>
        <p class="text-sm text-muted">
          Flyway のサンプルアクターを含めた稼働中ユーザーのメトリクスです。
        </p>
      </header>

    <div class="mt-5 grid gap-6 md:grid-cols-2 xl:grid-cols-3">
        <article
          v-for="actor in orderedUsers"
          :key="actor.userId"
          class="rounded-2xl border border-outline/70 bg-white/95 p-5 transition hover:-translate-y-0.5 hover:shadow-lg"
        >
          <div class="flex items-start justify-between gap-4">
            <div class="flex items-start gap-3">
              <div class="flex h-12 w-12 items-center justify-center overflow-hidden rounded-full bg-brand/10 text-sm font-semibold text-brand">
                <img
                  v-if="avatarSrc(actor)"
                  :src="avatarSrc(actor)"
                  :alt="`${actor.handle} avatar`"
                  class="h-full w-full object-cover"
                />
                <span v-else>{{ (actor.handle || '?').slice(0, 2).toUpperCase() }}</span>
              </div>
              <div class="space-y-1">
                <p class="text-xs font-semibold uppercase tracking-wide text-muted">Actor</p>
                <p class="text-base font-semibold text-slate-900">{{ actor.handle }}</p>
                <p class="text-xs text-muted">貢献度 {{ formatNumber(actor.contribution, 3) }}</p>
              </div>
            </div>
            <div class="text-right">
              <span class="block rounded-full bg-brand/10 px-3 py-1 text-xs font-semibold text-brand">Power {{ formatNumber(actor.purchasingPower, 2) }}</span>
              <p class="mt-2 text-[11px] text-muted">投稿数 {{ actor.posts }}</p>
            </div>
          </div>

          <dl class="mt-4 grid grid-cols-2 gap-3 text-xs text-muted">
            <div>
              <dt class="font-semibold text-slate-600">Contribution (c)</dt>
              <dd class="mt-1 text-sm font-semibold text-slate-900">{{ formatNumber(actor.contribution, 3) }}</dd>
            </div>
            <div>
              <dt class="font-semibold text-slate-600">Budget</dt>
              <dd class="mt-1 text-sm font-semibold text-slate-900">{{ formatNumber(actor.budget, 2) }}</dd>
            </div>
            <div>
              <dt class="font-semibold text-slate-600">投稿数</dt>
              <dd class="mt-1 text-sm font-semibold text-slate-900">{{ actor.posts }}</dd>
            </div>
            <div>
              <dt class="font-semibold text-slate-600">受領変化 (Posts)</dt>
              <dd class="mt-1 text-sm font-semibold text-slate-900">{{ formatNumber(actor.deltaReceivedPosts, 2) }}</dd>
            </div>
            <div>
              <dt class="font-semibold text-slate-600">受領変化 (Direct)</dt>
              <dd class="mt-1 text-sm font-semibold text-slate-900">{{ formatNumber(actor.deltaReceivedDirect, 2) }}</dd>
            </div>
            <div>
              <dt class="font-semibold text-slate-600">送付変化</dt>
              <dd class="mt-1 text-sm font-semibold text-slate-900">{{ formatNumber(actor.deltaSent, 2) }}</dd>
            </div>
          </dl>
        </article>
  </div>
    </section>

    <section v-if="!loading && !error" class="rounded-card bg-surface p-6 shadow-card">
      <header class="flex flex-col gap-1">
        <h2 class="text-xl font-semibold text-slate-900">評価行列 (E)</h2>
        <p class="text-sm text-muted">アクティブアクター同士の評価スコアをグリッド表示します。</p>
      </header>

      <div class="mt-5 overflow-x-auto">
        <table class="min-w-full border-collapse border border-outline/70 text-sm">
          <thead class="bg-slate-100 text-xs uppercase tracking-wide text-muted">
            <tr>
              <th scope="col" class="border border-outline/60 px-4 py-3 text-right">貢献度</th>
              <th scope="col" class="border border-outline/60 px-4 py-3 text-right">購買力</th>
              <th scope="col" class="border border-outline/60 px-4 py-3 text-left">Evaluator \ Evaluatee</th>
              <th
                v-for="col in orderedUsers"
                :key="`col-${col.userId}`"
                class="border border-outline/60 px-4 py-3 text-right text-slate-600"
              >
                {{ col.handle }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in orderedUsers" :key="`row-${row.userId}`" class="odd:bg-white even:bg-slate-50/60">
              <td class="border border-outline/60 px-4 py-3 text-right text-slate-800">
                {{ formatNumber(row.contribution, 3) }}
              </td>
              <td class="border border-outline/60 px-4 py-3 text-right text-slate-800">
                {{ formatNumber(row.purchasingPower, 3) }}
              </td>
              <th scope="row" class="border border-outline/60 px-4 py-3 text-left font-semibold text-slate-700">
                {{ row.handle }}
              </th>
              <td
                v-for="col in orderedUsers"
                :key="`cell-${row.userId}-${col.userId}`"
                class="border border-outline/60 px-4 py-3 text-right text-slate-800"
              >
                {{ formatNumber(matrixValue(row.userId, col.userId), 3) }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import api from '../lib/api'
import { rememberActors } from '../stores/power'

const loading = ref(true)
const error = ref('')
const summary = ref(null)
const matrixRows = ref([])

const orderedUsers = computed(() => summary.value?.users ?? [])

const totalPurchasingPower = computed(() =>
  orderedUsers.value.reduce((sum, user) => sum + Number(user.purchasingPower || 0), 0)
)

const totalPosts = computed(() => orderedUsers.value.reduce((sum, user) => sum + Number(user.posts || 0), 0))

const avgContribution = computed(() => {
  if (!orderedUsers.value.length) return 0
  const total = orderedUsers.value.reduce((sum, user) => sum + Number(user.contribution || 0), 0)
  return total / orderedUsers.value.length
})

const matrixMap = computed(() => {
  const map = new Map()
  for (const entry of matrixRows.value) {
    const evaluatorId = entry.evaluatorId
    const evaluateeId = entry.evaluateeId
    if (!map.has(evaluatorId)) {
      map.set(evaluatorId, new Map())
    }
    map.get(evaluatorId).set(evaluateeId, Number(entry.value))
  }
  return map
})

const avatarModules = import.meta.glob('../assets/avatars/*.{png,jpg,jpeg,svg}', { eager: true, import: 'default' })
const avatarLookup = Object.fromEntries(
  Object.entries(avatarModules).map(([path, module]) => {
    const filename = path.split('/').pop() || ''
    const key = filename.split('.').shift()?.toLowerCase() || ''
    return [key, module]
  })
)

function avatarSrc(actor) {
  if (!actor) return null
  const idKey = actor.userId ? String(actor.userId).toLowerCase() : null
  if (idKey && avatarLookup[idKey]) return avatarLookup[idKey]
  const handleKey = actor.handle ? String(actor.handle).toLowerCase() : null
  return (handleKey && avatarLookup[handleKey]) || null
}

function matrixValue(evaluatorId, evaluateeId) {
  return matrixMap.value.get(evaluatorId)?.get(evaluateeId) ?? 0
}

function formatNumber(value, digits = 2) {
  const number = Number(value ?? 0)
  return new Intl.NumberFormat('ja-JP', {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits,
  }).format(number)
}

async function loadDashboard() {
  loading.value = true
  error.value = ''
  try {
    const [summaryRes, matrixRes] = await Promise.all([
      api.get('/v1/dashboard/evaluation-summary'),
      api.get('/v1/dashboard/eval-matrix'),
    ])
    rememberActors(summaryRes.data?.users)
    summary.value = {
      ...summaryRes.data,
      // summary の users は order に基づき並び替えておく
      users: alignUsers(summaryRes.data.order, summaryRes.data.users ?? []),
    }
    matrixRows.value = matrixRes.data?.rows ?? []
  } catch (err) {
    error.value = err?.response?.data?.message || err?.message || '不明なエラーが発生しました。'
  } finally {
    loading.value = false
  }
}

function alignUsers(order = [], users = []) {
  if (!Array.isArray(order) || !order.length) return users
  const byId = new Map(users.map((u) => [u.userId, u]))
  return order
    .map((id) => byId.get(id))
    .filter(Boolean)
}

function reload() {
  loadDashboard()
}

async function runRecovery(){
  try{
    // trigger backend recovery job
    await api.post('/admin/recover')
    // refresh dashboard data to reflect changes
    await loadDashboard()
    // small visual confirmation in console (UI will refresh values)
    console.info('Recovery run: dashboard reloaded')
  }catch(e){
    console.error('Recovery failed', e)
    // surface error in UI
    error.value = e?.response?.data?.message || e?.message || '自然回収の実行に失敗しました。'
  }
}

onMounted(() => {
  loadDashboard()
})

</script>
