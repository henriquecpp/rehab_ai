<template>
  <UICard title="Gestão de Pacientes">
    <div class="mb-4">
      <input type="text" v-model="searchQuery" class="form-input" placeholder="🔍 Buscar paciente...">
    </div>
    
    <div v-if="pending" class="py-10 text-center text-gray-600">
      Carregando pacientes...
    </div>
    
    <div v-else-if="error" class="alert alert-danger">
      Erro ao carregar pacientes: {{ error.message }}
    </div>
    
    <div v-else-if="patients" class="grid gap-3">
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
            <h3 class="text-base font-semibold text-gray-900">{{ patient.fullName }}</h3>
            <p class="text-sm text-gray-600">{{ patient.email }}</p>
          </div>
        </div>
        <span class="badge" :class="patient.active ? 'badge-success' : 'badge-warning'">
          {{ patient.active ? 'Ativo' : 'Inativo' }}
        </span>
      </NuxtLink>
    </div>
  </UICard>
</template>

<script setup lang="ts">
definePageMeta({ 
  middleware: ['auth-only'],
  layout: 'default'
})

interface PatientUser {
  id: string;
  email: string;
  fullName: string;
  role: 'PATIENT';
  active: boolean;
}

const { data: patients, pending, error } = await useApiFetch<PatientUser[]>(
  '/users', 
  {
    query: {
      role: 'PATIENT',
    },
    lazy: true 
  }
)

const searchQuery = ref('')

// Filtro local simples
const filteredPatients = computed(() => {
  if (!patients.value) return []
  if (!searchQuery.value) return patients.value
  
  return patients.value.filter(p => 
    p.fullName.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    p.email.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

function getInitials(name: string) {
  if (!name) return '';
  return name.split(' ').map(n => n[0]).join('').toUpperCase();
}
</script>

<style scoped>
.form-input { @apply w-full rounded-lg border border-gray-300 px-3 py-2.5 text-sm shadow-sm; }
.patient-card { @apply flex cursor-pointer items-center justify-between rounded-lg border border-gray-200 bg-white p-4 transition hover:border-primary hover:shadow-md; }
.patient-avatar { @apply flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-gray-200 font-semibold text-gray-700; }
.badge { @apply shrink-0 rounded-full px-3 py-0.5 text-xs font-semibold; }
.badge-success { @apply bg-green-100 text-green-800; }
.badge-warning { @apply bg-yellow-100 text-yellow-800; }
.alert-danger { @apply border-danger bg-red-50 text-red-800; }
</style>