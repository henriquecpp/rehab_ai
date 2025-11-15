<template>
  <div class="rounded-xl bg-white p-6 shadow-[0_1px_3px_rgba(0,0,0,0.1)]">
    <h2 v-if="title" class="mb-4 text-lg font-semibold text-gray-900">
      {{ title }}
    </h2>
    <slot />
  </div>
</template>

<script setup lang="ts">
defineProps({
  title: {
    type: String,
    default: ''
  }
})
</script>