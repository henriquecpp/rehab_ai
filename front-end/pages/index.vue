<script setup lang="ts">
definePageMeta({ middleware: ['auth-only'] })

import { useAuthStore } from '~/store/auth';
const authStore = useAuthStore();

if (authStore.isClinician) {
  await navigateTo('/patients');
} else if (authStore.isPatient) {
  await navigateTo('/patients');
} else {
  await navigateTo('/login');
}
</script>