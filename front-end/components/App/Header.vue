<template>
  <header class="sticky top-0 z-50 flex items-center justify-between border-b border-gray-200 bg-white px-8 py-4">
    <h1 class="text-2xl font-semibold text-gray-900">
      {{ pageTitle }}
    </h1>
    
    <div v-if="authStore.user" class="flex items-center gap-3">
      <div class="flex h-10 w-10 items-center justify-center rounded-full bg-primary font-semibold text-white">
        {{ getInitials(authStore.user.fullName) }}
      </div>
      <div>
        <div class="font-semibold">{{ authStore.user.fullName }}</div>
        <div class="text-xs text-gray-600">{{ authStore.user.role }}</div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/store/auth';
const authStore = useAuthStore();
const route = useRoute();

const patientName = useNuxtData(route.path.startsWith('/users/') ? `/users/${route.params.id}` : 'non-user-key')?.value?.fullName;

const pageTitle = computed(() => {
  if (route.name === 'patients-id' && patientName) {
    return patientName;
  }
  

  const menu = {
    '/patients': 'Pacientes',
    '/prescribe': 'Nova Prescrição',
  }
  return menu[route.path as keyof typeof menu] || 'Dashboard';
})

function getInitials(name: string) {
  if (!name) return '';
  return name.split(' ').map(n => n[0]).join('').toUpperCase();
}
</script>