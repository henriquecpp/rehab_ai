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
      <div
        v-if="prescriptionFileMetadata"
        class="p-6 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-lg shadow-md border-2 border-blue-200"
      >
        <div class="flex items-start justify-between mb-4">
          <div class="flex items-center gap-3">
            <span class="text-4xl">📄</span>
            <div>
              <h2 class="text-xl font-semibold text-blue-900">
                Documento Original
              </h2>
              <p class="text-sm text-blue-700">
                Arquivo usado pela IA para gerar este plano
              </p>
            </div>
          </div>
          <div class="flex gap-2">
            <button
              type="button"
              @click="showFileViewer = true"
              class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm font-medium flex items-center gap-2"
            >
              <span>👁️</span> Visualizar
            </button>
            <button
              type="button"
              @click="downloadFile"
              class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 text-sm font-medium flex items-center gap-2"
            >
              <span>📥</span> Download
            </button>
          </div>
        </div>

        <div class="bg-white rounded-lg p-4 border border-blue-200">
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span class="font-semibold text-gray-700">Nome do Arquivo:</span>
              <p class="text-gray-900">
                {{ prescriptionFileMetadata.fileName }}
              </p>
            </div>
            <div>
              <span class="font-semibold text-gray-700">Tipo:</span>
              <p class="text-gray-900">
                {{ formatFileType(prescriptionFileMetadata.fileType) }}
              </p>
            </div>
          </div>
        </div>
      </div>

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
          {{ isSubmitting ? "Salvando..." : "Criar Plano (Versão 1)" }}
        </button>
      </div>
    </form>

    <div v-else class="text-center p-10 text-gray-600">
      <p>Não foi possível carregar os dados.</p>
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

    <!-- File Viewer Modal -->
    <UIBaseModal
      :model-value="showFileViewer"
      @close="showFileViewer = false"
      :title="prescriptionFileMetadata?.fileName || 'Visualizar Documento'"
    >
      <div class="w-full" style="height: 75vh">
        <div
          v-if="loadingFile"
          class="bg-white rounded-lg p-10 text-center h-full flex items-center justify-center"
        >
          <div class="flex flex-col items-center">
            <div
              class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mb-4"
            ></div>
            <p class="text-gray-600">Carregando arquivo...</p>
          </div>
        </div>
        <div
          v-else-if="fileViewerError"
          class="bg-red-50 rounded-lg p-10 text-center h-full flex items-center justify-center"
        >
          <div class="flex flex-col items-center">
            <span class="text-6xl mb-4">⚠️</span>
            <p class="text-red-800 font-semibold mb-2">
              Erro ao carregar arquivo
            </p>
            <p class="text-red-600 text-sm mb-4">{{ fileViewerError }}</p>
            <button
              type="button"
              @click="downloadFile"
              class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              Tentar Download
            </button>
          </div>
        </div>
        <iframe
          v-else-if="fileUrl"
          :src="fileUrl"
          class="w-full h-full rounded-lg border border-gray-300"
          frameborder="0"
          title="Visualização do documento"
        >
        </iframe>
      </div>
      <template #footer>
        <button
          type="button"
          class="btn-secondary"
          @click="showFileViewer = false"
        >
          Fechar
        </button>
        <button type="button" class="btn-primary" @click="downloadFile">
          <span>📥</span> Download
        </button>
      </template>
    </UIBaseModal>
  </div>
</template>

<script setup lang="ts">
import type { NuxtError } from "#app";
import { computed, ref, watch } from "vue";
import { useToast } from "~/composables/useToast";
import type { ExerciseDto } from "~/types/exercise";
import type { CreatePlanRequest, PlanDataStructure } from "~/types/plan";
import type {
  PlanDraftResponse,
  PrescriptionListItem,
  PrescriptionResponse,
} from "~/types/prescription";

definePageMeta({
  middleware: ["auth-only"],
});

const route = useRoute();
const router = useRouter();
const { addToast } = useToast();
const isSubmitting = ref(false);

