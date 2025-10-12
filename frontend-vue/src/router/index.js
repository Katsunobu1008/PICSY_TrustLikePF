// frontend-vue/src/router/index.js
// 役割: ルーティングと、URLクエリ ?actor=UUID -> ストアへのセット

import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import AboutView from '../views/AboutView.vue'
import { setActor, startPolling } from '../stores/power'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/about', name: 'about', component: AboutView },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../components/admin/AdminPanel.vue'),
    },
  ],
})

router.beforeEach((to, from, next) => {
  const actor = to.query.actor
  if (actor) setActor(actor)
  startPolling()
  next()
})

export default router
