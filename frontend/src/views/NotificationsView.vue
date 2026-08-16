<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { notificationsApi } from '../api'
import { notificationTypeLabel, formatDateTime, errorMessage } from '../utils/format'

const notifications = ref([])
const loading = ref(false)

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
  <div>
    <div class="toolbar">
      <h2>Уведомления</h2>
      <el-button @click="load">Обновить</el-button>
    </div>

    <el-table v-loading="loading" :data="notifications" border stripe>
      <el-table-column label="Дата" width="160">
        <template #default="{ row }">{{ formatDateTime(row.notificationDate) }}</template>
      </el-table-column>
      <el-table-column label="Тип" width="200">
        <template #default="{ row }">
          <el-tag :type="row.type === 'UPCOMING_PAYMENT' ? 'warning' : 'success'">
            {{ notificationTypeLabel(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="message" label="Сообщение" min-width="300" />
      <el-table-column label="Действия" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="remove(row)">Удалить</el-button>
        </template>
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
</style>