const prescriptionId = computed(() => {
  const id = route.query.prescriptionId;
  return Array.isArray(id) ? id[0] : id;
});
const userId = computed(() => {
  const id = route.query.userId;
  return Array.isArray(id) ? id[0] : id;
});

interface EditablePlanForm {
  userId: string;
  title: string;
  description: string;
  diagnosis: string;
  exercises: ExerciseDto[];
  goals: string[];
  notes: string;
  origin: "AI_GENERATED" | "CLINICIAN_CREATED" | "AI_REVIEWED_BY_THERAPIST";
  confidenceScore?: number | null;
  priority: "LOW" | "MEDIUM" | "HIGH";
  painLevelStart: number;
  painLevelExpectedEnd: number;
  startDate?: string;
  endDate?: string;
  tags: string[];
}

const editablePlan = ref<EditablePlanForm | null>(null);
const pending = ref(true);
// Use o tipo de erro correto do Nuxt
const error = ref<NuxtError | Error | null>(null);

const showFileViewer = ref(false);
const fileUrl = ref<string | null>(null);
const loadingFile = ref(false);
const fileViewerError = ref<string | null>(null);
const prescriptionFileMetadata = ref<{
  fileId: string;
  fileName: string;
  fileType: string;
  viewUrl?: string;
  downloadUrl?: string;
} | null>(null);

const formatFileType = (fileType: string): string => {
  const types: Record<string, string> = {
    MEDICAL_REPORT: "Laudo Médico",
    PRESCRIPTION: "Prescrição",
    IMAGE: "Imagem",
    OTHER: "Outro",
  };
  return types[fileType] || fileType;
};

// Helper para formatar data: '2025-11-18T09:00:00Z' ou undefined -> '2025-11-18'
const formatDateForInput = (dateString?: string) => {
  if (!dateString) return new Date().toISOString().split("T")[0];
  try {
    return new Date(dateString).toISOString().split("T")[0];
  } catch (e) {
    return new Date().toISOString().split("T")[0];
  }
};

