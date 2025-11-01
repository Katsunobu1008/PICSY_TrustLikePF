// frontend-vue/src/main.js
// 役割: Vueアプリ起動。Pinia/Router を組み込み。URLの ?actor からストア初期化。
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './assets/main.css'

import { rememberActors, setActor, startPolling } from './stores/power'
import api from './lib/api'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')

async function bootstrapActorContext() {
  await router.isReady()
  const current = router.currentRoute.value
  const queryActor = typeof current.query.actor === 'string' ? current.query.actor : null
  if (queryActor) {
    setActor(queryActor)
    return
  }

  try {
    const { data } = await api.get('/v1/dashboard/active-users')
    rememberActors(data?.users)
    const first = data?.users?.[0]?.userId
    if (first) {
      const firstName = data?.users?.[0]?.name || null
      setActor(first, firstName)
      router.replace({
        path: current.path,
        query: { ...current.query, actor: first },
      })
    }
  } catch (error) {
    console.warn('failed to bootstrap actor context', error)
  }
}

bootstrapActorContext()

// 購買力の定期ポーリング開始
startPolling()

// 以降、ルーターのクエリ変更で ?actor が変わったらストアへ反映
router.afterEach((to) => {
  const a = to.query.actor
  if (typeof a === 'string') setActor(a)
})
