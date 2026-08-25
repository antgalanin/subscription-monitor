import { defineStore } from 'pinia'
import { authApi } from '../api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    loaded: false
  }),
  getters: {
    isAuthenticated: (state) => state.user !== null,
    isAdmin: (state) => state.user?.role === 'ADMIN'
  },
  actions: {
    async fetchUser() {
      try {
        const { data } = await authApi.me()
        this.user = data
      } catch {
        this.user = null
      } finally {
        this.loaded = true
      }
    },
    async login(credentials) {
      const { data } = await authApi.login(credentials)
      this.user = data
      this.loaded = true
    },
    clearSession() {
      this.user = null
      this.loaded = true
    },
    async logout() {
      try {
        await authApi.logout()
      } finally {
        this.user = null
      }
    }
  }
})
