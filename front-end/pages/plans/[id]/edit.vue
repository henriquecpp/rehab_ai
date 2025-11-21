<template>
  <div>
    <h1 class="mb-6 text-3xl font-bold text-gray-800">
      Editar Plano - Versão {{ currentVersion }}
    </h1>

    <div v-if="pending" class="text-center p-10">
      <p>Carregando dados do plano...</p>
    </div>

    <div
      v-else-if="error"
      class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded"
    >
      <p>
        <strong>Erro ao buscar dados:</strong>
        {{ error }}
      </p>
    </div>

    <div
      v-else-if="planData && planData.status !== 'DRAFT'"
      class="bg-yellow-100 border border-yellow-400 text-yellow-700 px-4 py-3 rounded"
    >
      <p>
        <strong>Atenção:</strong> Apenas planos com status DRAFT podem ser
        editados. Este plano está como {{ getStatusLabel(planData.status) }}.
      </p>
      <button
        @click="$router.push(`/plans/${route.params.id}`)"
        class="mt-3 px-4 py-2 bg-yellow-600 text-white rounded hover:bg-yellow-700"
      >
        Voltar para Detalhes
      </button>
    </div>

    <form
      v-else-if="editablePlan"
      @submit.prevent="handleUpdatePlan"
      class="space-y-8"
    >
      <div class="p-6 bg-white rounded-lg shadow-md border border-gray-200">
        <h2 class="text-xl font-semibold mb-4 border-b pb-2">
          Detalhes do Plano
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label for="title" class="form-label">Título do Plano</label>
            <input
              id="title"
              v-model="editablePlan.title"
              type="text"
              class="form-input"
              placeholder="Ex: Fortalecimento de Joelho"
              required
            />
          </div>
          <div>
            <label for="diagnosis" class="form-label"
              >Diagnóstico Principal</label
            >
            <input
              id="diagnosis"
              v-model="editablePlan.diagnosis"
              type="text"
              class="form-input"
              placeholder="Ex: Gonartrose"
              required
            />
          </div>
          <div class="md:col-span-2">
            <label for="description" class="form-label">Descrição</label>
            <textarea
              id="description"
              v-model="editablePlan.description"
              rows="2"
              class="form-input"
              placeholder="Breve descrição do plano..."
            ></textarea>
          </div>
          <div class="md:col-span-2">
            <label for="goals" class="form-label"
              >Metas (separadas por vírgula)</label
            >
            <input
              id="goals"
              :value="editablePlan.goals.join(', ')"
              @input="updateGoals"
              type="text"
              class="form-input"
              placeholder="Ex: Reduzir dor, Aumentar mobilidade"
            />
          </div>
          <div class="md:col-span-2">
            <label for="notes" class="form-label">Notas Clínicas</label>
            <textarea
              id="notes"
              v-model="editablePlan.notes"
              rows="2"
              class="form-input"
              placeholder="Notas adicionais para o paciente ou outros clínicos..."
            ></textarea>
          </div>
        </div>
      </div>

      <div class="p-6 bg-white rounded-lg shadow-md border border-gray-200">
        <h2 class="text-xl font-semibold mb-4 border-b pb-2">
          Metadados Clínicos
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label for="priority" class="form-label">Prioridade</label>
            <select
              id="priority"
              v-model="editablePlan.priority"
              class="form-input"
            >
              <option value="LOW">Baixa</option>
              <option value="MEDIUM">Média</option>
              <option value="HIGH">Alta</option>
            </select>
          </div>
          <div>
            <label for="tags" class="form-label"
              >Tags (separadas por vírgula)</label
            >
            <input
              id="tags"
              :value="editablePlan.tags.join(', ')"
              @input="updateTags"
              type="text"
              class="form-input"
              placeholder="Ex: Pos-operatorio, Fortalecimento"
            />
          </div>
          <div>
            <label for="painLevelStart" class="form-label"
              >Nível de Dor Inicial (0-10)</label
            >
            <input
              id="painLevelStart"
              v-model.number="editablePlan.painLevelStart"
              type="number"
              min="0"
              max="10"
              class="form-input"
            />
          </div>
          <div>
            <label for="painLevelExpectedEnd" class="form-label"
              >Dor Esperada no Final (0-10)</label
            >
            <input
              id="painLevelExpectedEnd"
              v-model.number="editablePlan.painLevelExpectedEnd"
              type="number"
              min="0"
              max="10"
              class="form-input"
            />
          </div>
          <div>
            <label for="startDate" class="form-label">Data de Início</label>
            <input
              id="startDate"
              v-model="editablePlan.startDate"
              type="date"
              class="form-input"
            />
          </div>
          <div>
            <label for="endDate" class="form-label">Data de Término</label>
            <input
              id="endDate"
              v-model="editablePlan.endDate"
              type="date"
              class="form-input"
            />
          </div>
        </div>
      </div>

      <div class="p-6 bg-white rounded-lg shadow-md border border-gray-200">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-xl font-semibold">Exercícios</h2>
          <button
            type="button"
            @click="isExerciseModalOpen = true"
            class="btn-secondary"
          >
            Adicionar da Biblioteca
          </button>
        </div>

        <div v-if="editablePlan.exercises.length > 0" class="space-y-6">
          <div
            v-for="(exercise, index) in editablePlan.exercises"
            :key="index"
            class="space-y-4 border-b pb-4"
          >
            <div class="flex justify-between items-center">
              <h3 class="font-semibold text-lg text-primary">
                {{ index + 1 }}. {{ exercise.name }}
              </h3>
              <button
                type="button"
                @click="removeExercise(index)"
                class="text-red-600 hover:text-red-800 text-sm font-semibold"
              >
                Remover
              </button>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label class="form-label">Exercício</label>
                <input v-model="exercise.name" type="text" class="form-input" />
              </div>
              <div>
                <label class="form-label">Séries (sets)</label>
                <input
                  v-model.number="exercise.sets"
                  type="number"
                  class="form-input"
                />
              </div>
              <div>
                <label class="form-label">Repetições (reps)</label>
                <input
                  v-model.number="exercise.repetitions"
                  type="number"
                  class="form-input"
                />
              </div>
              <div class="md:col-span-3">
                <label class="form-label">Descrição/Instrução</label>
                <textarea
                  v-model="exercise.description"
                  rows="2"
                  class="form-input"
                ></textarea>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="text-center text-gray-500 py-6">
          <p>
            Nenhum exercício adicionado. <br />Clique em "Adicionar da
            Biblioteca" para começar.
          </p>
        </div>
      </div>

      <div class="flex justify-end gap-4">
        <button type="button" class="btn-secondary" @click="router.back()">
          Cancelar
        </button>
        <button type="submit" class="btn-primary" :disabled="isSubmitting">
          {{ isSubmitting ? "Salvando..." : "Salvar Alterações" }}
        </button>
      </div>
    </form>

    <div v-else class="text-center p-10 text-gray-600">
      <p>Não foi possível carregar os dados do plano.</p>
    </div>

    <UIBaseModal
      :model-value="isExerciseModalOpen"
      @close="isExerciseModalOpen = false"
      title="Biblioteca de Exercícios do Paciente"
    >
      <div class="max-h-[60vh] overflow-y-auto p-1">
        <div
          v-if="exerciseLibrary.length === 0"
          class="text-center text-gray-500 py-6"
        >
          Nenhum exercício encontrado nas prescrições anteriores deste paciente.
        </div>

        <div v-else class="space-y-3">
          <div
            v-for="exercise in exerciseLibrary"
            :key="exercise.name"
            class="flex items-start gap-3 p-3 rounded-lg border border-gray-200"
          >
            <input
              :id="`ex-${exercise.name}`"
              type="checkbox"
              :value="exercise"
              v-model="exercisesToAddToPlan"
              class="mt-1 h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary-dark"
            />
            <label :for="`ex-${exercise.name}`" class="flex-1">
              <span class="font-semibold text-gray-800">{{
                exercise.name
              }}</span>
              <p class="text-sm text-gray-600">{{ exercise.description }}</p>
            </label>
          </div>
        </div>
      </div>
      <template #footer>
        <button
          type="button"
          class="btn-secondary"
          @click="isExerciseModalOpen = false"
        >
          Cancelar
        </button>
        <button
          type="button"
          class="btn-primary"
          @click="addSelectedExercisesToPlan"
        >
          Adicionar Selecionados ({{ exercisesToAddToPlan.length }})
        </button>
      </template>
    </UIBaseModal>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from "~/store/auth";
