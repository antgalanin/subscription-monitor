import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../auth'
import { authApi } from '../../api'

vi.mock('../../api', () => ({
  authApi: {
    me: vi.fn(),
    login: vi.fn(),
    logout: vi.fn()
  }
}))

const testUser = { id: '1', username: 'anton', role: 'USER' }
const adminUser = { id: '2', username: 'admin', role: 'ADMIN' }

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.resetAllMocks()
  })

  it('изначально пользователь не аутентифицирован', () => {
    const store = useAuthStore()
    expect(store.isAuthenticated).toBe(false)
    expect(store.loaded).toBe(false)
  })

  it('fetchUser сохраняет пользователя при успешном ответе', async () => {
    authApi.me.mockResolvedValue({ data: testUser })
    const store = useAuthStore()
    await store.fetchUser()
    expect(store.user).toEqual(testUser)
    expect(store.isAuthenticated).toBe(true)
    expect(store.loaded).toBe(true)
  })

  it('fetchUser сбрасывает пользователя при 401', async () => {
    authApi.me.mockRejectedValue({ response: { status: 401 } })
    const store = useAuthStore()
    await store.fetchUser()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
    expect(store.loaded).toBe(true)
  })

  it('login сохраняет пользователя из ответа', async () => {
    authApi.login.mockResolvedValue({ data: testUser })
    const store = useAuthStore()
    await store.login({ username: 'anton', password: 'secret123' })
    expect(authApi.login).toHaveBeenCalledWith({ username: 'anton', password: 'secret123' })
    expect(store.user).toEqual(testUser)
  })

  it('login пробрасывает ошибку при неверных данных', async () => {
    authApi.login.mockRejectedValue({ response: { status: 401 } })
    const store = useAuthStore()
    await expect(store.login({ username: 'anton', password: 'wrong' })).rejects.toBeTruthy()
    expect(store.user).toBeNull()
  })

  it('logout очищает пользователя', async () => {
    authApi.me.mockResolvedValue({ data: testUser })
    authApi.logout.mockResolvedValue({})
    const store = useAuthStore()
    await store.fetchUser()
    await store.logout()
    expect(store.user).toBeNull()
  })

  it('logout очищает пользователя даже при ошибке запроса', async () => {
    authApi.me.mockResolvedValue({ data: testUser })
    authApi.logout.mockRejectedValue(new Error('network'))
    const store = useAuthStore()
    await store.fetchUser()
    await store.logout().catch(() => {})
    expect(store.user).toBeNull()
  })

  it('clearSession сбрасывает пользователя, но помечает состояние загруженным', async () => {
    authApi.me.mockResolvedValue({ data: testUser })
    const store = useAuthStore()
    await store.fetchUser()
    store.clearSession()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
    expect(store.loaded).toBe(true)
  })

  it('isAdmin отличает администратора от пользователя', async () => {
    authApi.me.mockResolvedValue({ data: adminUser })
    const store = useAuthStore()
    await store.fetchUser()
    expect(store.isAdmin).toBe(true)
  })
})
