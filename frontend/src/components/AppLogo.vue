<script setup>
import { useId } from 'vue'

defineProps({
  size: { type: [Number, String], default: 32 }
})

const gradientId = `sm-logo-${useId()}`
</script>

<template>
  <svg
    class="logo"
    :width="size"
    :height="size"
    viewBox="0 0 48 48"
    role="img"
    aria-label="Subscription Monitor"
    xmlns="http://www.w3.org/2000/svg"
  >
    <defs>
      <linearGradient :id="gradientId" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0" stop-color="#34E7B8" />
        <stop offset="1" stop-color="#0E9E7E" />
      </linearGradient>
    </defs>
    <rect width="48" height="48" rx="13" :fill="`url(#${gradientId})`" />
    <path
      d="M31.78 16.22A11 11 0 1 1 20.24 13.66"
      fill="none"
      stroke="#05231C"
      stroke-width="4.6"
      stroke-linecap="round"
    />
    <path d="M26.07 11.54 16.12 9.95 19.48 19.15Z" fill="#05231C" />
  </svg>
</template>

<style scoped>
.logo {
  flex: none;
  border-radius: 27%;
}
</style>
