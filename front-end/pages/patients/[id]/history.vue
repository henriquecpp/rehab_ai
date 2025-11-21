<template>
  <UICard title="Histórico Médico do Paciente">

    <div class="space-y-8">
      <!-- Notas Clínicas -->
      <div>
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
      </div>

      <!-- Condições Médicas -->
      <div>
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-xl font-semibold text-gray-800">Condições Médicas</h3>
          <button
            @click="openModal('condition')"
            class="btn-add"
            title="Adicionar Condição"
          >
            + Adicionar
          </button>
        </div>
        <UIDataTable
          :columns="conditionsColumns"
          :data="conditions || []"
          :actions="conditionsActions"
          :loading="conditionsPending"
          searchable
          search-placeholder="Buscar condições..."
          empty-message="Nenhuma condição médica registrada"
        >
          <template #cell-onsetDate="{ value }">
            <span class="text-sm">{{ formatDate(value) }}</span>
          </template>
          <template #cell-code="{ value }">
            <span
              v-if="value"
              class="inline-block rounded-full bg-blue-100 px-2 py-1 text-xs font-medium text-blue-800"
            >
              {{ value }}
            </span>
            <span v-else class="text-gray-400 text-xs">-</span>
          </template>
        </UIDataTable>
      </div>

      <!-- Alergias -->
      <div>
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
        <UIDataTable
          :columns="allergiesColumns"
          :data="allergies || []"
          :actions="allergiesActions"
          :loading="allergiesPending"
          searchable
          search-placeholder="Buscar alergias..."
          empty-message="Nenhuma alergia registrada"
        >
          <template #cell-severity="{ value }">
            <span
              class="inline-block rounded-full px-3 py-1 text-xs font-semibold"
              :class="{
                'bg-red-100 text-red-800': value === 'Grave',
                'bg-yellow-100 text-yellow-800': value === 'Moderada',
                'bg-green-100 text-green-800': value === 'Leve',
                'bg-gray-100 text-gray-700':
                  !value ||
                  (value !== 'Grave' &&
                    value !== 'Moderada' &&
                    value !== 'Leve'),
              }"
            >
              {{ value || "-" }}
            </span>
          </template>
        </UIDataTable>
      </div>

      <!-- Medicações -->
      <div>
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-xl font-semibold text-gray-800">Medicações</h3>
          <button
            @click="openModal('medication')"
            class="btn-add"
            title="Adicionar Medicação"
          >
            + Adicionar
          </button>
        </div>
        <UIDataTable
          :columns="medicationsColumns"
          :data="meds || []"
          :actions="medicationsActions"
          :loading="medsPending"
          searchable
          search-placeholder="Buscar medicações..."
          empty-message="Nenhuma medicação registrada"
        >
          <template #cell-startDate="{ value }">
            <span class="text-sm">{{ formatDate(value) }}</span>
          </template>
        </UIDataTable>
      </div>
    </div>

    <!-- Modals -->
    <UIBaseModal v-if="showModal" :model-value="showModal" @close="closeModal">
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
definePageMeta({
  middleware: "auth-only",
  layout: "default",
});

const route = useRoute();
// Use route params instead of selected patient
const patientId = computed(() => route.params.id as string);

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

// Fetch data directly using patientId from route
const {
  data: notes,
  pending: notesPending,
  error: notesError,
  refresh: refreshNotes,
} = await useApiFetch<ClinicalNote[]>(
  `/patients/${patientId.value}/history/notes`,
  { lazy: false }
);

const {
  data: conditions,
  pending: conditionsPending,
  error: conditionsError,
  refresh: refreshConditions,
} = await useApiFetch<MedicalCondition[]>(
  `/patients/${patientId.value}/conditions`,
  { lazy: false }
);

const {
  data: allergies,
  pending: allergiesPending,
  error: allergiesError,
  refresh: refreshAllergies,
} = await useApiFetch<Allergy[]>(`/patients/${patientId.value}/allergies`, {
  lazy: false,
});

const {
  data: meds,
  pending: medsPending,
  error: medsError,
  refresh: refreshMeds,
} = await useApiFetch<Medication[]>(
  `/patients/${patientId.value}/medications`,
  { lazy: false }
);

const conditionsColumns = [
  { key: "description", label: "Descrição", sortable: true },
  { key: "code", label: "Código (CID-10)", sortable: true },
  { key: "onsetDate", label: "Data de Início", sortable: true },
];

