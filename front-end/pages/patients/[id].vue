<template>
  <div>
    <div v-if="userPending" class="mb-6">
      <div class="h-10 w-1/2 animate-pulse rounded bg-gray-200"></div>
    </div>
    <div v-else-if="user" class="mb-6">
      <h1 class="text-3xl font-bold text-gray-900">{{ user.fullName }}</h1>
      <p class="text-gray-600">{{ user.email }}</p>
    </div>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <div class="lg:col-span-2">
        <UICard title="Planos de Reabilitação">
          <div v-if="plansPending">Carregando planos...</div>
          <div v-else-if="plansError" class="alert alert-danger">Erro ao carregar planos.</div>
          <div v-else-if="plans && plans.length > 0" class="flex flex-col gap-3">
            <div v-for="plan in plans" :key="plan.id" class="plan-card">
              <div>
                <h4 class="font-semibold">{{ parsePlanData(plan.planData).title || 'Plano sem título' }}</h4>
                <p class="text-sm text-gray-600">
                  Versão: {{ plan.version }} | Criado em: {{ formatDate(plan.createdAt) }}
                </p>
              </div>
              <span class="badge" :class="getPlanStatusClass(plan.status)">
                {{ plan.status }}
              </span>
            </div>
          </div>
          <div v-else class="text-gray-500">Nenhum plano de reabilitação encontrado.</div>
        </UICard>

        <UICard title="Histórico de Notas Clínicas" class="mt-6">
          <div v-if="notesPending">Carregando notas...</div>
          <div v-else-if="notesError" class="alert alert-danger">Erro ao carregar notas.</div>
          <div v-else-if="notes && notes.length > 0" class="flex flex-col gap-3">
            <div v-for="note in notes" :key="note.id" class="history-note">
              <p>{{ note.note }}</p>
              <span class="text-xs text-gray-500">Registrado em: {{ formatDate(note.timestamp) }}</span>
            </div>
          </div>
          <div v-else class="text-gray-500">Nenhuma nota clínica registrada.</div>
        </UICard>
      </div>

      <div class="lg:col-span-1">
        <UICard title="Condições Médicas (API Seção 5.4)">
          <div v-if="conditionsPending">Carregando condições...</div>
          <div v-else-if="conditionsError" class="alert alert-danger">Erro ao carregar condições.</div>
          <div v-else-if="conditions && conditions.length > 0" class="flex flex-col gap-2">
            <div v-for="condition in conditions" :key="condition.id" class="condition-item">
              <strong class="text-sm">{{ condition.description }}</strong>
              <span class="text-xs text-gray-500">Início: {{ formatDate(condition.onsetDate) }}</span>
            </div>
          </div>
          <div v-else class="text-gray-500">Nenhuma condição registrada.</div>
        </UICard>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ 
  middleware: ['auth-only'],
  layout: 'default'
})

const route = useRoute();
const userId = route.params.id as string;

interface PatientUser { id: string, email: string, fullName: string }
interface Plan {
  id: string;
  version: number;
  planData: string;
  status: 'DRAFT' | 'APPROVED' | 'ARCHIVED';
  createdAt: string;
}
interface ClinicalNote { id: string; note: string; timestamp: string; }
interface MedicalCondition { id: string; description: string; onsetDate: string; }
interface PlanData { title?: string; }

const { data: user, pending: userPending } = await useApiFetch<PatientUser>(`/users/${userId}`, { lazy: true });

const { data: plans, pending: plansPending, error: plansError } = await useApiFetch<Plan[]>(`/plans/user/${userId}`, { lazy: true });

const { data: notes, pending: notesPending, error: notesError } = await useApiFetch<ClinicalNote[]>(`/patients/${userId}/history/notes`, { lazy: true });

const { data: conditions, pending: conditionsPending, error: conditionsError } = await useApiFetch<MedicalCondition[]>(`/patients/${userId}/conditions`, { lazy: true });


function parsePlanData(planDataString: string): PlanData {
  try {
    return JSON.parse(planDataString) as PlanData;
  } catch (e) {
    console.error('Falha ao fazer parse do planData:', e);
    return { title: 'Erro ao carregar plano' };
  }
}
function formatDate(dateString: string) {
  if (!dateString) return 'Data não informada';
  return new Date(dateString).toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
}

function getPlanStatusClass(status: string) {
  switch (status) {
    case 'APPROVED': return 'badge-success';
    case 'DRAFT': return 'badge-warning';
    case 'ARCHIVED': return 'badge-secondary';
    default: return 'badge-secondary';
  }
}
</script>

<style scoped>
.alert-danger { @apply border-danger bg-red-50 text-red-800; }
.plan-card { @apply flex items-center justify-between rounded-lg border border-gray-200 bg-gray-50 p-4; }
.history-note { @apply rounded-lg border-b border-gray-200 p-3; }
.condition-item { @apply flex flex-col rounded-lg bg-gray-50 p-3; }

.badge { @apply shrink-0 rounded-full px-3 py-0.5 text-xs font-semibold; }
.badge-success { @apply bg-green-100 text-green-800; }
.badge-warning { @apply bg-yellow-100 text-yellow-800; }
.badge-secondary { @apply bg-gray-100 text-gray-700; }
</style>