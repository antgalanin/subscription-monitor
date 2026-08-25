<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'
import { useAuthStore } from '../stores/auth'
import { errorMessage } from '../utils/format'
import AuthShell from '../components/AuthShell.vue'

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
  <AuthShell>
    <h2 class="title">Регистрация</h2>
    <p class="subtitle">Создайте аккаунт — это займёт меньше минуты</p>

    <el-form label-position="top" @submit.prevent="handleRegister">
      <el-form-item label="Имя пользователя">
        <el-input v-model="form.username" size="large" autocomplete="username" />
      </el-form-item>
      <el-form-item label="Email">
        <el-input v-model="form.email" type="email" size="large" autocomplete="email" />
      </el-form-item>
      <el-form-item label="Пароль">
        <el-input
          v-model="form.password"
          type="password"
          size="large"
          show-password
          autocomplete="new-password"
        />
      </el-form-item>
      <el-form-item label="Повторите пароль">
        <el-input
          v-model="form.passwordRepeat"
          type="password"
          size="large"
          show-password
          autocomplete="new-password"
        />
      </el-form-item>
      <el-button
        type="primary"
        size="large"
        native-type="submit"
        :loading="loading"
        class="submit"
      >
        Зарегистрироваться
      </el-button>
    </el-form>

    <p class="switch">
      Уже есть аккаунт?
      <router-link :to="{ name: 'login' }">Войти</router-link>
    </p>
  </AuthShell>
</template>

<style scoped>
.title {
  font-size: 22px;
  font-weight: 650;
  text-align: center;
}

.subtitle {
  margin: 8px 0 24px;
  text-align: center;
  font-size: 14px;
  color: var(--ink-3);
}

.submit {
  width: 100%;
  margin-top: 4px;
}

.switch {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: var(--ink-3);
}
</style>
