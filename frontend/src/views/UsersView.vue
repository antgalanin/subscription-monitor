<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Refresh } from '@element-plus/icons-vue'
import { usersApi } from '../api'
import { useAuthStore } from '../stores/auth'
import { useBreakpoint } from '../composables/useBreakpoint'
import { invalidateOwners } from '../composables/useOwners'
import { formatDate, userRoleLabel, pluralize, errorMessage } from '../utils/format'
import PageHeader from '../components/PageHeader.vue'
import StatusTag from '../components/StatusTag.vue'

const auth = useAuthStore()
const { isMobile } = useBreakpoint()

const users = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const form = reactive({ username: '', email: '', role: 'USER', notificationDays: 3, password: '' })

const isCreating = computed(() => editingId.value === null)

const dialogWidth = computed(() => (isMobile.value ? '94vw' : '440px'))

const subtitle = computed(() => {
  const total = users.value.length
  if (!total) return ''
  const admins = users.value.filter((user) => user.role === 'ADMIN').length
  return `${total} ${pluralize(total, ['пользователь', 'пользователя', 'пользователей'])} · ${admins} с правами администратора`
})

function initials(username) {
  return (username ?? '?').slice(0, 2).toUpperCase()
}

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

function openCreate() {
  editingId.value = null
  Object.assign(form, { username: '', email: '', role: 'USER', notificationDays: 3, password: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    username: row.username,
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
  if (isCreating.value && form.username.trim().length < 3) {
    ElMessage.warning('Имя пользователя должно содержать минимум 3 символа')
    return
  }
  if (isCreating.value && form.password.length < 8) {
    ElMessage.warning('Пароль должен содержать минимум 8 символов')
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
    if (isCreating.value) {
      await usersApi.create({ ...payload, username: form.username.trim() })
      ElMessage.success('Пользователь создан')
    } else {
      await usersApi.update(editingId.value, payload)
      ElMessage.success('Пользователь обновлён')
    }
    invalidateOwners()
    dialogVisible.value = false
    await load()
  } catch (error) {
    const fallback = isCreating.value
      ? 'Не удалось создать пользователя'
      : 'Не удалось обновить пользователя'
    ElMessage.error(errorMessage(error, fallback))
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(
      `Удалить пользователя «${row.username}» со всеми его подписками?`,
      'Подтверждение',
      { confirmButtonText: 'Удалить', cancelButtonText: 'Отмена', type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await usersApi.remove(row.id)
    invalidateOwners()
    ElMessage.success('Пользователь удалён')
    await load()
  } catch (error) {
    ElMessage.error(errorMessage(error, 'Не удалось удалить пользователя'))
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <PageHeader title="Пользователи" :subtitle="subtitle">
      <template #actions>
        <el-button :icon="Refresh" @click="load">Обновить</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreate">Добавить пользователя</el-button>
      </template>
    </PageHeader>

    <div v-if="isMobile" v-loading="loading" class="records">
      <article v-for="row in users" :key="row.id" class="record">
        <div class="record__head">
          <div class="person">
            <span class="person__avatar">{{ initials(row.username) }}</span>
            <div>
              <p class="record__title">{{ row.username }}</p>
              <p class="person__email">{{ row.email }}</p>
            </div>
          </div>
          <StatusTag
            :tone="row.role === 'ADMIN' ? 'blue' : 'slate'"
            :label="userRoleLabel(row.role)"
            :dot="false"
          />
        </div>
        <div class="record__meta">
          <div>
            <p class="record__label">Уведомлять за</p>
            <p class="record__value">{{ row.notificationDays }} дн.</p>
          </div>
          <div>
            <p class="record__label">Регистрация</p>
            <p class="record__value">{{ formatDate(row.createdAt) }}</p>
          </div>
        </div>
        <div class="record__actions">
          <el-button :icon="Edit" @click="openEdit(row)">Изменить</el-button>
          <el-button
            v-if="row.id !== auth.user?.id"
            :icon="Delete"
            type="danger"
            plain
            @click="remove(row)"
          >
            Удалить
          </el-button>
        </div>
      </article>
    </div>

    <el-table v-else v-loading="loading" :data="users">
      <el-table-column label="Пользователь" min-width="230">
        <template #default="{ row }">
          <div class="person">
            <span class="person__avatar">{{ initials(row.username) }}</span>
            <div>
              <p class="person__name">{{ row.username }}</p>
              <p class="person__email">{{ row.email }}</p>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="Роль" width="170">
        <template #default="{ row }">
          <StatusTag
            :tone="row.role === 'ADMIN' ? 'blue' : 'slate'"
            :label="userRoleLabel(row.role)"
            :dot="false"
          />
        </template>
      </el-table-column>
      <el-table-column label="Уведомлять за" width="150" class-name="num-cell">
        <template #default="{ row }">{{ row.notificationDays }} дн.</template>
      </el-table-column>
      <el-table-column label="Регистрация" width="140" class-name="num-cell">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="Действия" width="120" align="right">
        <template #default="{ row }">
          <div class="row-actions">
            <el-button :icon="Edit" circle text title="Изменить" @click="openEdit(row)" />
            <el-button
              v-if="row.id !== auth.user?.id"
              :icon="Delete"
              circle
              text
              type="danger"
              title="Удалить"
              @click="remove(row)"
            />
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="isCreating ? 'Новый пользователь' : 'Редактирование пользователя'"
      :width="dialogWidth"
    >
      <el-form label-position="top">
        <el-form-item v-if="isCreating" label="Имя пользователя">
          <el-input v-model="form.username" autocomplete="off" />
        </el-form-item>
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
        <el-form-item :label="isCreating ? 'Пароль' : 'Новый пароль (не обязательно)'">
          <el-input v-model="form.password" type="password" show-password autocomplete="new-password" />
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
.full {
  width: 100%;
}

.person {
  display: flex;
  align-items: center;
  gap: 11px;
  min-width: 0;
}

.person__avatar {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  flex: none;
  border-radius: var(--r-sm);
  background: var(--surface-3);
  color: var(--ink-2);
  font-size: 12px;
  font-weight: 700;
}

.person__name {
  font-weight: 600;
  line-height: 1.3;
}

.person__email {
  font-size: 12px;
  color: var(--ink-3);
  line-height: 1.3;
  overflow-wrap: anywhere;
}

.row-actions {
  display: flex;
  gap: 2px;
  justify-content: flex-end;
}
</style>