import type { ExerciseDto } from "~/types/exercise";
import type { PlanDataStructure } from "~/types/plan";
import type { PrescriptionListItem } from "~/types/prescription";

definePageMeta({
  middleware: ["auth-only"],
});

const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();

// Check if user has permission to edit plans
if (!authStore.isClinician && !authStore.isAdmin) {
  router.push("/plans");
}

const isSubmitting = ref(false);
const pending = ref(true);
const error = ref<string | null>(null);

interface EditablePlanForm {
  title: string;
  description: string;
  diagnosis: string;
  exercises: ExerciseDto[];
  goals: string[];
  notes: string;
  priority: "LOW" | "MEDIUM" | "HIGH";
  painLevelStart: number;
  painLevelExpectedEnd: number;
  startDate?: string;
  endDate?: string;
  tags: string[];
}

interface PlanResponse {
  id: string;
  userId: string;
  status: "DRAFT" | "APPROVED" | "ARCHIVED";
  version: number;
  planData: string; // JSON string
  priority: "LOW" | "MEDIUM" | "HIGH";
  painLevelStart: number;
  painLevelExpectedEnd: number;
  startDate?: string;
  endDate?: string;
  tags: string[];
}

const planData = ref<PlanResponse | null>(null);
const editablePlan = ref<EditablePlanForm | null>(null);
const currentVersion = ref(1);

