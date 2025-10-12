// frontend-vue/src/stores/actor.js
// 役割: URLクエリ ?actor=<UUID> をソースオブトゥルースにする。
// - 無効な値なら空にする（上位でフォールバックする余地を残す）
import { defineStore } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { computed } from 'vue'

export const useActorStore = defineStore('actor', () => {
  const route = useRoute()
  const router = useRouter()

  const actorId = computed(() => {
    const a = route.query.actor
    return typeof a === 'string' && a.length >= 8 ? a : ''
  })

  function setActorQuery(newId) {
    router.replace({ query: { ...route.query, actor: newId } })
  }

  return { actorId, setActorQuery }
})
