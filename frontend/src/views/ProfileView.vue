<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Key, Message } from '@element-plus/icons-vue'
import { authApi } from '../api'
import { useAuthStore } from '../stores/auth'
import { useBreakpoint } from '../composables/useBreakpoint'
import { formatDate, userRoleLabel, errorMessage } from '../utils/format'
import PageHeader from '../components/PageHeader.vue'
import StatusTag from '../components/StatusTag.vue'

const auth = useAuthStore()
const { isMobile } = useBreakpoint()

const emailDialog = ref(false)
const emailForm = reactive({ email: '' })
const emailSaving = ref(false)

const passwordDialog = ref(false)
const passwordForm = reactive({ currentPassword: '', newPassword: '', newPasswordRepeat: '' })
const passwordSaving = ref(false)

const dialogWidth = computed(() => (isMobile.value ? '94vw' : '420px'))
const initials = computed(() => (auth.user?.username ?? '?').slice(0, 2).toUpperCase())

const fields = computed(() => [
  { label: 'Email', value: auth.user?.email ?? '—' },
  { label: 'Уведомлять за', value: `${auth.user?.notificationDays ?? '—'} дн. до списания` },
  { label: 'Дата регистрации', value: formatDate(auth.user?.createdAt) }
])

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
  <div class="page">
    <PageHeader title="Профиль" subtitle="Учётная запись и настройки напоминаний" />

    <section class="card profile">
      <header class="profile__head">
        <span class="profile__avatar">{{ initials }}</span>
        <div class="profile__id">
          <p class="profile__name">{{ auth.user?.username }}</p>
          <StatusTag
            :tone="auth.isAdmin ? 'blue' : 'slate'"
            :label="userRoleLabel(auth.user?.role)"
            :dot="false"
          />
        </div>
      </header>

      <dl class="fields">
        <div v-for="field in fields" :key="field.label" class="field">
          <dt class="field__label">{{ field.label }}</dt>
          <dd class="field__value">{{ field.value }}</dd>
        </div>
      </dl>

      <footer class="profile__actions">
        <el-button :icon="Message" @click="openEmailDialog">Изменить email</el-button>
        <el-button :icon="Key" @click="openPasswordDialog">Изменить пароль</el-button>
      </footer>
    </section>

    <el-dialog v-model="emailDialog" title="Изменение email" :width="dialogWidth">
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

    <el-dialog v-model="passwordDialog" title="Изменение пароля" :width="dialogWidth">
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
        <el-button type="primary" :loading="passwordSaving" @click="savePassword">
          Сохранить
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.profile {
  max-width: 640px;
  padding: 22px;
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.profile__head {
  display: flex;
  align-items: center;
  gap: 16px;
}

.profile__avatar {
  display: grid;
  place-items: center;
  width: 56px;
  height: 56px;
  flex: none;
  border-radius: var(--r-md);
  background: var(--brand-gradient);
  color: #05231c;
  font-size: 20px;
  font-weight: 700;
}

.profile__id {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  min-width: 0;
}

.profile__name {
  font-size: 20px;
  font-weight: 650;
  letter-spacing: -0.01em;
  overflow-wrap: anywhere;
}

.fields {
  margin: 0;
  display: flex;
  flex-direction: column;
  border-top: 1px solid var(--border);
}

.field {
  display: grid;
  grid-template-columns: 200px minmax(0, 1fr);
  gap: 16px;
  padding: 13px 0;
  border-bottom: 1px solid var(--border);
}

.field__label {
  color: var(--ink-3);
  font-size: 14px;
}

.field__value {
  margin: 0;
  font-size: 14px;
  color: var(--ink);
  overflow-wrap: anywhere;
}

.profile__actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 767px) {
  .profile {
    padding: 18px;
    gap: 18px;
  }

  .field {
    grid-template-columns: minmax(0, 1fr);
    gap: 2px;
    padding: 11px 0;
  }

  .profile__actions .el-button {
    flex: 1;
    margin-left: 0;
  }
}
</style>
