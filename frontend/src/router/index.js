import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/RegisterView.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('../components/AppLayout.vue'),
    children: [
      { path: '', redirect: { name: 'subscriptions' } },
      { path: 'subscriptions', name: 'subscriptions', component: () => import('../views/SubscriptionsView.vue') },
      { path: 'categories', name: 'categories', component: () => import('../views/CategoriesView.vue') },
      { path: 'notifications', name: 'notifications', component: () => import('../views/NotificationsView.vue') },
      { path: 'statistics', name: 'statistics', component: () => import('../views/StatisticsView.vue') },
      { path: 'profile', name: 'profile', component: () => import('../views/ProfileView.vue') },
      { path: 'users', name: 'users', component: () => import('../views/UsersView.vue'), meta: { adminOnly: true } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (!auth.loaded) {
    await auth.fetchUser()
  }
  if (to.meta.public) {
    return auth.isAuthenticated ? { name: 'subscriptions' } : true
  }
  if (!auth.isAuthenticated) {
    return { name: 'login' }
  }
  if (to.meta.adminOnly && !auth.isAdmin) {
    return { name: 'subscriptions' }
  }
  return true
})

export default router
