<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { subscriptionsApi, paymentsApi, categoriesApi } from '../api'
import { formatMoney, formatDate, errorMessage } from '../utils/format'

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

const selectableCategories = computed(() => categories.value.filter((c) => c.type !== 'LEGACY'))

async function load() {
  loading.value = true
  try {
    const [subsRes, paysRes, catsRes] = await Promise.all([
      subscriptionsApi.list(),
      paymentsApi.list(),
      categoriesApi.list()
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
  <div>
    <div class="toolbar">
      <h2>Подписки</h2>
      <el-button type="primary" @click="openCreate">Добавить подписку</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="name" label="Название" min-width="160" />
      <el-table-column prop="categoryName" label="Категория" min-width="140" />
      <el-table-column label="Стоимость" width="140">
        <template #default="{ row }">{{ formatMoney(row.cost, row.currency) }}</template>
      </el-table-column>
      <el-table-column label="Период" width="110">
        <template #default="{ row }">{{ row.billingPeriodDays ? `${row.billingPeriodDays} дн.` : '—' }}</template>
      </el-table-column>
      <el-table-column label="Следующее списание" width="180">
        <template #default="{ row }">{{ formatDate(row.nextBillingDate) }}</template>
      </el-table-column>
      <el-table-column label="Статус" width="110">
        <template #default="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'info'">
            {{ row.isActive ? 'Активна' : 'Неактивна' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Действия" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">Изменить</el-button>
          <el-button size="small" type="danger" @click="remove(row)">Удалить</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId === null ? 'Новая подписка' : 'Редактирование подписки'"
      width="480px"
    >
      <el-form label-position="top">
        <el-form-item label="Название">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="Категория">
          <el-select v-model="form.categoryId" placeholder="Выберите категорию" class="full">
            <el-option v-for="c in selectableCategories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
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
