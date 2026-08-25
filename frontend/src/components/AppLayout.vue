<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Bell,
  CreditCard,
  Moon,
  PriceTag,
  Sunny,
  SwitchButton,
  TrendCharts,
  User,
  UserFilled
} from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { useBreakpoint } from '../composables/useBreakpoint'
import { useTheme } from '../composables/useTheme'
import { userRoleLabel } from '../utils/format'
import AppLogo from './AppLogo.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const { isCompact, isMobile } = useBreakpoint()
const { isDark, toggleTheme } = useTheme()

const drawer = ref(false)

const links = computed(() =>
  [
    { to: '/subscriptions', label: 'Подписки', icon: CreditCard },
    { to: '/categories', label: 'Категории', icon: PriceTag },
    { to: '/notifications', label: 'Уведомления', icon: Bell },
    { to: '/statistics', label: 'Статистика', icon: TrendCharts },
    { to: '/users', label: 'Пользователи', icon: UserFilled, admin: true },
    { to: '/profile', label: 'Профиль', icon: User }
  ].filter((link) => !link.admin || auth.isAdmin)
)

const pageTitle = computed(() => route.meta.title ?? '')
const initials = computed(() => (auth.user?.username ?? '?').slice(0, 2).toUpperCase())

watch(
  () => route.path,
  () => {
    drawer.value = false
  }
)

async function handleLogout() {
  await auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="shell">
    <header class="topbar">
      <button
        v-if="isCompact"
        class="icon-button"
        type="button"
        aria-label="Открыть меню"
        @click="drawer = true"
      >
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <path
            d="M3 5.5h14M3 10h14M3 14.5h14"
            stroke="currentColor"
            stroke-width="1.8"
            stroke-linecap="round"
          />
        </svg>
      </button>

      <router-link v-if="!isCompact" to="/subscriptions" class="brand">
        <AppLogo :size="30" />
        <span class="brand__name">Subscription Monitor</span>
      </router-link>
      <h2 v-else class="topbar__title">{{ pageTitle }}</h2>

      <span class="topbar__spacer" />

      <button
        class="icon-button"
        type="button"
        :aria-label="isDark ? 'Включить светлую тему' : 'Включить тёмную тему'"
        @click="toggleTheme"
      >
        <el-icon :size="18">
          <Sunny v-if="isDark" />
          <Moon v-else />
        </el-icon>
      </button>

      <el-dropdown trigger="click" placement="bottom-end">
        <button class="user" type="button">
          <span class="user__avatar">{{ initials }}</span>
          <span v-if="!isMobile" class="user__name">{{ auth.user?.username }}</span>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <div class="user-card">
              <p class="user-card__name">{{ auth.user?.username }}</p>
              <p class="user-card__role">{{ userRoleLabel(auth.user?.role) }}</p>
            </div>
            <el-dropdown-item :icon="User" @click="router.push({ name: 'profile' })">
              Профиль
            </el-dropdown-item>
            <el-dropdown-item :icon="SwitchButton" divided @click="handleLogout">
              Выйти
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </header>

    <div class="body">
      <aside v-if="!isCompact" class="sidebar">
        <nav class="nav">
          <router-link
            v-for="link in links"
            :key="link.to"
            :to="link.to"
            class="nav__item"
            active-class="is-active"
          >
            <el-icon :size="18"><component :is="link.icon" /></el-icon>
            {{ link.label }}
          </router-link>
        </nav>
      </aside>

      <main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>

    <el-drawer v-model="drawer" direction="ltr" size="272px" :with-header="false">
      <div class="drawer">
        <router-link to="/subscriptions" class="brand brand--drawer">
          <AppLogo :size="32" />
          <span class="brand__name">Subscription Monitor</span>
        </router-link>
        <nav class="nav">
          <router-link
            v-for="link in links"
            :key="link.to"
            :to="link.to"
            class="nav__item"
            active-class="is-active"
          >
            <el-icon :size="18"><component :is="link.icon" /></el-icon>
            {{ link.label }}
          </router-link>
        </nav>
        <button class="nav__item nav__item--button" type="button" @click="handleLogout">
          <el-icon :size="18"><SwitchButton /></el-icon>
          Выйти
        </button>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.shell {
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 10px;
  height: calc(var(--topbar-h) + env(safe-area-inset-top));
  padding: env(safe-area-inset-top) max(14px, env(safe-area-inset-right)) 0
    max(14px, env(safe-area-inset-left));
  background: var(--topbar-bg);
  backdrop-filter: saturate(180%) blur(14px);
  -webkit-backdrop-filter: saturate(180%) blur(14px);
  border-bottom: 1px solid var(--border);
}

.topbar__spacer {
  flex: 1;
}

.topbar__title {
  font-size: 17px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--ink);
  font-weight: 600;
  letter-spacing: -0.01em;
}

