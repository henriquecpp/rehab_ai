<script setup lang="ts">
definePageMeta({ middleware: ['auth-only'] })

import { useAuthStore } from '~/store/auth';
const authStore = useAuthStore();

if (authStore.isClinician) {
  await navigateTo('/patients');
} else if (authStore.isPatient) {
  if (authStore.user?.id) {
    await navigateTo(`/patients/${authStore.user.id}`);
  } else {
    console.error("Não foi possível obter o ID do paciente para redirecionamento.");
    await authStore.logout();
  }
} else {
  await navigateTo('/login');
}
</script>