const downloadFile = async () => {
  if (!prescriptionFileMetadata.value) return;

  try {
    const blob = await $api(
      `/files/${prescriptionFileMetadata.value.fileId}/download`,
      {
        responseType: "blob",
      }
    );
    const url = window.URL.createObjectURL(blob as Blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = prescriptionFileMetadata.value.fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error("Download error:", error);
    alert("Erro ao baixar arquivo");
  }
};

watch(showFileViewer, async (show) => {
  if (show && prescriptionFileMetadata.value) {
    loadingFile.value = true;
    fileViewerError.value = null;
    try {
      const response = await $api(
        `/files/${prescriptionFileMetadata.value.fileId}/download`,
        {
          responseType: "arrayBuffer",
        }
      );

      const blob = new Blob([response as ArrayBuffer], {
        type: "application/pdf",
      });
      fileUrl.value = window.URL.createObjectURL(blob);
    } catch (error: any) {
      console.error("File load error:", error);
      fileViewerError.value =
        error.message || "Não foi possível carregar o arquivo";
    } finally {
      loadingFile.value = false;
    }
  } else if (!show && fileUrl.value) {
    window.URL.revokeObjectURL(fileUrl.value);
    fileUrl.value = null;
  }
});

if (!userId.value) {
  error.value = createError({
    statusCode: 400,
    statusMessage: "Nenhum ID de usuário fornecido para criar o plano.",
  });
  pending.value = false;
} else if (prescriptionId.value) {
  const {
    data: prescriptionData,
    pending: prescriptionPending,
    error: prescriptionError,
  } = await useApiFetch<PrescriptionResponse>(
    `/prescriptions/${prescriptionId.value}`,
    {
      lazy: false,
    }
  );

  pending.value = prescriptionPending.value;
  error.value = prescriptionError.value || null;

  if (prescriptionData.value && prescriptionData.value.originalText) {
    if (prescriptionData.value.fileMetadata) {
      prescriptionFileMetadata.value = {
        fileId: prescriptionData.value.fileMetadata.fileId,
        fileName: prescriptionData.value.fileMetadata.fileName,
        fileType: prescriptionData.value.fileMetadata.fileType,
        viewUrl: prescriptionData.value.fileMetadata.viewUrl,
        downloadUrl: prescriptionData.value.fileMetadata.downloadUrl,
      };
    }

    try {
      const parsedDraft = JSON.parse(
        prescriptionData.value.originalText
      ) as PlanDraftResponse;

      editablePlan.value = {
        userId: prescriptionData.value.userId,
        title: parsedDraft.title || "",
        description: parsedDraft.description || "",
        diagnosis: parsedDraft.diagnosis || "",
        exercises: parsedDraft.exercises || [],
        goals: parsedDraft.goals || [],
        notes: "",
        origin: "AI_REVIEWED_BY_THERAPIST",
        confidenceScore: 1,
        priority: "MEDIUM",
        painLevelStart: 5,
        painLevelExpectedEnd: 1,
        startDate: formatDateForInput(parsedDraft.startDate),
        endDate: formatDateForInput(parsedDraft.endDate),
        tags: [],
      };
    } catch (e: any) {
      console.error("Falha ao fazer o parse do JSON da prescrição:", e);
      error.value = createError({
        statusCode: 500,
        statusMessage: "Falha ao ler dados da prescrição.",
      });
    }
  }
} else if (userId.value) {
  editablePlan.value = {
    userId: userId.value,
    title: "",
    description: "",
    diagnosis: "",
    exercises: [],
    goals: [],
    notes: "",
    origin: "CLINICIAN_CREATED",
    confidenceScore: 1,
    priority: "MEDIUM",
    painLevelStart: 5,
    painLevelExpectedEnd: 1,
    startDate: formatDateForInput(),
    endDate: formatDateForInput(),
    tags: [],
  };
  pending.value = false;
}

const isExerciseModalOpen = ref(false);
const exercisesToAddToPlan = ref<ExerciseDto[]>([]);

const prescriptionListUrl = computed(() => {
  return userId.value ? `/prescriptions/user/${userId.value}` : null;
});
const {
  data: prescriptionList,
  pending: libPending,
  error: libError,
} = await useApiFetch<PrescriptionListItem[]>(prescriptionListUrl, {
  lazy: false,
});

const exerciseLibrary = computed(() => {
  const exercisesInPlan = editablePlan.value?.exercises || [];
  const namesInPlan = new Set(
    exercisesInPlan.map((ex) => ex.name.toLowerCase())
  );
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

async function handleCreatePlan() {
  if (!editablePlan.value) return;

  isSubmitting.value = true;
  try {
    const planDataToStingify: PlanDataStructure = {
      title: editablePlan.value.title,
      description: editablePlan.value.description,
      diagnosis: editablePlan.value.diagnosis,
      exercises: editablePlan.value.exercises,
      goals: editablePlan.value.goals,
      notes: editablePlan.value.notes,
    };
    const planDataString = JSON.stringify(planDataToStingify);

    const payload: CreatePlanRequest = {
      userId: editablePlan.value.userId,
      prescriptionId: prescriptionId.value || null,
      planData: planDataString,

      origin: editablePlan.value.origin,
      confidenceScore: 1,
      priority: editablePlan.value.priority,
      painLevelStart: editablePlan.value.painLevelStart,
      painLevelExpectedEnd: editablePlan.value.painLevelExpectedEnd,
      startDate: formatDateForApi(editablePlan.value.startDate),
      endDate: formatDateForApi(editablePlan.value.endDate),
      tags: editablePlan.value.tags,
      active: true,
    };

    if (!payload.prescriptionId) {
      delete payload.prescriptionId;
    }

    console.dir({ payload }, { depth: null });

    await $api("/plans", {
      method: "POST",
      body: payload,
    });

    addToast("Plano criado com sucesso!", "success");
    await router.push("/plans");
  } catch (err) {
    console.error("Falha ao criar o plano:", err);
    addToast("Erro ao criar o plano. Tente novamente.", "error");
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
