<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CreditCard, Delete, Edit, Plus } from '@element-plus/icons-vue'
import { subscriptionsApi, paymentsApi, categoriesApi } from '../api'
import { useBreakpoint } from '../composables/useBreakpoint'
import { useOwners } from '../composables/useOwners'
import { formatMoney, formatDate, relativeDate, daysUntil, pluralize, errorMessage } from '../utils/format'
import PageHeader from '../components/PageHeader.vue'
import StatusTag from '../components/StatusTag.vue'
import EmptyState from '../components/EmptyState.vue'

const { isMobile } = useBreakpoint()
const { showOwner, loadOwners, ownerName, isForeign } = useOwners()

const subscriptions = ref([])
const payments = ref([])
const categories = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)

const emptyForm = {
  name: '',
  categoryId: null,
  cost: null,
  currency: 'RUB',
  billingPeriodDays: 30,
  nextBillingDate: null,
  isActive: true
}
const form = reactive({ ...emptyForm })

const paymentById = computed(() => new Map(payments.value.map((p) => [p.id, p])))
const categoryById = computed(() => new Map(categories.value.map((c) => [c.id, c])))

const rows = computed(() =>
  subscriptions.value.map((s) => {
    const payment = paymentById.value.get(s.paymentId)
    return {
      ...s,
      categoryName: categoryById.value.get(s.categoryId)?.name ?? '—',
      cost: payment?.cost,
      currency: payment?.currency,
      billingPeriodDays: payment?.billingPeriodDays,
      nextBillingDate: payment?.nextBillingDate
    }
  })
)

const activeCount = computed(() => rows.value.filter((row) => row.isActive).length)

const subtitle = computed(() => {
  const total = rows.value.length
  if (!total) return 'Список пуст'
  const noun = pluralize(total, ['подписка', 'подписки', 'подписок'])
  const scope = showOwner.value ? ' всех пользователей' : ''
  return `${total} ${noun}${scope} · ${activeCount.value} активных`
})

const selectableCategories = computed(() => categories.value.filter((c) => c.type !== 'LEGACY'))
const dialogWidth = computed(() => (isMobile.value ? '94vw' : '480px'))

function billingTone(row) {
  if (!row.isActive) return 'slate'
  const days = daysUntil(row.nextBillingDate)
  if (days === null) return 'slate'
  if (days <= 0) return 'rose'
  if (days <= 3) return 'amber'
  return 'slate'
}

async function load() {
  loading.value = true
  try {
    const [subsRes, paysRes, catsRes] = await Promise.all([
      subscriptionsApi.list(),
      paymentsApi.list(),
      categoriesApi.list(),
      loadOwners()
    ])
    subscriptions.value = subsRes.data
    payments.value = paysRes.data
    categories.value = catsRes.data
  } catch (error) {
    ElMessage.error(errorMessage(error, 'Не удалось загрузить подписки'))
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, emptyForm)
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    categoryId: row.categoryId,
    cost: row.cost,
    currency: row.currency ?? 'RUB',
    billingPeriodDays: row.billingPeriodDays ?? 30,
    nextBillingDate: row.nextBillingDate,
    isActive: row.isActive
  })
  dialogVisible.value = true
}

