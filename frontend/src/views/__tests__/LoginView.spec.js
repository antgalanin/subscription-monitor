import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import LoginView from '../LoginView.vue'
import { authApi } from '../../api'

vi.mock('../../api', () => ({
  authApi: {
    me: vi.fn(),
    login: vi.fn(),
    logout: vi.fn()
  }
}))

const pushMock = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock }),
  RouterLink: { template: '<a><slot /></a>' }
}))

function mountView() {
  return mount(LoginView, {
    global: {
      plugins: [createPinia(), ElementPlus],
      stubs: { RouterLink: { template: '<a><slot /></a>' } }
    }
  })
}

describe('LoginView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('отображает форму входа', () => {
    const wrapper = mountView()
    expect(wrapper.text()).toContain('Вход в систему')
    expect(wrapper.findAll('input').length).toBeGreaterThanOrEqual(2)
    expect(wrapper.find('button[type="submit"]').exists()).toBe(true)
  })

  it('не отправляет запрос с пустыми полями', async () => {
    const wrapper = mountView()
    await wrapper.find('form').trigger('submit.prevent')
    expect(authApi.login).not.toHaveBeenCalled()
  })

  it('выполняет вход и переходит к подпискам', async () => {
    authApi.login.mockResolvedValue({ data: { id: '1', username: 'anton', role: 'USER' } })
    const wrapper = mountView()
    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('anton')
    await inputs[1].setValue('secret123')
    await wrapper.find('form').trigger('submit.prevent')
    await vi.waitFor(() => {
      expect(authApi.login).toHaveBeenCalledWith({ username: 'anton', password: 'secret123' })
      expect(pushMock).toHaveBeenCalledWith({ name: 'subscriptions' })
    })
  })

  it('остаётся на странице при неверных данных', async () => {
    authApi.login.mockRejectedValue({ response: { status: 401, data: { message: 'Invalid username or password' } } })
    const wrapper = mountView()
    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('anton')
    await inputs[1].setValue('wrong')
    await wrapper.find('form').trigger('submit.prevent')
    await vi.waitFor(() => {
      expect(authApi.login).toHaveBeenCalled()
    })
    expect(pushMock).not.toHaveBeenCalled()
  })
})
