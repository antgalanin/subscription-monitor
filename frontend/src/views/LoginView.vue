<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { errorMessage } from '../utils/format'

const router = useRouter()
const auth = useAuthStore()

const form = reactive({ username: '', password: '' })
const loading = ref(false)

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('Введите имя пользователя и пароль')
    return
  }
  loading.value = true
  try {
    await auth.login({ username: form.username, password: form.password })
    router.push({ name: 'subscriptions' })
  } catch (error) {
    ElMessage.error(errorMessage(error, 'Не удалось войти'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2>Вход в систему</h2>
      <el-form label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="Имя пользователя">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="Пароль">
          <el-input v-model="form.password" type="password" show-password autocomplete="current-password" />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" class="submit">
          Войти
        </el-button>
      </el-form>
      <div class="switch">
        Нет аккаунта?
        <router-link :to="{ name: 'register' }">Зарегистрироваться</router-link>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
.auth-card {
  width: 360px;
}
.auth-card h2 {
  margin: 0 0 20px;
  text-align: center;
}
.submit {
  width: 100%;
}
.switch {
  margin-top: 16px;
  text-align: center;
  color: #606266;
}
</style>
