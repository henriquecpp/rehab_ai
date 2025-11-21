<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="max-w-6xl mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <div class="flex items-center gap-4 mb-4">
          <button
            @click="$router.back()"
            class="text-gray-600 hover:text-gray-900"
          >
            ← Voltar
          </button>
        </div>
        <h1 class="text-3xl font-bold text-gray-900 mb-2">
          Detalhes do Workflow AI
        </h1>
        <p class="text-gray-600">
          Acompanhe o processamento de IA da prescrição
        </p>
      </div>

      <div v-if="loading" class="text-center py-12">
        <div
          class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"
        ></div>
        <p class="mt-4 text-gray-600">Carregando workflow...</p>
      </div>

      <div v-else-if="workflow" class="space-y-6">
       <div class="bg-white rounded-lg shadow-sm p-6">
          <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
            <div>
              <p class="text-sm text-gray-500 mb-1">Status</p>
              <span
                class="px-3 py-1 inline-flex text-sm font-semibold rounded-full"
                :class="getStatusClass(workflow.status)"
              >
                {{ workflow.status }}
              </span>
            </div>
            <div>
              <p class="text-sm text-gray-500 mb-1">Estágio Atual</p>
              <p class="text-lg font-semibold text-gray-900">
                {{ workflow.currentStage }}
              </p>
            </div>
            <div>
              <p class="text-sm text-gray-500 mb-1">Iniciado em</p>
              <p class="text-lg font-semibold text-gray-900">
                {{ formatDate(workflow.startedAt) }}
              </p>
            </div>
            <div>
              <p class="text-sm text-gray-500 mb-1">Tempo Decorrido</p>
              <p class="text-lg font-semibold text-gray-900">
                {{ getElapsedTime(workflow.startedAt) }}
              </p>
            </div>
          </div>

          <div
            v-if="workflow.fileId"
            class="mt-6 pt-6 border-t border-gray-200"
          >
            <p class="text-sm text-gray-500 mb-2">Arquivo</p>
            <p class="text-gray-900">ID: {{ workflow.fileId }}</p>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow-sm p-6">
          <h2 class="text-xl font-bold text-gray-900 mb-6">
            Progresso dos Estágios
          </h2>

          <div class="space-y-4">
            <div
              v-for="(stage, index) in stages"
              :key="stage.name"
              class="flex items-center gap-4"
            >
              <div
                class="flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center text-2xl"
                :class="
                  getStageIconClass(
                    stage.name,
                    workflow.currentStage,
                    workflow.status
                  )
                "
              >
                {{
                  getStageIcon(
                    stage.name,
                    workflow.currentStage,
                    workflow.status
                  )
                }}
              </div>

              <div class="flex-1">
                <div class="flex items-center justify-between mb-1">
                  <h3 class="text-lg font-semibold text-gray-900">
                    {{ stage.label }}
                  </h3>
                  <span
                    v-if="
                      isStageCompleted(
                        stage.name,
                        workflow.currentStage,
                        workflow.status
                      )
                    "
                    class="text-sm text-green-600 font-medium"
                  >
                    ✓ Concluído
                  </span>
                  <span
                    v-else-if="
                      stage.name === workflow.currentStage &&
                      workflow.status === 'RUNNING'
                    "
                    class="text-sm text-blue-600 font-medium"
                  >
                    🔄 Em execução...
                  </span>
                  <span
                    v-else-if="
                      stage.name === workflow.currentStage &&
                      workflow.status === 'FAILED'
                    "
                    class="text-sm text-red-600 font-medium"
                  >
                    ✗ Falhou
                  </span>
                </div>
                <p class="text-sm text-gray-600">{{ stage.description }}</p>
              </div>

              <div
                v-if="index < stages.length - 1"
                class="absolute left-6 h-8 w-0.5 bg-gray-300 translate-y-16"
                :class="{
                  'bg-green-500': isStageCompleted(
                    stage.name,
                    workflow.currentStage,
                    workflow.status
                  ),
                }"
              ></div>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow-sm p-6">
          <h2 class="text-xl font-bold text-gray-900 mb-4">Ações</h2>

          <div class="flex flex-wrap gap-3">
            <button
              v-if="workflow.status === 'RUNNING'"
              @click="advanceWorkflow"
              :disabled="actionLoading"
              class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              {{
                actionLoading ? "⏳ Processando..." : "⏭️ Avançar Manualmente"
              }}
            </button>

            <button
              v-if="workflow.status === 'RUNNING'"
              @click="completeWorkflow"
              :disabled="actionLoading"
              class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              {{
                actionLoading ? "⏳ Processando..." : "✓ Marcar como Completo"
              }}
            </button>

            <button
              v-if="workflow.status === 'RUNNING'"
              @click="failWorkflow"
              :disabled="actionLoading"
              class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              {{ actionLoading ? "⏳ Processando..." : "✗ Marcar como Falho" }}
            </button>

            <button
              v-if="workflow.status === 'FAILED'"
              @click="retryWorkflow"
              :disabled="actionLoading"
              class="px-4 py-2 bg-orange-600 text-white rounded-lg hover:bg-orange-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              {{ actionLoading ? "⏳ Processando..." : "🔄 Tentar Novamente" }}
            </button>

            <button
              @click="viewTraces"
              class="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700"
            >
              📋 Ver Traces da IA
            </button>

            <button
              @click="refreshWorkflow"
              :disabled="actionLoading"
              class="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              🔄 Atualizar
            </button>
          </div>
        </div>

        <div
          v-if="workflow.status === 'FAILED' && workflow.error"
          class="bg-red-50 border border-red-200 rounded-lg p-6"
        >
          <h2 class="text-xl font-bold text-red-900 mb-2">⚠️ Erro</h2>
          <p class="text-red-800">{{ workflow.error }}</p>
        </div>

        <div
          v-if="showTracesModal"
          class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
          @click.self="showTracesModal = false"
        >
          <div
            class="bg-white rounded-lg shadow-xl max-w-4xl w-full mx-4 max-h-[80vh] overflow-y-auto"
          >
            <div class="p-6">
              <div class="flex justify-between items-center mb-4">
                <h2 class="text-2xl font-bold text-gray-900">
                  Traces da IA (AWS Bedrock)
                </h2>
                <button
                  @click="showTracesModal = false"
                  class="text-gray-400 hover:text-gray-600 text-2xl"
                >
                  ✕
                </button>
              </div>

              <div v-if="loadingTraces" class="text-center py-8">
                <div
                  class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"
                ></div>
              </div>

              <div
                v-else-if="traces.length === 0"
                class="text-center py-8 text-gray-500"
              >
                Nenhum trace encontrado
              </div>

              <div v-else class="space-y-4">
                <div
                  v-for="trace in traces"
                  :key="trace.id"
                  class="border border-gray-200 rounded-lg p-4"
                >
                  <div class="flex justify-between items-start mb-3">
                    <div>
                      <span class="font-semibold text-gray-900">Trace ID:</span>
                      <span class="text-sm text-gray-600 ml-2 font-mono">{{
                        trace.traceId
                      }}</span>
                    </div>
                    <span class="text-xs text-gray-500">{{
                      formatDate(trace.timestamp)
                    }}</span>
                  </div>

                  <div class="grid grid-cols-2 gap-4 mb-3">
                    <div>
                      <span class="text-sm font-medium text-gray-700"
                        >Latência:</span
                      >
                      <span class="text-sm text-gray-900 ml-2"
                        >{{ trace.latencyMs }}ms</span
                      >
                    </div>
                    <div>
                      <span class="text-sm font-medium text-gray-700"
                        >Tokens:</span
                      >
                      <span class="text-sm text-gray-900 ml-2"
                        >{{ trace.inputTokens }} in /
                        {{ trace.outputTokens }} out</span
                      >
                    </div>
                  </div>

                  <div v-if="trace.inputSummary" class="mb-2">
                    <span class="text-sm font-medium text-gray-700"
                      >Input Summary:</span
                    >
                    <p class="text-sm text-gray-600 mt-1">
                      {{ trace.inputSummary }}
                    </p>
                  </div>

                  <div v-if="trace.outputSummary" class="mb-2">
                    <span class="text-sm font-medium text-gray-700"
                      >Output Summary:</span
                    >
                    <p class="text-sm text-gray-600 mt-1">
                      {{ trace.outputSummary }}
                    </p>
                  </div>

                  <div
                    v-if="trace.guardrailBlocked"
                    class="mt-3 p-2 bg-red-50 border border-red-200 rounded"
                  >
                    <span class="text-sm font-medium text-red-700"
                      >⚠️ Bloqueado por Guardrail</span
                    >
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="text-center py-12">
        <p class="text-xl text-gray-600">Workflow não encontrado</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useApiFetch } from "~/composables/useApiFetch";

