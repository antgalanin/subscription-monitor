<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, CircleCheck, Clock, Delete, Refresh } from '@element-plus/icons-vue'
import { notificationsApi } from '../api'
import { notificationTypeLabel, formatDateTime, pluralize, errorMessage } from '../utils/format'
import PageHeader from '../components/PageHeader.vue'
import StatusTag from '../components/StatusTag.vue'
import EmptyState from '../components/EmptyState.vue'

const notifications = ref([])
const loading = ref(false)

const sorted = computed(() =>
  [...notifications.value].sort(
    (a, b) => new Date(b.notificationDate) - new Date(a.notificationDate)
  )
)

const subtitle = computed(() => {
  const total = notifications.value.length
  if (!total) return 'Новых уведомлений нет'
  return `${total} ${pluralize(total, ['уведомление', 'уведомления', 'уведомлений'])}`
})

function isUpcoming(row) {
  return row.type === 'UPCOMING_PAYMENT'
}

async function load() {
  loading.value = true
  try {
    const { data } = await notificationsApi.myReceived()
    notifications.value = data
  } catch (error) {
    ElMessage.error(errorMessage(error, 'Не удалось загрузить уведомления'))
  } finally {
    loading.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm('Удалить уведомление?', 'Подтверждение', {
      confirmButtonText: 'Удалить',
      cancelButtonText: 'Отмена',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await notificationsApi.remove(row.id)
    ElMessage.success('Уведомление удалено')
    await load()
  } catch (error) {
    ElMessage.error(errorMessage(error, 'Не удалось удалить уведомление'))
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <PageHeader title="Уведомления" :subtitle="subtitle">
      <template #actions>
        <el-button :icon="Refresh" @click="load">Обновить</el-button>
      </template>
    </PageHeader>

    <EmptyState
      v-if="!loading && !sorted.length"
      title="Уведомлений нет"
      text="Здесь появятся напоминания о ближайших списаниях и отметки о прошедших платежах."
    >
      <template #icon><el-icon><Bell /></el-icon></template>
    </EmptyState>

    <div v-else v-loading="loading" class="feed">
      <article v-for="row in sorted" :key="row.id" class="item">
        <span class="item__icon" :class="isUpcoming(row) ? 'item__icon--amber' : 'item__icon--mint'">
          <el-icon :size="18">
            <Clock v-if="isUpcoming(row)" />
            <CircleCheck v-else />
          </el-icon>
        </span>
        <div class="item__body">
          <div class="item__top">
            <StatusTag
              :tone="isUpcoming(row) ? 'amber' : 'mint'"
              :label="notificationTypeLabel(row.type)"
              :dot="false"
            />
            <time class="item__date num">{{ formatDateTime(row.notificationDate) }}</time>
          </div>
          <p class="item__message">{{ row.message }}</p>
        </div>
        <el-button
          class="item__remove"
          :icon="Delete"
          circle
          text
          type="danger"
          title="Удалить"
          @click="remove(row)"
        />
      </article>
    </div>
  </div>
</template>

<style scoped>
.feed {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  box-shadow: var(--shadow-sm);
  transition: border-color 0.15s var(--ease);
}

.item:hover {
  border-color: var(--border-strong);
}

.item__icon {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  flex: none;
  border-radius: var(--r-sm);
}

.item__icon--amber {
  color: var(--tone-amber);
  background: color-mix(in srgb, var(--tone-amber) 14%, transparent);
}

.item__icon--mint {
  color: var(--tone-mint);
  background: color-mix(in srgb, var(--tone-mint) 14%, transparent);
}

.item__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.item__top {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.item__date {
  font-size: 12px;
  color: var(--ink-3);
}

.item__message {
  font-size: 14px;
  color: var(--ink);
  overflow-wrap: anywhere;
}

.item__remove {
  flex: none;
  margin-top: 2px;
}

@media (max-width: 767px) {
  .item {
    padding: 14px;
    gap: 12px;
  }

  .item__icon {
    width: 34px;
    height: 34px;
  }
}
</style>
