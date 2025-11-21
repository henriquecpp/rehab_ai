<template>
  <UICard>
    <template #title>
      <span>Arquivos do Paciente</span>
    </template>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
      <div class="stat-card">
        <div class="stat-icon bg-blue-100 text-blue-600">📁</div>
        <div>
          <div class="stat-label">Total de Arquivos</div>
          <div class="stat-value">{{ files.length }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon bg-purple-100 text-purple-600">🔒</div>
        <div>
          <div class="stat-label">Pseudonimizados</div>
          <div class="stat-value">{{ pseudonymizedFiles }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon bg-orange-100 text-orange-600">💾</div>
        <div>
          <div class="stat-label">Armazenamento</div>
          <div class="stat-value">{{ totalSize }}</div>
        </div>
      </div>
    </div>

    <!-- Upload Section -->
    <div class="upload-section mb-6">
      <h3 class="text-lg font-semibold text-gray-800 mb-3">
        Enviar Novo Arquivo
      </h3>
      <div class="flex items-center gap-3">
        <input
          ref="fileInput"
          type="file"
          @change="handleFileSelect"
          class="hidden"
        />
        <button
          @click="triggerFileInput"
          class="btn-primary"
          :disabled="uploading"
        >
          <span v-if="uploading">📤 Enviando...</span>
          <span v-else>📤 Selecionar Arquivo</span>
        </button>
        <span v-if="selectedFileName" class="text-sm text-gray-600">
          {{ selectedFileName }}
        </span>
      </div>
    </div>

    <!-- Filters -->
    <div class="filters-section mb-6">
      <div class="flex flex-col sm:flex-row gap-3">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Buscar arquivos..."
          class="input-field flex-1"
        />
        <select v-model="statusFilter" class="input-field">
          <option value="">Todos os Status</option>
          <option value="UPLOADED">Enviado</option>
          <option value="PROCESSING">Processando</option>
          <option value="PSEUDONYMIZED">Pseudonimizado</option>
          <option value="READY">Pronto</option>
          <option value="ERROR">Erro</option>
        </select>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p class="mt-2 text-gray-600">Carregando arquivos...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="filteredFiles.length === 0" class="empty-state">
      <p class="text-xl mb-2">📂</p>
      <p>Nenhum arquivo encontrado</p>
    </div>

    <!-- Files Table -->
    <div v-else class="overflow-x-auto">
      <table class="files-table">
        <thead>
          <tr>
            <th>Arquivo</th>
            <th>Tipo</th>
            <th>Status</th>
            <th>Tamanho</th>
            <th>Data</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="file in filteredFiles" :key="file.id">
            <td>
              <div class="flex items-center">
                <span class="text-2xl mr-3">{{
                  getFileIcon(file.fileType)
                }}</span>
                <div>
                  <div class="text-sm font-medium text-gray-900">
                    {{ file.originalName }}
                  </div>
                  <div class="text-xs text-gray-500">{{ file.id }}</div>
                </div>
              </div>
            </td>
            <td>
              <span class="text-sm text-gray-900">{{ file.fileType }}</span>
            </td>
            <td>
              <span class="badge" :class="getStatusClass(file.status)">
                {{ file.status }}
              </span>
            </td>
            <td>
              <span class="text-sm text-gray-900">{{
                formatSize(file.sizeBytes)
              }}</span>
            </td>
            <td>
              <span class="text-sm text-gray-500">{{
                formatDate(file.createdAt)
              }}</span>
            </td>
            <td>
              <div class="flex items-center gap-2">
                <button
                  @click="downloadFile(file.id, file.originalName)"
                  class="action-btn text-blue-600 hover:text-blue-900"
                  title="Download"
                >
                  📥
                </button>
                <button
                  v-if="file.status !== 'PSEUDONYMIZED'"
                  @click="pseudonymizeFile(file.id)"
                  class="action-btn text-purple-600 hover:text-purple-900"
                  title="Pseudonimizar (LGPD)"
                >
                  🔒
                </button>
                <button
                  v-if="file.status === 'PSEUDONYMIZED'"
                  @click="viewAnonymizationLogs(file.id)"
                  class="action-btn text-green-600 hover:text-green-900"
                  title="Ver Logs LGPD"
                >
                  📋
                </button>
                <button
                  @click="deleteFile(file.id)"
                  class="action-btn text-red-600 hover:text-red-900"
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
      class="modal-overlay"
      @click.self="showLogsModal = false"
    >
      <div class="modal-content">
        <div class="modal-header">
          <h2 class="modal-title">Logs de Pseudonimização LGPD</h2>
          <button @click="showLogsModal = false" class="modal-close">✕</button>
        </div>

        <div v-if="loadingLogs" class="modal-loading">
          <div class="spinner"></div>
        </div>

        <div v-else-if="anonymizationLogs.length === 0" class="modal-empty">
          Nenhum log encontrado
        </div>

        <div v-else class="modal-body">
          <div v-for="log in anonymizationLogs" :key="log.id" class="log-card">
            <div class="log-header">
              <span class="log-operation">{{ log.ruleApplied }}</span>
              <span class="log-timestamp">{{ formatDate(log.timestamp) }}</span>
            </div>
            <div class="log-field">
              <strong>Campo:</strong> {{ log.fieldChanged }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </UICard>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";

definePageMeta({ middleware: "auth-only" });

const route = useRoute();
const patientId = computed(() => route.params.id as string);

interface File {
  id: string;
  userId: string;
  originalName: string;
  s3Path: string;
  status: "UPLOADED" | "PSEUDONYMIZED" | "PROCESSING" | "READY" | "ERROR";
  fileType: "MEDICAL_REPORT" | "PRESCRIPTION" | "IMAGE" | "OTHER";
  sizeBytes: number;
  hashSha256: string;
  createdAt: string;
  updatedAt: string;
}

interface AnonymizationLog {
  id: string;
  fileId: string;
  ruleApplied: string;
  fieldChanged: string;
  timestamp: string;
}

const files = ref<File[]>([]);
const loading = ref(true);
const searchQuery = ref("");
const statusFilter = ref("");
const showLogsModal = ref(false);
const loadingLogs = ref(false);
const anonymizationLogs = ref<AnonymizationLog[]>([]);
const uploading = ref(false);
const selectedFileName = ref("");
const fileInput = ref<HTMLInputElement | null>(null);

const filteredFiles = computed(() => {
  return files.value.filter((file) => {
    const matchesSearch = file.originalName
      .toLowerCase()
      .includes(searchQuery.value.toLowerCase());
    const matchesStatus =
      !statusFilter.value || file.status === statusFilter.value;
    return matchesSearch && matchesStatus;
  });
});

const pseudonymizedFiles = computed(() => {
  return files.value.filter((f) => f.status === "PSEUDONYMIZED").length;
});

const totalSize = computed(() => {
  const bytes = files.value.reduce((sum, f) => sum + f.sizeBytes, 0);
  return formatSize(bytes);
});

const triggerFileInput = () => {
  fileInput.value?.click();
};

const fetchFiles = async () => {
  loading.value = true;
  try {
    const { data, error } = await useApiFetch<File[]>(
      `/files?userId=${patientId.value}`,
      {
        method: "GET",
        key: `files-list-${patientId.value}`,
      }
    );

    if (error.value) {
      throw new Error(error.value.message || "Erro ao carregar arquivos");
    }

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

const handleFileSelect = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];

  if (!file) return;

  selectedFileName.value = file.name;
  uploading.value = true;

  try {
    const formData = new FormData();
    formData.append("file", file);

    // API expects userId and fileType as query parameters
    const { data, error } = await useApiFetch<File>(
      `/files/upload?userId=${patientId.value}&fileType=OTHER`,
      {
        method: "POST",
        body: formData,
        key: `file-upload-${Date.now()}`,
      }
    );

    if (error.value) {
      throw new Error(error.value.message || "Erro ao enviar arquivo");
    }

    if (data.value) {
      alert("Arquivo enviado com sucesso!");
      selectedFileName.value = "";
      if (fileInput.value) {
        fileInput.value.value = "";
      }
      await fetchFiles();
    }
  } catch (error) {
    console.error("Erro ao enviar arquivo:", error);
    alert("Erro ao enviar arquivo");
  } finally {
    uploading.value = false;
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
    const { error } = await useApiFetch(`/files/${fileId}/pseudonymize`, {
      method: "POST",
      key: `pseudonymize-file-${fileId}-${Date.now()}`,
    });

    if (error.value) {
      throw new Error(error.value.message || "Erro ao pseudonimizar arquivo");
    }

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
    const { error } = await useApiFetch(`/files/${fileId}`, {
      method: "DELETE",
      key: `delete-file-${fileId}-${Date.now()}`,
    });

    if (error.value) {
      throw new Error(error.value.message || "Erro ao deletar arquivo");
    }

    alert("Arquivo deletado com sucesso!");
    await fetchFiles();
  } catch (error) {
    console.error("Erro ao deletar arquivo:", error);
    alert("Erro ao deletar arquivo");
  }
};

const getFileIcon = (fileType: string): string => {
  const icons = {
    MEDICAL_REPORT: "�",
    PRESCRIPTION: "💊",
    IMAGE: "🖼️",
    OTHER: "📁",
  };
  return icons[fileType as keyof typeof icons] || "📁";
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

<style scoped>
.stat-card {
  @apply flex items-center gap-4 bg-white border border-gray-200 rounded-lg p-4;
}
.stat-icon {
  @apply w-12 h-12 flex items-center justify-center rounded-lg text-2xl;
}
.stat-label {
  @apply text-xs text-gray-600 uppercase tracking-wide;
}
.stat-value {
  @apply text-2xl font-bold text-gray-900;
}

.upload-section {
  @apply bg-gray-50 border border-gray-200 rounded-lg p-4;
}

.filters-section {
  @apply bg-white border border-gray-200 rounded-lg p-4;
}

.input-field {
  @apply px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent;
}

.btn-primary {
  @apply px-4 py-2 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition;
}

.loading-state {
  @apply py-10 text-center;
}

.spinner {
  @apply inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600;
}

.empty-state {
  @apply py-10 text-center text-gray-500;
}

.files-table {
  @apply min-w-full divide-y divide-gray-200;
}
.files-table thead {
  @apply bg-gray-50;
}
.files-table th {
  @apply px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider;
}
.files-table tbody {
  @apply bg-white divide-y divide-gray-200;
}
.files-table tr:hover {
  @apply bg-gray-50;
}
.files-table td {
  @apply px-6 py-4 whitespace-nowrap;
}

.badge {
  @apply px-2 inline-flex text-xs leading-5 font-semibold rounded-full;
}

.action-btn {
  @apply text-lg transition;
}

.modal-overlay {
  @apply fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50;
}
.modal-content {
  @apply bg-white rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[80vh] overflow-y-auto;
}
.modal-header {
  @apply flex justify-between items-center p-6 border-b border-gray-200;
}
.modal-title {
  @apply text-2xl font-bold text-gray-900;
}
.modal-close {
  @apply text-gray-400 hover:text-gray-600 text-2xl;
}
.modal-loading {
  @apply text-center py-8;
}
.modal-empty {
  @apply text-center py-8 text-gray-500;
}
.modal-body {
  @apply p-6 space-y-4;
}
.log-card {
  @apply border border-gray-200 rounded-lg p-4;
}
.log-header {
  @apply flex justify-between items-start mb-2;
}
.log-operation {
  @apply font-semibold text-gray-900;
}
.log-timestamp {
  @apply text-xs text-gray-500;
}
.log-field {
  @apply text-sm text-gray-600 mb-2;
}
.log-user {
  @apply text-sm text-gray-600;
}
</style>
