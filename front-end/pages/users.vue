<template>
  <UICard title="Gestão de Usuários">
    <div class="mb-4 flex gap-4">
      <div class="flex-1">
        <label class="form-label">Filtrar por Função</label>
        <select v-model="selectedRole" class="form-input">
          <option value="">Todos</option>
          <option value="ADMIN">Admin</option>
          <option value="CLINICIAN">Clinician</option>
          <option value="PATIENT">Patient</option>
        </select>
      </div>
      <div class="flex-1">
        <label class="form-label">Filtrar por Status</label>
        <select v-model="selectedStatus" class="form-input">
          <option value="">Todos</option>
          <option value="true">Ativos</option>
          <option value="false">Inativos</option>
        </select>
      </div>
    </div>
    
    <div v-if="pending" class="py-10 text-center text-gray-600">
      Carregando usuários...
    </div>
    
    <div v-else-if="error" class="alert alert-danger">
      Erro ao carregar usuários: {{ error.message }}
    </div>
    
    <div v-else-if="users" class="grid gap-3">
      <div v-for="user in users" :key="user.id" class="user-card">
        <div class="flex items-center gap-4">
          <div class="user-avatar">
            {{ getInitials(user.fullName) }}
          </div>
          <div>
            <h3 class="text-base font-semibold text-gray-900">{{ user.fullName }}</h3>
            <p class="text-sm text-gray-600">{{ user.email }}</p>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <span class="badge" :class="getRoleClass(user.role)">
            {{ user.role }}
          </span>
          <span class="badge" :class="user.active ? 'badge-success' : 'badge-warning'">
            {{ user.active ? 'Ativo' : 'Inativo' }}
          </span>
        </div>
      </div>
    </div>
  </UICard>
</template>

<script setup lang="ts">
definePageMeta({ 
  middleware: ['auth-only'],
  layout: 'default'
})

interface ApiUser {
  id: string;
  email: string;
  fullName: string;
  role: 'PATIENT' | 'CLINICIAN' | 'ADMIN';
  active: boolean;
}

const selectedRole = ref<'PATIENT' | 'CLINICIAN' | 'ADMIN' | ''>('');
const selectedStatus = ref<'true' | 'false' | ''>('');

const queryParams = computed(() => {
  const params: Record<string, string> = {};
  if (selectedRole.value) {
    params.role = selectedRole.value;
  }
  if (selectedStatus.value) {
    params.activeOnly = selectedStatus.value;
  }
  return params;
});

const { data: users, pending, error } = await useApiFetch<ApiUser[]>(
  '/users', 
  {
    query: queryParams,
    watch: [queryParams]
  }
)

function getInitials(name: string) {
  if (!name) return '';
  return name.split(' ').map(n => n[0]).join('').toUpperCase();
}

function getRoleClass(role: string) {
  switch (role) {
    case 'ADMIN': return 'badge-danger';
    case 'CLINICIAN': return 'badge-info';
    case 'PATIENT': return 'badge-secondary';
    default: return 'badge-secondary';
  }
}
</script>

<style scoped>
.form-input { @apply w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm shadow-sm; }
.form-label { @apply mb-1 block text-xs font-semibold text-gray-600; }
.user-card { @apply flex items-center justify-between rounded-lg border border-gray-200 bg-white p-4; }
.user-avatar { @apply flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-gray-200 font-semibold text-gray-700; }
.badge { @apply shrink-0 rounded-full px-3 py-0.5 text-xs font-semibold; }
.badge-success { @apply bg-green-100 text-green-800; }
.badge-warning { @apply bg-yellow-100 text-yellow-800; }
.badge-danger { @apply bg-red-100 text-red-800; }
.badge-info { @apply bg-blue-100 text-blue-800; }
.badge-secondary { @apply bg-gray-100 text-gray-700; }
.alert-danger { @apply mb-4 flex gap-3 rounded-lg border-l-4 border-danger bg-red-50 p-4 text-red-800; }
</style>