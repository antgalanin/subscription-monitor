import axios from 'axios'
import router from '../router'
import { useAuthStore } from '../stores/auth'

const client = axios.create({
  baseURL: '/api',
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN'
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const url = error.config?.url
    if (status === 401 && url !== '/auth/me') {
      useAuthStore().clearSession()
      if (router.currentRoute.value.name !== 'login') {
        router.push({ name: 'login' })
      }
    }
    return Promise.reject(error)
  }
)

export default client
