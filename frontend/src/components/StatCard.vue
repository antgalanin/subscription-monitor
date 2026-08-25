<script setup>
defineProps({
  label: { type: String, required: true },
  value: { type: [String, Number], default: '—' },
  hint: { type: String, default: '' },
  hero: { type: Boolean, default: false }
})
</script>

<template>
  <div class="stat" :class="{ 'stat--hero': hero }">
    <p class="stat__label">{{ label }}</p>
    <p class="stat__value">{{ value }}</p>
    <p v-if="hint" class="stat__hint">{{ hint }}</p>
  </div>
</template>

<style scoped>
.stat {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  box-shadow: var(--shadow-sm);
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.stat__label {
  font-size: 13px;
  color: var(--ink-3);
}

.stat__value {
  font-size: 26px;
  font-weight: 650;
  letter-spacing: -0.02em;
  line-height: 1.15;
  overflow-wrap: anywhere;
}

.stat--hero {
  background: var(--brand-gradient);
  border-color: transparent;
  color: #05231C;
}

.stat--hero .stat__label,
.stat--hero .stat__hint {
  color: rgba(5, 35, 28, 0.72);
}

.stat--hero .stat__value {
  color: #05231C;
  font-size: 48px;
  font-weight: 700;
}

.stat__hint {
  font-size: 12px;
  color: var(--ink-3);
}

@media (max-width: 767px) {
  .stat {
    padding: 14px 16px;
  }

  .stat__value {
    font-size: 22px;
  }

  .stat--hero {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    gap: 14px;
  }

  .stat--hero .stat__value {
    font-size: 40px;
  }
}
</style>
