import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const APP_NAME = 'Subscription Monitor'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { public: true, title: 'Вход' }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/RegisterView.vue'),
    meta: { public: true, title: 'Регистрация' }
  },
  {
    path: '/',
    component: () => import('../components/AppLayout.vue'),
    children: [
      { path: '', redirect: { name: 'subscriptions' } },
      {
        path: 'subscriptions',
        name: 'subscriptions',
        component: () => import('../views/SubscriptionsView.vue'),
        meta: { title: 'Подписки' }
      },
      {
        path: 'categories',
        name: 'categories',
        component: () => import('../views/CategoriesView.vue'),
        meta: { title: 'Категории' }
      },
      {
        path: 'notifications',
        name: 'notifications',
        component: () => import('../views/NotificationsView.vue'),
        meta: { title: 'Уведомления' }
      },
      {
        path: 'statistics',
        name: 'statistics',
        component: () => import('../views/StatisticsView.vue'),
        meta: { title: 'Статистика' }
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('../views/ProfileView.vue'),
        meta: { title: 'Профиль' }
      },
      {
        path: 'users',
        name: 'users',
        component: () => import('../views/UsersView.vue'),
        meta: { adminOnly: true, title: 'Пользователи' }
      }
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

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · ${APP_NAME}` : APP_NAME
})

export default router
