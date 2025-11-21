<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="max-w-5xl mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <div class="flex items-center justify-between mb-4">
          <button
            @click="$router.push('/plans')"
            class="text-gray-600 hover:text-gray-900"
          >
            ← Voltar para Planos
          </button>
          <div v-if="canManagePlans" class="flex gap-3">
            <button
              @click="$router.push(`/plans/${route.params.id}/versions`)"
              class="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700"
            >
              📋 Ver Versões
            </button>
            <button
              v-if="plan?.status === 'DRAFT'"
              @click="editPlan"
              class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              ✏️ Editar
            </button>
            <button
              v-if="plan?.status === 'DRAFT'"
              @click="showApproveDialog = true"
              class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700"
            >
              ✓ Aprovar
            </button>
            <button
              v-if="plan?.status !== 'ARCHIVED'"
              @click="showArchiveDialog = true"
              class="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700"
            >
              📦 Arquivar
            </button>
          </div>
        </div>
        <h1 class="text-3xl font-bold text-gray-900 mb-2">Detalhes do Plano</h1>
        <p class="text-gray-600">
          Visualize informações completas do plano de tratamento
        </p>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="text-center py-12">
        <div
          class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"
        ></div>
        <p class="mt-4 text-gray-600">Carregando plano...</p>
      </div>

      <!-- Plan Content -->
      <div v-else-if="plan" class="space-y-6">
        <!-- Overview Card -->
        <div class="bg-white rounded-lg shadow-sm p-6">
          <div class="flex items-start justify-between mb-6">
            <div class="flex-1">
              <h2 class="text-2xl font-bold text-gray-900 mb-2">
                {{ plan.title }}
              </h2>
              <p class="text-gray-600">{{ plan.description }}</p>
            </div>
            <span
              class="px-4 py-2 inline-flex text-sm font-semibold rounded-full"
              :class="getStatusClass(plan.status)"
            >
              {{ getStatusLabel(plan.status) }}
            </span>
          </div>

          <div
            v-if="plan.diagnosis"
            class="mb-6 p-4 bg-blue-50 border-l-4 border-blue-500 rounded-lg"
          >
            <div class="flex items-start gap-3">
              <span class="text-2xl">🏥</span>
              <div>
                <p class="text-sm font-semibold text-blue-900 mb-1">
                  Diagnóstico
                </p>
                <p class="text-sm text-blue-800">{{ plan.diagnosis }}</p>
              </div>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div class="p-4 bg-gray-50 rounded-lg">
              <p class="text-sm text-gray-600 mb-1">Paciente</p>
              <p class="font-semibold text-gray-900">{{ plan.patientName }}</p>
            </div>
            <div class="p-4 bg-gray-50 rounded-lg">
              <p class="text-sm text-gray-600 mb-1">Criado por</p>
              <p class="font-semibold text-gray-900">
                {{ plan.clinicianName || "Sistema" }}
              </p>
            </div>
            <div class="p-4 bg-gray-50 rounded-lg">
              <p class="text-sm text-gray-600 mb-1">Versão</p>
              <p class="font-semibold text-gray-900">{{ plan.version }}</p>
            </div>
            <div class="p-4 bg-gray-50 rounded-lg">
              <p class="text-sm text-gray-600 mb-1">Prioridade</p>
              <p class="font-semibold text-gray-900 capitalize">
                {{ plan.priority.toLowerCase() }}
              </p>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
            <div class="p-4 bg-red-50 rounded-lg">
              <p class="text-sm text-red-600 mb-1">Nível de Dor Inicial</p>
              <p class="font-bold text-2xl text-red-700">
                {{ plan.painLevelStart }}/10
              </p>
            </div>
            <div class="p-4 bg-green-50 rounded-lg">
              <p class="text-sm text-green-600 mb-1">Nível de Dor Esperado</p>
              <p class="font-bold text-2xl text-green-700">
                {{ plan.painLevelExpectedEnd }}/10
              </p>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow-sm p-6">
          <h3 class="text-xl font-bold text-gray-900 mb-4">
            Exercícios do Plano
          </h3>

          <div
            v-if="plan.exercises && plan.exercises.length > 0"
            class="space-y-4"
          >
            <div
              v-for="(exercise, index) in plan.exercises"
              :key="index"
              class="border border-gray-200 rounded-lg p-4 hover:border-blue-300 transition-colors"
            >
              <div class="flex items-start gap-4">
                <div
                  class="flex-shrink-0 w-10 h-10 bg-blue-100 text-blue-700 rounded-full flex items-center justify-center font-bold"
                >
                  {{ index + 1 }}
                </div>
                <div class="flex-1">
                  <h4 class="font-semibold text-gray-900 mb-2">
                    {{ exercise.name }}
                  </h4>
                  <p class="text-sm text-gray-600 mb-3">
                    {{ exercise.description }}
                  </p>
                  <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                    <div>
                      <span class="text-gray-500">Séries:</span>
                      <span class="font-medium text-gray-900 ml-2">{{
                        exercise.sets
                      }}</span>
                    </div>
                    <div>
                      <span class="text-gray-500">Repetições:</span>
                      <span class="font-medium text-gray-900 ml-2">{{
                        exercise.reps
                      }}</span>
                    </div>
                    <div v-if="exercise.duration">
                      <span class="text-gray-500">Duração:</span>
                      <span class="font-medium text-gray-900 ml-2"
                        >{{ exercise.duration }}s</span
                      >
                    </div>
                    <div>
                      <span class="text-gray-500">Frequência:</span>
                      <span class="font-medium text-gray-900 ml-2">{{
                        exercise.frequency
                      }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="text-center py-8 text-gray-500">
            Nenhum exercício cadastrado neste plano
          </div>
        </div>

        <!-- Schedule -->
        <div v-if="plan.schedule" class="bg-white rounded-lg shadow-sm p-6">
          <h3 class="text-xl font-bold text-gray-900 mb-4">Cronograma</h3>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="p-4 bg-gray-50 rounded-lg">
              <p class="text-sm text-gray-600 mb-1">Data de Início</p>
              <p class="font-semibold text-gray-900">
                {{ formatDate(plan.schedule.startDate) }}
              </p>
            </div>
            <div class="p-4 bg-gray-50 rounded-lg">
              <p class="text-sm text-gray-600 mb-1">Data de Término</p>
              <p class="font-semibold text-gray-900">
                {{ formatDate(plan.schedule.endDate) }}
              </p>
            </div>
            <div class="p-4 bg-gray-50 rounded-lg">
              <p class="text-sm text-gray-600 mb-1">Duração</p>
              <p class="font-semibold text-gray-900">
                {{ plan.schedule.duration }}
              </p>
            </div>
            <div class="p-4 bg-gray-50 rounded-lg">
              <p class="text-sm text-gray-600 mb-1">Frequência Semanal</p>
              <p class="font-semibold text-gray-900">
                {{ plan.schedule.weeklyFrequency }}x por semana
              </p>
            </div>
          </div>
        </div>

        <!-- Goals -->
        <div
          v-if="plan.goals && plan.goals.length > 0"
          class="bg-white rounded-lg shadow-sm p-6"
        >
          <h3 class="text-xl font-bold text-gray-900 mb-4">
            Objetivos do Tratamento
          </h3>

          <ul class="space-y-2">
            <li
              v-for="(goal, index) in plan.goals"
              :key="index"
              class="flex items-start gap-3"
            >
              <span class="text-green-600 text-xl">🎯</span>
              <span class="text-gray-900">{{ goal }}</span>
            </li>
          </ul>
        </div>

        <div v-if="plan.notes" class="bg-white rounded-lg shadow-sm p-6">
          <h3 class="text-xl font-bold text-gray-900 mb-4">
            Observações Clínicas
          </h3>
          <p class="text-gray-700 whitespace-pre-line">{{ plan.notes }}</p>
        </div>

        <div class="bg-white rounded-lg shadow-sm p-6">
          <h3 class="text-xl font-bold text-gray-900 mb-4">
            Informações do Sistema
          </h3>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="p-3 bg-gray-50 rounded">
              <p class="text-sm text-gray-600 mb-1">ID do Plano</p>
              <p class="text-xs font-mono text-gray-900">{{ plan.id }}</p>
            </div>
            <div class="p-3 bg-gray-50 rounded">
              <p class="text-sm text-gray-600 mb-1">ID do Paciente</p>
              <p class="text-xs font-mono text-gray-900">
                {{ plan.patientId }}
              </p>
            </div>
            <div class="p-3 bg-gray-50 rounded">
              <p class="text-sm text-gray-600 mb-1">Criado em</p>
              <p class="text-sm text-gray-900">
                {{ formatDate(plan.createdAt) }}
              </p>
            </div>
            <div class="p-3 bg-gray-50 rounded">
              <p class="text-sm text-gray-600 mb-1">Última atualização</p>
              <p class="text-sm text-gray-900">
                {{ formatDate(plan.updatedAt) }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="text-center py-12">
        <p class="text-xl text-gray-600">Plano não encontrado</p>
      </div>
    </div>

    <div
      v-if="showApproveDialog"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click.self="showApproveDialog = false"
    >
      <div class="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4">
        <h3 class="text-xl font-bold text-gray-900 mb-4">Aprovar Plano</h3>
        <p class="text-gray-600 mb-6">
          Tem certeza que deseja aprovar este plano? Uma vez aprovado, o plano
          não poderá ser editado.
        </p>
        <div class="flex gap-3 justify-end">
          <button
            @click="showApproveDialog = false"
            class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
            :disabled="approving"
          >
            Cancelar
          </button>
          <button
            @click="approvePlan"
            class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700"
            :disabled="approving"
          >
            {{ approving ? "Aprovando..." : "Aprovar" }}
          </button>
        </div>
      </div>
    </div>

    <div
      v-if="showArchiveDialog"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click.self="showArchiveDialog = false"
    >
      <div class="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4">
        <h3 class="text-xl font-bold text-gray-900 mb-4">Arquivar Plano</h3>
        <p class="text-gray-600 mb-6">
          Tem certeza que deseja arquivar este plano? O plano arquivado não será
          mais exibido nas listagens ativas.
        </p>
        <div class="flex gap-3 justify-end">
          <button
            @click="showArchiveDialog = false"
            class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
            :disabled="archiving"
          >
            Cancelar
          </button>
          <button
            @click="archivePlan"
            class="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700"
            :disabled="archiving"
          >
            {{ archiving ? "Arquivando..." : "Arquivar" }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useApiFetch } from "~/composables/useApiFetch";
import { useAuthStore } from "~/store/auth";

definePageMeta({
  middleware: ["auth-only"],
  layout: "default",
});

const authStore = useAuthStore();
const canManagePlans = computed(
  () => authStore.isClinician || authStore.isAdmin
);

interface Exercise {
  name: string;
  description: string;
  sets: number;
  reps?: number;
  repetitions?: number;
  duration?: number;
  frequency: string;
}

interface PlanData {
  title: string;
  description: string;
  diagnosis?: string;
  exercises: Exercise[];
  goals?: string[];
  notes?: string;
}

interface BackendPlan {
  id: string;
  userId: string;
  prescriptionId: string;
  therapistId: string;
  origin: string;
  version: number;
  status: "DRAFT" | "APPROVED" | "ARCHIVED";
  planData: string; // JSON string
  priority: string;
  painLevelStart: number;
  painLevelExpectedEnd: number;
  startDate: number; // Unix timestamp
  endDate: number; // Unix timestamp
  tags: string[];
  active: boolean;
  createdAt: number; // Unix timestamp
  updatedAt: number; // Unix timestamp
  publishedAt?: number;
  reviewedAt?: number;
}

interface Plan {
  id: string;
  title: string;
  description: string;
  diagnosis?: string;
  patientId: string;
  patientName: string;
  clinicianName?: string;
  status: "DRAFT" | "APPROVED" | "ARCHIVED";
  version: number;
  exercises: Exercise[];
  schedule?: {
    startDate: string;
    endDate: string;
    duration: string;
    weeklyFrequency: number;
  };
  priority: string;
  painLevelStart: number;
  painLevelExpectedEnd: number;
  goals?: string[];
  notes?: string;
  createdAt: string;
  updatedAt: string;
  publishedAt?: string;
  reviewedAt?: string;
}

const route = useRoute();
const router = useRouter();
const plan = ref<Plan | null>(null);
const loading = ref(true);
const showApproveDialog = ref(false);
const showArchiveDialog = ref(false);
const approving = ref(false);
const archiving = ref(false);

const convertTimestampToDate = (timestamp: number): string => {
  return new Date(timestamp * 1000).toISOString();
};

const calculateDuration = (startDate: number, endDate: number): string => {
  const start = new Date(startDate * 1000);
  const end = new Date(endDate * 1000);
  const diffTime = Math.abs(end.getTime() - start.getTime());
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

  if (diffDays === 0) {
    return "A definir";
  } else if (diffDays < 7) {
    return `${diffDays} dia${diffDays > 1 ? "s" : ""}`;
  } else if (diffDays < 30) {
    const weeks = Math.floor(diffDays / 7);
    return `${weeks} semana${weeks > 1 ? "s" : ""}`;
  } else {
    const months = Math.floor(diffDays / 30);
    return `${months} ${months > 1 ? "meses" : "mês"}`;
  }
};

const getFrequencyPerWeek = (exercises: Exercise[]): number => {
  let totalPerWeek = 0;
  exercises.forEach((ex) => {
    const freq = ex.frequency?.toUpperCase() || "";
    if (freq.includes("DIARIO") || freq.includes("DAILY")) {
      totalPerWeek += 7;
    } else if (freq.includes("TRES") || freq.includes("THREE")) {
      totalPerWeek += 3;
    } else if (freq.includes("SEMANAL") || freq.includes("WEEKLY")) {
      totalPerWeek += 1;
    } else if (freq.includes("CINCO") || freq.includes("FIVE")) {
      totalPerWeek += 5;
    }
  });
  return totalPerWeek > 0 ? Math.ceil(totalPerWeek / exercises.length) : 3;
};

const translateFrequency = (frequency: string): string => {
  const freq = frequency?.toUpperCase() || "";
  const translations: Record<string, string> = {
    DIARIO: "Diário",
    DAILY: "Diário",
    TRES_VEZES_SEMANA: "3x por semana",
    THREE_TIMES_WEEK: "3x por semana",
    SEMANAL: "Semanal",
    WEEKLY: "Semanal",
    CINCO_VEZES_SEMANA: "5x por semana",
    FIVE_TIMES_WEEK: "5x por semana",
  };
  return translations[freq] || frequency || "A definir";
};

const fetchPlan = async () => {
  loading.value = true;
  try {
    const { data } = await useApiFetch<BackendPlan>(
      `/plans/${route.params.id}`,
      {
        method: "GET",
      }
    );

    if (data.value) {
      const backendPlan = data.value;

      let planData: PlanData;
      try {
        planData = JSON.parse(backendPlan.planData);
      } catch (e) {
        console.error("Error parsing planData:", e);
        planData = {
          title: "Plano sem título",
          description: "",
          exercises: [],
        };
      }

      let patientName = "Paciente";
      try {
        const { data: userData } = await useApiFetch<any>(
          `/users/${backendPlan.userId}`
        );
        if (userData.value) {
          patientName = userData.value.fullName || userData.value.email;
        }
      } catch (e) {
        console.warn("Could not fetch patient name:", e);
      }

      let clinicianName = "Sistema";
      if (backendPlan.therapistId) {
        try {
          const { data: therapistData } = await useApiFetch<any>(
            `/users/${backendPlan.therapistId}`
          );
          if (therapistData.value) {
            clinicianName =
              therapistData.value.fullName || therapistData.value.email;
          }
        } catch (e) {
          console.warn("Could not fetch therapist name:", e);
        }
      }

      const mappedExercises = planData.exercises.map((ex) => ({
        ...ex,
        reps: ex.repetitions || ex.reps || 10,
        frequency: translateFrequency(ex.frequency),
      }));

      plan.value = {
        id: backendPlan.id,
        title: planData.title || "Plano de Reabilitação",
        description: planData.description || "",
        diagnosis: planData.diagnosis,
        patientId: backendPlan.userId,
        patientName: patientName,
        clinicianName: clinicianName,
        status: backendPlan.status,
        version: backendPlan.version,
        exercises: mappedExercises,
        schedule: {
          startDate: convertTimestampToDate(backendPlan.startDate),
          endDate: convertTimestampToDate(backendPlan.endDate),
          duration: calculateDuration(
            backendPlan.startDate,
            backendPlan.endDate
          ),
          weeklyFrequency: getFrequencyPerWeek(mappedExercises),
        },
        priority: backendPlan.priority,
        painLevelStart: backendPlan.painLevelStart,
        painLevelExpectedEnd: backendPlan.painLevelExpectedEnd,
        goals: planData.goals || [],
        notes: planData.notes || "",
        createdAt: convertTimestampToDate(backendPlan.createdAt),
        updatedAt: convertTimestampToDate(backendPlan.updatedAt),
        publishedAt: backendPlan.publishedAt
          ? convertTimestampToDate(backendPlan.publishedAt)
          : undefined,
        reviewedAt: backendPlan.reviewedAt
          ? convertTimestampToDate(backendPlan.reviewedAt)
          : undefined,
      };

      if (authStore.isPatient && backendPlan.status === "DRAFT") {
        alert(
          "Este plano ainda está em rascunho e não pode ser visualizado. Aguarde a aprovação do seu terapeuta."
        );
        router.push("/plans");
        return;
      }
    }
  } catch (error) {
    console.error("Erro ao carregar plano:", error);
    alert("Erro ao carregar plano");
  } finally {
    loading.value = false;
  }
};

const editPlan = () => {
  router.push(`/plans/${route.params.id}/edit`);
};

const getStatusClass = (status: string): string => {
  const classes = {
    DRAFT: "bg-yellow-100 text-yellow-800",
    APPROVED: "bg-green-100 text-green-800",
    ARCHIVED: "bg-gray-100 text-gray-800",
  };
  return classes[status as keyof typeof classes] || "bg-gray-100 text-gray-800";
};

const getStatusLabel = (status: string): string => {
  const labels = {
    DRAFT: "Rascunho",
    APPROVED: "Aprovado",
    ARCHIVED: "Arquivado",
  };
  return labels[status as keyof typeof labels] || status;
};

const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  return date.toLocaleDateString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

const approvePlan = async () => {
  approving.value = true;
  try {
    await useApiFetch(`/plans/${route.params.id}/approve`, {
      method: "POST",
    });
    showApproveDialog.value = false;
    await fetchPlan();
    alert("Plano aprovado com sucesso!");
  } catch (error) {
    console.error("Erro ao aprovar plano:", error);
    alert("Erro ao aprovar plano. Tente novamente.");
  } finally {
    approving.value = false;
  }
};

const archivePlan = async () => {
  archiving.value = true;
  try {
    await useApiFetch(`/plans/${route.params.id}/archive`, {
      method: "POST",
    });
    showArchiveDialog.value = false;
    await fetchPlan();
    alert("Plano arquivado com sucesso!");
  } catch (error) {
    console.error("Erro ao arquivar plano:", error);
    alert("Erro ao arquivar plano. Tente novamente.");
  } finally {
    archiving.value = false;
  }
};

onMounted(() => {
  fetchPlan();
});
</script>