.brand:hover {
  text-decoration: none;
}

.brand__name {
  font-size: 15px;
  white-space: nowrap;
}

.brand--drawer {
  padding: 6px 10px 14px;
}

.icon-button {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  flex: none;
  border: 1px solid transparent;
  border-radius: var(--r-sm);
  background: transparent;
  color: var(--ink-2);
  cursor: pointer;
  transition: background-color 0.15s var(--ease), color 0.15s var(--ease);
}

.icon-button:hover {
  background: var(--surface-2);
  color: var(--ink);
}

.user {
  display: flex;
  align-items: center;
  gap: 9px;
  height: 38px;
  padding: 0 10px 0 4px;
  border: 1px solid transparent;
  border-radius: var(--r-full);
  background: transparent;
  color: var(--ink);
  font: inherit;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.15s var(--ease);
}

.user:hover {
  background: var(--surface-2);
}

.user__avatar {
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--brand-gradient);
  color: #05231c;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.user__name {
  font-size: 14px;
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-card {
  padding: 6px 12px 10px;
  border-bottom: 1px solid var(--border);
  margin-bottom: 4px;
}

.user-card__name {
  font-size: 14px;
  font-weight: 600;
}

.user-card__role {
  font-size: 12px;
  color: var(--ink-3);
}

.body {
  flex: 1;
  display: flex;
  min-height: 0;
}

.sidebar {
  position: sticky;
  top: calc(var(--topbar-h) + env(safe-area-inset-top));
  align-self: flex-start;
  width: var(--sidebar-w);
  flex: none;
  height: calc(100dvh - var(--topbar-h) - env(safe-area-inset-top));
  overflow-y: auto;
  padding: 16px 12px;
  background: var(--surface);
  border-right: 1px solid var(--border);
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav__item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--r-sm);
  color: var(--ink-2);
  font-size: 14px;
  font-weight: 500;
  transition: background-color 0.15s var(--ease), color 0.15s var(--ease);
}

.nav__item:hover {
  background: var(--surface-2);
  color: var(--ink);
  text-decoration: none;
}

.nav__item.is-active {
  background: color-mix(in srgb, var(--el-color-primary) 13%, transparent);
  color: var(--el-color-primary);
  font-weight: 600;
}

.nav__item--button {
  width: 100%;
  border: none;
  background: transparent;
  font-family: inherit;
  cursor: pointer;
  text-align: left;
}

.drawer {
  display: flex;
  flex-direction: column;
  gap: 2px;
  height: 100%;
  padding-top: env(safe-area-inset-top);
}

.drawer .nav {
  flex: 1;
}

.main {
  flex: 1;
  min-width: 0;
  padding: 26px max(28px, env(safe-area-inset-right)) 48px max(28px, env(safe-area-inset-left));
}

@media (max-width: 1023px) {
  .main {
    padding: 18px max(16px, env(safe-area-inset-right))
      calc(40px + env(safe-area-inset-bottom)) max(16px, env(safe-area-inset-left));
  }
}
</style>
