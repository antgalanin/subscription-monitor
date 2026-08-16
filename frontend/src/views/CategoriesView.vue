<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { categoriesApi } from '../api'
import { useAuthStore } from '../stores/auth'
import { categoryTypeLabel, formatDate, errorMessage } from '../utils/format'

const auth = useAuthStore()

const categories = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const form = reactive({ name: '' })

const TYPE_TAG = { SYSTEM: 'primary', CUSTOM: 'success', LEGACY: 'info' }

function isEditable(row) {
  return row.type === 'CUSTOM' && (auth.isAdmin || row.createdByUserId === auth.user?.id)
}

async function load() {
  loading.value = true
  try {
    const { data } = await categoriesApi.list()
    categories.value = data.filter((c) => c.type !== 'LEGACY')
  } catch (error) {
    ElMessage.error(errorMessage(error, 'Не удалось загрузить категории'))
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.name = ''
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.name = row.name
  dialogVisible.value = true
}

async function save() {
  if (!form.name) {
    ElMessage.warning('Введите название категории')
    return
  }
  saving.value = true
  try {
    if (editingId.value === null) {
      await categoriesApi.create({ name: form.name, type: 'CUSTOM' })
      ElMessage.success('Категория создана')
    } else {
      await categoriesApi.update(editingId.value, { name: form.name, type: 'CUSTOM' })
      ElMessage.success('Категория обновлена')
    }
    dialogVisible.value = false
    await load()
  } catch (error) {
    ElMessage.error(errorMessage(error, 'Не удалось сохранить категорию'))
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`Удалить категорию «${row.name}»?`, 'Подтверждение', {
      confirmButtonText: 'Удалить',
      cancelButtonText: 'Отмена',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await categoriesApi.remove(row.id)
    ElMessage.success('Категория удалена')
    await load()
  } catch (error) {
    ElMessage.error(errorMessage(error, 'Не удалось удалить категорию'))
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="toolbar">
      <h2>Категории</h2>
      <el-button type="primary" @click="openCreate">Добавить категорию</el-button>
    </div>

    <el-table v-loading="loading" :data="categories" border stripe>
      <el-table-column prop="name" label="Название" min-width="200" />
      <el-table-column label="Тип" width="180">
        <template #default="{ row }">
          <el-tag :type="TYPE_TAG[row.type] ?? 'info'">{{ categoryTypeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Создана" width="140">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="Действия" width="160" fixed="right">
        <template #default="{ row }">
          <template v-if="isEditable(row)">
            <el-button size="small" @click="openEdit(row)">Изменить</el-button>
            <el-button size="small" type="danger" @click="remove(row)">Удалить</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId === null ? 'Новая категория' : 'Редактирование категории'"
      width="420px"
    >
      <el-form label-position="top" @submit.prevent="save">
        <el-form-item label="Название">
          <el-input v-model="form.name" />
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
</style>
