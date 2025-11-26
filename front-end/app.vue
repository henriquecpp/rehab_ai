<template>
  <div
    v-if="authStore.isAuthLoading"
    class="fixed inset-0 flex items-center justify-center bg-gray-100"
  >
    <p>Carregando...</p>
  </div>

  <NuxtLayout v-if="!authStore.isAuthLoading">
    <NuxtPage />
  </NuxtLayout>
</template>

<script setup lang="ts">
const authStore = useAuthStore();
import { onMounted } from "vue";
onMounted(() => {
  authStore.initAuth();
});
</script>
