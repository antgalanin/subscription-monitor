<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, TrendCharts } from '@element-plus/icons-vue'
import { analyticsApi } from '../api'
import { useBreakpoint } from '../composables/useBreakpoint'
import { formatMoney, formatDate, relativeDate, categoryTypeLabel, errorMessage } from '../utils/format'
import PageHeader from '../components/PageHeader.vue'
import StatCard from '../components/StatCard.vue'
import StatusTag from '../components/StatusTag.vue'
import EmptyState from '../components/EmptyState.vue'

const { isMobile } = useBreakpoint()

const statistics = ref(null)
const upcomingPayments = ref([])
const categoryStatistics = ref([])
const loading = ref(false)

const URGENCY = {
  Overdue: { label: 'Просрочен', tone: 'rose' },
  Today: { label: 'Сегодня', tone: 'rose' },
  Tomorrow: { label: 'Завтра', tone: 'amber' },
  'In 2 Days': { label: 'Через 2 дня', tone: 'amber' },
  'In 3 Days': { label: 'Через 3 дня', tone: 'amber' },
  'This Week': { label: 'На этой неделе', tone: 'blue' },
  'This Month': { label: 'В этом месяце', tone: 'slate' },
  Later: { label: 'Позже', tone: 'slate' }
}

function urgency(value) {
  return URGENCY[value] ?? { label: value ?? '—', tone: 'slate' }
}

const upcomingSorted = computed(() =>
  [...upcomingPayments.value].sort(
    (a, b) => new Date(a.nextBillingDate) - new Date(b.nextBillingDate)
  )
)

const nextPayment = computed(() => upcomingSorted.value[0] ?? null)

const spending = computed(() => {
  const source = [
    { currency: 'RUB', symbol: '₽', key: 'totalCostRub', total: statistics.value?.totalCostRub },
    { currency: 'USD', symbol: '$', key: 'totalCostUsd', total: statistics.value?.totalCostUsd },
    { currency: 'EUR', symbol: '€', key: 'totalCostEur', total: statistics.value?.totalCostEur }
  ]
  const used = source.filter((item) => Number(item.total) > 0)
  return used.length ? used : source.slice(0, 1)
})

const totalActive = computed(() =>
  categoryStatistics.value.reduce((sum, row) => sum + (Number(row.activeSubscriptions) || 0), 0)
)

