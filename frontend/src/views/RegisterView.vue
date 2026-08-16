<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'
import { useAuthStore } from '../stores/auth'
import { errorMessage } from '../utils/format'

const router = useRouter()
const auth = useAuthStore()

const form = reactive({ username: '', email: '', password: '', passwordRepeat: '' })
const loading = ref(false)

async function handleRegister() {
  if (!form.username || !form.email || !form.password) {
    ElMessage.warning('Заполните все поля')
    return
  }
  if (form.password.length < 8) {
    ElMessage.warning('Пароль должен содержать минимум 8 символов')
    return
  }
  if (form.password !== form.passwordRepeat) {
    ElMessage.warning('Пароли не совпадают')
    return
  }
  loading.value = true
  try {
    await authApi.register({ username: form.username, email: form.email, password: form.password })
    await auth.login({ username: form.username, password: form.password })
    ElMessage.success('Регистрация выполнена')
    router.push({ name: 'subscriptions' })
  } catch (error) {
    ElMessage.error(errorMessage(error, 'Не удалось зарегистрироваться'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2>Регистрация</h2>
      <el-form label-position="top" @submit.prevent="handleRegister">
        <el-form-item label="Имя пользователя">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="Email">
          <el-input v-model="form.email" type="email" autocomplete="email" />
        </el-form-item>
        <el-form-item label="Пароль">
          <el-input v-model="form.password" type="password" show-password autocomplete="new-password" />
        </el-form-item>
        <el-form-item label="Повторите пароль">
          <el-input v-model="form.passwordRepeat" type="password" show-password autocomplete="new-password" />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" class="submit">
          Зарегистрироваться
        </el-button>
      </el-form>
      <div class="switch">
        Уже есть аккаунт?
        <router-link :to="{ name: 'login' }">Войти</router-link>
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
