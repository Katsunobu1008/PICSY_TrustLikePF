// frontend-vue/src/main.js
// 役割: Vueアプリ起動。Pinia/Router を組み込み。URL ?actor= を監視して power ストアへ同期。

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './assets/base.css'

import { setActor, startPolling } from './stores/power'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')

// ルータ準備完了後に、URL ?actor= を読んでストアへセット＆ポーリング開始
router.isReady().then(() => {
  const q = router.currentRoute.value.query
  const actor = typeof q.actor === 'string' && q.actor.length > 0 ? q.actor : null
  setActor(actor)
  startPolling()
})

// URL ?actor= の変更を常に同期（アドレスバー手修正/リンク遷移でも追従）
router.afterEach((to) => {
  const a = typeof to.query.actor === 'string' ? to.query.actor : null
  setActor(a)
})