function share(row) {
  if (!totalActive.value) return 0
  return Math.round(((Number(row.activeSubscriptions) || 0) / totalActive.value) * 100)
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
  <div v-loading="loading" class="page">
    <PageHeader title="Статистика" subtitle="Расходы, ближайшие списания и разрез по категориям">
      <template #actions>
        <el-button :icon="Refresh" @click="load">Обновить</el-button>
      </template>
    </PageHeader>

    <section class="tiles">
      <StatCard
        hero
        class="tiles__hero"
        label="Активные подписки"
        :value="statistics?.activeSubscriptions ?? '—'"
      />
      <StatCard
        v-for="item in spending"
        :key="item.currency"
        :label="`Расходы в месяц,\u00a0${item.symbol}`"
        :value="formatMoney(item.total, item.currency)"
      />
      <StatCard
        label="Ближайшее списание"
        :value="nextPayment ? formatDate(nextPayment.nextBillingDate) : '—'"
        :hint="nextPayment ? `${nextPayment.subscriptionName} · ${relativeDate(nextPayment.nextBillingDate)}` : 'Нет запланированных платежей'"
      />
    </section>

    <h2 class="section-title">Предстоящие платежи</h2>

    <EmptyState
      v-if="!loading && !upcomingSorted.length"
      title="Списаний не запланировано"
      text="Как только появится активная подписка с датой списания, платёж окажется здесь."
    >
      <template #icon><el-icon><TrendCharts /></el-icon></template>
    </EmptyState>

    <div v-else-if="isMobile" class="records">
      <article v-for="row in upcomingSorted" :key="row.subscriptionName + row.nextBillingDate" class="record">
        <div class="record__head">
          <span class="record__title">{{ row.subscriptionName }}</span>
          <StatusTag :tone="urgency(row.paymentUrgency).tone" :label="urgency(row.paymentUrgency).label" />
        </div>
        <div class="record__meta">
          <div>
            <p class="record__label">Сумма</p>
            <p class="record__value">{{ formatMoney(row.cost, row.currency) }}</p>
          </div>
          <div>
            <p class="record__label">Дата</p>
            <p class="record__value">{{ formatDate(row.nextBillingDate) }}</p>
          </div>
          <div>
            <p class="record__label">Категория</p>
            <p class="record__value">{{ row.categoryName }}</p>
          </div>
        </div>
      </article>
    </div>

    <el-table v-else :data="upcomingSorted">
      <el-table-column prop="subscriptionName" label="Подписка" min-width="180">
        <template #default="{ row }">
          <span class="cell-strong">{{ row.subscriptionName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="Категория" min-width="150" />
      <el-table-column label="Сумма" width="140" class-name="num-cell">
        <template #default="{ row }">{{ formatMoney(row.cost, row.currency) }}</template>
      </el-table-column>
      <el-table-column label="Дата списания" width="150" class-name="num-cell">
        <template #default="{ row }">{{ formatDate(row.nextBillingDate) }}</template>
      </el-table-column>
      <el-table-column label="Срочность" width="170">
        <template #default="{ row }">
          <StatusTag :tone="urgency(row.paymentUrgency).tone" :label="urgency(row.paymentUrgency).label" />
        </template>
      </el-table-column>
    </el-table>

    <h2 class="section-title">Расходы по категориям</h2>

    <EmptyState
      v-if="!loading && !categoryStatistics.length"
      title="Пока нечего разбивать"
      text="Разрез появится, когда у подписок будут категории."
    >
      <template #icon><el-icon><TrendCharts /></el-icon></template>
    </EmptyState>

    <div v-else-if="isMobile" class="records">
      <article v-for="row in categoryStatistics" :key="row.categoryName" class="record">
        <div class="record__head">
          <span class="record__title">{{ row.categoryName }}</span>
          <span class="muted">{{ categoryTypeLabel(row.categoryType) }}</span>
        </div>
        <div>
          <div class="share">
            <span>{{ row.activeSubscriptions }} активных</span>
            <span class="num">{{ share(row) }}%</span>
          </div>
          <div class="meter"><div class="meter__fill" :style="{ width: `${share(row)}%` }" /></div>
        </div>
        <div class="record__meta">
          <div v-for="item in spending" :key="item.currency">
            <p class="record__label">Сумма, {{ item.symbol }}</p>
            <p class="record__value">
              {{ formatMoney(row[item.key], item.currency) }}
            </p>
          </div>
        </div>
      </article>
    </div>

    <el-table v-else :data="categoryStatistics">
      <el-table-column prop="categoryName" label="Категория" min-width="170">
        <template #default="{ row }">
          <span class="cell-strong">{{ row.categoryName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Тип" width="160">
        <template #default="{ row }">{{ categoryTypeLabel(row.categoryType) }}</template>
      </el-table-column>
      <el-table-column label="Доля активных" min-width="180">
        <template #default="{ row }">
          <div class="share">
            <span class="num">{{ row.activeSubscriptions }}</span>
            <span class="num muted">{{ share(row) }}%</span>
          </div>
          <div class="meter"><div class="meter__fill" :style="{ width: `${share(row)}%` }" /></div>
        </template>
      </el-table-column>
      <el-table-column label="Сумма, ₽" width="140" class-name="num-cell">
        <template #default="{ row }">{{ formatMoney(row.totalCostRub, 'RUB') }}</template>
      </el-table-column>
      <el-table-column label="Сумма, $" width="140" class-name="num-cell">
        <template #default="{ row }">{{ formatMoney(row.totalCostUsd, 'USD') }}</template>
      </el-table-column>
      <el-table-column label="Сумма, €" width="140" class-name="num-cell">
        <template #default="{ row }">{{ formatMoney(row.totalCostEur, 'EUR') }}</template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.tiles {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
}

.tiles > * {
  flex: 1 1 190px;
}

.tiles__hero {
  flex: 1.5 1 260px;
}

.cell-strong {
  font-weight: 600;
}

.share {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
  font-size: 13px;
  margin-bottom: 6px;
}

@media (max-width: 767px) {
  .tiles {
    gap: 10px;
  }

  .tiles > * {
    flex: 1 1 140px;
  }

  .tiles__hero {
    flex: 1 1 100%;
  }
}
</style>
