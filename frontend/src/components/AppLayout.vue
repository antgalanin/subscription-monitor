<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { Monitor } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

async function handleLogout() {
  await auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <el-container class="layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon :size="22"><Monitor /></el-icon>
        <span>Subscription Monitor</span>
      </div>
      <el-menu router :default-active="route.path" class="menu">
        <el-menu-item index="/subscriptions">Подписки</el-menu-item>
        <el-menu-item index="/categories">Категории</el-menu-item>
        <el-menu-item index="/notifications">Уведомления</el-menu-item>
        <el-menu-item index="/statistics">Статистика</el-menu-item>
        <el-menu-item v-if="auth.isAdmin" index="/users">Пользователи</el-menu-item>
        <el-menu-item index="/profile">Профиль</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="username">{{ auth.user?.username }}</span>
        <el-button text @click="handleLogout">Выйти</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  min-height: 100vh;
}
.sidebar {
  background-color: #ffffff;
  border-right: 1px solid #e4e7ed;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 18px 16px;
  font-weight: 600;
}
.menu {
  border-right: none;
}
.header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  background-color: #ffffff;
  border-bottom: 1px solid #e4e7ed;
}
.username {
  color: #606266;
}
</style>
