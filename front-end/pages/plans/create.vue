<template>
  <div>
    <UICard v-if="pending && !draft" title="Carregando Rascunho da IA">
      <p class="text-gray-600">Buscando dados da prescrição gerada...</p>
    </UICard>

    <UICard v-else-if="error" title="Erro ao Carregar Rascunho">
      <div class="alert alert-danger">
        Não foi possível carregar o rascunho: {{ error.message }}
      </div>
      <NuxtLink to="/plans" class="btn-secondary mt-4">
        &larr; Voltar
      </NuxtLink>
    </UICard>

    <!-- Form -->
    <form v-else-if="draft" @submit.prevent="handleSavePlan">
      <UICard>
        <template #title>
          <div class="flex justify-between items-center">
            <span>Revisar e Criar Plano</span>
            <span
              v-if="draft.confidenceScore"
              class="badge text-base"
              :class="getConfidenceClass(draft.confidenceScore)"
            >
              Confiança: {{ (draft.confidenceScore * 100).toFixed(0) }}%
            </span>
          </div>
        </template>

        <div v-if="saveError" class="alert alert-danger">{{ saveError }}</div>
        <div v-if="isSaving" class="alert alert-info">Salvando plano...</div>

        <!-- Campos -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div class="form-group">
            <label class="form-label">Título do Plano</label>
            <input type="text" v-model="draft.title" class="form-input" />
          </div>

          <div class="form-group">
            <label class="form-label">Diagnóstico</label>
            <input type="text" v-model="draft.diagnosis" class="form-input" />
          </div>
        </div>

        <div class="form-group mt-4">
          <label class="form-label">Descrição</label>
          <textarea v-model="draft.description" rows="3" class="form-input" />
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mt-4">
          <div class="form-group">
            <label class="form-label">Duração (dias)</label>
            <input
              type="number"
              v-model.number="draft.duration"
              class="form-input"
            />
          </div>

          <div class="form-group">
            <label class="form-label">Início</label>
            <input type="date" v-model="draft.startDate" class="form-input" />
          </div>

          <div class="form-group">
            <label class="form-label">Término</label>
            <input type="date" v-model="draft.endDate" class="form-input" />
          </div>
        </div>
      </UICard>

      <!-- Objetivos -->
      <UICard title="Objetivos" class="mt-6">
        <div
          v-for="(goal, index) in draft.goals"
          :key="index"
          class="flex gap-2 mb-2"
        >
          <input type="text" v-model="draft.goals[index]" class="form-input" />
          <button
            type="button"
            class="btn-danger-outline"
            @click="removeGoal(index)"
          >
            X
          </button>
        </div>
        <button
          type="button"
          class="btn-secondary-outline mt-2"
          @click="addGoal"
        >
          + Adicionar Objetivo
        </button>
      </UICard>

      <!-- Exercícios -->
      <UICard title="Exercícios" class="mt-6">
        <div class="flex flex-col gap-4">
          <div
            v-for="(exercise, index) in draft.exercises"
            :key="index"
            class="exercise-editor"
          >
            <div class="flex justify-between items-center mb-3">
              <h4 class="text-lg font-semibold text-primary">
                Exercício {{ index + 1 }}
              </h4>
              <button
                type="button"
                @click="removeExercise(index)"
                class="btn-danger-outline"
              >
                Remover
              </button>
            </div>
            <UIExerciseForm v-model="draft.exercises[index]" />
          </div>
        </div>

        <button
          type="button"
          class="btn-secondary-outline mt-4"
          @click="addExercise"
        >
          + Adicionar Exercício
        </button>
      </UICard>

      <div class="mt-6 flex justify-end gap-4">
        <NuxtLink to="/plans" class="btn-secondary">Cancelar</NuxtLink>
        <button type="submit" class="btn-primary" :disabled="isSaving">
          {{ isSaving ? "Salvando..." : "Salvar e Criar Plano" }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { $api } from "~/utils/api";

definePageMeta({ middleware: "auth-only" });

const route = useRoute();
const router = useRouter();

const prescriptionId = computed(
  () => route.query.prescriptionId as string | null
);
const userId = computed(() => route.query.userId as string | null);

const isSaving = ref(false);
const saveError = ref<string | null>(null);

const {
  data: draft,
  pending,
  error,
} = useApiFetch(
  computed(() =>
    prescriptionId.value && userId.value
      ? `/prescriptions/generated/${prescriptionId.value}/plan-draft`
      : null
  ),
  {
    query: computed(() => ({ userId: userId.value })),
    key: computed(() => `draft-${prescriptionId.value || "none"}`),
    watch: [prescriptionId, userId],
    lazy: true,
    server: false, // 🚀 impede SSR quebrado
  }
);

/* --------------------------
   Manipulação de Objetivos
-------------------------- */
const addGoal = () => draft.value?.goals.push("Novo objetivo");
const removeGoal = (i: number) => draft.value?.goals.splice(i, 1);

/* --------------------------
   Manipulação de Exercícios
-------------------------- */
const addExercise = () => {
  draft.value?.exercises.push({
    name: "Novo Exercício",
    description: "",
    sets: 3,
    repetitions: 10,
    duration: null,
    frequency: "DIARIO",
  });
};

const removeExercise = (i: number) => draft.value?.exercises.splice(i, 1);

/* --------------------------
   SALVAR PLANO
-------------------------- */
async function handleSavePlan() {
  if (!draft.value) return;

  isSaving.value = true;
  saveError.value = null;

  try {
    const payload = {
      title: draft.value.title,
      description: draft.value.description,
      diagnosis: draft.value.diagnosis,
      exercises: draft.value.exercises,
      goals: draft.value.goals,
      duration: draft.value.duration,
      frequency: draft.value.frequency,
      startDate: draft.value.startDate,
      endDate: draft.value.endDate,
      _aiMetadata: {
        confidenceScore: draft.value.confidenceScore,
        prescriptionId: draft.value.prescriptionId,
      },
    };

    await $api("/plans", {
      method: "POST",
      body: {
        userId: draft.value.userId,
        prescriptionId: draft.value.prescriptionId,
        planData: JSON.stringify(payload),
      },
    });

    router.push("/plans");
  } catch (err: any) {
    console.error("Erro ao salvar:", err);
    saveError.value = err.data?.message || err.message;
  } finally {
    isSaving.value = false;
  }
}


function getConfidenceClass(score: number) {
  if (score > 0.85) return "badge-success";
  if (score > 0.7) return "badge-warning";
  return "badge-danger";
}
</script>

<style scoped>
.form-input {
  @apply w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm shadow-sm;
}
.form-label {
  @apply mb-1 block text-sm font-semibold text-gray-700;
}
.form-group {
  @apply flex flex-col;
}
.alert {
  @apply mb-4 flex gap-3 rounded-lg border-l-4 p-4;
}
.alert-info {
  @apply border-primary bg-blue-50 text-blue-800;
}
.alert-danger {
  @apply border-danger bg-red-50 text-red-800;
}
</style>
