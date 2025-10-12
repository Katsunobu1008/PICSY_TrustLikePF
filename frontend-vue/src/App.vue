<!-- frontend-vue/src/App.vue -->
<!-- 役割: ルートレイアウト。ヘッダにアクターと購買力を表示、右上にAdminリンク。 -->
<template>
  <div class="shell">
    <header class="bar">
      <div class="brand">PICSY • TrustLike MVP</div>
      <div class="grow"></div>
      <div class="actor" v-if="actor">
        actor: <code>{{ short(actor) }}</code>
        <span class="power">power: {{ power.purchasingPower.toFixed(4) }}</span>
      </div>
      <router-link to="/admin">Admin</router-link>
    </header>
    <main class="page">
      <router-view/>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { getState } from './stores/power'

const power = getState()
const actor = computed(() => power.actor)
function short(id){ return String(id).slice(0,8) }
</script>

<style>
:root{
  --border: #d0d4d9;
  --muted:  #6b7280;
}
* { box-sizing: border-box; }
html, body, #app { height: 100%; margin: 0; }
.shell { min-height: 100%; display:flex; flex-direction:column; }
.bar { display:flex; align-items:center; gap:16px; padding:10px 16px; border-bottom:1px solid var(--border); }
.brand { font-weight: 600; }
.grow { flex:1 }
.page { flex:1; max-width: 900px; width:100%; margin: 0 auto; padding: 16px; }
.actor { color:#374151; display:flex; gap:10px; align-items:center; }
.actor code{ background:#f3f4f6; padding:2px 6px; border-radius:4px; }
.power { color:#111827; font-weight:600; }
</style>
