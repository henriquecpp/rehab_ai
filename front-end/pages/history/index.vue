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
        <!-- Notas Clínicas -->
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-xl font-semibold text-gray-800">Notas Clínicas</h3>
          <button
            @click="openModal('note')"
            class="btn-add"
            title="Adicionar Nota"
          >
            + Adicionar
          </button>
        </div>
        <div v-if="notesPending">Carregando notas...</div>
        <div v-else-if="notesError" class="alert alert-danger">
          Erro ao carregar notas.
        </div>
        <div v-else-if="notes && notes.length > 0" class="history-list">
          <div v-for="note in notes" :key="note.id" class="history-item">
            <div class="flex justify-between items-start">
              <div class="flex-1">
                <p>{{ note.note }}</p>
                <span class="text-xs text-gray-500"
                  >Em: {{ formatDate(note.timestamp) }}</span
                >
              </div>
              <button
                @click="deleteNote(note.id)"
                class="text-red-600 hover:text-red-800 text-sm ml-2"
                title="Deletar"
              >
                🗑️
              </button>
            </div>
          </div>
        </div>
        <div v-else class="text-gray-500">Nenhuma nota clínica.</div>

        <!-- Condições Médicas -->
        <div class="flex justify-between items-center mt-8 mb-4">
          <h3 class="text-xl font-semibold text-gray-800">Condições Médicas</h3>
          <button
            @click="openModal('condition')"
            class="btn-add"
            title="Adicionar Condição"
          >
            + Adicionar
          </button>
        </div>
        <div v-if="conditionsPending">Carregando condições...</div>
        <div v-else-if="conditionsError" class="alert alert-danger">
          Erro ao carregar condições.
        </div>
        <div
          v-else-if="conditions && conditions.length > 0"
          class="history-list"
        >
          <div v-for="item in conditions" :key="item.id" class="history-item">
            <div class="flex justify-between items-start">
              <div class="flex-1">
                <p class="font-semibold">{{ item.description }}</p>
                <span class="text-xs text-gray-500"
                  >Início: {{ formatDate(item.onsetDate) }}</span
                >
                <span v-if="item.code" class="badge badge-info mt-1">{{
                  item.code
                }}</span>
              </div>
              <button
                @click="deleteCondition(item.id)"
                class="text-red-600 hover:text-red-800 text-sm ml-2"
                title="Deletar"
              >
                🗑️
              </button>
            </div>
          </div>
        </div>
        <div v-else class="text-gray-500">Nenhuma condição médica.</div>
      </div>

      <div>
        <!-- Alergias -->
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-xl font-semibold text-gray-800">Alergias</h3>
          <button
            @click="openModal('allergy')"
            class="btn-add"
            title="Adicionar Alergia"
          >
            + Adicionar
          </button>
        </div>
        <div v-if="allergiesPending">Carregando alergias...</div>
        <div v-else-if="allergiesError" class="alert alert-danger">
          Erro ao carregar alergias.
        </div>
        <div v-else-if="allergies && allergies.length > 0" class="history-list">
          <div v-for="item in allergies" :key="item.id" class="history-item">
            <div class="flex justify-between items-start">
              <div class="flex-1">
                <p class="font-semibold">{{ item.substance }}</p>
                <p class="text-sm">Reação: {{ item.reaction }}</p>
                <span class="badge" :class="getAllergyClass(item.severity)">{{
                  item.severity
                }}</span>
              </div>
              <button
                @click="deleteAllergy(item.id)"
                class="text-red-600 hover:text-red-800 text-sm ml-2"
                title="Deletar"
              >
                🗑️
              </button>
            </div>
          </div>
        </div>
        <div v-else class="text-gray-500">Nenhuma alergia.</div>

        <!-- Medicações -->
        <div class="flex justify-between items-center mt-8 mb-4">
          <h3 class="text-xl font-semibold text-gray-800">Medicações</h3>
          <button
            @click="openModal('medication')"
            class="btn-add"
            title="Adicionar Medicação"
          >
            + Adicionar
          </button>
        </div>
        <div v-if="medsPending">Carregando medicações...</div>
        <div v-else-if="medsError" class="alert alert-danger">
          Erro ao carregar medicações.
        </div>
        <div v-else-if="meds && meds.length > 0" class="history-list">
          <div v-for="item in meds" :key="item.id" class="history-item">
            <div class="flex justify-between items-start">
              <div class="flex-1">
                <p class="font-semibold">
                  {{ item.drugName }} ({{ item.dose }})
                </p>
                <p class="text-sm">
                  Frequência: {{ item.frequency }} | Rota: {{ item.route }}
                </p>
                <span class="text-xs text-gray-500">
                  {{ formatDate(item.startDate) }} até
                  {{ item.endDate ? formatDate(item.endDate) : "Contínuo" }}
                </span>
              </div>
              <button
                @click="deleteMedication(item.id)"
                class="text-red-600 hover:text-red-800 text-sm ml-2"
                title="Deletar"
              >
                🗑️
              </button>
            </div>
          </div>
        </div>
        <div v-else class="text-gray-500">Nenhuma medicação.</div>
      </div>
    </div>

    <!-- Modals -->
    <UIBaseModal v-if="showModal" @close="closeModal">
      <div class="p-6">
        <h2 class="text-2xl font-bold mb-4">{{ getModalTitle() }}</h2>

        <!-- Nota Clínica Form -->
        <form
          v-if="modalType === 'note'"
          @submit.prevent="submitNote"
          class="space-y-4"
        >
          <div>
            <label class="form-label">Nota Clínica *</label>
            <textarea
              v-model="noteForm.note"
              class="form-input"
              rows="4"
              required
              placeholder="Digite a nota clínica..."
            ></textarea>
          </div>
          <div class="flex gap-3">
            <button type="submit" class="btn-primary" :disabled="saving">
              {{ saving ? "Salvando..." : "Salvar" }}
            </button>
            <button type="button" @click="closeModal" class="btn-secondary">
              Cancelar
            </button>
          </div>
        </form>

        <!-- Condição Médica Form -->
        <form
          v-if="modalType === 'condition'"
          @submit.prevent="submitCondition"
          class="space-y-4"
        >
          <div>
            <label class="form-label">Descrição *</label>
            <input
              v-model="conditionForm.description"
              type="text"
              class="form-input"
              required
              placeholder="Ex: Hipertensão arterial"
            />
          </div>
          <div>
            <label class="form-label">Código CID-10</label>
            <input
              v-model="conditionForm.code"
              type="text"
              class="form-input"
              placeholder="Ex: I10"
            />
          </div>
          <div>
            <label class="form-label">Data de Início *</label>
            <input
              v-model="conditionForm.onsetDate"
              type="date"
              class="form-input"
              required
            />
          </div>
          <div class="flex gap-3">
            <button type="submit" class="btn-primary" :disabled="saving">
              {{ saving ? "Salvando..." : "Salvar" }}
            </button>
            <button type="button" @click="closeModal" class="btn-secondary">
              Cancelar
            </button>
          </div>
        </form>

        <!-- Alergia Form -->
        <form
          v-if="modalType === 'allergy'"
          @submit.prevent="submitAllergy"
          class="space-y-4"
        >
          <div>
            <label class="form-label">Substância *</label>
            <input
              v-model="allergyForm.substance"
              type="text"
              class="form-input"
              required
              placeholder="Ex: Penicilina"
            />
          </div>
          <div>
            <label class="form-label">Reação *</label>
            <input
              v-model="allergyForm.reaction"
              type="text"
              class="form-input"
              required
              placeholder="Ex: Urticária, coceira"
            />
          </div>
          <div>
            <label class="form-label">Severidade *</label>
            <select v-model="allergyForm.severity" class="form-input" required>
              <option value="">Selecione</option>
              <option value="LOW">Leve</option>
              <option value="MEDIUM">Moderada</option>
              <option value="HIGH">Alta</option>
              <option value="CRITICAL">Crítica</option>
            </select>
          </div>
          <div class="flex gap-3">
            <button type="submit" class="btn-primary" :disabled="saving">
              {{ saving ? "Salvando..." : "Salvar" }}
            </button>
            <button type="button" @click="closeModal" class="btn-secondary">
              Cancelar
            </button>
          </div>
        </form>

        <!-- Medicação Form -->
        <form
          v-if="modalType === 'medication'"
          @submit.prevent="submitMedication"
          class="space-y-4"
        >
          <div>
            <label class="form-label">Nome do Medicamento *</label>
            <input
              v-model="medicationForm.drugName"
              type="text"
              class="form-input"
              required
              placeholder="Ex: Losartana"
            />
          </div>
          <div>
            <label class="form-label">Dose *</label>
            <input
              v-model="medicationForm.dose"
              type="text"
              class="form-input"
              required
              placeholder="Ex: 50mg"
            />
          </div>
          <div>
            <label class="form-label">Via de Administração *</label>
            <select v-model="medicationForm.route" class="form-input" required>
              <option value="">Selecione</option>
              <option value="Oral">Oral</option>
              <option value="Intravenosa">Intravenosa</option>
              <option value="Intramuscular">Intramuscular</option>
              <option value="Subcutânea">Subcutânea</option>
              <option value="Tópica">Tópica</option>
              <option value="Inalatória">Inalatória</option>
            </select>
          </div>
          <div>
            <label class="form-label">Frequência *</label>
            <input
              v-model="medicationForm.frequency"
              type="text"
              class="form-input"
              required
              placeholder="Ex: 1x ao dia, 8/8h"
            />
          </div>
          <div>
            <label class="form-label">Data de Início *</label>
            <input
              v-model="medicationForm.startDate"
              type="date"
              class="form-input"
              required
            />
          </div>
          <div>
            <label class="form-label">Data de Término</label>
            <input
              v-model="medicationForm.endDate"
              type="date"
              class="form-input"
              placeholder="Deixe vazio para medicação contínua"
            />
          </div>
          <div class="flex gap-3">
            <button type="submit" class="btn-primary" :disabled="saving">
              {{ saving ? "Salvando..." : "Salvar" }}
            </button>
            <button type="button" @click="closeModal" class="btn-secondary">
              Cancelar
            </button>
          </div>
        </form>
      </div>
    </UIBaseModal>
  </UICard>