definePageMeta({
  middleware: ["auth-only"],
  layout: "default",
});

interface Workflow {
  id: string;
  fileId: string;
  userId: string;
  status: "RUNNING" | "COMPLETED" | "FAILED";
  currentStage: "EXTRACTION" | "NORMALIZATION" | "AI_GENERATION";
  startedAt: string;
  completedAt?: string;
  error?: string;
}

interface Trace {
  id: string;
  workflowId: string;
  traceId: string;
  timestamp: string;
  latencyMs: number;
  inputTokens: number;
  outputTokens: number;
  inputSummary?: string;
  outputSummary?: string;
  guardrailBlocked: boolean;
}

const route = useRoute();
const router = useRouter();
const workflow = ref<Workflow | null>(null);
const loading = ref(true);
const actionLoading = ref(false);
const showTracesModal = ref(false);
const loadingTraces = ref(false);
const traces = ref<Trace[]>([]);

const stages = [
  {
    name: "EXTRACTION",
    label: "Extração de Texto",
    description: "OCR e extração de texto da imagem da prescrição",
  },
  {
    name: "NORMALIZATION",
    label: "Normalização",
    description: "Limpeza e padronização dos dados extraídos",
  },
  {
    name: "AI_GENERATION",
    label: "Geração de Plano (IA)",
    description: "Criação do plano de tratamento usando AWS Bedrock",
  },
];

