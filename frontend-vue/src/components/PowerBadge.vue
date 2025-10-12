<!-- frontend-vue/src/components/PowerBadge.vue -->
<!-- 役割: 現在アクターの Eii, c, power を表示。tx成功イベントで即時更新、定期ポーリングも開始 -->
<template>
  <div class="card" style="display:flex;gap:12px;align-items:center;">
    <div>
      <div style="font-size:12px;color:var(--muted)">Power</div>
      <div style="font-size:18px;font-weight:700">{{ power.power.toFixed(6) }}</div>
    </div>
    <div style="font-size:12px;color:var(--muted)">
      Eii: {{ power.eii.toFixed(6) }} ・ c: {{ power.c.toFixed(6) }}
    </div>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, watch } from 'vue'
import { usePowerStore, bus } from '../stores/power'

const props = defineProps({ actorId: { type: String, required: true } })
const power = usePowerStore()

async function refresh() {
  await power.fetchPower(props.actorId)
}

onMounted(async () => {
  await refresh()
  power.startPolling(props.actorId)
})
onBeforeUnmount(() => power.stopPolling())

watch(() => props.actorId, async (id) => {
  if (!id) return
  await refresh()
  power.startPolling(id)
})

// 念のため、外部から強制リフレッシュも可能
bus.on('power:refresh', refresh)
</script>
