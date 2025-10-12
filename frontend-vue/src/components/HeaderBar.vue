<!-- frontend-vue/src/components/HeaderBar.vue -->
<!-- 役割: 上部バー。アクターのURLクエリ編集、PowerBadge 表示、管理ボタン -->
<template>
  <header class="hdr">
    <div class="hdr__left">
      <strong>PICSY</strong>
    </div>
    <div class="hdr__center row">
      <input class="input" v-model="localActor" placeholder="actor UUID (URL query)" />
      <button class="btn" @click="apply">Use Actor</button>
      <small class="muted">URL: ?actor={{actorId||'(unset)'}} </small>
    </div>
    <div class="hdr__right row">
      <PowerBadge v-if="actorId" :actor-id="actorId" />
      <router-link class="btn" to="/admin">Admin</router-link>
    </div>
  </header>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useActorStore } from '../stores/actor'
import PowerBadge from './PowerBadge.vue'

const route = useRoute()
const actor = useActorStore()

const actorId = actor.actorId
const localActor = ref(actorId.value)

watch(() => route.query.actor, (v) => { localActor.value = v || '' })

function apply() {
  if (!localActor.value) return
  actor.setActorQuery(localActor.value.trim())
}
</script>

<style scoped>
.hdr {
  position: sticky; top: 0; z-index: 10;
  background: var(--card); border-bottom: 1px solid var(--border);
  display: grid; grid-template-columns: 160px 1fr 340px; gap: 16px;
  align-items: center; padding: 10px 16px;
}
.hdr__left { font-weight: 800; }
.muted { color: var(--muted); }
</style>