const conditionsActions = [
  {
    label: "Deletar",
    handler: (row: MedicalCondition) => deleteCondition(row.id),
    variant: "danger" as const,
  },
];

const allergiesColumns = [
  { key: "substance", label: "Substância", sortable: true },
  { key: "reaction", label: "Reação", sortable: true },
  { key: "severity", label: "Severidade", sortable: true },
];

const allergiesActions = [
  {
    label: "Deletar",
    handler: (row: Allergy) => deleteAllergy(row.id),
    variant: "danger" as const,
  },
];

const medicationsColumns = [
  { key: "drugName", label: "Medicamento", sortable: true },
  { key: "dose", label: "Dose", sortable: false },
  { key: "frequency", label: "Frequência", sortable: false },
  { key: "route", label: "Via", sortable: false },
  { key: "startDate", label: "Início", sortable: true },
];

const medicationsActions = [
  {
    label: "Deletar",
    handler: (row: Medication) => deleteMedication(row.id),
    variant: "danger" as const,
  },
];

const showModal = ref(false);
const modalType = ref<"note" | "condition" | "allergy" | "medication">("note");
const saving = ref(false);

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

async function submitNote() {
  saving.value = true;
  try {
    const { data, error } = await useApiFetch(
      `/patients/${patientId.value}/history/notes`,
      {
        method: "POST",
        body: { note: noteForm.value.note },
        key: `add-note-${Date.now()}`,
      }
    );

    if (error.value) {
      throw new Error(error.value.message || "Erro ao adicionar nota");
    }

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
  saving.value = true;
  try {
    const { data, error } = await useApiFetch(
      `/patients/${patientId.value}/conditions`,
      {
        method: "POST",
        body: {
          description: conditionForm.value.description,
          code: conditionForm.value.code || null,
          onsetDate: conditionForm.value.onsetDate,
        },
        key: `add-condition-${Date.now()}`,
      }
    );

    if (error.value) {
      throw new Error(error.value.message || "Erro ao adicionar condição");
    }

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
  saving.value = true;
  try {
    const { data, error } = await useApiFetch(
      `/patients/${patientId.value}/allergies`,
      {
        method: "POST",
        body: {
          substance: allergyForm.value.substance,
          reaction: allergyForm.value.reaction,
          severity: allergyForm.value.severity,
        },
        key: `add-allergy-${Date.now()}`,
      }
    );

    if (error.value) {
      throw new Error(error.value.message || "Erro ao adicionar alergia");
    }

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
  saving.value = true;
  try {
    const { data, error } = await useApiFetch(
      `/patients/${patientId.value}/medications`,
      {
        method: "POST",
        body: {
          drugName: medicationForm.value.drugName,
          dose: medicationForm.value.dose,
          route: medicationForm.value.route,
          frequency: medicationForm.value.frequency,
          startDate: medicationForm.value.startDate,
          endDate: medicationForm.value.endDate || null,
        },
        key: `add-medication-${Date.now()}`,
      }
    );

    if (error.value) {
      throw new Error(error.value.message || "Erro ao adicionar medicação");
    }

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

async function deleteNote(noteId: string) {
  if (!confirm("Tem certeza que deseja deletar esta nota clínica?")) return;

  try {
    await useApiFetch(`/patients/${patientId.value}/history/notes/${noteId}`, {
      method: "DELETE",
    });

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
    const { error } = await useApiFetch(
      `/patients/${patientId.value}/conditions/${conditionId}`,
      {
        method: "DELETE",
        key: `delete-condition-${conditionId}-${Date.now()}`,
      }
    );

    if (error.value) {
      throw new Error(error.value.message || "Erro ao deletar condição");
    }

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
    const { error } = await useApiFetch(
      `/patients/${patientId.value}/allergies/${allergyId}`,
      {
        method: "DELETE",
        key: `delete-allergy-${allergyId}-${Date.now()}`,
      }
    );

    if (error.value) {
      throw new Error(error.value.message || "Erro ao deletar alergia");
    }

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
    const { error } = await useApiFetch(
      `/patients/${patientId.value}/medications/${medicationId}`,
      {
        method: "DELETE",
        key: `delete-medication-${medicationId}-${Date.now()}`,
      }
    );

    if (error.value) {
      throw new Error(error.value.message || "Erro ao deletar medicação");
    }

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
.btn-add {
  @apply px-3 py-1.5 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700;
}
.btn-primary {
  @apply px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50;
}
.btn-secondary {
  @apply px-4 py-2 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300;
}
</style>