// Helper para formatar data para input: '2025-11-18T09:00:00Z' -> '2025-11-18'
const formatDateForInput = (dateString?: string) => {
  if (!dateString) return "";
  try {
    return new Date(dateString).toISOString().split("T")[0];
  } catch (e) {
    return "";
  }
};

// Load plan data
const loadPlanData = async () => {
  pending.value = true;
  error.value = null;

  try {
    const { data: plan } = await useApiFetch<PlanResponse>(
      `/plans/${route.params.id}`,
      {
        key: `plan-edit-${route.params.id}`,
        lazy: false,
      }
    );

    if (!plan.value) {
      error.value = "Plano não encontrado.";
      return;
    }

    planData.value = plan.value;
    currentVersion.value = plan.value.version;

    // Parse planData JSON
    let parsedPlanData: PlanDataStructure;
    try {
      parsedPlanData = JSON.parse(plan.value.planData);
    } catch (e) {
      console.error("Erro ao fazer parse do planData:", e);
      error.value = "Erro ao carregar dados do plano.";
      return;
    }

    // Populate editable form
    editablePlan.value = {
      title: parsedPlanData.title || "",
      description: parsedPlanData.description || "",
      diagnosis: parsedPlanData.diagnosis || "",
      exercises: parsedPlanData.exercises || [],
      goals: parsedPlanData.goals || [],
      notes: parsedPlanData.notes || "",
      priority: plan.value.priority || "MEDIUM",
      painLevelStart: plan.value.painLevelStart || 0,
      painLevelExpectedEnd: plan.value.painLevelExpectedEnd || 0,
      startDate: formatDateForInput(plan.value.startDate),
      endDate: formatDateForInput(plan.value.endDate),
      tags: plan.value.tags || [],
    };
  } catch (e: any) {
    console.error("Erro ao carregar plano:", e);
    error.value = e.message || "Erro ao carregar plano.";
  } finally {
    pending.value = false;
  }
};

// Load plan on mount
onMounted(() => {
  loadPlanData();
});

// Exercise library
const isExerciseModalOpen = ref(false);
const exercisesToAddToPlan = ref<ExerciseDto[]>([]);

const prescriptionListUrl = computed(() => {
  return planData.value?.userId
    ? `/prescriptions/user/${planData.value.userId}`
    : null;
});

const { data: prescriptionList } = await useApiFetch<PrescriptionListItem[]>(
  prescriptionListUrl,
  {
    key: computed(() => `prescriptions-${planData.value?.userId || "none"}`),
    lazy: true,
  }
);

