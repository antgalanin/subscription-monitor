import { ref } from 'vue'

const isMobile = ref(false)
const isCompact = ref(false)

if (typeof window !== 'undefined' && typeof window.matchMedia === 'function') {
  const bind = (query, target) => {
    const mq = window.matchMedia(query)
    target.value = mq.matches
    mq.addEventListener('change', (event) => {
      target.value = event.matches
    })
  }
  bind('(max-width: 767px)', isMobile)
  bind('(max-width: 1023px)', isCompact)
}

export function useBreakpoint() {
  return { isMobile, isCompact }
}
