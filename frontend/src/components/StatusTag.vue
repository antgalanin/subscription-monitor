<script setup>
defineProps({
  tone: { type: String, default: 'slate' },
  label: { type: String, required: true },
  dot: { type: Boolean, default: true }
})
</script>

<template>
  <span class="tag" :class="`tag--${tone}`">
    <span v-if="dot" class="tag__dot" aria-hidden="true" />
    {{ label }}
  </span>
</template>

<style scoped>
.tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 10px;
  border-radius: var(--r-full);
  font-size: 12px;
  font-weight: 600;
  line-height: 1.5;
  white-space: nowrap;
  color: var(--tone);
  background: color-mix(in srgb, var(--tone) 14%, transparent);
  border: 1px solid color-mix(in srgb, var(--tone) 30%, transparent);
}

.tag__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.tag--mint {
  --tone: var(--tone-mint);
}
.tag--amber {
  --tone: var(--tone-amber);
}
.tag--rose {
  --tone: var(--tone-rose);
}
.tag--blue {
  --tone: var(--tone-blue);
}
.tag--slate {
  --tone: var(--tone-slate);
}
</style>