async function save() {
  if (!form.name || !form.categoryId || !form.cost || !form.nextBillingDate) {
    ElMessage.warning('Заполните все поля')
    return
  }
  saving.value = true
  try {
    if (editingId.value === null) {
      const { data: payment } = await paymentsApi.create({
        cost: form.cost,
        currency: form.currency,
        billingPeriodDays: form.billingPeriodDays,
        nextBillingDate: form.nextBillingDate
      })
      await subscriptionsApi.create({
        name: form.name,
        categoryId: form.categoryId,
        paymentId: payment.id,
        isActive: form.isActive
      })
      ElMessage.success('Подписка создана')
    } else {
      const current = subscriptions.value.find((s) => s.id === editingId.value)
      const oldNextBillingDate = paymentById.value.get(current?.paymentId)?.nextBillingDate ?? null
      await subscriptionsApi.updateWithPayment(editingId.value, {
        name: form.name,
        categoryId: form.categoryId,
        isActive: form.isActive,
        cost: form.cost,
        currency: form.currency,
        billingPeriodDays: form.billingPeriodDays,
        nextBillingDate: form.nextBillingDate,
        oldNextBillingDate
      })
      ElMessage.success('Подписка обновлена')
    }
    dialogVisible.value = false
    await load()
  } catch (error) {
    ElMessage.error(errorMessage(error, 'Не удалось сохранить подписку'))
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`Удалить подписку «${row.name}»?`, 'Подтверждение', {
      confirmButtonText: 'Удалить',
      cancelButtonText: 'Отмена',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await subscriptionsApi.remove(row.id)
    ElMessage.success('Подписка удалена')
    await load()
  } catch (error) {
    ElMessage.error(errorMessage(error, 'Не удалось удалить подписку'))
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <PageHeader title="Подписки" :subtitle="subtitle">
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openCreate">Добавить подписку</el-button>
      </template>
    </PageHeader>

    <EmptyState
      v-if="!loading && !rows.length"
      title="Пока нет подписок"
      text="Добавьте первую подписку — и увидите даты списаний, расходы и напоминания."
    >
      <template #icon><el-icon><CreditCard /></el-icon></template>
      <template #action>
        <el-button type="primary" :icon="Plus" @click="openCreate">Добавить подписку</el-button>
      </template>
    </EmptyState>

    <div v-else-if="isMobile" v-loading="loading" class="records">
      <article v-for="row in rows" :key="row.id" class="record">
        <div class="record__head">
          <span class="record__title">{{ row.name }}</span>
          <StatusTag
            :tone="row.isActive ? 'mint' : 'slate'"
            :label="row.isActive ? 'Активна' : 'Неактивна'"
          />
        </div>
        <div class="record__meta">
          <div>
            <p class="record__label">Стоимость</p>
            <p class="record__value">{{ formatMoney(row.cost, row.currency) }}</p>
          </div>
          <div>
            <p class="record__label">Категория</p>
            <p class="record__value">{{ row.categoryName }}</p>
          </div>
          <div v-if="showOwner">
            <p class="record__label">Владелец</p>
            <p class="record__value">{{ ownerName(row.userId) }}</p>
          </div>
          <div>
            <p class="record__label">Период</p>
            <p class="record__value">
              {{ row.billingPeriodDays ? `${row.billingPeriodDays} дн.` : '—' }}
            </p>
          </div>
          <div>
            <p class="record__label">Списание</p>
            <p class="record__value">
              {{ formatDate(row.nextBillingDate) }}
              <span class="record__note">{{ relativeDate(row.nextBillingDate) }}</span>
            </p>
          </div>
        </div>
        <div class="record__actions">
          <el-button :icon="Edit" @click="openEdit(row)">Изменить</el-button>
          <el-button :icon="Delete" type="danger" plain @click="remove(row)">Удалить</el-button>
        </div>
      </article>
    </div>

    <el-table v-else v-loading="loading" :data="rows">
      <el-table-column prop="name" label="Название" min-width="160">
        <template #default="{ row }">
          <span class="cell-strong">{{ row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="Категория" min-width="130" />
      <el-table-column v-if="showOwner" label="Владелец" min-width="120">
        <template #default="{ row }">
          <span :class="{ 'cell-foreign': isForeign(row.userId) }">{{ ownerName(row.userId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Стоимость" width="130" class-name="num-cell">
        <template #default="{ row }">{{ formatMoney(row.cost, row.currency) }}</template>
      </el-table-column>
      <el-table-column label="Период" width="100" class-name="num-cell">
        <template #default="{ row }">
          {{ row.billingPeriodDays ? `${row.billingPeriodDays} дн.` : '—' }}
        </template>
      </el-table-column>
      <el-table-column label="Следующее списание" min-width="165">
        <template #default="{ row }">
          <span class="num">{{ formatDate(row.nextBillingDate) }}</span>
          <span class="cell-note" :class="`cell-note--${billingTone(row)}`">
            {{ relativeDate(row.nextBillingDate) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="Статус" width="145">
        <template #default="{ row }">
          <StatusTag
            :tone="row.isActive ? 'mint' : 'slate'"
            :label="row.isActive ? 'Активна' : 'Неактивна'"
          />
        </template>
      </el-table-column>
      <el-table-column label="Действия" width="110" align="right" fixed="right">
        <template #default="{ row }">
          <div class="row-actions">
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
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId === null ? 'Новая подписка' : 'Редактирование подписки'"
      :width="dialogWidth"
    >
      <el-form label-position="top">
        <el-form-item label="Название">
          <el-input v-model="form.name" placeholder="Например, Netflix" />
        </el-form-item>
        <el-form-item label="Категория">
          <el-select v-model="form.categoryId" placeholder="Выберите категорию" class="full">
            <el-option v-for="c in selectableCategories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <div class="form-row">
          <el-form-item label="Стоимость">
            <el-input-number v-model="form.cost" :min="0.01" :precision="2" :step="10" class="full" />
          </el-form-item>
          <el-form-item label="Валюта">
            <el-select v-model="form.currency" class="full">
              <el-option label="₽ RUB" value="RUB" />
              <el-option label="$ USD" value="USD" />
              <el-option label="€ EUR" value="EUR" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="Период оплаты (дней)">
            <el-input-number v-model="form.billingPeriodDays" :min="1" class="full" />
          </el-form-item>
          <el-form-item label="Дата следующего списания">
            <el-date-picker
              v-model="form.nextBillingDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="Выберите дату"
              class="full"
            />
          </el-form-item>
        </div>
        <el-form-item label="Активна">
          <el-switch v-model="form.isActive" />
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

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 14px;
}

.cell-strong {
  font-weight: 600;
}

.cell-foreign {
  color: var(--tone-blue);
}

.cell-note {
  display: block;
  font-size: 12px;
  color: var(--ink-3);
}

.cell-note--rose {
  color: var(--tone-rose);
}

.cell-note--amber {
  color: var(--tone-amber);
}

.record__note {
  display: block;
  font-size: 12px;
  color: var(--ink-3);
}

.row-actions {
  display: flex;
  gap: 2px;
  justify-content: flex-end;
}

@media (max-width: 767px) {
  .form-row {
    grid-template-columns: minmax(0, 1fr);
    gap: 0;
  }
}
</style>