const fetchWorkflow = async () => {
  loading.value = true;
  try {
    const { data } = await useApiFetch<Workflow>(
      `/prescriptions/workflows/${route.params.id}`,
      {
        method: "GET",
      }
    );
    if (data.value) {
      workflow.value = data.value;
    }
  } catch (error) {
    console.error("Erro ao carregar workflow:", error);
    alert("Erro ao carregar workflow");
  } finally {
    loading.value = false;
  }
};

const advanceWorkflow = async () => {
  if (!confirm("Deseja avançar o workflow manualmente?")) return;

  actionLoading.value = true;
  try {
    await useApiFetch(`/prescriptions/workflows/${route.params.id}/advance`, {
      method: "POST",
    });
    alert("Workflow avançado com sucesso!");
    await fetchWorkflow();
  } catch (error) {
    console.error("Erro ao avançar workflow:", error);
    alert("Erro ao avançar workflow");
  } finally {
    actionLoading.value = false;
  }
};

const completeWorkflow = async () => {
  if (!confirm("Deseja marcar este workflow como completo?")) return;

  actionLoading.value = true;
  try {
    await useApiFetch(`/prescriptions/workflows/${route.params.id}/complete`, {
      method: "POST",
    });
    alert("Workflow marcado como completo!");
    await fetchWorkflow();
  } catch (error) {
    console.error("Erro ao completar workflow:", error);
    alert("Erro ao completar workflow");
  } finally {
    actionLoading.value = false;
  }
};

