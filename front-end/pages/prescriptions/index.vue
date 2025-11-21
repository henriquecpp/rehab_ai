<template>
  <UICard title="Caixa de Entrada de Prescrições da IA">
    <div class="mb-6 max-w-lg">
      <label class="form-label">Selecione um Paciente</label>
      <select
        v-model="selectedPatientId"
        class="form-input"
        :disabled="patientsPending"
      >
        <option value="" disabled>
          {{
            patientsPending
              ? "Carregando pacientes..."
              : "Selecione para ver as prescrições"
          }}
        </option>
        <option v-for="p in patients" :key="p.id" :value="p.id">
          {{ p.fullName }} ({{ p.email }})
        </option>
      </select>
    </div>

    <div v-if="patientsError" class="alert alert-danger">
      Erro ao carregar pacientes: {{ patientsError.message }}
    </div>

    <hr class="my-6 border-gray-200" v-if="selectedPatientId" />

    <div v-if="selectedPatientId">
      <h3 class="mb-4 text-xl font-semibold text-gray-800">
        Prescrições Geradas para {{ selectedPatientName }}
      </h3>

      <div v-if="prescriptionsPending" class="py-10 text-center text-gray-600">
        Carregando prescrições...
      </div>

      <div v-else-if="prescriptionsError" class="alert alert-danger">
        Erro ao carregar prescrições: {{ prescriptionsError.message }}
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
              {{ getPrescriptionTitle(pres) }}
            </h4>
            <p class="text-sm text-gray-600">
              Processada em: {{ formatDate(pres.createdAt) }}
            </p>
            <div class="mt-2 flex flex-wrap gap-2">
              <span
                class="badge"
                :class="getGuardrailClass(pres.guardrailStatus)"
              >
                {{ pres.guardrailStatus === "OK" ? "✓ Seguro" : "⚠️ Bloqueado" }}
              </span>
              <span class="badge badge-info">
                {{ pres.modelUsed || "Claude 3.5" }}
              </span>
              <span v-if="pres.promptVersion" class="badge badge-secondary">
                v{{ pres.promptVersion }}
              </span>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <NuxtLink
              :to="`/plans/create?prescriptionId=${pres.id}&userId=${selectedPatientId}`"
              class="btn-primary"
            >
              📋 Revisar e Criar Plano
            </NuxtLink>
          </div>
        </div>
      </div>

      <div v-else class="text-gray-500">
        Nenhuma prescrição gerada pela IA encontrada para este paciente.
      </div>
    </div>
  </UICard>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: "auth-only",
  layout: "default",
});

interface Patient {
  id: string;
  email: string;
  fullName: string;
}

interface Prescription {
  id: string;
  fileId: string;
  userId: string;
  normalizationId: string;
  prescriptionText: string; // Still using prescriptionText from backend
  parametersJson: string; // JSON string with plan parameters
  promptVersion: string;
  modelUsed: string;
  guardrailStatus: "OK" | "BLOCKED";
  createdAt: string;
}

const {
  data: patients,
  pending: patientsPending,
  error: patientsError,
} = await useApiFetch<Patient[]>("/users", {
  query: { role: "PATIENT" },
  lazy: true,
  key: "patientListForPrescriptions",
});

const selectedPatientId = ref("");

const selectedPatientName = computed(() => {
  return (
    patients.value?.find((p) => p.id === selectedPatientId.value)?.fullName ||
    ""
  );
});

const patientIdComputed = computed(() => selectedPatientId.value || null);

const {
  data: prescriptions,
  pending: prescriptionsPending,
  error: prescriptionsError,
} = await useApiFetch<Prescription[]>(
  computed(() =>
    patientIdComputed.value
      ? `/prescriptions/user/${patientIdComputed.value}`
      : null
  ),
  {
    lazy: true,
    key: computed(() => `prescriptions-for-user-${selectedPatientId.value}`),
    watch: [selectedPatientId],
  }
);

function getPrescriptionTitle(prescription: Prescription): string {
  try {
    if (prescription.parametersJson) {
      const data = JSON.parse(prescription.parametersJson);
      return data.title || "Prescrição sem título";
    }
    if (prescription.prescriptionText) {
      const data = JSON.parse(prescription.prescriptionText);
      return data.title || "Prescrição sem título";
    }
    return "Prescrição sem título";
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
.form-input {
  @apply w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm shadow-sm;
}
.form-label {
  @apply mb-1 block text-xs font-semibold text-gray-600;
}
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
.badge-secondary {
  @apply bg-gray-100 text-gray-800;
}
.btn-primary {
  @apply flex items-center gap-2 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white transition hover:bg-primary-dark;
}
.alert-danger {
  @apply mb-4 flex gap-3 rounded-lg border-l-4 border-danger bg-red-50 p-4 text-red-800;
}
</style>
