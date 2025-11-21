<template>
  <UICard title="Prescrições da IA">
    <div v-if="prescriptionsPending" class="py-10 text-center text-gray-600">
      Carregando prescrições...
    </div>

    <div v-else-if="prescriptionsError" class="alert alert-danger">
      Erro ao carregar prescrições: {{ prescriptionsError }}
    </div>

    <div
      v-else-if="prescriptions && prescriptions.length > 0"
      class="grid gap-4"
    >
      <div
        v-for="pres in prescriptions"
        :key="pres.id"
        class="prescription-card"
      >
        <div class="flex-1">
          <h4 class="font-semibold">
            {{ getPrescriptionTitle(pres.prescriptionText) }}
          </h4>
          <p class="text-sm text-gray-600">
            Processada em: {{ formatDate(pres.createdAt) }}
          </p>
          <span class="badge" :class="getGuardrailClass(pres.guardrailStatus)">
            Guardrail: {{ pres.guardrailStatus }}
          </span>
          <span class="badge badge-info ml-2">
            Modelo: {{ pres.modelUsed }}
          </span>
        </div>
        <div class="flex items-center gap-3">
          <NuxtLink
            :to="`/plans/create?prescriptionId=${pres.id}&userId=${patientId}`"
            class="btn-primary"
          >
            Revisar e Criar Plano
          </NuxtLink>
        </div>
      </div>
    </div>

    <div v-else class="text-gray-500 py-6 text-center">
      Nenhuma prescrição gerada pela IA encontrada para este paciente.
    </div>
  </UICard>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: "auth-only",
  layout: "default",
});

const route = useRoute();

const patientId = computed(() => route.params.id as string);

interface GeneratedPrescription {
  id: string;
  prescriptionText: string; // JSON String com o plano
  modelUsed: string;
  guardrailStatus: "OK" | "BLOCKED";
  createdAt: string;
}

const {
  data: prescriptions,
  pending: prescriptionsPending,
  error: prescriptionsError,
} = await useApiFetch<GeneratedPrescription[]>(
  computed(() => `/prescriptions/user/${patientId.value}`),
  {
    lazy: false,
    key: computed(() => `prescriptions-for-patient-${patientId.value}`),
  }
);

function getPrescriptionTitle(jsonString: string): string {
  try {
    const data = JSON.parse(jsonString);
    return data.title || "Prescrição sem título";
  } catch (e) {
    return "Erro ao ler prescrição";
  }
}

function formatDate(dateString: string) {
  return new Date(dateString).toLocaleString("pt-BR");
}

function getGuardrailClass(status: string) {
  return status === "OK" ? "badge-success" : "badge-danger";
}
</script>

<style scoped>
.prescription-card {
  @apply flex flex-col sm:flex-row sm:items-center sm:justify-between rounded-lg border border-gray-200 bg-white p-4 gap-3;
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
.btn-primary {
  @apply flex items-center gap-2 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white transition hover:bg-primary-dark;
}
.alert-danger {
  @apply mb-4 flex gap-3 rounded-lg border-l-4 border-danger bg-red-50 p-4 text-red-800;
}
</style>
