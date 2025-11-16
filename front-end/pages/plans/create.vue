<template>
  <div>
    <h1 class="mb-6 text-3xl font-bold text-gray-800">
      Criar Novo Plano de Reabilitação
    </h1>

    <div v-if="pending" class="text-center p-10">
      <p>Buscando sugestão da IA...</p>
    </div>

    <div
      v-else-if="error || !editablePlan"
      class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded"
    >
      <p>
        <strong>Erro ao buscar rascunho do plano:</strong>
        {{ error?.message || "Dados não encontrados." }}
      </p>
    </div>

    <form v-else @submit.prevent="handleCreatePlan" class="space-y-8">
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
              required
            />
          </div>
          <div>
            <label for="duration" class="form-label">Duração (semanas)</label>
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
            />
          </div>
        </div>
      </div>

      <div class="p-6 bg-white rounded-lg shadow-md border border-gray-200">
        <h2 class="text-xl font-semibold mb-4">Exercícios</h2>
        <div
          v-for="(exercise, index) in editablePlan.exercises"
          :key="index"
          class="space-y-4 border-b pb-4 mb-4"
        >
          <h3 class="font-semibold text-lg text-primary">
            {{ index + 1 }}. {{ exercise.name }}
          </h3>
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

      <div class="flex justify-end gap-4">
        <button type="button" class="btn-secondary" @click="router.back()">
          Cancelar
        </button>
        <button type="submit" class="btn-primary" :disabled="isSubmitting">
          {{ isSubmitting ? "Salvando..." : "Criar Plano (Versão 1)" }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import type { PlanDraftResponse } from "~/types/prescription";
import type { CreatePlanRequest } from "~/types/plan";

definePageMeta({
  middleware: ["auth-only"],
});

const route = useRoute();
const router = useRouter();
const isSubmitting = ref(false);

const prescriptionId = route.query.prescriptionId as string;
const userId = route.query.userId as string;


const editablePlan = ref<PlanDraftResponse | null>(null);

const {
  data: planDraft,
  pending,
  error,
} = await useApiFetch<PlanDraftResponse>(
  `/prescriptions/generated/${prescriptionId}/plan-draft`,
  {
    query: { userId },
    immediate: !!(prescriptionId && userId),
  }
);

watch(
  planDraft,
  (newDraft) => {
    if (newDraft) {
      editablePlan.value = JSON.parse(JSON.stringify(newDraft));
    }
  },
  { immediate: true }
);

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
    const planDataString = JSON.stringify(editablePlan.value);

    const payload: CreatePlanRequest = {
      userId: editablePlan.value.userId,
      prescriptionId: editablePlan.value.prescriptionId,
      planData: planDataString,
    };

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
