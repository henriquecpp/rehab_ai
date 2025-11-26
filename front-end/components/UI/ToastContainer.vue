<template>
  <div
    class="fixed top-4 right-4 z-[10000] flex flex-col gap-2 pointer-events-none"
  >
    <TransitionGroup name="toast">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="pointer-events-auto min-w-[300px] max-w-md p-4 rounded-lg shadow-lg flex items-start justify-between gap-3 transform transition-all duration-300"
        :class="{
          'bg-green-50 text-green-800 border border-green-200':
            toast.type === 'success',
          'bg-red-50 text-red-800 border border-red-200':
            toast.type === 'error',
          'bg-blue-50 text-blue-800 border border-blue-200':
            toast.type === 'info',
          'bg-yellow-50 text-yellow-800 border border-yellow-200':
            toast.type === 'warning',
        }"
      >
        <div class="flex items-center gap-2">
          <span v-if="toast.type === 'success'" class="text-xl">✓</span>
          <span v-else-if="toast.type === 'error'" class="text-xl">✕</span>
          <span v-else-if="toast.type === 'warning'" class="text-xl">⚠</span>
          <span v-else class="text-xl">ℹ</span>
          <p class="text-sm font-medium">{{ toast.message }}</p>
        </div>
        <button
          @click="removeToast(toast.id)"
          class="text-gray-400 hover:text-gray-600 transition-colors"
        >
          ✕
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup lang="ts">
import { useToast } from "~/composables/useToast";

const { toasts, removeToast } = useToast();
</script>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