</template>

<script setup lang="ts">
import { useAuthStore } from "~/store/auth";

definePageMeta({
  middleware: "auth-only",
  layout: "default",
});

const authStore = useAuthStore();

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
  refresh: refreshNotes,
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
  refresh: refreshConditions,
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
  refresh: refreshAllergies,
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
  refresh: refreshMeds,
} = await useApiFetch<Medication[]>(
  computed(() =>
    patientIdComputed.value
      ? `/patients/${patientIdComputed.value}/medications`
      : null
  ),
  { lazy: true, key: reactiveKey("meds-for-user") }
);

// Modal state
const showModal = ref(false);
const modalType = ref<"note" | "condition" | "allergy" | "medication">("note");
const saving = ref(false);

// Forms
const noteForm = ref({ note: "" });
const conditionForm = ref({ description: "", code: "", onsetDate: "" });
const allergyForm = ref({ substance: "", reaction: "", severity: "" });
const medicationForm = ref({
  drugName: "",
  dose: "",
  route: "",
  frequency: "",
  startDate: "",
  endDate: "",
});

function openModal(type: "note" | "condition" | "allergy" | "medication") {
  modalType.value = type;
  showModal.value = true;

  // Reset forms
  noteForm.value = { note: "" };
  conditionForm.value = { description: "", code: "", onsetDate: "" };
  allergyForm.value = { substance: "", reaction: "", severity: "" };
  medicationForm.value = {
    drugName: "",
    dose: "",
    route: "",
    frequency: "",
    startDate: "",
    endDate: "",
  };
}

