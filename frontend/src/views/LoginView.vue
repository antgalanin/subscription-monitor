<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { errorMessage } from '../utils/format'
import AuthShell from '../components/AuthShell.vue'

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
  <AuthShell>
    <h2 class="title">Вход в систему</h2>
    <p class="subtitle">Войдите, чтобы увидеть свои подписки и ближайшие списания</p>

    <el-form label-position="top" @submit.prevent="handleLogin">
      <el-form-item label="Имя пользователя">
        <el-input v-model="form.username" size="large" autocomplete="username" />
      </el-form-item>
      <el-form-item label="Пароль">
        <el-input
          v-model="form.password"
          type="password"
          size="large"
          show-password
          autocomplete="current-password"
        />
      </el-form-item>
      <el-button
        type="primary"
        size="large"
        native-type="submit"
        :loading="loading"
        class="submit"
      >
        Войти
      </el-button>
    </el-form>

    <p class="switch">
      Нет аккаунта?
      <router-link :to="{ name: 'register' }">Зарегистрироваться</router-link>
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
