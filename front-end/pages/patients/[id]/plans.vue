<template>
  <UICard>
    <template #title>
      <div class="flex justify-between items-center">
        <span>Planos de Reabilitação</span>
      </div>
    </template>

    <div class="flex flex-col gap-8">
      <section>
        <h2 class="section-title">Novo Plano</h2>
        <hr class="section-hr" />
        <div class="item-card bg-white border-gray-200">
          <div class="flex-1">
            <h4 class="font-semibold">Criar um novo plano do zero</h4>
            <p class="text-sm text-gray-600">
              Comece um plano de reabilitação vazio para este paciente.
            </p>
          </div>
          <NuxtLink
            :to="`/plans/create?userId=${patientId}`"
            class="btn-primary"
          >
            Criar Plano Vazio &rarr;
          </NuxtLink>
        </div>
      </section>

      <section>
        <h2 class="section-title">
          Prescrições Pendentes da IA
          <span v-if="!prescriptionsPending" class="section-count">
            ({{ prescriptions?.length || 0 }})
          </span>
        </h2>
        <hr class="section-hr" />

        <div
          v-if="prescriptionsPending"
          class="py-10 text-center text-gray-600"
        >
          Carregando prescrições pendentes...
        </div>
        <div v-else-if="prescriptionsError" class="alert alert-danger">
          Erro ao carregar prescrições
        </div>
        <div
          v-else-if="prescriptions && prescriptions.length > 0"
          class="grid gap-4"
        >
          <div
            v-for="pres in prescriptions"
            :key="pres.id"
            class="item-card bg-blue-50 border-blue-200"
          >
            <div class="flex-1">
              <h4 class="font-semibold">
                {{ getPlanTitle(pres.prescriptionText) }}
              </h4>
              <p class="text-sm text-gray-600">
                Processada em: {{ formatDate(pres.createdAt) }}
              </p>
              <div class="mt-2">
                <span
                  class="badge"
                  :class="getGuardrailClass(pres.guardrailStatus)"
                >
                  Guardrail: {{ pres.guardrailStatus }}
                </span>
                <span class="badge badge-info ml-2">
                  Modelo: {{ pres.modelUsed }}
                </span>
              </div>
            </div>
            <NuxtLink
              :to="`/plans/create?prescriptionId=${pres.id}&userId=${patientId}`"
              class="btn-primary"
            >
              Usar prescrição como base &rarr;
            </NuxtLink>
          </div>
        </div>
        <div v-else class="text-gray-500 text-center py-4">
          Nenhuma prescrição pendente da IA para este paciente.
        </div>
      </section>

      <section>
        <h2 class="section-title">
          Planos Salvos
          <span v-if="!plansPending" class="section-count">
            ({{ plans?.length || 0 }})
          </span>
        </h2>
        <hr class="section-hr" />

        <div v-if="plansPending" class="py-10 text-center text-gray-600">
          Carregando planos salvos...
        </div>
        <div v-else-if="plansError" class="alert alert-danger">
          Erro ao carregar planos
        </div>
        <div v-else-if="plans && plans.length > 0" class="grid gap-4">
          <div
            v-for="plan in plans"
            :key="plan.id"
            class="item-card bg-white border-gray-200"
          >
            <div class="flex-1">
              <h4 class="font-semibold">{{ getPlanTitle(plan.planData) }}</h4>
              <p class="text-sm text-gray-600">
                Versão: {{ plan.version }} | Salvo em:
                {{ formatDate(plan.createdAt) }}
              </p>
            </div>
            <div class="flex items-center gap-3">
              <span class="badge" :class="getPlanStatusClass(plan.status)">
                {{ plan.status }}
              </span>
              <NuxtLink :to="`/plans/${plan.id}`" class="btn-secondary">
                Ver Detalhes
              </NuxtLink>
            </div>
          </div>
        </div>
        <div v-else class="text-gray-500 text-center py-4">
          Nenhum plano de reabilitação salvo para este paciente.
        </div>
      </section>
    </div>
  </UICard>
</template>

<script setup lang="ts">
import type { PrescriptionListItem } from "~/types/prescription";

definePageMeta({ middleware: "auth-only" });

const route = useRoute();
const patientId = computed(() => route.params.id as string);

interface Plan {
  id: string;
  userId: string;
  version: number;
  planData: string;
  status: "DRAFT" | "APPROVED" | "ARCHIVED";
  createdAt: string;
}
interface PlanData {
  title?: string;
}

const {
  data: plans,
  pending: plansPending,
  error: plansError,
} = await useApiFetch<Plan[]>(
  computed(() => `/plans/user/${patientId.value}`),
  {
    lazy: false,
    key: computed(() => `plans-for-patient-${patientId.value}`),
  }
);

const {
  data: prescriptions,
  pending: prescriptionsPending,
  error: prescriptionsError,
} = await useApiFetch<PrescriptionListItem[]>(
  computed(() => `/prescriptions/user/${patientId.value}`),
  {
    lazy: false,
    key: computed(() => `prescriptions-for-patient-${patientId.value}`),
  }
);

function getPlanTitle(jsonString: string): string {
  if (!jsonString) {
    return "Prescrição sem título";
  }
  try {
    const data = JSON.parse(jsonString) as PlanData;
    return data.title || "Plano sem título";
  } catch (e) {
    console.error("Falha ao ler título da prescrição:", e);
    return "Erro ao ler título";
  }
}

function formatDate(dateString: string) {
  return new Date(dateString).toLocaleString("pt-BR", {
    dateStyle: "short",
    timeStyle: "short",
  });
}

function getGuardrailClass(status: string) {
  return status === "OK" ? "badge-success" : "badge-danger";
}

function getPlanStatusClass(status: string) {
  switch (status) {
    case "APPROVED":
      return "badge-success";
    case "DRAFT":
      return "badge-warning";
    case "ARCHIVED":
      return "badge-secondary";
    default:
      return "badge-secondary";
  }
}
</script>

<style scoped>
.section-title {
  @apply text-xl font-semibold text-gray-800;
}
.section-count {
  @apply text-base font-normal text-gray-500;
}
.section-hr {
  @apply my-3 border-gray-200;
}
.item-card {
  @apply flex flex-col sm:flex-row sm:items-center sm:justify-between rounded-lg border p-4 gap-3;
}
.badge {
  @apply inline-block rounded-full px-3 py-0.5 text-xs font-semibold;
}
.badge-success {
  @apply bg-green-100 text-green-800;
}
.badge-danger {
  @apply bg-red-100 text-red-800;
}
.badge-info {
  @apply bg-blue-100 text-blue-800;
}
.badge-warning {
  @apply bg-yellow-100 text-yellow-800;
}
.badge-secondary {
  @apply bg-gray-100 text-gray-700;
}
.btn-primary {
  @apply flex items-center gap-2 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white transition hover:bg-primary-dark;
}
.btn-secondary {
  @apply flex items-center gap-2 rounded-lg bg-gray-200 px-4 py-2 text-sm font-semibold text-gray-800 transition hover:bg-gray-300;
}
.alert-danger {
  @apply mb-4 flex gap-3 rounded-lg border-l-4 border-danger bg-red-50 p-4 text-red-800;
}
</style>
