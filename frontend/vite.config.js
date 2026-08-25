import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

// API target: localhost by default, VITE_API_TARGET to point dev server elsewhere
const apiTarget = process.env.VITE_API_TARGET || 'http://localhost:8080'

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: apiTarget,
        changeOrigin: true,
        secure: true,
        cookieDomainRewrite: 'localhost',
        cookiePathRewrite: '/'
      }
    }
  },
  test: {
    environment: 'jsdom',
    globals: true
  }
})
