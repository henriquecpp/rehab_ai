<template>
  <div>
    <h1 class="mb-6 text-3xl font-bold text-gray-800">
      {{ prescriptionId ? "Revisar Plano da IA" : "Criar Novo Plano" }}
    </h1>

    <div v-if="pending || libPending" class="text-center p-10">
      <p>
        {{
          pending
            ? "Buscando dados da prescrição..."
            : "Carregando biblioteca de exercícios..."
        }}
      </p>
    </div>

    <div
      v-else-if="error || libError"
      class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded"
    >
      <p>
        <strong>Erro ao buscar dados:</strong>
        {{ error?.message || libError?.message }}
      </p>
    </div>

    <form
      v-else-if="editablePlan"
      @submit.prevent="handleCreatePlan"
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
          <div>
            <label for="duration" class="form-label">Duração (dias)</label>
            <input
              id="duration"
              v-model.number="editablePlan.duration"
              type="number"
              class="form-input"
              required
            />
          </div>
          <div>
            <label for="frequency" class="form-label">Frequência Geral</label>
            <input
              id="frequency"
              v-model="editablePlan.frequency"
              type="text"
              class="form-input"
              placeholder="Ex: 3x por semana"
            />
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
          {{ isSubmitting ? "Salvando..." : "Criar Plano (Versão 1)" }}
        </button>
      </div>
    </form>

    <div v-else class="text-center p-10 text-gray-600">
      <p>Não foi possível carregar os dados da prescrição.</p>
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
import { ref, watch, computed } from "vue";
import type {
  PlanDraftResponse,
  PrescriptionResponse,
  PrescriptionListItem,
} from "~/types/prescription";
import type { CreatePlanRequest } from "~/types/plan";
import type { ExerciseDto } from "~/types/exercise";

definePageMeta({
  middleware: ["auth-only"],
});

const route = useRoute();
const router = useRouter();
const isSubmitting = ref(false);

const prescriptionId = route.query.prescriptionId as string;
const userId = route.query.userId as string;

// --- LÓGICA DE CARREGAMENTO DO FORMULÁRIO (Sem alterações) ---
const editablePlan = ref<PlanDraftResponse | null>(null);
const pending = ref(true);
const error = ref<Error | null>(null);

if (prescriptionId) {
  // Cenário 1: Carregar de um Rascunho
  const {
    data: prescriptionData,
    pending: prescriptionPending,
    error: prescriptionError,
  } = await useApiFetch<PrescriptionResponse>(
    `/prescriptions/${prescriptionId}`,
    {
      lazy: false,
    }
  );

  pending.value = prescriptionPending.value;
  error.value = prescriptionError.value;

  if (prescriptionData.value && prescriptionData.value.originalText) {
    try {
      const parsedPlan = JSON.parse(
        prescriptionData.value.originalText
      ) as PlanDraftResponse;

      editablePlan.value = {
        ...parsedPlan,
        userId: prescriptionData.value.userId,
        prescriptionId: prescriptionData.value.id,
        exercises: parsedPlan.exercises || [],
        goals: parsedPlan.goals || [],
      };
    } catch (e: any) {
      console.error("Falha ao fazer o parse do JSON da prescrição:", e);
      error.value = e;
    }
  }
} else if (userId) {
  // Cenário 2: Criar um Plano Vazio
  editablePlan.value = {
    userId: userId,
    prescriptionId: "",
    title: "",
    description: "",
    diagnosis: "",
    exercises: [],
    goals: [],
    duration: 30,
    frequency: "3x por semana",
    startDate: new Date().toISOString().split("T")[0],
    endDate: "",
    confidenceScore: 0,
    modelUsed: "manual",
    guardrailStatus: "N/A",
  };
  pending.value = false;
} else {
  // Cenário de Erro
  error.value = new Error("Nenhum ID de usuário fornecido para criar o plano.");
  pending.value = false;
}

// --- LÓGICA DO MODAL DE EXERCÍCIOS (Com lógica de filtro) ---
const isExerciseModalOpen = ref(false);
const exercisesToAddToPlan = ref<ExerciseDto[]>([]);

const {
  data: prescriptionList,
  pending: libPending,
  error: libError,
} = await useApiFetch<PrescriptionListItem[]>(`/prescriptions/user/${userId}`, {
  lazy: false,
  immediate: !!userId,
});

// --- ATUALIZAÇÃO ESTÁ AQUI ---
const exerciseLibrary = computed(() => {
  // 1. Pega os nomes dos exercícios JÁ NO PLANO
  // Isso torna esta computed property reativa a 'editablePlan.exercises'
  const exercisesInPlan = editablePlan.value?.exercises || [];
  const namesInPlan = new Set(
    exercisesInPlan.map((ex) => ex.name.toLowerCase())
  );

  // 2. Constrói a biblioteca completa de exercícios históricos
  if (!prescriptionList.value) return [];
  const allExercises: ExerciseDto[] = [];
  for (const pres of prescriptionList.value) {
    try {
      const planData = JSON.parse(pres.prescriptionText) as PlanDraftResponse;
      if (planData.exercises) {
        allExercises.push(...planData.exercises);
      }
    } catch (e) {
      console.warn(`Ignorando prescrição malformada: ${pres.id}`);
    }
  }

  // 3. De-duplica a biblioteca
  const uniqueExercises = new Map<string, ExerciseDto>();
  for (const exercise of allExercises) {
    if (exercise.name && !uniqueExercises.has(exercise.name.toLowerCase())) {
      uniqueExercises.set(exercise.name.toLowerCase(), exercise);
    }
  }

  const fullLibrary = Array.from(uniqueExercises.values());

  // 4. Filtra a biblioteca, removendo os que já estão no plano
  return fullLibrary.filter(
    (exercise) => !namesInPlan.has(exercise.name.toLowerCase())
  );
});
// --- FIM DA ATUALIZAÇÃO ---

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

// --- FUNÇÕES HELPER (Sem alterações) ---
function updateGoals(event: Event) {
  if (editablePlan.value) {
    const target = event.target as HTMLInputElement;
    editablePlan.value.goals = target.value
      .split(",")
      .map((g) => g.trim())
      .filter(Boolean);
  }
}

async function handleCreatePlan() {
  if (!editablePlan.value) return;

  isSubmitting.value = true;
  try {
    editablePlan.value.prescriptionId = prescriptionId || "";
    const planDataString = JSON.stringify(editablePlan.value);

    const payload: CreatePlanRequest = {
      userId: editablePlan.value.userId,
      prescriptionId: prescriptionId || null,
      planData: planDataString,
    };

    if (!payload.prescriptionId) {
      delete payload.prescriptionId;
    }

    await $api("/plans", {
      method: "POST",
      body: payload,
    });

    await router.push("/plans");
  } catch (err) {
    console.error("Falha ao criar o plano:", err);
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<style scoped>
/* Estilos (Existentes) */
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
