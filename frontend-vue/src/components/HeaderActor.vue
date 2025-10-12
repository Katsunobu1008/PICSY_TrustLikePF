<!-- frontend-vue/src/components/HeaderActor.vue -->
<!-- 役割: URLクエリの?actor= を操作し、ストアに反映。 -->
<template>
  <div class="actor">
    <input v-model="actor" placeholder="actor UUID..." />
    <button @click="apply">Use Actor</button>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { setActor } from '../stores/power'

const route = useRoute()
const router = useRouter()
const actor = ref(route.query.actor || '')

watch(() => route.query.actor, (val) => {
  actor.value = val || ''
  setActor(actor.value || null)
}, { immediate: true })

function apply(){
  const q = { ...route.query }
  if (actor.value) q.actor = actor.value
  else delete q.actor
  router.replace({ query: q })
}
</script>

<style scoped>
.actor { display:flex; gap:8px; align-items:center; }
input { width: 340px; }
</style>