function closeModal() {
  showModal.value = false;
}

function getModalTitle() {
  const titles = {
    note: "Adicionar Nota Clínica",
    condition: "Adicionar Condição Médica",
    allergy: "Adicionar Alergia",
    medication: "Adicionar Medicação",
  };
  return titles[modalType.value];
}

// Submit functions
async function submitNote() {
  if (!selectedPatientId.value) return;

  saving.value = true;
  try {
    await useApiFetch(`/patients/${selectedPatientId.value}/history/notes`, {
      method: "POST",
      body: { note: noteForm.value.note },
    });

    alert("Nota clínica adicionada com sucesso!");
    closeModal();
    await refreshNotes();
  } catch (error) {
    console.error("Erro ao adicionar nota:", error);
    alert("Erro ao adicionar nota clínica");
  } finally {
    saving.value = false;
  }
}

async function submitCondition() {
  if (!selectedPatientId.value) return;

  saving.value = true;
  try {
    await useApiFetch(`/patients/${selectedPatientId.value}/conditions`, {
      method: "POST",
      body: {
        description: conditionForm.value.description,
        code: conditionForm.value.code || null,
        onsetDate: conditionForm.value.onsetDate,
      },
    });

    alert("Condição médica adicionada com sucesso!");
    closeModal();
    await refreshConditions();
  } catch (error) {
    console.error("Erro ao adicionar condição:", error);
    alert("Erro ao adicionar condição médica");
  } finally {
    saving.value = false;
  }
}

