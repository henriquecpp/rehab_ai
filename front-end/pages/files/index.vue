<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="max-w-7xl mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-2">
          Gerenciamento de Arquivos
        </h1>
        <p class="text-gray-600">Visualize e gerencie seus arquivos enviados</p>
      </div>

      <!-- Stats Cards -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <StatCard
          title="Total de Arquivos"
          :value="files.length.toString()"
          icon="📁"
          color="blue"
        />
        <StatCard
          title="Uploads Recentes"
          :value="recentFiles.toString()"
          icon="📤"
          color="green"
        />
        <StatCard
          title="Pseudonimizados"
          :value="pseudonymizedFiles.toString()"
          icon="🔒"
          color="purple"
        />
        <StatCard
          title="Armazenamento"
          :value="totalSize"
          icon="💾"
          color="orange"
        />
      </div>

      <!-- Filters -->
      <div class="bg-white rounded-lg shadow-sm p-4 mb-6">
        <div class="flex flex-col md:flex-row gap-4">
          <div class="flex-1">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Buscar arquivos..."
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <select
            v-model="statusFilter"
            class="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">Todos os Status</option>
            <option value="UPLOADED">Enviado</option>
            <option value="PROCESSING">Processando</option>
            <option value="PSEUDONYMIZED">Pseudonimizado</option>
            <option value="READY">Pronto</option>
            <option value="ERROR">Erro</option>
          </select>
          <select
            v-model="typeFilter"
            class="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">Todos os Tipos</option>
            <option value="PDF">PDF</option>
            <option value="IMAGE">Imagem</option>
            <option value="DOCUMENT">Documento</option>
          </select>
        </div>
      </div>

      <!-- Files Table -->
      <div class="bg-white rounded-lg shadow-sm overflow-hidden">
        <div v-if="loading" class="p-8 text-center">
          <div
            class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"
          ></div>
          <p class="mt-2 text-gray-600">Carregando arquivos...</p>
        </div>

        <div
          v-else-if="filteredFiles.length === 0"
          class="p-8 text-center text-gray-500"
        >
          <p class="text-xl mb-2">📂</p>
          <p>Nenhum arquivo encontrado</p>
        </div>

        <table v-else class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th
                class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                Arquivo
              </th>
              <th
                class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                Tipo
              </th>
              <th
                class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                Status
              </th>
              <th
                class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                Tamanho
              </th>
              <th
                class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                Data
              </th>
              <th
                class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                Ações
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr
              v-for="file in filteredFiles"
              :key="file.id"
              class="hover:bg-gray-50"
            >
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                  <span class="text-2xl mr-3">{{
                    getFileIcon(file.contentType)
                  }}</span>
                  <div>
                    <div class="text-sm font-medium text-gray-900">
                      {{ file.originalName }}
                    </div>
                    <div class="text-xs text-gray-500">{{ file.id }}</div>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="text-sm text-gray-900">{{
                  file.contentType
                }}</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span
                  class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full"
                  :class="getStatusClass(file.status)"
                >
                  {{ file.status }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                {{ formatSize(file.size) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ formatDate(file.uploadedAt) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <div class="flex items-center gap-2">
                  <button
                    @click="downloadFile(file.id, file.originalName)"
                    class="text-blue-600 hover:text-blue-900"
                    title="Download"
                  >
                    📥
                  </button>
                  <button
                    v-if="file.status !== 'PSEUDONYMIZED'"
                    @click="pseudonymizeFile(file.id)"
                    class="text-purple-600 hover:text-purple-900"
                    title="Pseudonimizar (LGPD)"
                  >
                    🔒
                  </button>
                  <button
                    v-if="file.status === 'PSEUDONYMIZED'"
                    @click="viewAnonymizationLogs(file.id)"
                    class="text-green-600 hover:text-green-900"
                    title="Ver Logs LGPD"
                  >
                    📋
                  </button>
                  <button
                    @click="deleteFile(file.id)"
                    class="text-red-600 hover:text-red-900"
                    title="Deletar"
                  >
                    🗑️
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Anonymization Logs Modal -->
      <div
        v-if="showLogsModal"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
        @click.self="showLogsModal = false"
      >
        <div
          class="bg-white rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[80vh] overflow-y-auto"
        >
          <div class="p-6">
            <div class="flex justify-between items-center mb-4">
              <h2 class="text-2xl font-bold text-gray-900">
                Logs de Pseudonimização LGPD
              </h2>
              <button
                @click="showLogsModal = false"
                class="text-gray-400 hover:text-gray-600"
              >
                ✕
              </button>
            </div>

            <div v-if="loadingLogs" class="text-center py-8">
              <div
                class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"
              ></div>
            </div>

            <div
              v-else-if="anonymizationLogs.length === 0"
              class="text-center py-8 text-gray-500"
            >
              Nenhum log encontrado
            </div>

            <div v-else class="space-y-4">
              <div
                v-for="log in anonymizationLogs"
                :key="log.id"
                class="border border-gray-200 rounded-lg p-4"
              >
                <div class="flex justify-between items-start mb-2">
                  <span class="font-semibold text-gray-900">{{
                    log.operation
                  }}</span>
                  <span class="text-xs text-gray-500">{{
                    formatDate(log.timestamp)
                  }}</span>
                </div>
                <div class="text-sm text-gray-600 mb-2">
                  <strong>Campo:</strong> {{ log.fieldName }}
                </div>
                <div class="text-sm text-gray-600">
                  <strong>Usuário:</strong> {{ log.userId }}
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
import { computed, onMounted, ref } from "vue";
import { useApiFetch } from "~/composables/useApiFetch";

definePageMeta({
  middleware: ["auth-only"],
  layout: "default",
});

interface File {
  id: string;
  originalName: string;
  storedName: string;
  contentType: string;
  size: number;
  status: "UPLOADED" | "PROCESSING" | "PSEUDONYMIZED" | "READY" | "ERROR";
  uploadedAt: string;
  userId: string;
}

interface AnonymizationLog {
  id: string;
  fileId: string;
  operation: string;
  fieldName: string;
  timestamp: string;
  userId: string;
}

const files = ref<File[]>([]);
const loading = ref(true);
const searchQuery = ref("");
const statusFilter = ref("");
const typeFilter = ref("");
const showLogsModal = ref(false);
const loadingLogs = ref(false);
const anonymizationLogs = ref<AnonymizationLog[]>([]);

const filteredFiles = computed(() => {
  return files.value.filter((file) => {
    const matchesSearch = file.originalName
      .toLowerCase()
      .includes(searchQuery.value.toLowerCase());
    const matchesStatus =
      !statusFilter.value || file.status === statusFilter.value;
    const matchesType =
      !typeFilter.value ||
      file.contentType.includes(typeFilter.value.toLowerCase());
    return matchesSearch && matchesStatus && matchesType;
  });
});

const recentFiles = computed(() => {
  const today = new Date();
  const weekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);
  return files.value.filter((f) => new Date(f.uploadedAt) > weekAgo).length;
});

const pseudonymizedFiles = computed(() => {
  return files.value.filter((f) => f.status === "PSEUDONYMIZED").length;
});

const totalSize = computed(() => {
  const bytes = files.value.reduce((sum, f) => sum + f.size, 0);
  return formatSize(bytes);
});

const fetchFiles = async () => {
  loading.value = true;
  try {
    const { data } = await useApiFetch<File[]>("/files", {
      method: "GET",
    });
    if (data.value) {
      files.value = data.value;
    }
  } catch (error) {
    console.error("Erro ao carregar arquivos:", error);
    alert("Erro ao carregar arquivos");
  } finally {
    loading.value = false;
  }
};

const downloadFile = async (fileId: string, fileName: string) => {
  try {
    const { data } = await useApiFetch<Blob>(`/files/${fileId}/download`, {
      method: "GET",
    });

    if (data.value) {
      const url = window.URL.createObjectURL(data.value);
      const a = document.createElement("a");
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    }
  } catch (error) {
    console.error("Erro ao baixar arquivo:", error);
    alert("Erro ao baixar arquivo");
  }
};

const pseudonymizeFile = async (fileId: string) => {
  if (
    !confirm(
      "Deseja pseudonimizar este arquivo? Esta ação remove dados sensíveis (LGPD)."
    )
  ) {
    return;
  }

  try {
    await useApiFetch(`/files/${fileId}/pseudonymize`, {
      method: "POST",
    });
    alert("Arquivo pseudonimizado com sucesso!");
    await fetchFiles();
  } catch (error) {
    console.error("Erro ao pseudonimizar arquivo:", error);
    alert("Erro ao pseudonimizar arquivo");
  }
};

const viewAnonymizationLogs = async (fileId: string) => {
  showLogsModal.value = true;
  loadingLogs.value = true;

  try {
    const { data } = await useApiFetch<AnonymizationLog[]>(
      `/files/${fileId}/anonymization-logs`,
      {
        method: "GET",
      }
    );
    if (data.value) {
      anonymizationLogs.value = data.value;
    }
  } catch (error) {
    console.error("Erro ao carregar logs:", error);
    alert("Erro ao carregar logs de anonimização");
  } finally {
    loadingLogs.value = false;
  }
};

const deleteFile = async (fileId: string) => {
  if (
    !confirm(
      "Tem certeza que deseja deletar este arquivo? Esta ação não pode ser desfeita."
    )
  ) {
    return;
  }

  try {
    await useApiFetch(`/files/${fileId}`, {
      method: "DELETE",
    });
    alert("Arquivo deletado com sucesso!");
    await fetchFiles();
  } catch (error) {
    console.error("Erro ao deletar arquivo:", error);
    alert("Erro ao deletar arquivo");
  }
};

const getFileIcon = (contentType: string): string => {
  if (contentType.includes("pdf")) return "📄";
  if (contentType.includes("image")) return "🖼️";
  if (contentType.includes("video")) return "🎥";
  if (contentType.includes("audio")) return "🎵";
  if (contentType.includes("word") || contentType.includes("document"))
    return "📝";
  if (contentType.includes("excel") || contentType.includes("spreadsheet"))
    return "📊";
  return "📁";
};

const getStatusClass = (status: string): string => {
  const classes = {
    UPLOADED: "bg-blue-100 text-blue-800",
    PROCESSING: "bg-yellow-100 text-yellow-800",
    PSEUDONYMIZED: "bg-purple-100 text-purple-800",
    READY: "bg-green-100 text-green-800",
    ERROR: "bg-red-100 text-red-800",
  };
  return classes[status as keyof typeof classes] || "bg-gray-100 text-gray-800";
};

const formatSize = (bytes: number): string => {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
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
  fetchFiles();
});
</script>