const failWorkflow = async () => {
  const reason = prompt("Digite o motivo da falha:");
  if (!reason) return;

  actionLoading.value = true;
  try {
    await useApiFetch(`/prescriptions/workflows/${route.params.id}/fail`, {
      method: "POST",
      body: { reason },
    });
    alert("Workflow marcado como falho!");
    await fetchWorkflow();
  } catch (error) {
    console.error("Erro ao falhar workflow:", error);
    alert("Erro ao falhar workflow");
  } finally {
    actionLoading.value = false;
  }
};

const retryWorkflow = async () => {
  if (!confirm("Deseja tentar executar este workflow novamente?")) return;

  actionLoading.value = true;
  try {
    await useApiFetch(`/prescriptions/workflows/${route.params.id}/retry`, {
      method: "POST",
    });
    alert("Workflow reiniciado com sucesso!");
    await fetchWorkflow();
  } catch (error) {
    console.error("Erro ao reiniciar workflow:", error);
    alert("Erro ao reiniciar workflow");
  } finally {
    actionLoading.value = false;
  }
};

const viewTraces = async () => {
  showTracesModal.value = true;
  loadingTraces.value = true;

  try {
    const { data } = await useApiFetch<Trace[]>("/prescriptions/traces", {
      method: "GET",
      params: { workflowId: route.params.id },
    });
    if (data.value) {
      traces.value = data.value;
    }
  } catch (error) {
    console.error("Erro ao carregar traces:", error);
    alert("Erro ao carregar traces");
  } finally {
    loadingTraces.value = false;
  }
};

const refreshWorkflow = async () => {
  await fetchWorkflow();
};

const getStatusClass = (status: string): string => {
  const classes = {
    RUNNING: "bg-blue-100 text-blue-800",
    COMPLETED: "bg-green-100 text-green-800",
    FAILED: "bg-red-100 text-red-800",
  };
  return classes[status as keyof typeof classes] || "bg-gray-100 text-gray-800";
};

const getStageIconClass = (
  stageName: string,
  currentStage: string,
  status: string
): string => {
  if (isStageCompleted(stageName, currentStage, status)) {
    return "bg-green-100 text-green-600";
  } else if (stageName === currentStage && status === "RUNNING") {
    return "bg-blue-100 text-blue-600 animate-pulse";
  } else if (stageName === currentStage && status === "FAILED") {
    return "bg-red-100 text-red-600";
  }
  return "bg-gray-100 text-gray-400";
};

const getStageIcon = (
  stageName: string,
  currentStage: string,
  status: string
): string => {
  if (isStageCompleted(stageName, currentStage, status)) {
    return "✓";
  } else if (stageName === currentStage && status === "RUNNING") {
    return "⏳";
  } else if (stageName === currentStage && status === "FAILED") {
    return "✗";
  }
  return "○";
};

const isStageCompleted = (
  stageName: string,
  currentStage: string,
  status: string
): boolean => {
  const stageOrder = ["EXTRACTION", "NORMALIZATION", "AI_GENERATION"];
  const currentIndex = stageOrder.indexOf(currentStage);
  const stageIndex = stageOrder.indexOf(stageName);

  if (status === "COMPLETED") {
    return true;
  }

  return stageIndex < currentIndex;
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

const getElapsedTime = (startedAt: string): string => {
  const start = new Date(startedAt).getTime();
  const now = new Date().getTime();
  const diff = now - start;

  const minutes = Math.floor(diff / 60000);
  const seconds = Math.floor((diff % 60000) / 1000);

  if (minutes > 0) {
    return `${minutes}m ${seconds}s`;
  }
  return `${seconds}s`;
};

onMounted(() => {
  fetchWorkflow();

  const interval = setInterval(() => {
    if (workflow.value?.status === "RUNNING") {
      fetchWorkflow();
    }
  }, 5000);

  return () => clearInterval(interval);
});
</script>
