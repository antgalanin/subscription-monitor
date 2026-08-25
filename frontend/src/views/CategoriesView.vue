<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Lock, Plus, PriceTag } from '@element-plus/icons-vue'
import { categoriesApi } from '../api'
import { useAuthStore } from '../stores/auth'
import { useBreakpoint } from '../composables/useBreakpoint'
import { useOwners } from '../composables/useOwners'
import { categoryTypeLabel, formatDate, pluralize, errorMessage } from '../utils/format'
import PageHeader from '../components/PageHeader.vue'
import StatusTag from '../components/StatusTag.vue'
import EmptyState from '../components/EmptyState.vue'

const auth = useAuthStore()
const { isMobile } = useBreakpoint()
const { showOwner, loadOwners, ownerName, isForeign } = useOwners()

const categories = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const form = reactive({ name: '' })

const TYPE_TONE = { SYSTEM: 'blue', CUSTOM: 'mint', LEGACY: 'slate' }

const dialogWidth = computed(() => (isMobile.value ? '94vw' : '420px'))

const subtitle = computed(() => {
  const total = categories.value.length
  if (!total) return 'Список пуст'
  const custom = categories.value.filter((c) => c.type === 'CUSTOM').length
  const noun = pluralize(total, ['категория', 'категории', 'категорий'])
  const customNoun = showOwner.value
    ? pluralize(custom, ['пользовательская', 'пользовательские', 'пользовательских'])
    : pluralize(custom, ['своя', 'свои', 'своих'])
  return `${total} ${noun} · ${custom} ${customNoun}`
})

function isEditable(row) {
  return row.type === 'CUSTOM' && (auth.isAdmin || row.createdByUserId === auth.user?.id)
}

async function load() {
  loading.value = true
  try {
    const [{ data }] = await Promise.all([categoriesApi.list(), loadOwners()])
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
  <div class="page">
    <PageHeader title="Категории" :subtitle="subtitle">
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openCreate">Добавить категорию</el-button>
      </template>
    </PageHeader>

    <EmptyState
      v-if="!loading && !categories.length"
      title="Категорий пока нет"
      text="Категории помогают группировать подписки и видеть расходы по направлениям."
    >
      <template #icon><el-icon><PriceTag /></el-icon></template>
      <template #action>
        <el-button type="primary" :icon="Plus" @click="openCreate">Добавить категорию</el-button>
      </template>
    </EmptyState>

    <div v-else-if="isMobile" v-loading="loading" class="records">
      <article v-for="row in categories" :key="row.id" class="record">
        <div class="record__head">
          <span class="record__title">{{ row.name }}</span>
          <StatusTag
            :tone="TYPE_TONE[row.type] ?? 'slate'"
            :label="categoryTypeLabel(row.type)"
            :dot="false"
          />
        </div>
        <div class="record__meta">
          <div v-if="showOwner">
            <p class="record__label">Владелец</p>
            <p class="record__value">{{ ownerName(row.createdByUserId) }}</p>
          </div>
          <div>
            <p class="record__label">Создана</p>
            <p class="record__value">{{ formatDate(row.createdAt) }}</p>
          </div>
        </div>
        <div v-if="isEditable(row)" class="record__actions">
          <el-button :icon="Edit" @click="openEdit(row)">Изменить</el-button>
          <el-button :icon="Delete" type="danger" plain @click="remove(row)">Удалить</el-button>
        </div>
      </article>
    </div>

    <el-table v-else v-loading="loading" :data="categories">
      <el-table-column prop="name" label="Название" min-width="220">
        <template #default="{ row }">
          <span class="cell-strong">{{ row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Тип" width="200">
        <template #default="{ row }">
          <StatusTag
            :tone="TYPE_TONE[row.type] ?? 'slate'"
            :label="categoryTypeLabel(row.type)"
            :dot="false"
          />
        </template>
      </el-table-column>
      <el-table-column v-if="showOwner" label="Владелец" min-width="160">
        <template #default="{ row }">
          <span :class="{ 'cell-foreign': isForeign(row.createdByUserId) }">
            {{ ownerName(row.createdByUserId) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="Создана" width="150" class-name="num-cell">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="Действия" width="120" align="right">
        <template #default="{ row }">
          <div v-if="isEditable(row)" class="row-actions">
            <el-button :icon="Edit" circle text title="Изменить" @click="openEdit(row)" />
            <el-button
              :icon="Delete"
              circle
              text
              type="danger"
              title="Удалить"
              @click="remove(row)"
            />
          </div>
          <el-icon v-else class="row-lock" title="Системная категория"><Lock /></el-icon>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId === null ? 'Новая категория' : 'Редактирование категории'"
      :width="dialogWidth"
    >
      <el-form label-position="top" @submit.prevent="save">
        <el-form-item label="Название">
          <el-input v-model="form.name" placeholder="Например, Музыка" />
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
.cell-strong {
  font-weight: 600;
}

.cell-foreign {
  color: var(--tone-blue);
}

.row-actions {
  display: flex;
  gap: 2px;
  justify-content: flex-end;
}

.row-lock {
  color: var(--ink-3);
  float: right;
  margin-top: 6px;
}
</style>
