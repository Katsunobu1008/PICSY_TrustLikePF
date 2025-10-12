<!-- frontend-vue/src/components/ComposePost.vue -->
<!-- 役割: 原作（無料）を作るコンポーザー。送信後はタイムライン再読込を通知 -->
<template>
  <div class="card">
    <h3 style="margin-top:0">Create Original</h3>
    <div class="row">
      <input class="input" v-model="content" placeholder="What's happening?" />
    </div>
    <div class="row" style="margin-top:8px">
      <label style="width:160px">Royalty Rate (ρ 0-1)</label>
      <input class="input" type="number" min="0" max="1" step="0.01" v-model.number="royalty" />
    </div>
    <div class="row" style="margin-top:12px;justify-content:flex-end">
      <button class="btn primary" :disabled="!canPost" @click="submit">Post (free)</button>
    </div>
    <p v-if="err" style="color:var(--danger);margin:6px 0 0">{{ err }}</p>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import api from '../lib/api'
import { useActorStore } from '../stores/actor'
import { bus } from '../stores/power'

const actor = useActorStore()
const content = ref('')
const royalty = ref(0.2)
const err = ref('')

const canPost = computed(() => content.value.trim().length > 0 && royalty.value >= 0 && royalty.value <= 1)

async function submit(){
  err.value = ''
  try {
    const body = {
      creatorId: actor.actorId,
      contentText: content.value.trim(),
      royaltyRate: royalty.value,
      parentPostId: null,
      mediaKeys: [],
    }
    await api.post('/api/posts', body)
    content.value = ''
    // タイムラインを更新指示
    bus.emit('timeline:refresh')
  } catch (e) {
    err.value = `${e.code || 'ERROR'}: ${e.message || 'Failed'}`
  }
}
</script>
