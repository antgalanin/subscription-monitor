import { describe, it, expect, beforeEach } from 'vitest'
import { useTheme } from '../useTheme'

describe('useTheme', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('переключает класс dark на html и запоминает выбор', () => {
    const { isDark, toggleTheme } = useTheme()
    const initial = isDark.value

    toggleTheme()
    expect(isDark.value).toBe(!initial)
    expect(document.documentElement.classList.contains('dark')).toBe(!initial)
    expect(localStorage.getItem('sm-theme')).toBe(!initial ? 'dark' : 'light')

    toggleTheme()
    expect(isDark.value).toBe(initial)
    expect(document.documentElement.classList.contains('dark')).toBe(initial)
  })

  it('обновляет meta theme-color под выбранную тему', () => {
    const meta = document.createElement('meta')
    meta.setAttribute('name', 'theme-color')
    document.head.appendChild(meta)

    const { isDark, toggleTheme } = useTheme()
    toggleTheme()
    expect(meta.getAttribute('content')).toBe(isDark.value ? '#0B1220' : '#F4F7FB')

    toggleTheme()
    expect(meta.getAttribute('content')).toBe(isDark.value ? '#0B1220' : '#F4F7FB')

    meta.remove()
  })
})
