// frontend-vue/src/main.js
// 役割: Vueアプリ起動。Pinia/Router を組み込み。
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './assets/base.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
// frontend-vue/src/main.js 末尾の初期化
import { setActor, startPolling } from './stores/power'
import { parseActorFromUrl, onUrlActorChange } from './stores/actor'

const actor = parseActorFromUrl()
setActor(actor)
startPolling()

onUrlActorChange((uuid) => setActor(uuid))
