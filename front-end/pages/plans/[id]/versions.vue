<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="max-w-7xl mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <div class="flex items-center gap-4 mb-4">
          <button
            @click="$router.push(`/plans/${route.params.id}`)"
            class="text-gray-600 hover:text-gray-900"
          >
            ← Voltar para Plano
          </button>
        </div>
        <h1 class="text-3xl font-bold text-gray-900 mb-2">
          Versionamento do Plano
        </h1>
        <p class="text-gray-600">
          Histórico de versões e controle de aprovação
        </p>
      </div>

      <div v-if="currentPlan" class="bg-white rounded-lg shadow-sm p-6 mb-6">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h2 class="text-xl font-bold text-gray-900">
              {{ getPlanTitle(currentPlan) }}
            </h2>
            <p class="text-gray-600">
              ID do Paciente: {{ currentPlan.userId }}
            </p>
          </div>
          <div class="flex items-center gap-4">
            <span
              class="px-4 py-2 inline-flex text-sm font-semibold rounded-full"
              :class="getStatusClass(currentPlan.status)"
            >
              {{ getStatusLabel(currentPlan.status) }}
            </span>
            <span class="text-sm text-gray-600"
              >Versão {{ currentPlan.version }}</span
            >
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mt-6">
          <div class="p-4 bg-gray-50 rounded-lg">
            <p class="text-sm text-gray-600 mb-1">Terapeuta</p>
            <p class="font-semibold text-gray-900">
              {{ currentPlan.therapistId || "N/A" }}
            </p>
          </div>
          <div class="p-4 bg-gray-50 rounded-lg">
            <p class="text-sm text-gray-600 mb-1">Criado em</p>
            <p class="font-semibold text-gray-900">
              {{ formatDate(currentPlan.createdAt) }}
            </p>
          </div>
          <div class="p-4 bg-gray-50 rounded-lg">
            <p class="text-sm text-gray-600 mb-1">Última atualização</p>
            <p class="font-semibold text-gray-900">
              {{ formatDate(currentPlan.updatedAt) }}
            </p>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow-sm p-6 mb-6">
        <h2 class="text-xl font-bold text-gray-900 mb-4">Ações</h2>

        <div class="flex flex-wrap gap-3">
          <button
            v-if="currentPlan?.status === 'DRAFT'"
            @click="approvePlan"
            :disabled="actionLoading"
            class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
          >
            {{ actionLoading ? "⏳ Processando..." : "✓ Aprovar Plano" }}
          </button>

          <button
            v-if="currentPlan?.status === 'APPROVED'"
            @click="createNewVersion"
            :disabled="actionLoading"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
          >
            {{ actionLoading ? "⏳ Processando..." : "📝 Nova Versão" }}
          </button>

          <button
            v-if="currentPlan?.status !== 'ARCHIVED'"
            @click="archivePlan"
            :disabled="actionLoading"
            class="px-4 py-2 bg-orange-600 text-white rounded-lg hover:bg-orange-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
          >
            {{ actionLoading ? "⏳ Processando..." : "📦 Arquivar" }}
          </button>

          <button
            @click="showAuditLog = true"
            class="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700"
          >
            📋 Logs de Auditoria
          </button>

          <button
            @click="fetchVersions"
            :disabled="loading"
            class="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
          >
            🔄 Atualizar
          </button>
        </div>
      </div>

      <div class="bg-white rounded-lg shadow-sm p-6">
        <h2 class="text-xl font-bold text-gray-900 mb-6">
          Histórico de Versões
        </h2>

        <div v-if="loading" class="text-center py-8">
          <div
            class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"
          ></div>
        </div>

        <div
          v-else-if="!currentPlan?.prescriptionId"
          class="text-center py-8 bg-yellow-50 rounded-lg border border-yellow-200"
        >
          <span class="text-4xl mb-3 block">📝</span>
          <p class="text-gray-700 font-medium mb-2">Plano Criado Manualmente</p>
          <p class="text-gray-600 text-sm">
            Este plano foi criado manualmente e não possui versionamento baseado
            em prescrição.
          </p>
        </div>

        <div
          v-else-if="versions.length === 0"
          class="text-center py-8 text-gray-500"
        >
          Nenhuma versão encontrada
        </div>

        <div v-else class="space-y-4">
          <div
            v-for="version in versions"
            :key="version.id"
            class="border rounded-lg p-4 hover:border-blue-300 transition-colors"
            :class="{
              'border-blue-500 bg-blue-50': version.id === currentPlan?.id,
              'border-gray-200': version.id !== currentPlan?.id,
            }"
          >
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <div class="flex items-center gap-3 mb-2">
                  <h3 class="text-lg font-semibold text-gray-900">
                    Versão {{ version.version }}
                    <span
                      v-if="version.id === currentPlan?.id"
                      class="text-sm text-blue-600 ml-2"
                    >
                      (Atual)
                    </span>
                  </h3>
                  <span
                    class="px-2 py-1 text-xs font-semibold rounded-full"
                    :class="getStatusClass(version.status)"
                  >
                    {{ getStatusLabel(version.status) }}
                  </span>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-3 mb-3">
                  <div class="text-sm">
                    <span class="text-gray-600">Terapeuta:</span>
                    <span class="text-gray-900 font-medium ml-2">{{
                      version.therapistId || "N/A"
                    }}</span>
                  </div>
                  <div class="text-sm">
                    <span class="text-gray-600">Data:</span>
                    <span class="text-gray-900 font-medium ml-2">{{
                      formatDate(version.createdAt)
                    }}</span>
                  </div>
                </div>

                <p class="text-sm text-gray-600 mb-3">
                  {{ getPlanDescription(version) || "Sem descrição" }}
                </p>

                <div
                  v-if="version.updateReason"
                  class="text-sm bg-gray-50 rounded p-3 mb-3"
                >
                  <p class="font-medium text-gray-700 mb-1">
                    Motivo da Atualização:
                  </p>
                  <p class="text-gray-600">{{ version.updateReason }}</p>
                </div>
              </div>

              <div class="flex flex-col gap-2 ml-4">
                <button
                  v-if="version.id !== currentPlan?.id"
                  @click="viewVersion(version)"
                  class="px-3 py-1 text-sm bg-blue-100 text-blue-700 rounded hover:bg-blue-200"
                >
                  👁️ Ver
                </button>
                <button
                  v-if="
                    version.id !== currentPlan?.id &&
                    currentPlan?.status !== 'ARCHIVED'
                  "
                  @click="rollbackToVersion(version.id)"
                  class="px-3 py-1 text-sm bg-orange-100 text-orange-700 rounded hover:bg-orange-200"
                >
                  ⏮️ Restaurar
                </button>
                <button
                  v-if="version.id !== currentPlan?.id"
                  @click="compareVersions(version)"
                  class="px-3 py-1 text-sm bg-purple-100 text-purple-700 rounded hover:bg-purple-200"
                >
                  🔀 Comparar
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div
        v-if="showVersionModal && selectedVersion"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[9999]"
        @click.self="showVersionModal = false"
      >
        <div
          class="bg-white rounded-lg shadow-xl max-w-4xl w-full mx-4 max-h-[80vh] overflow-y-auto"
        >
          <div class="p-6">
            <div class="flex justify-between items-center mb-4">
              <h2 class="text-2xl font-bold text-gray-900">
                Versão {{ selectedVersion.version }} -
                {{ getPlanTitle(selectedVersion) }}
              </h2>
              <button
                @click="showVersionModal = false"
                class="text-gray-400 hover:text-gray-600 text-2xl"
              >
                ✕
              </button>
            </div>

            <div class="space-y-4">
              <div class="p-4 bg-gray-50 rounded-lg">
                <p class="text-sm text-gray-600 mb-2">
                  <strong>Descrição:</strong>
                </p>
                <p class="text-gray-900">
                  {{ getPlanDescription(selectedVersion) || "Sem descrição" }}
                </p>
              </div>

              <div class="p-4 bg-gray-50 rounded-lg">
                <p class="text-sm text-gray-600 mb-2">
                  <strong>Exercícios:</strong>
                </p>
                <ul class="list-disc list-inside space-y-1">
                  <li
                    v-for="(exercise, idx) in parsePlanData(
                      selectedVersion.planData
                    ).exercises"
                    :key="idx"
                    class="text-gray-900"
                  >
                    {{ exercise.name }}
                    <span v-if="exercise.sets" class="text-sm text-gray-600">
                      - {{ exercise.sets }} séries
                    </span>
                    <span
                      v-if="exercise.repetitions"
                      class="text-sm text-gray-600"
                    >
                      x {{ exercise.repetitions }} repetições
                    </span>
                  </li>
                </ul>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div class="p-4 bg-gray-50 rounded-lg">
                  <p class="text-sm text-gray-600 mb-1">Status</p>
                  <span
                    class="px-3 py-1 inline-flex text-sm font-semibold rounded-full"
                    :class="getStatusClass(selectedVersion.status)"
                  >
                    {{ getStatusLabel(selectedVersion.status) }}
                  </span>
                </div>
                <div class="p-4 bg-gray-50 rounded-lg">
                  <p class="text-sm text-gray-600 mb-1">Criado em</p>
                  <p class="font-semibold text-gray-900">
                    {{ formatDate(selectedVersion.createdAt) }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div
        v-if="showAuditLog"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[9999]"
        @click.self="showAuditLog = false"
      >
        <div
          class="bg-white rounded-lg shadow-xl max-w-4xl w-full mx-4 max-h-[80vh] overflow-y-auto"
        >
          <div class="p-6">
            <div class="flex justify-between items-center mb-4">
              <h2 class="text-2xl font-bold text-gray-900">
                Logs de Auditoria
              </h2>
              <button
                @click="showAuditLog = false"
                class="text-gray-400 hover:text-gray-600 text-2xl"
              >
                ✕
              </button>
            </div>

            <div v-if="loadingAudit" class="text-center py-8">
              <div
                class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"
              ></div>
            </div>

            <div
              v-else-if="auditLogs.length === 0"
              class="text-center py-8 text-gray-500"
            >
              Nenhum log encontrado
            </div>

            <div v-else class="space-y-3">
              <div
                v-for="log in auditLogs"
                :key="log.id"
                class="border border-gray-200 rounded-lg p-4"
              >
                <div class="flex justify-between items-start mb-2">
                  <span class="font-semibold text-gray-900">{{
                    log.action
                  }}</span>
                  <span class="text-xs text-gray-500">{{
                    formatDate(log.timestamp)
                  }}</span>
                </div>
                <div class="text-sm text-gray-600">
                  <strong>Usuário:</strong> {{ log.userName }} ({{
                    log.userId
                  }})
                </div>
                <div v-if="log.details" class="text-sm text-gray-600 mt-2">
                  <strong>Detalhes:</strong> {{ log.details }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Compare Versions Modal -->
      <div
        v-if="showCompareModal"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[9999]"
        @click.self="showCompareModal = false"
      >
        <div
          class="bg-white rounded-lg shadow-xl max-w-6xl w-full mx-4 max-h-[85vh] overflow-y-auto"
        >
          <div class="p-6">
            <!-- Header -->
            <div class="flex justify-between items-center mb-6 pb-4 border-b">
              <div>
                <h2 class="text-2xl font-bold text-gray-900 mb-2">
                  Comparação de Versões
                </h2>
                <div class="flex items-center gap-4 text-sm text-gray-600">
                  <div class="flex items-center gap-2">
                    <span
                      class="px-2 py-1 bg-red-100 text-red-700 rounded font-mono"
                    >
                      Versão {{ diffData?.version1.number }}
                    </span>
                    <span>{{ diffData?.version1.date }}</span>
                  </div>
                  <span>→</span>
                  <div class="flex items-center gap-2">
                    <span
                      class="px-2 py-1 bg-green-100 text-green-700 rounded font-mono"
                    >
                      Versão {{ diffData?.version2.number }}
                    </span>
                    <span>{{ diffData?.version2.date }}</span>
                  </div>
                </div>
              </div>
              <button
                @click="showCompareModal = false"
                class="text-gray-400 hover:text-gray-600 text-2xl"
              >
                ✕
              </button>
            </div>

            <!-- Loading State -->
            <div v-if="loadingCompare" class="text-center py-12">
              <div
                class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mb-4"
              ></div>
              <p class="text-gray-600">Comparando versões...</p>
            </div>

            <!-- No Changes -->
            <div
              v-else-if="diffData && diffData.changes.length === 0"
              class="text-center py-12 bg-gray-50 rounded-lg"
            >
              <span class="text-6xl mb-4 block">✓</span>
              <h3 class="text-xl font-semibold text-gray-900 mb-2">
                Sem Diferenças
              </h3>
              <p class="text-gray-600">
                As versões selecionadas são idênticas.
              </p>
            </div>

            <!-- Diff Display -->
            <div v-else-if="diffData" class="space-y-4">
              <!-- Changes Summary -->
              <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <div class="flex items-center gap-2">
                  <span class="text-blue-600 font-semibold">📊 Resumo:</span>
                  <span class="text-blue-900">
                    {{ diffData.changes.length }} campo(s) modificado(s)
                  </span>
                </div>
              </div>

              <!-- Each Change -->
              <div
                v-for="(change, idx) in diffData.changes"
                :key="idx"
                class="border border-gray-200 rounded-lg overflow-hidden"
              >
                <!-- Change Header -->
                <div class="bg-gray-50 px-4 py-3 border-b border-gray-200">
                  <div class="flex items-center gap-2">
                    <span class="font-semibold text-gray-900">{{
                      change.field
                    }}</span>
                    <span
                      class="px-2 py-0.5 text-xs font-medium bg-yellow-100 text-yellow-800 rounded"
                    >
                      modificado
                    </span>
                  </div>
                </div>

                <!-- String/Text Diff -->
                <div v-if="!change.isArray && !change.isExercises" class="p-4">
                  <!-- Before (Red) -->
                  <div class="mb-2">
                    <div
                      class="flex items-start gap-2 p-3 bg-red-50 border-l-4 border-red-400 rounded"
                    >
                      <span class="text-red-700 font-mono text-sm">−</span>
                      <div class="flex-1">
                        <p class="text-sm text-red-900 font-medium mb-1">
                          Versão {{ diffData.version1.number }}
                        </p>
                        <p class="text-sm text-red-800 whitespace-pre-wrap">
                          {{ change.before || "(vazio)" }}
                        </p>
                      </div>
                    </div>
                  </div>

                  <!-- After (Green) -->
                  <div>
                    <div
                      class="flex items-start gap-2 p-3 bg-green-50 border-l-4 border-green-400 rounded"
                    >
                      <span class="text-green-700 font-mono text-sm">+</span>
                      <div class="flex-1">
                        <p class="text-sm text-green-900 font-medium mb-1">
                          Versão {{ diffData.version2.number }}
                        </p>
                        <p class="text-sm text-green-800 whitespace-pre-wrap">
                          {{ change.after || "(vazio)" }}
                        </p>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Array Diff (Goals) -->
                <div v-else-if="change.isArray" class="p-4">
                  <div class="grid grid-cols-2 gap-4">
                    <!-- Before -->
                    <div>
                      <p class="text-sm font-medium text-gray-700 mb-2">
                        Versão {{ diffData.version1.number }}
                      </p>
                      <ul class="space-y-1">
                        <li
                          v-for="(item, i) in change.before"
                          :key="i"
                          class="text-sm p-2 bg-red-50 text-red-800 rounded border-l-2 border-red-400"
                        >
                          − {{ item }}
                        </li>
                      </ul>
                      <p
                        v-if="change.before.length === 0"
                        class="text-sm text-gray-500 italic"
                      >
                        (nenhuma meta)
                      </p>
                    </div>

                    <!-- After -->
                    <div>
                      <p class="text-sm font-medium text-gray-700 mb-2">
                        Versão {{ diffData.version2.number }}
                      </p>
                      <ul class="space-y-1">
                        <li
                          v-for="(item, i) in change.after"
                          :key="i"
                          class="text-sm p-2 bg-green-50 text-green-800 rounded border-l-2 border-green-400"
                        >
                          + {{ item }}
                        </li>
                      </ul>
                      <p
                        v-if="change.after.length === 0"
                        class="text-sm text-gray-500 italic"
                      >
                        (nenhuma meta)
                      </p>
                    </div>
                  </div>
                </div>

                <!-- Exercises Diff -->
                <div v-else-if="change.isExercises" class="p-4">
                  <div class="grid grid-cols-2 gap-4">
                    <!-- Before -->
                    <div>
                      <p class="text-sm font-medium text-gray-700 mb-2">
                        Versão {{ diffData.version1.number }} ({{
                          change.before.length
                        }}
                        exercícios)
                      </p>
                      <div class="space-y-2">
                        <div
                          v-for="(ex, i) in change.before"
                          :key="i"
                          class="text-sm p-3 bg-red-50 rounded border-l-2 border-red-400"
                        >
                          <p class="font-medium text-red-900">
                            − {{ ex.name }}
                          </p>
                          <p class="text-xs text-red-700 mt-1">
                            <span v-if="ex.sets">{{ ex.sets }} séries</span>
                            <span v-if="ex.repetitions">
                              × {{ ex.repetitions }} reps</span
                            >
                            <span v-if="ex.duration">
                              • {{ ex.duration }} min</span
                            >
                          </p>
                        </div>
                      </div>
                      <p
                        v-if="change.before.length === 0"
                        class="text-sm text-gray-500 italic"
                      >
                        (nenhum exercício)
                      </p>
                    </div>

                    <!-- After -->
                    <div>
                      <p class="text-sm font-medium text-gray-700 mb-2">
                        Versão {{ diffData.version2.number }} ({{
                          change.after.length
                        }}
                        exercícios)
                      </p>
                      <div class="space-y-2">
                        <div
                          v-for="(ex, i) in change.after"
                          :key="i"
                          class="text-sm p-3 bg-green-50 rounded border-l-2 border-green-400"
                        >
                          <p class="font-medium text-green-900">
                            + {{ ex.name }}
                          </p>
                          <p class="text-xs text-green-700 mt-1">
                            <span v-if="ex.sets">{{ ex.sets }} séries</span>
                            <span v-if="ex.repetitions">
                              × {{ ex.repetitions }} reps</span
                            >
                            <span v-if="ex.duration">
                              • {{ ex.duration }} min</span
                            >
                          </p>
                        </div>
                      </div>
                      <p
                        v-if="change.after.length === 0"
                        class="text-sm text-gray-500 italic"
                      >
                        (nenhum exercício)
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Legend -->
              <div class="bg-gray-50 rounded-lg p-4 mt-6">
                <p class="text-sm font-medium text-gray-700 mb-2">Legenda:</p>
                <div class="flex flex-wrap gap-4 text-sm">
                  <div class="flex items-center gap-2">
                    <span class="text-red-600 font-mono">−</span>
                    <span class="text-gray-600">Removido/Anterior</span>
                  </div>
                  <div class="flex items-center gap-2">
                    <span class="text-green-600 font-mono">+</span>
                    <span class="text-gray-600">Adicionado/Novo</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
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

interface Plan {
  id: string;
  userId: string;
  prescriptionId?: string;
  therapistId?: string;
  origin: string;
  confidenceScore?: number;
  version: number;
  status: "DRAFT" | "APPROVED" | "ARCHIVED";
  planData: string; // JSON string containing title, description, diagnosis, exercises, etc.
  priority: string;
  painLevelStart: number;
  painLevelExpectedEnd: number;
  startDate?: number;
  endDate?: number;
  tags: string[];
  active: boolean;
  createdAt: number;
  updatedAt: number;
  publishedAt?: number;
  publishedBy?: string;
  reviewedAt?: number;
  reviewedBy?: string;
  updateReason?: string;
}

interface PlanDataStructure {
  title?: string;
  description?: string;
  diagnosis?: string;
  exercises?: Array<{
    name: string;
    sets?: number;
    repetitions?: number;
    duration?: number;
    frequency?: string;
    description?: string;
  }>;
  goals?: string[];
  notes?: string;
}

interface AuditLog {
  id: string;
  planId: string;
  action: string;
  userId: string;
  userName: string;
  timestamp: string;
  details?: string;
  changeDiff?: {
    before: any;
    after: any;
    changes: Array<{
      field: string;
      action: string;
    }>;
  };
}

const parsePlanData = (planDataString: string): PlanDataStructure => {
  try {
    return JSON.parse(planDataString);
  } catch (e) {
    console.error("Error parsing planData:", e);
    return {};
  }
};

const getPlanTitle = (plan: Plan): string => {
  const parsed = parsePlanData(plan.planData);
  return parsed.title || "Plano sem título";
};

const getPlanDescription = (plan: Plan): string => {
  const parsed = parsePlanData(plan.planData);
  return parsed.description || "";
};

const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();

const currentPlan = ref<Plan | null>(null);
const versions = ref<Plan[]>([]);
const loading = ref(true);
const actionLoading = ref(false);
const showVersionModal = ref(false);
const showAuditLog = ref(false);
const showCompareModal = ref(false);
const loadingAudit = ref(false);
const loadingCompare = ref(false);
const selectedVersion = ref<Plan | null>(null);
const compareVersion = ref<Plan | null>(null);
const auditLogs = ref<AuditLog[]>([]);
const diffData = ref<any>(null);

const fetchVersions = async () => {
  loading.value = true;
  try {
    const planData = await $api<Plan>(`/plans/${route.params.id}`, {
      method: "GET",
    });

    if (planData) {
      currentPlan.value = planData;

      if (planData.prescriptionId) {
        const versionsData = await $api<Plan[]>(
          `/plans/prescription/${planData.prescriptionId}/versions`,
          { method: "GET" }
        );

        if (versionsData) {
          versions.value = versionsData;
        }
      } else {
        versions.value = [planData];
        console.warn(
          "Este plano não possui prescriptionId, então não há versões associadas."
        );
      }
    }
  } catch (error) {
    console.error("Erro ao carregar versões:", error);
    alert("Erro ao carregar versões do plano");
  } finally {
    loading.value = false;
  }
};

const approvePlan = async () => {
  if (
    !confirm("Deseja aprovar este plano? Ele poderá ser usado pelo paciente.")
  ) {
    return;
  }

  actionLoading.value = true;
  try {
    await $api(`/plans/${route.params.id}/approve`, {
      method: "POST",
      query: {
        approvedBy: authStore.user?.id,
      },
    });
    alert("Plano aprovado com sucesso!");
    await fetchVersions();
  } catch (error) {
    console.error("Erro ao aprovar plano:", error);
    alert("Erro ao aprovar plano");
  } finally {
    actionLoading.value = false;
  }
};

const createNewVersion = async () => {
  const description = prompt("Descreva as mudanças desta nova versão:");
  if (!description) return;

  actionLoading.value = true;
  try {
    const response = await $api<Plan>(`/plans/${route.params.id}/new-version`, {
      method: "POST",
      query: {
        changedBy: authStore.user?.id,
        reason: description,
      },
    });

    if (response) {
      if (
        confirm(
          "Nova versão criada com sucesso! Deseja editar a nova versão agora?"
        )
      ) {
        await router.push(`/plans/${response.id}/edit`);
      } else {
        await fetchVersions();
      }
    }
  } catch (error) {
    console.error("Erro ao criar nova versão:", error);
    alert("Erro ao criar nova versão");
  } finally {
    actionLoading.value = false;
  }
};

const archivePlan = async () => {
  if (
    !confirm("Deseja arquivar este plano? Ele não poderá mais ser editado.")
  ) {
    return;
  }

  actionLoading.value = true;
  try {
    await $api(`/plans/${route.params.id}/archive`, {
      method: "POST",
      query: {
        archivedBy: authStore.user?.id,
        reason: "Arquivado via interface",
      },
    });
    alert("Plano arquivado com sucesso!");
    await fetchVersions();
  } catch (error) {
    console.error("Erro ao arquivar plano:", error);
    alert("Erro ao arquivar plano");
  } finally {
    actionLoading.value = false;
  }
};

const rollbackToVersion = async (versionId: string) => {
  const targetVersion = versions.value.find((v) => v.id === versionId);
  if (!targetVersion) {
    alert("Versão não encontrada");
    return;
  }

  if (
    !confirm(
      `Deseja restaurar para a versão ${targetVersion.version}? Isso criará uma nova versão com os dados antigos.`
    )
  ) {
    return;
  }

  actionLoading.value = true;
  try {
    await $api(`/plans/${route.params.id}/rollback`, {
      method: "POST",
      query: {
        toVersion: targetVersion.version,
        reason: `Rollback para versão ${targetVersion.version}`,
      },
    });
    alert("Versão restaurada com sucesso! Uma nova versão foi criada.");
    await fetchVersions();
  } catch (error) {
    console.error("Erro ao restaurar versão:", error);
    alert("Erro ao restaurar versão");
  } finally {
    actionLoading.value = false;
  }
};

const viewVersion = (version: Plan) => {
  selectedVersion.value = version;
  showVersionModal.value = true;
};

const compareVersions = async (version: Plan) => {
  if (!selectedVersion.value) {
    selectedVersion.value = currentPlan.value;
  }

  // Determine older and newer versions to ensure correct diff direction (Red -> Green)
  const v1 = selectedVersion.value!;
  const v2 = version;
  const olderVersion = v1.version < v2.version ? v1 : v2;
  const newerVersion = v1.version > v2.version ? v1 : v2;

  compareVersion.value = version;
  loadingCompare.value = true;
  showCompareModal.value = true;

  try {
    // Fetch audit logs to find the changeDiff between these versions
    const auditData = await $api<AuditLog[]>(
      `/plans/${route.params.id}/audit`,
      {
        method: "GET",
      }
    );

    if (auditData && auditData.length > 0) {
      // Find the audit log that corresponds to this version change
      const relevantLog = auditData.find(
        (log) =>
          log.changeDiff &&
          (log.action === "VERSION_CREATED" ||
            log.action === "PLAN_UPDATED" ||
            log.action === "ROLLBACK")
      );

      if (relevantLog && relevantLog.changeDiff) {
        // Use API's changeDiff data
        diffData.value = {
          version1: {
            number: olderVersion.version,
            date: formatDate(olderVersion.createdAt),
            data: relevantLog.changeDiff.before,
          },
          version2: {
            number: newerVersion.version,
            date: formatDate(newerVersion.createdAt),
            data: relevantLog.changeDiff.after,
          },
          changes: transformApiChanges(
            relevantLog.changeDiff.before,
            relevantLog.changeDiff.after,
            relevantLog.changeDiff.changes
          ),
        };
      } else {
        // Fallback to client-side diff if no changeDiff in audit logs
        const version1Data = parsePlanData(olderVersion.planData);
        const version2Data = parsePlanData(newerVersion.planData);

        diffData.value = {
          version1: {
            number: olderVersion.version,
            date: formatDate(olderVersion.createdAt),
            data: version1Data,
          },
          version2: {
            number: newerVersion.version,
            date: formatDate(newerVersion.createdAt),
            data: version2Data,
          },
          changes: generateDiff(version1Data, version2Data),
        };
      }
    } else {
      // No audit data, use client-side diff
      const version1Data = parsePlanData(olderVersion.planData);
      const version2Data = parsePlanData(newerVersion.planData);

      diffData.value = {
        version1: {
          number: olderVersion.version,
          date: formatDate(olderVersion.createdAt),
          data: version1Data,
        },
        version2: {
          number: newerVersion.version,
          date: formatDate(newerVersion.createdAt),
          data: version2Data,
        },
        changes: generateDiff(version1Data, version2Data),
      };
    }
  } catch (error) {
    console.error("Erro ao comparar versões:", error);
    alert("Erro ao comparar versões");
    showCompareModal.value = false;
  } finally {
    loadingCompare.value = false;
  }
};

const generateDiff = (before: any, after: any) => {
  const changes: any[] = [];

  // Compare title
  if (before.title !== after.title) {
    changes.push({
      field: "Título",
      type: "modified",
      before: before.title || "",
      after: after.title || "",
    });
  }

  // Compare diagnosis
  if (before.diagnosis !== after.diagnosis) {
    changes.push({
      field: "Diagnóstico",
      type: "modified",
      before: before.diagnosis || "",
      after: after.diagnosis || "",
    });
  }

  // Compare description
  if (before.description !== after.description) {
    changes.push({
      field: "Descrição",
      type: "modified",
      before: before.description || "",
      after: after.description || "",
    });
  }

  // Compare goals
  const beforeGoals = JSON.stringify(before.goals || []);
  const afterGoals = JSON.stringify(after.goals || []);
  if (beforeGoals !== afterGoals) {
    changes.push({
      field: "Metas",
      type: "modified",
      before: before.goals || [],
      after: after.goals || [],
      isArray: true,
    });
  }

  // Compare exercises
  const beforeExercises = JSON.stringify(before.exercises || []);
  const afterExercises = JSON.stringify(after.exercises || []);
  if (beforeExercises !== afterExercises) {
    changes.push({
      field: "Exercícios",
      type: "modified",
      before: before.exercises || [],
      after: after.exercises || [],
      isExercises: true,
    });
  }

  // Compare notes
  if (before.notes !== after.notes) {
    changes.push({
      field: "Notas",
      type: "modified",
      before: before.notes || "",
      after: after.notes || "",
    });
  }

  return changes;
};

const transformApiChanges = (
  beforeData: any,
  afterData: any,
  apiChanges: Array<{ field: string; action: string }>
) => {
  const changes: any[] = [];

  // Map of API field names to display names
  const fieldNameMap: { [key: string]: string } = {
    title: "Título",
    diagnosis: "Diagnóstico",
    description: "Descrição",
    goals: "Metas",
    exercises: "Exercícios",
    notes: "Notas",
  };

  apiChanges.forEach((change) => {
    const fieldName = fieldNameMap[change.field] || change.field;
    const beforeValue = beforeData[change.field];
    const afterValue = afterData[change.field];

    // Check if field is an array
    const isArray =
      Array.isArray(beforeValue) ||
      Array.isArray(afterValue) ||
      change.field === "goals";
    const isExercises = change.field === "exercises";

    changes.push({
      field: fieldName,
      type: change.action,
      before: beforeValue || (isArray ? [] : ""),
      after: afterValue || (isArray ? [] : ""),
      isArray: isArray && !isExercises,
      isExercises: isExercises,
    });
  });

  return changes;
};

const fetchAuditLogs = async () => {
  loadingAudit.value = true;
  try {
    const { data } = await useApiFetch<AuditLog[]>(
      `/plans/${route.params.id}/audit`,
      {
        method: "GET",
      }
    );
    if (data.value) {
      auditLogs.value = data.value;
    }
  } catch (error) {
    console.error("Erro ao carregar logs:", error);
    alert("Erro ao carregar logs de auditoria");
  } finally {
    loadingAudit.value = false;
  }
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

const formatDate = (timestamp: string | number): string => {
  const date =
    typeof timestamp === "number"
      ? new Date(timestamp > 10000000000 ? timestamp : timestamp * 1000)
      : new Date(timestamp);

  return date.toLocaleDateString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

onMounted(() => {
  fetchVersions();
});
</script>
