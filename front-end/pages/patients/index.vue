<template>
  <UICard title="Gestão de Pacientes">
    <div
      class="mb-4 flex flex-col items-center justify-between gap-4 md:flex-row"
    >
      <div class="w-full md:w-2/3 lg:w-1/2">
        <input
          type="text"
          v-model="searchQuery"
          class="form-input"
          placeholder="🔍 Buscar paciente por nome ou email..."
        />
      </div>
      <button class="btn-primary w-full md:w-auto" @click="isModalOpen = true">
        Cadastrar Novo Paciente
      </button>
    </div>

    <div v-if="pending" class="py-10 text-center text-gray-600">
      Carregando pacientes...
    </div>

    <div v-else-if="error" class="alert alert-danger">
      Erro ao carregar pacientes: {{ error.message }}
    </div>

    <div v-else-if="patients && filteredPatients.length > 0" class="grid gap-3">
      <NuxtLink
        v-for="patient in filteredPatients"
        :key="patient.id"
        :to="`/patients/${patient.id}`"
        class="patient-card"
      >
        <div class="flex items-center gap-4">
          <div class="patient-avatar">
            {{ getInitials(patient.fullName) }}
          </div>
          <div>
            <h3 class="text-base font-semibold text-gray-900">
              {{ patient.fullName }}
            </h3>
            <p class="text-sm text-gray-600">{{ patient.email }}</p>
          </div>
        </div>
        <span
          class="badge"
          :class="patient.active ? 'badge-success' : 'badge-warning'"
        >
          {{ patient.active ? "Ativo" : "Inativo" }}
        </span>
      </NuxtLink>
    </div>

    <div v-else class="py-10 text-center text-gray-500">
      <p v-if="searchQuery">
        Nenhum paciente encontrado para "{{ searchQuery }}".
      </p>
      <p v-else>Nenhum paciente cadastrado ainda.</p>
    </div>
  </UICard>

  <PatientRegisterModal
    v-model="isModalOpen"
    @patient-created="handlePatientCreated"
  />
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import PatientRegisterModal from "~/components/Patient/RegisterModal.vue";

definePageMeta({
  middleware: ["auth-only"],
  layout: "default",
});

const authStore = useAuthStore();
if (!authStore.isClinician) {
  await navigateTo("/");
}

interface PatientUser {
  id: string;
  email: string;
  fullName: string;
  role: "PATIENT";
  active: boolean;
}

const {
  data: patients,
  pending,
  error,
  refresh,
} = await useApiFetch<PatientUser[]>("/users", {
  query: {
    role: "PATIENT",
  },
  lazy: true,
});

const isModalOpen = ref(false);
const searchQuery = ref("");

const filteredPatients = computed(() => {
  if (!patients.value) return [];
  if (!searchQuery.value) return patients.value;

  return patients.value.filter(
    (p) =>
      p.fullName.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      p.email.toLowerCase().includes(searchQuery.value.toLowerCase())
  );
});

function getInitials(name: string) {
  if (!name) return "";
  return name
    .split(" ")
    .map((n) => n[0])
    .join("")
    .toUpperCase();
}

async function handlePatientCreated() {
  console.log("Paciente criado! Recarregando lista...");
  await refresh();
}
</script>

<style scoped>
.form-input {
  @apply w-full rounded-lg border border-gray-300 px-3 py-2.5 text-sm shadow-sm transition focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary;
}
.patient-card {
  @apply flex cursor-pointer items-center justify-between rounded-lg border border-gray-200 bg-white p-4 transition hover:border-primary hover:shadow-md;
}
.patient-avatar {
  @apply flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-gray-200 font-semibold text-gray-700;
}
.badge {
  @apply shrink-0 rounded-full px-3 py-0.5 text-xs font-semibold;
}
.badge-success {
  @apply bg-green-100 text-green-800;
}
.badge-warning {
  @apply bg-yellow-100 text-yellow-800;
}
.alert-danger {
  @apply rounded-lg border border-danger bg-red-50 p-3 text-sm text-red-800;
}

.btn-primary {
  @apply flex items-center justify-center gap-2 rounded-lg bg-primary px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-primary-dark disabled:opacity-70;
}
</style>
