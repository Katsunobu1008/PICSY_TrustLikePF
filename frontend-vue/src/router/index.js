// frontend-vue/src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import AdminView from '../views/AdminView.vue'
import DashboardPlaceholderView from '../views/DashboardPlaceholderView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomeView },
    { path: '/dashboard', component: DashboardPlaceholderView },
    { path: '/admin', component: AdminView },
  ],
})

export default router
