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
              {{ currentPlan.title }}
            </h2>
            <p class="text-gray-600">Paciente: {{ currentPlan.patientName }}</p>
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
            <p class="text-sm text-gray-600 mb-1">Criado por</p>
            <p class="font-semibold text-gray-900">
              {{ currentPlan.createdBy }}
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
                    <span class="text-gray-600">Criado por:</span>
                    <span class="text-gray-900 font-medium ml-2">{{
                      version.createdBy
                    }}</span>
                  </div>
                  <div class="text-sm">
                    <span class="text-gray-600">Data:</span>
                    <span class="text-gray-900 font-medium ml-2">{{
                      formatDate(version.createdAt)
                    }}</span>
                  </div>
                </div>

                <p
                  v-if="version.description"
                  class="text-sm text-gray-600 mb-3"
                >
                  {{ version.description }}
                </p>

                <div
                  v-if="version.changes"
                  class="text-sm bg-gray-50 rounded p-3 mb-3"
                >
                  <p class="font-medium text-gray-700 mb-1">Mudanças:</p>
                  <p class="text-gray-600">{{ version.changes }}</p>
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
                  v-if="selectedVersion && selectedVersion.id !== version.id"
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
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
        @click.self="showVersionModal = false"
      >
        <div
          class="bg-white rounded-lg shadow-xl max-w-4xl w-full mx-4 max-h-[80vh] overflow-y-auto"
        >
          <div class="p-6">
            <div class="flex justify-between items-center mb-4">
              <h2 class="text-2xl font-bold text-gray-900">
                Versão {{ selectedVersion.version }} -
                {{ selectedVersion.title }}
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
                  {{ selectedVersion.description || "Sem descrição" }}
                </p>
              </div>

              <div class="p-4 bg-gray-50 rounded-lg">
                <p class="text-sm text-gray-600 mb-2">
                  <strong>Exercícios:</strong>
                </p>
                <ul class="list-disc list-inside space-y-1">
                  <li
                    v-for="exercise in selectedVersion.exercises"
                    :key="exercise"
                    class="text-gray-900"
                  >
                    {{ exercise }}
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
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
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
  title: string;
  description: string;
  patientName: string;
  createdBy: string;
  status: "DRAFT" | "APPROVED" | "ARCHIVED";
  version: number;
  createdAt: string;
  updatedAt: string;
  exercises: string[];
  changes?: string;
}

interface AuditLog {
  id: string;
  planId: string;
  action: string;
  userId: string;
  userName: string;
  timestamp: string;
  details?: string;
}

const route = useRoute();
const router = useRouter();

const currentPlan = ref<Plan | null>(null);
const versions = ref<Plan[]>([]);
const loading = ref(true);
const actionLoading = ref(false);
const showVersionModal = ref(false);
const showAuditLog = ref(false);
const loadingAudit = ref(false);
const selectedVersion = ref<Plan | null>(null);
const auditLogs = ref<AuditLog[]>([]);

const fetchVersions = async () => {
  loading.value = true;
  try {
    const [planData, versionsData] = await Promise.all([
      useApiFetch<Plan>(`/plans/${route.params.id}`, { method: "GET" }),
      useApiFetch<Plan[]>(`/plans/prescription/${route.params.id}/versions`, {
        method: "GET",
      }),
    ]);

    if (planData.data.value) {
      currentPlan.value = planData.data.value;
    }
    if (versionsData.data.value) {
      versions.value = versionsData.data.value;
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
    await useApiFetch(`/plans/${route.params.id}/approve`, {
      method: "POST",
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
    const { data } = await useApiFetch<Plan>(
      `/plans/${route.params.id}/new-version`,
      {
        method: "POST",
        body: { description },
      }
    );

    if (data.value) {
      alert("Nova versão criada com sucesso!");
      router.push(`/plans/${data.value.id}`);
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
    await useApiFetch(`/plans/${route.params.id}/archive`, {
      method: "POST",
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
  if (
    !confirm("Deseja restaurar esta versão? A versão atual será substituída.")
  ) {
    return;
  }

  actionLoading.value = true;
  try {
    await useApiFetch(`/plans/${route.params.id}/rollback`, {
      method: "POST",
      body: { versionId },
    });
    alert("Versão restaurada com sucesso!");
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

const compareVersions = (version: Plan) => {
  alert(
    `Comparando versão ${selectedVersion.value?.version} com versão ${version.version}.\n\nFuncionalidade de diff detalhado em desenvolvimento.`
  );
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

onMounted(() => {
  fetchVersions();
});
</script>
