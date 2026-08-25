<script setup>
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' }
})
</script>

<template>
  <header class="head">
    <div class="head__text">
      <h1 class="head__title">{{ title }}</h1>
      <p v-if="subtitle" class="head__subtitle">{{ subtitle }}</p>
    </div>
    <div v-if="$slots.actions" class="head__actions">
      <slot name="actions" />
    </div>
  </header>
</template>

<style scoped>
.head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.head__title {
  font-size: 26px;
  font-weight: 650;
}

.head__subtitle {
  margin-top: 4px;
  color: var(--ink-3);
  font-size: 14px;
}

.head__actions {
  display: flex;
  gap: 10px;
}

@media (max-width: 767px) {
  .head {
    align-items: stretch;
  }

  .head__title {
    font-size: 22px;
  }

  .head__actions {
    width: 100%;
  }

  .head__actions :deep(.el-button),
  .head__actions :deep(.el-select) {
    flex: 1;
    min-width: 0;
    margin-left: 0;
  }
}
</style>
