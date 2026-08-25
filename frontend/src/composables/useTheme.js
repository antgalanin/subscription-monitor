import { computed, ref } from 'vue'

const STORAGE_KEY = 'sm-theme'
const THEME_COLOR = { dark: '#0B1220', light: '#F4F7FB' }

const dark = ref(
  typeof document !== 'undefined' ? document.documentElement.classList.contains('dark') : true
)

function apply(value) {
  dark.value = value
  if (typeof document === 'undefined') return
  const color = value ? THEME_COLOR.dark : THEME_COLOR.light
  document.documentElement.classList.toggle('dark', value)
  document.documentElement.style.backgroundColor = color
  const meta = document.querySelector('meta[name="theme-color"]')
  if (meta) meta.setAttribute('content', color)
  try {
    localStorage.setItem(STORAGE_KEY, value ? 'dark' : 'light')
  } catch {
    /* storage unavailable */
  }
}

export function useTheme() {
  return {
    isDark: computed(() => dark.value),
    toggleTheme: () => apply(!dark.value)
  }
}
