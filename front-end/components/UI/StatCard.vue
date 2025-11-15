<template>
  <div class="rounded-xl bg-white p-6 shadow-[0_1px_3px_rgba(0,0,0,0.1)]" :class="borderColorClass">
    <div class="mb-2 text-sm font-medium text-gray-600">{{ label }}</div>
    <div class="text-3xl font-bold text-gray-900">{{ value }}</div>
    <div v-if="changeText" class="mt-2 flex items-center gap-1 text-sm" :class="changeColorClass">
      <span v-if="changeType === 'positive'">↑</span>
      <span v-if="changeType === 'negative'">↓</span>
      <span>{{ changeText }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps({
  label: String,
  value: [String, Number],
  changeText: String,
  changeType: {
    type: String,
    default: 'neutral' // 'positive', 'negative', 'neutral'
  },
  color: {
    type: String,
    default: 'primary' // 'primary', 'success', 'warning', 'danger'
  }
})

const borderColorClass = computed(() => {
  return {
    'primary': 'border-l-4 border-primary',
    'success': 'border-l-4 border-success',
    'warning': 'border-l-4 border-warning',
    'danger': 'border-l-4 border-danger',
  }[props.color] || 'border-l-4 border-gray-300'
})

const changeColorClass = computed(() => {
  return {
    'positive': 'text-success',
    'negative': 'text-danger',
    'neutral': 'text-gray-600',
  }[props.changeType]
})
</script>