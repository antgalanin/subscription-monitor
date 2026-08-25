<script setup>
defineProps({
  title: { type: String, required: true },
  text: { type: String, default: '' }
})
</script>

<template>
  <div class="empty">
    <div class="empty__icon" aria-hidden="true">
      <slot name="icon" />
    </div>
    <p class="empty__title">{{ title }}</p>
    <p v-if="text" class="empty__text">{{ text }}</p>
    <div v-if="$slots.action" class="empty__action">
      <slot name="action" />
    </div>
  </div>
</template>

<style scoped>
.empty {
  background: var(--surface);
  border: 1px dashed var(--border-strong);
  border-radius: var(--r-md);
  padding: 48px 24px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.empty__icon {
  width: 52px;
  height: 52px;
  border-radius: var(--r-md);
  display: grid;
  place-items: center;
  margin-bottom: 8px;
  color: var(--el-color-primary);
  background: color-mix(in srgb, var(--el-color-primary) 12%, transparent);
  font-size: 24px;
}

.empty__title {
  font-size: 16px;
  font-weight: 600;
}

.empty__text {
  color: var(--ink-3);
  font-size: 14px;
  max-width: 42ch;
}

.empty__action {
  margin-top: 12px;
}

@media (max-width: 767px) {
  .empty {
    padding: 36px 18px;
  }
}
</style>
