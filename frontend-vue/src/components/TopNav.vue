<!-- frontend-vue/src/components/TopNav.vue -->
<!-- 役割: 上部バー。?actor= の編集、現在パワー表示、Admin への導線 -->
<template>
  <header class="hdr">
    <div class="brand">PICSY • TrustLike</div>

    <div class="actor">
      <input class="in" v-model="local" placeholder="actor UUID (URL query)" />
      <button class="btn" @click="apply">Use</button>
      <small class="muted">URL: ?actor={{ actor || '(unset)' }}</small>
    </div>

    <div class="right">
      <div v-if="actor" class="power">⚡ {{ (state.power ?? 0).toFixed(6) }}</div>
      <router-link class="btn ghost" to="/admin">Admin</router-link>
    </div>
  </header>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getState } from '../stores/power'

const route = useRoute()
const router = useRouter()
const state = getState()

const actor = computed(() => state.actor)
const local = ref(route.query.actor || '')

// ルートが変わったら入力欄も追従
watch(() => route.query.actor, v => (local.value = v || ''))

function apply(){
  const q = { ...route.query }
  if (local.value) q.actor = local.value.trim()
  else delete q.actor
  router.replace({ query: q })
}
</script>

<style scoped>
.hdr{ position:sticky;top:0;z-index:10; display:grid; grid-template-columns: 220px 1fr auto; gap:12px;
      align-items:center; padding:10px 16px; border-bottom:1px solid var(--border); background:#fff; }
.brand{ font-weight:800; letter-spacing:.2px; }
.actor{ display:flex; gap:8px; align-items:center; }
.in{ width:340px; padding:6px 8px; border:1px solid var(--border); border-radius:6px; }
.btn{ padding:6px 10px; border:1px solid var(--border); border-radius:6px; background:#111827; color:#fff; }
.btn.ghost{ background:transparent; color:#111827 }
.muted{ color:var(--muted) }
.right{ display:flex; gap:10px; align-items:center }
.power{ font-weight:700 }
</style>
