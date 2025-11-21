<template>
  <div>
    <div v-if="userPending" class="mb-6">
      <div class="h-10 w-1/2 animate-pulse rounded bg-gray-200"></div>
    </div>
    <div v-else-if="user" class="mb-6">
      <h1 class="text-3xl font-bold text-gray-900">{{ user.fullName }}</h1>
      <p class="text-gray-600">{{ user.email }}</p>
    </div>

    <div
      v-if="authStore.isClinician"
      class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6"
    >
      <NuxtLink
        :to="`/patients/${patientId}/history`"
        class="quick-action-card"
      >
        <div class="flex items-center gap-3">
          <div class="icon-circle bg-blue-100 text-blue-600">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-6 w-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
              />
            </svg>
          </div>
          <div>
            <h3 class="font-semibold text-gray-900">Histórico Médico</h3>
            <p class="text-sm text-gray-600">Notas, condições, alergias</p>
          </div>
        </div>
      </NuxtLink>

      <NuxtLink
        :to="`/patients/${patientId}/medical-records`"
        class="quick-action-card"
      >
        <div class="flex items-center gap-3">
          <div class="icon-circle bg-green-100 text-green-600">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-6 w-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          </div>
          <div>
            <h3 class="font-semibold text-gray-900">Registros Médicos</h3>
            <p class="text-sm text-gray-600">
              Perfil, sinais vitais, medicações
            </p>
          </div>
        </div>
      </NuxtLink>

      <NuxtLink
        :to="`/patients/${patientId}/prescriptions`"
        class="quick-action-card"
      >
        <div class="flex items-center gap-3">
          <div class="icon-circle bg-purple-100 text-purple-600">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-6 w-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
              />
            </svg>
          </div>
          <div>
            <h3 class="font-semibold text-gray-900">Prescrições IA</h3>
            <p class="text-sm text-gray-600">Planos gerados por IA</p>
          </div>
        </div>
      </NuxtLink>

      <NuxtLink :to="`/patients/${patientId}/plans`" class="quick-action-card">
        <div class="flex items-center gap-3">
          <div class="icon-circle bg-orange-100 text-orange-600">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-6 w-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"
              />
            </svg>
          </div>
          <div>
            <h3 class="font-semibold text-gray-900">Planos</h3>
            <p class="text-sm text-gray-600">Planos de reabilitação</p>
          </div>
        </div>
      </NuxtLink>

      <NuxtLink :to="`/patients/${patientId}/files`" class="quick-action-card">
        <div class="flex items-center gap-3">
          <div class="icon-circle bg-indigo-100 text-indigo-600">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-6 w-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"
              />
            </svg>
          </div>
          <div>
            <h3 class="font-semibold text-gray-900">Arquivos</h3>
            <p class="text-sm text-gray-600">Documentos e imagens</p>
          </div>
        </div>
      </NuxtLink>

      <NuxtLink
        :to="`/patients/${patientId}/schedule`"
        class="quick-action-card"
      >
        <div class="flex items-center gap-3">
          <div class="icon-circle bg-teal-100 text-teal-600">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-6 w-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
          </div>
          <div>
            <h3 class="font-semibold text-gray-900">Agenda</h3>
            <p class="text-sm text-gray-600">Consultas e sessões</p>
          </div>
        </div>
      </NuxtLink>
    </div>

    <!-- PATIENT VIEW: Progress Dashboard -->
    <div v-else-if="authStore.isPatient">
      <!-- Welcome Section -->
      <div
        class="rounded-xl bg-gradient-to-r from-blue-500 to-blue-600 p-8 text-white mb-6"
      >
        <h2 class="text-2xl font-bold mb-2">
          Bem-vindo ao seu painel de progresso!
        </h2>
        <p class="text-blue-100">Acompanhe seu tratamento e evolução</p>
      </div>

      <!-- Stats Cards -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div class="rounded-xl bg-white p-6 shadow-[0_1px_3px_rgba(0,0,0,0.1)]">
          <div class="flex items-center gap-4">
            <div
              class="flex h-12 w-12 items-center justify-center rounded-full bg-green-100 text-green-600"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-6 w-6"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
            <div>
              <p class="text-sm text-gray-600">Planos Ativos</p>
              <p v-if="plansPending" class="text-2xl font-bold text-gray-400">
                ...
              </p>
              <p v-else class="text-2xl font-bold text-gray-900">
                {{ activePlansCount }}
              </p>
            </div>
          </div>
        </div>

        <div class="rounded-xl bg-white p-6 shadow-[0_1px_3px_rgba(0,0,0,0.1)]">
          <div class="flex items-center gap-4">
            <div
              class="flex h-12 w-12 items-center justify-center rounded-full bg-blue-100 text-blue-600"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-6 w-6"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                />
              </svg>
            </div>
            <div>
              <p class="text-sm text-gray-600">Notas Recentes</p>
              <p v-if="notesPending" class="text-2xl font-bold text-gray-400">
                ...
              </p>
              <p v-else class="text-2xl font-bold text-gray-900">
                {{ recentNotes?.length || 0 }}
              </p>
            </div>
          </div>
        </div>

        <div class="rounded-xl bg-white p-6 shadow-[0_1px_3px_rgba(0,0,0,0.1)]">
          <div class="flex items-center gap-4">
            <div
              class="flex h-12 w-12 items-center justify-center rounded-full bg-purple-100 text-purple-600"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-6 w-6"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M13 10V3L4 14h7v7l9-11h-7z"
                />
              </svg>
            </div>
            <div>
              <p class="text-sm text-gray-600">Progresso</p>
              <p class="text-2xl font-bold text-gray-900">
                {{ progressPercentage }}%
              </p>
            </div>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div class="lg:col-span-2">
          <UICard title="Meus Planos de Reabilitação">
            <div v-if="plansPending">Carregando planos...</div>
            <div v-else-if="plans && plans.length > 0" class="space-y-4">
              <div
                v-for="plan in plans"
                :key="plan.id"
                class="border border-gray-200 rounded-lg p-4 hover:border-blue-400 transition-all"
              >
                <div class="flex justify-between items-start">
                  <div>
                    <h4 class="font-semibold text-gray-900">
                      {{
                        parsePlanData(plan.planData).title || "Plano sem título"
                      }}
                    </h4>
                    <p class="text-sm text-gray-600 mt-1">
                      Versão {{ plan.version }} • Criado em
                      {{ formatDate(plan.createdAt) }}
                    </p>
                  </div>
                  <span
                    class="px-3 py-1 text-xs font-semibold rounded-full"
                    :class="{
                      'bg-green-100 text-green-800': plan.status === 'APPROVED',
                      'bg-yellow-100 text-yellow-800': plan.status === 'DRAFT',
                      'bg-gray-100 text-gray-700': plan.status === 'ARCHIVED',
                    }"
                  >
                    {{ plan.status }}
                  </span>
                </div>
                <NuxtLink
                  :to="`/plans/${plan.id}`"
                  class="inline-block mt-3 text-sm text-blue-600 hover:text-blue-700"
                >
                  Ver detalhes →
                </NuxtLink>
              </div>
            </div>
            <div v-else class="text-center py-8 text-gray-500">
              <p>Nenhum plano de reabilitação ainda.</p>
              <p class="text-sm mt-2">
                Seu fisioterapeuta criará planos personalizados para você.
              </p>
            </div>
          </UICard>
        </div>

        <div class="lg:col-span-1">
          <UICard title="Notas Recentes">
            <div v-if="notesPending">Carregando...</div>
            <div
              v-else-if="recentNotes && recentNotes.length > 0"
              class="space-y-3"
            >
              <div
                v-for="note in recentNotes.slice(0, 5)"
                :key="note.id"
                class="border-b border-gray-200 pb-3 last:border-0"
              >
                <p class="text-sm text-gray-900">{{ note.note }}</p>
                <span class="text-xs text-gray-500">{{
                  formatDate(note.timestamp)
                }}</span>
              </div>
              <NuxtLink
                to="/medical-records"
                class="block text-sm text-blue-600 hover:text-blue-700 text-center mt-4"
              >
                Ver todos →
              </NuxtLink>
            </div>
            <div v-else class="text-center py-8 text-gray-500">
              <p class="text-sm">Nenhuma nota ainda.</p>
            </div>
          </UICard>
        </div>
      </div>

      <div class="mt-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">Acesso Rápido</h3>
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <NuxtLink
            to="/medical-records"
            class="flex items-center gap-3 p-4 rounded-lg border border-gray-200 hover:border-blue-400 hover:shadow-md transition-all"
          >
            <div
              class="flex h-10 w-10 items-center justify-center rounded-full bg-green-100 text-green-600"
            >
              📋
            </div>
            <span class="font-medium text-gray-900">Meus Registros</span>
          </NuxtLink>

          <!-- <NuxtLink
            to="/exercises"
            class="flex items-center gap-3 p-4 rounded-lg border border-gray-200 hover:border-blue-400 hover:shadow-md transition-all"
          >
            <div
              class="flex h-10 w-10 items-center justify-center rounded-full bg-purple-100 text-purple-600"
            >
              💪
            </div>
            <span class="font-medium text-gray-900">Meus Exercícios</span>
          </NuxtLink> -->

          <NuxtLink
            to="/schedule"
            class="flex items-center gap-3 p-4 rounded-lg border border-gray-200 hover:border-blue-400 hover:shadow-md transition-all"
          >
            <div
              class="flex h-10 w-10 items-center justify-center rounded-full bg-teal-100 text-teal-600"
            >
              📅
            </div>
            <span class="font-medium text-gray-900">Agenda</span>
          </NuxtLink>

          <NuxtLink
            to="/messages"
            class="flex items-center gap-3 p-4 rounded-lg border border-gray-200 hover:border-blue-400 hover:shadow-md transition-all"
          >
            <div
              class="flex h-10 w-10 items-center justify-center rounded-full bg-blue-100 text-blue-600"
            >
              💬
            </div>
            <span class="font-medium text-gray-900">Mensagens</span>
          </NuxtLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from "~/store/auth";

