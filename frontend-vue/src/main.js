// frontend-vue/src/main.js
// 役割: Vueアプリ起動。Pinia/Router を組み込み。URLの ?actor からストア初期化。
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

// URL の ?actor を一度だけ反映
const params = new URLSearchParams(window.location.search)
const actor = params.get('actor')
if (actor) setActor(actor)

// 購買力の定期ポーリング開始
startPolling()

// 以降、ルーターのクエリ変更で ?actor が変わったらストアへ反映
router.afterEach((to) => {
  const a = to.query.actor
  if (typeof a === 'string') setActor(a)
})
