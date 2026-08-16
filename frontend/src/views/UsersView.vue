<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { usersApi } from '../api'
import { useAuthStore } from '../stores/auth'
import { formatDate, userRoleLabel, errorMessage } from '../utils/format'

const auth = useAuthStore()

const users = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const form = reactive({ email: '', role: 'USER', notificationDays: 3, password: '' })

async function load() {
  loading.value = true
  try {
    const { data } = await usersApi.list()
    users.value = data
  } catch (error) {
    ElMessage.error(errorMessage(error, 'Не удалось загрузить пользователей'))
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    email: row.email,
    role: row.role,
    notificationDays: row.notificationDays,
    password: ''
  })
  dialogVisible.value = true
}

async function save() {
  if (!form.email) {
    ElMessage.warning('Введите email')
    return
  }
  saving.value = true
  try {
    const payload = {
      email: form.email,
      role: form.role,
      notificationDays: form.notificationDays
    }
    if (form.password) {
      payload.password = form.password
    }
    await usersApi.update(editingId.value, payload)
    ElMessage.success('Пользователь обновлён')
    dialogVisible.value = false
    await load()
  } catch (error) {
    ElMessage.error(errorMessage(error, 'Не удалось обновить пользователя'))
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`Удалить пользователя «${row.username}» со всеми его подписками?`, 'Подтверждение', {
      confirmButtonText: 'Удалить',
      cancelButtonText: 'Отмена',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await usersApi.remove(row.id)
    ElMessage.success('Пользователь удалён')
    await load()
  } catch (error) {
    ElMessage.error(errorMessage(error, 'Не удалось удалить пользователя'))
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="toolbar">
      <h2>Пользователи</h2>
      <el-button @click="load">Обновить</el-button>
    </div>

    <el-table v-loading="loading" :data="users" border stripe>
      <el-table-column prop="username" label="Имя пользователя" min-width="150" />
      <el-table-column prop="email" label="Email" min-width="200" />
      <el-table-column label="Роль" width="150">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">{{ userRoleLabel(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="notificationDays" label="Уведомлять за (дней)" width="170" />
      <el-table-column label="Регистрация" width="130">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="Действия" width="170" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">Изменить</el-button>
          <el-button
            v-if="row.id !== auth.user?.id"
            size="small"
            type="danger"
            @click="remove(row)"
          >
            Удалить
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="Редактирование пользователя" width="440px">
      <el-form label-position="top">
        <el-form-item label="Email">
          <el-input v-model="form.email" type="email" />
        </el-form-item>
        <el-form-item label="Роль">
          <el-select v-model="form.role" class="full">
            <el-option label="Пользователь" value="USER" />
            <el-option label="Администратор" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="Уведомлять за (дней)">
          <el-input-number v-model="form.notificationDays" :min="0" class="full" />
        </el-form-item>
        <el-form-item label="Новый пароль (не обязательно)">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Отмена</el-button>
        <el-button type="primary" :loading="saving" @click="save">Сохранить</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.toolbar h2 {
  margin: 0;
}
.full {
  width: 100%;
}
</style>
