<!-- frontend-vue/src/views/AdminView.vue -->
<!-- 役割: γ表示/更新、自然回収・再計算、凍結/解除、（あれば）行列ヒートマップ -->
<template>
  <div class="stack">
    <section class="card">
      <h3 style="margin:0 0 8px">Recovery (γ)</h3>
      <GammaCard />
    </section>

    <section class="card">
      <h3 style="margin:0 0 8px">Engine Controls</h3>
      <EngineControls />
    </section>

    <section class="card">
      <h3 style="margin:0 0 8px">Freeze / Reactivate</h3>
      <FreezeUnfreezeForm />
    </section>

    <section class="card">
      <h3 style="margin:0 0 8px">Evaluation Matrix (Active-only)</h3>
      <MatrixHeatmap />
    </section>
  </div>
</template>

<script setup>
import GammaCard from '../components/admin/GammaCard.vue'
import EngineControls from '../components/admin/EngineControls.vue'
import FreezeUnfreezeForm from '../components/admin/FreezeUnfreezeForm.vue'
import MatrixHeatmap from '../components/admin/MatrixHeatmap.vue'
</script>

<style scoped>
.stack { display: grid; grid-template-columns: 1fr; gap: 16px; }
</style>