async function submitAllergy() {
  if (!selectedPatientId.value) return;

  saving.value = true;
  try {
    await useApiFetch(`/patients/${selectedPatientId.value}/allergies`, {
      method: "POST",
      body: {
        substance: allergyForm.value.substance,
        reaction: allergyForm.value.reaction,
        severity: allergyForm.value.severity,
      },
    });

    alert("Alergia adicionada com sucesso!");
    closeModal();
    await refreshAllergies();
  } catch (error) {
    console.error("Erro ao adicionar alergia:", error);
    alert("Erro ao adicionar alergia");
  } finally {
    saving.value = false;
  }
}

async function submitMedication() {
  if (!selectedPatientId.value) return;

  saving.value = true;
  try {
    await useApiFetch(`/patients/${selectedPatientId.value}/medications`, {
      method: "POST",
      body: {
        drugName: medicationForm.value.drugName,
        dose: medicationForm.value.dose,
        route: medicationForm.value.route,
        frequency: medicationForm.value.frequency,
        startDate: medicationForm.value.startDate,
        endDate: medicationForm.value.endDate || null,
      },
    });

    alert("Medicação adicionada com sucesso!");
    closeModal();
    await refreshMeds();
  } catch (error) {
    console.error("Erro ao adicionar medicação:", error);
    alert("Erro ao adicionar medicação");
  } finally {
    saving.value = false;
  }
}

// Delete functions
async function deleteNote(noteId: string) {
  if (!confirm("Tem certeza que deseja deletar esta nota clínica?")) return;

  try {
    await useApiFetch(
      `/patients/${selectedPatientId.value}/history/notes/${noteId}`,
      {
        method: "DELETE",
      }
    );

    alert("Nota deletada com sucesso!");
    await refreshNotes();
  } catch (error) {
    console.error("Erro ao deletar nota:", error);
    alert("Erro ao deletar nota");
  }
}

async function deleteCondition(conditionId: string) {
  if (!confirm("Tem certeza que deseja deletar esta condição médica?")) return;

  try {
    await useApiFetch(
      `/patients/${selectedPatientId.value}/conditions/${conditionId}`,
      {
        method: "DELETE",
      }
    );

    alert("Condição deletada com sucesso!");
    await refreshConditions();
  } catch (error) {
    console.error("Erro ao deletar condição:", error);
    alert("Erro ao deletar condição");
  }
}

async function deleteAllergy(allergyId: string) {
  if (!confirm("Tem certeza que deseja deletar esta alergia?")) return;

  try {
    await useApiFetch(
      `/patients/${selectedPatientId.value}/allergies/${allergyId}`,
      {
        method: "DELETE",
      }
    );

    alert("Alergia deletada com sucesso!");
    await refreshAllergies();
  } catch (error) {
    console.error("Erro ao deletar alergia:", error);
    alert("Erro ao deletar alergia");
  }
}

async function deleteMedication(medicationId: string) {
  if (!confirm("Tem certeza que deseja deletar esta medicação?")) return;

  try {
    await useApiFetch(
      `/patients/${selectedPatientId.value}/medications/${medicationId}`,
      {
        method: "DELETE",
      }
    );

    alert("Medicação deletada com sucesso!");
    await refreshMeds();
  } catch (error) {
    console.error("Erro ao deletar medicação:", error);
    alert("Erro ao deletar medicação");
  }
}

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
