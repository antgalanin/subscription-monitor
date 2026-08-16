<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { analyticsApi } from '../api'
import { formatMoney, formatDate, categoryTypeLabel, errorMessage } from '../utils/format'

const statistics = ref(null)
const upcomingPayments = ref([])
const categoryStatistics = ref([])
const loading = ref(false)

const URGENCY_LABELS = {
  Overdue: 'Просрочен',
  Today: 'Сегодня',
  Tomorrow: 'Завтра',
  'In 2 Days': 'Через 2 дня',
  'In 3 Days': 'Через 3 дня',
  'This Week': 'На этой неделе',
  'This Month': 'В этом месяце',
  Later: 'Позже'
}

const URGENCY_TAG = {
  Overdue: 'danger',
  Today: 'danger',
  Tomorrow: 'warning',
  'In 2 Days': 'warning',
  'In 3 Days': 'warning',
  'This Week': 'primary',
  'This Month': 'info',
  Later: 'info'
}

async function load() {
  loading.value = true
  try {
    const [statsRes, upcomingRes, catsRes] = await Promise.all([
      analyticsApi.myStatistics(),
      analyticsApi.myUpcomingPayments(),
      analyticsApi.myCategoryStatistics()
    ])
    statistics.value = statsRes.data
    upcomingPayments.value = upcomingRes.data
    categoryStatistics.value = catsRes.data
  } catch (error) {
    ElMessage.error(errorMessage(error, 'Не удалось загрузить статистику'))
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading">
    <div class="toolbar">
      <h2>Статистика</h2>
      <el-button @click="load">Обновить</el-button>
    </div>

    <el-row :gutter="16" class="cards">
      <el-col :span="6">
        <el-card>
          <div class="stat-label">Активные подписки</div>
          <div class="stat-value">{{ statistics?.activeSubscriptions ?? '—' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-label">Расходы в месяц, ₽</div>
          <div class="stat-value">{{ formatMoney(statistics?.totalCostRub, 'RUB') }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-label">Расходы в месяц, $</div>
          <div class="stat-value">{{ formatMoney(statistics?.totalCostUsd, 'USD') }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-label">Расходы в месяц, €</div>
          <div class="stat-value">{{ formatMoney(statistics?.totalCostEur, 'EUR') }}</div>
        </el-card>
      </el-col>
    </el-row>

    <h3>Предстоящие платежи</h3>
    <el-table :data="upcomingPayments" border stripe class="table">
      <el-table-column prop="subscriptionName" label="Подписка" min-width="160" />
      <el-table-column prop="categoryName" label="Категория" min-width="140" />
      <el-table-column label="Сумма" width="130">
        <template #default="{ row }">{{ formatMoney(row.cost, row.currency) }}</template>
      </el-table-column>
      <el-table-column label="Дата списания" width="140">
        <template #default="{ row }">{{ formatDate(row.nextBillingDate) }}</template>
      </el-table-column>
      <el-table-column label="Срочность" width="150">
        <template #default="{ row }">
          <el-tag :type="URGENCY_TAG[row.paymentUrgency] ?? 'info'">
            {{ URGENCY_LABELS[row.paymentUrgency] ?? row.paymentUrgency }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <h3>По категориям</h3>
    <el-table :data="categoryStatistics" border stripe class="table">
      <el-table-column prop="categoryName" label="Категория" min-width="160" />
      <el-table-column label="Тип" width="160">
        <template #default="{ row }">{{ categoryTypeLabel(row.categoryType) }}</template>
      </el-table-column>
      <el-table-column prop="activeSubscriptions" label="Активных" width="110" />
      <el-table-column label="Сумма, ₽" width="140">
        <template #default="{ row }">{{ formatMoney(row.totalCostRub, 'RUB') }}</template>
      </el-table-column>
      <el-table-column label="Сумма, $" width="140">
        <template #default="{ row }">{{ formatMoney(row.totalCostUsd, 'USD') }}</template>
      </el-table-column>
      <el-table-column label="Сумма, €" width="140">
        <template #default="{ row }">{{ formatMoney(row.totalCostEur, 'EUR') }}</template>
      </el-table-column>
    </el-table>
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
.cards {
  margin-bottom: 8px;
}
.stat-label {
  color: #909399;
  font-size: 13px;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 22px;
  font-weight: 600;
}
.table {
  margin-bottom: 24px;
}
</style>