definePageMeta({
  middleware: ["auth-only"],
  layout: "default",
});

const authStore = useAuthStore();
const route = useRoute();
const patientId = computed(() => route.params.id as string);

interface PatientUser {
  id: string;
  email: string;
  fullName: string;
}

interface Plan {
  id: string;
  version: number;
  planData: string;
  status: "DRAFT" | "APPROVED" | "ARCHIVED";
  createdAt: string;
}

interface ClinicalNote {
  id: string;
  note: string;
  timestamp: string;
}

const { data: user, pending: userPending } = await useApiFetch<PatientUser>(
  `/users/${patientId.value}`,
  { lazy: true }
);

const { data: plans, pending: plansPending } = await useApiFetch<Plan[]>(
  `/plans/user/${patientId.value}`,
  { lazy: true }
);

const { data: recentNotes, pending: notesPending } = await useApiFetch<
  ClinicalNote[]
>(`/patients/${patientId.value}/history/notes`, { lazy: true });

const activePlansCount = computed(() => {
  if (!plans.value) return 0;
  return plans.value.filter((p) => p.status === "APPROVED").length;
});

const progressPercentage = computed(() => {
  if (!plans.value || plans.value.length === 0) return 0;
  const approved = plans.value.filter((p) => p.status === "APPROVED").length;
  return Math.round((approved / plans.value.length) * 100);
});

function formatDate(dateString: string) {
  if (!dateString) return "Data não informada";
  return new Date(dateString).toLocaleDateString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
}

function parsePlanData(planDataString: string) {
  try {
    return JSON.parse(planDataString);
  } catch (e) {
    return { title: "Plano sem título" };
  }
}
</script>

<style scoped>
.quick-action-card {
  @apply block rounded-lg border border-gray-200 bg-white p-4 transition-all hover:border-blue-400 hover:shadow-md;
}
.icon-circle {
  @apply flex h-12 w-12 items-center justify-center rounded-full;
}
.alert-danger {
  @apply border-danger bg-red-50 text-red-800;
}
.plan-card {
  @apply flex items-center justify-between rounded-lg border border-gray-200 bg-gray-50 p-4;
}
.history-note {
  @apply rounded-lg border-b border-gray-200 p-3;
}
.condition-item {
  @apply flex flex-col rounded-lg bg-gray-50 p-3;
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
.badge-secondary {
  @apply bg-gray-100 text-gray-700;
}
</style>