const exerciseLibrary = computed(() => {
  const exercisesInPlan = editablePlan.value?.exercises || [];
  const namesInPlan = new Set(
    exercisesInPlan.map((ex) => ex.name.toLowerCase())
  );
  if (!prescriptionList.value) return [];
  const allExercises: ExerciseDto[] = [];
  for (const pres of prescriptionList.value) {
    try {
      const planDataParsed = JSON.parse(
        pres.prescriptionText
      ) as PlanDataStructure;
      if (planDataParsed.exercises) {
        allExercises.push(...planDataParsed.exercises);
      }
    } catch (e) {
      console.warn(`Ignorando prescrição malformada: ${pres.id}`);
    }
  }
  const uniqueExercises = new Map<string, ExerciseDto>();
  for (const exercise of allExercises) {
    if (exercise.name && !uniqueExercises.has(exercise.name.toLowerCase())) {
      uniqueExercises.set(exercise.name.toLowerCase(), exercise);
    }
  }
  const fullLibrary = Array.from(uniqueExercises.values());
  return fullLibrary.filter(
    (exercise) => !namesInPlan.has(exercise.name.toLowerCase())
  );
});

function addSelectedExercisesToPlan() {
  if (editablePlan.value) {
    const clonedExercises = JSON.parse(
      JSON.stringify(exercisesToAddToPlan.value)
    ) as ExerciseDto[];
    editablePlan.value.exercises.push(...clonedExercises);
  }
  exercisesToAddToPlan.value = [];
  isExerciseModalOpen.value = false;
}

function removeExercise(index: number) {
  if (editablePlan.value) {
    editablePlan.value.exercises.splice(index, 1);
  }
}

function updateGoals(event: Event) {
  if (editablePlan.value) {
    const target = event.target as HTMLInputElement;
    editablePlan.value.goals = target.value
      .split(",")
      .map((g) => g.trim())
      .filter(Boolean);
  }
}

function updateTags(event: Event) {
  if (editablePlan.value) {
    const target = event.target as HTMLInputElement;
    editablePlan.value.tags = target.value
      .split(",")
      .map((t) => t.trim())
      .filter(Boolean);
  }
}

const formatDateForApi = (dateString?: string) => {
  if (!dateString) return undefined;
  try {
    return new Date(`${dateString}T00:00:00.000Z`).toISOString();
  } catch (e) {
    return undefined;
  }
};

function getStatusLabel(status: string): string {
  const labels = {
    DRAFT: "Rascunho",
    APPROVED: "Aprovado",
    ARCHIVED: "Arquivado",
  };
  return labels[status as keyof typeof labels] || status;
}

async function handleUpdatePlan() {
  if (!editablePlan.value || !planData.value) return;

  if (planData.value.status !== "DRAFT") {
    alert("Apenas planos com status DRAFT podem ser editados.");
    return;
  }

  isSubmitting.value = true;
  try {
    const planDataToStringify: PlanDataStructure = {
      title: editablePlan.value.title,
      description: editablePlan.value.description,
      diagnosis: editablePlan.value.diagnosis,
      exercises: editablePlan.value.exercises,
      goals: editablePlan.value.goals,
      notes: editablePlan.value.notes,
    };
    const planDataString = JSON.stringify(planDataToStringify);

    const payload: any = {
      planData: planDataString,
      priority: editablePlan.value.priority,
      painLevelStart: editablePlan.value.painLevelStart,
      painLevelExpectedEnd: editablePlan.value.painLevelExpectedEnd,
      startDate: formatDateForApi(editablePlan.value.startDate),
      endDate: formatDateForApi(editablePlan.value.endDate),
      tags: editablePlan.value.tags,
    };

    console.log("Updating plan with payload:", payload);

    await $api(`/plans/${route.params.id}`, {
      method: "PUT",
      body: payload,
    });

    alert("Plano atualizado com sucesso!");
    await router.push(`/plans/${route.params.id}`);
  } catch (err) {
    console.error("Falha ao atualizar o plano:", err);
    alert("Erro ao atualizar o plano. Verifique o console para mais detalhes.");
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<style scoped>
.form-label {
  @apply block mb-1.5 text-sm font-medium text-gray-700;
}
.form-input {
  @apply w-full rounded-md border-gray-300 shadow-sm focus:border-primary focus:ring-primary-dark;
}
.btn-primary {
  @apply py-2 px-4 bg-primary text-white font-semibold rounded-lg shadow-md hover:bg-primary-dark disabled:opacity-50;
}
.btn-secondary {
  @apply py-2 px-4 bg-gray-200 text-gray-800 font-semibold rounded-lg hover:bg-gray-300;
}
</style>
