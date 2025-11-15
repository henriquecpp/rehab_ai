<template>
  <UICard title="Histórico Médico do Paciente">
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
              : "Selecione para ver o histórico"
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

    <div v-if="selectedPatientId" class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div>
        <h3 class="text-xl font-semibold text-gray-800 mb-4">Notas Clínicas</h3>
        <div v-if="notesPending">Carregando notas...</div>
        <div v-else-if="notesError" class="alert alert-danger">
          Erro ao carregar notas.
        </div>
        <div v-else-if="notes && notes.length > 0" class="history-list">
          <div v-for="note in notes" :key="note.id" class="history-item">
            <p>{{ note.note }}</p>
            <span class="text-xs text-gray-500"
              >Em: {{ formatDate(note.timestamp) }}</span
            >
          </div>
        </div>
        <div v-else class="text-gray-500">Nenhuma nota clínica.</div>

        <h3 class="text-xl font-semibold text-gray-800 mt-8 mb-4">
          Condições Médicas
        </h3>
        <div v-if="conditionsPending">Carregando condições...</div>
        <div v-else-if="conditionsError" class="alert alert-danger">
          Erro ao carregar condições.
        </div>
        <div
          v-else-if="conditions && conditions.length > 0"
          class="history-list"
        >
          <div v-for="item in conditions" :key="item.id" class="history-item">
            <p class="font-semibold">{{ item.description }}</p>
            <span class="text-xs text-gray-500"
              >Início: {{ formatDate(item.onsetDate) }}</span
            >
            <span v-if="item.code" class="badge badge-info mt-1">{{
              item.code
            }}</span>
          </div>
        </div>
        <div v-else class="text-gray-500">Nenhuma condição médica.</div>
      </div>

      <div>
        <h3 class="text-xl font-semibold text-gray-800 mb-4">Alergias</h3>
        <div v-if="allergiesPending">Carregando alergias...</div>
        <div v-else-if="allergiesError" class="alert alert-danger">
          Erro ao carregar alergias.
        </div>
        <div v-else-if="allergies && allergies.length > 0" class="history-list">
          <div v-for="item in allergies" :key="item.id" class="history-item">
            <p class="font-semibold">{{ item.substance }}</p>
            <p class="text-sm">Reação: {{ item.reaction }}</p>
            <span class="badge" :class="getAllergyClass(item.severity)">{{
              item.severity
            }}</span>
          </div>
        </div>
        <div v-else class="text-gray-500">Nenhuma alergia.</div>

        <h3 class="text-xl font-semibold text-gray-800 mt-8 mb-4">
          Medicações
        </h3>
        <div v-if="medsPending">Carregando medicações...</div>
        <div v-else-if="medsError" class="alert alert-danger">
          Erro ao carregar medicações.
        </div>
        <div v-else-if="meds && meds.length > 0" class="history-list">
          <div v-for="item in meds" :key="item.id" class="history-item">
            <p class="font-semibold">{{ item.drugName }} ({{ item.dose }})</p>
            <p class="text-sm">
              Frequência: {{ item.frequency }} | Rota: {{ item.route }}
            </p>
            <span class="text-xs text-gray-500">
              {{ formatDate(item.startDate) }} até
              {{ item.endDate ? formatDate(item.endDate) : "Contínuo" }}
            </span>
          </div>
        </div>
        <div v-else class="text-gray-500">Nenhuma medicação.</div>
      </div>
    </div>
  </UICard>
</template>

<script setup lang="ts">
definePageMeta({ middleware: "auth-only" });

interface Patient {
  id: string;
  email: string;
  fullName: string;
}
interface ClinicalNote {
  id: string;
  note: string;
  timestamp: string;
}
interface MedicalCondition {
  id: string;
  code: string | null;
  description: string;
  onsetDate: string;
}
interface Allergy {
  id: string;
  substance: string;
  reaction: string;
  severity: string;
}
interface Medication {
  id: string;
  drugName: string;
  dose: string;
  route: string;
  frequency: string;
  startDate: string;
  endDate: string | null;
}


const {
  data: patients,
  pending: patientsPending,
  error: patientsError,
} = await useApiFetch<Patient[]>("/users", {
  query: { role: "PATIENT" },
  lazy: true,
  key: "patientListForHistory",
});

const selectedPatientId = ref("");
const patientIdComputed = computed(() => selectedPatientId.value || null);


const reactiveKey = (prefix: string) =>
  computed(() => `${prefix}-${selectedPatientId.value || "none"}`);

const {
  data: notes,
  pending: notesPending,
  error: notesError,
} = await useApiFetch<ClinicalNote[]>(
  computed(() =>
    patientIdComputed.value
      ? `/patients/${patientIdComputed.value}/history/notes`
      : null
  ),
  { lazy: true, key: reactiveKey("notes-for-user") }
);


const {
  data: conditions,
  pending: conditionsPending,
  error: conditionsError,
} = await useApiFetch<MedicalCondition[]>(
  computed(() =>
    patientIdComputed.value
      ? `/patients/${patientIdComputed.value}/conditions`
      : null
  ),
  { lazy: true, key: reactiveKey("conditions-for-user") }
);

const {
  data: allergies,
  pending: allergiesPending,
  error: allergiesError,
} = await useApiFetch<Allergy[]>(
  computed(() =>
    patientIdComputed.value
      ? `/patients/${patientIdComputed.value}/allergies`
      : null
  ),
  { lazy: true, key: reactiveKey("allergies-for-user") }
);

const {
  data: meds,
  pending: medsPending,
  error: medsError,
} = await useApiFetch<Medication[]>(
  computed(() =>
    patientIdComputed.value
      ? `/patients/${patientIdComputed.value}/medications`
      : null
  ),
  { lazy: true, key: reactiveKey("meds-for-user") }
);

function formatDate(dateString: string) {
  if (!dateString) return "Data indefinida";
  return new Date(dateString).toLocaleDateString("pt-BR");
}

function getAllergyClass(severity: string) {
  if (!severity) return "badge-secondary";
  if (
    severity.toUpperCase() === "HIGH" ||
    severity.toUpperCase() === "CRITICAL"
  )
    return "badge-danger";
  if (severity.toUpperCase() === "MEDIUM") return "badge-warning";
  return "badge-info";
}
</script>

<style scoped>

.form-input {
  @apply w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm shadow-sm;
}
.form-label {
  @apply mb-1 block text-xs font-semibold text-gray-600;
}
.alert-danger {
  @apply mb-4 flex gap-3 rounded-lg border-l-4 border-danger bg-red-50 p-4 text-red-800;
}
.history-list {
  @apply flex flex-col gap-3;
}
.history-item {
  @apply rounded-lg border border-gray-200 bg-white p-4;
}
.badge {
  @apply inline-block rounded-full px-3 py-0.5 text-xs font-semibold;
}
.badge-danger {
  @apply bg-red-100 text-red-800;
}
.badge-warning {
  @apply bg-yellow-100 text-yellow-800;
}
.badge-info {
  @apply bg-blue-100 text-blue-800;
}
.badge-secondary {
  @apply bg-gray-100 text-gray-700;
}
</style>
