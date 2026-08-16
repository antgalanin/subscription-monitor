<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'
import { useAuthStore } from '../stores/auth'
import { formatDate, userRoleLabel, errorMessage } from '../utils/format'

const auth = useAuthStore()

const emailDialog = ref(false)
const emailForm = reactive({ email: '' })
const emailSaving = ref(false)

const passwordDialog = ref(false)
const passwordForm = reactive({ currentPassword: '', newPassword: '', newPasswordRepeat: '' })
const passwordSaving = ref(false)

function openEmailDialog() {
  emailForm.email = auth.user?.email ?? ''
  emailDialog.value = true
}

async function saveEmail() {
  if (!emailForm.email) {
    ElMessage.warning('Введите email')
    return
  }
  emailSaving.value = true
  try {
    await authApi.updateEmail(auth.user.id, emailForm.email)
    await auth.fetchUser()
    ElMessage.success('Email обновлён')
    emailDialog.value = false
  } catch (error) {
    ElMessage.error(errorMessage(error, 'Не удалось обновить email'))
  } finally {
    emailSaving.value = false
  }
}

function openPasswordDialog() {
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.newPasswordRepeat = ''
  passwordDialog.value = true
}

async function savePassword() {
  if (!passwordForm.currentPassword || !passwordForm.newPassword) {
    ElMessage.warning('Заполните все поля')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning('Новый пароль должен содержать минимум 6 символов')
    return
  }
  if (passwordForm.newPassword !== passwordForm.newPasswordRepeat) {
    ElMessage.warning('Пароли не совпадают')
    return
  }
  passwordSaving.value = true
  try {
    await authApi.changePassword(auth.user.id, {
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('Пароль изменён')
    passwordDialog.value = false
  } catch (error) {
    ElMessage.error(errorMessage(error, 'Не удалось изменить пароль'))
  } finally {
    passwordSaving.value = false
  }
}
</script>

<template>
  <div>
    <div class="toolbar">
      <h2>Профиль</h2>
    </div>

    <el-card class="profile-card">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="Имя пользователя">{{ auth.user?.username }}</el-descriptions-item>
        <el-descriptions-item label="Email">{{ auth.user?.email }}</el-descriptions-item>
        <el-descriptions-item label="Роль">{{ userRoleLabel(auth.user?.role) }}</el-descriptions-item>
        <el-descriptions-item label="Уведомлять за (дней)">{{ auth.user?.notificationDays }}</el-descriptions-item>
        <el-descriptions-item label="Дата регистрации">{{ formatDate(auth.user?.createdAt) }}</el-descriptions-item>
      </el-descriptions>
      <div class="actions">
        <el-button @click="openEmailDialog">Изменить email</el-button>
        <el-button @click="openPasswordDialog">Изменить пароль</el-button>
      </div>
    </el-card>

    <el-dialog v-model="emailDialog" title="Изменение email" width="420px">
      <el-form label-position="top" @submit.prevent="saveEmail">
        <el-form-item label="Новый email">
          <el-input v-model="emailForm.email" type="email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="emailDialog = false">Отмена</el-button>
        <el-button type="primary" :loading="emailSaving" @click="saveEmail">Сохранить</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordDialog" title="Изменение пароля" width="420px">
      <el-form label-position="top" @submit.prevent="savePassword">
        <el-form-item label="Текущий пароль">
          <el-input v-model="passwordForm.currentPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="Новый пароль">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="Повторите новый пароль">
          <el-input v-model="passwordForm.newPasswordRepeat" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialog = false">Отмена</el-button>
        <el-button type="primary" :loading="passwordSaving" @click="savePassword">Сохранить</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
.toolbar h2 {
  margin: 0;
}
.profile-card {
  max-width: 560px;
}
.actions {
  margin-top: 16px;
  display: flex;
  gap: 8px;
}
</style>
