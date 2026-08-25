<script setup>
import { Moon, Sunny } from '@element-plus/icons-vue'
import { useTheme } from '../composables/useTheme'
import AppLogo from './AppLogo.vue'

const { isDark, toggleTheme } = useTheme()

const highlights = [
  'Все списания — на одном экране',
  'Напоминание приходит до того, как спишут',
  'Расходы по категориям и валютам'
]
</script>

<template>
  <div class="auth">
    <aside class="auth__brand">
      <div class="auth__brand-top">
        <AppLogo :size="38" />
        <span class="auth__brand-name">Subscription Monitor</span>
      </div>
      <div>
        <p class="auth__headline">Подписки под контролем</p>
        <ul class="auth__list">
          <li v-for="item in highlights" :key="item">{{ item }}</li>
        </ul>
      </div>
      <p class="auth__note">Личный учёт подписок и регулярных платежей</p>
    </aside>

    <main class="auth__panel">
      <button
        class="auth__theme"
        type="button"
        :aria-label="isDark ? 'Включить светлую тему' : 'Включить тёмную тему'"
        @click="toggleTheme"
      >
        <el-icon :size="18">
          <Sunny v-if="isDark" />
          <Moon v-else />
        </el-icon>
      </button>

      <section class="auth__card">
        <AppLogo class="auth__card-logo" :size="44" />
        <slot />
      </section>
    </main>
  </div>
</template>

<style scoped>
.auth {
  min-height: 100dvh;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
}

.auth__brand {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 40px;
  padding: 48px;
  background: var(--brand-gradient);
  color: #05231c;
}

.auth__brand-top {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 600;
}

.auth__brand-name {
  font-size: 16px;
  letter-spacing: -0.01em;
}

.auth__headline {
  font-size: clamp(28px, 3vw, 40px);
  font-weight: 700;
  letter-spacing: -0.03em;
  line-height: 1.1;
  max-width: 14ch;
}

.auth__list {
  margin: 24px 0 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 34ch;
}

.auth__list li {
  position: relative;
  padding-left: 26px;
  font-size: 15px;
  color: rgba(5, 35, 28, 0.82);
}

.auth__list li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #05231c;
  opacity: 0.55;
}

.auth__note {
  font-size: 13px;
  color: rgba(5, 35, 28, 0.66);
}

.auth__panel {
  position: relative;
  display: grid;
  place-items: center;
  padding: 32px max(20px, env(safe-area-inset-right)) 32px max(20px, env(safe-area-inset-left));
  background: var(--bg);
}

.auth__theme {
  position: absolute;
  top: max(18px, env(safe-area-inset-top));
  right: 18px;
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border: 1px solid var(--border);
  border-radius: var(--r-sm);
  background: var(--surface);
  color: var(--ink-2);
  cursor: pointer;
  transition: color 0.15s var(--ease), border-color 0.15s var(--ease);
}

.auth__theme:hover {
  color: var(--ink);
  border-color: var(--border-strong);
}

.auth__card {
  width: 100%;
  max-width: 380px;
  padding: 28px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-md);
}

.auth__card-logo {
  display: none;
  margin: 0 auto 18px;
}

@media (max-width: 899px) {
  .auth {
    grid-template-columns: minmax(0, 1fr);
  }

  .auth__brand {
    display: none;
  }

  .auth__card-logo {
    display: block;
  }

  .auth__card {
    padding: 24px;
    box-shadow: var(--shadow-lg);
  }
}
</style>
