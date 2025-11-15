<template>
  <UICard title="Nova Prescrição com IA">
    
    <div class="alert alert-info">
      <span>💡</span>
      <div>
        <strong>Fluxo Integrado:</strong> Upload do laudo → Extração OCR → Normalização → Geração IA → Plano estruturado
      </div>
    </div>

    <div v-if="errorMsg" class="alert alert-danger">
      {{ errorMsg }}
    </div>
    <div v-if="successMsg" class="alert alert-success">
      {{ successMsg }}
    </div>

    <div class="form-group">
      <label class="form-label">Selecionar Paciente</label>
      <select v-model="selectedPatientId" class="form-select" :disabled="isLoading">
        <option value="" disabled>
          {{ patientsLoading ? 'Carregando pacientes...' : 'Selecione um paciente' }}
        </option>
        <option v-for="p in patients" :key="p.id" :value="p.id">
          {{ p.fullName }} ({{ p.email }})
        </option>
      </select>
    </div>

    <div class="form-group">
      <label class="form-label">Upload de Laudo Médico</label>
      <div class="upload-area" @click="openFileInput" @dragover.prevent="isDragging = true" @dragleave.prevent="isDragging = false" @drop.prevent="handleFileDrop" :class="{ 'dragover': isDragging }">
        <input type="file" ref="fileInput" @change="handleFileSelect" style="display: none;" accept=".pdf,.jpg,.jpeg,.png">
        
        <div v-if="!selectedFile">
          <div style="font-size: 48px; margin-bottom: 12px;">📄</div>
          <div style="font-weight: 600; margin-bottom: 8px;">Clique ou arraste o laudo para cá</div>
          <div style="font-size: 14px; color: var(--gray-600);">PDF, JPG, PNG - Máx. 10MB</div>
        </div>
        <div v-else class="text-center">
          <div style="font-size: 48px; margin-bottom: 12px;">✅</div>
          <div style="font-weight: 600; margin-bottom: 8px;">Arquivo selecionado:</div>
          <div style="font-size: 14px; color: var(--gray-700);">{{ selectedFile.name }}</div>
        </div>
      </div>
    </div>

    <div id="workflowControls">
      <button 
        class="btn-primary w-full justify-center py-3" 
        :disabled="!isValid"
        @click="startPrescriptionWorkflow"
      >
        <span v-if="!isLoading">🚀 Iniciar Processamento com IA</span>
        <span v-else>Processando...</span>
      </button>
    </div>

  </UICard>
</template>

<script setup lang="ts">
definePageMeta({ 
  middleware: ['auth-only'],
  layout: 'default'
})

import { useAuthStore } from '~/store/auth';
const authStore = useAuthStore();

const isLoading = ref(false);
const isDragging = ref(false);
const errorMsg = ref<string | null>(null);
const successMsg = ref<string | null>(null);

const selectedPatientId = ref("");
const selectedFile = ref<File | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);

const isValid = computed(() => selectedPatientId.value && selectedFile.value && !isLoading.value);

interface PatientUser {
  id: string;
  email: string;
  fullName: string;
}
const { data: patients, pending: patientsLoading } = await useApiFetch<PatientUser[]>(
  '/users', 
  { query: { role: 'PATIENT' } }
);

function openFileInput() {
  fileInput.value?.click();
}
function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    selectedFile.value = target.files[0];
    isDragging.value = false;
  }
}
function handleFileDrop(event: DragEvent) {
  isDragging.value = false;
  if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
    selectedFile.value = event.dataTransfer.files[0];
  }
}

async function startPrescriptionWorkflow() {
  if (!isValid.value) return;

  isLoading.value = true;
  errorMsg.value = null;
  successMsg.value = null;

  try {
    const formData = new FormData();
    formData.append('file', selectedFile.value!);
    formData.append('userId', selectedPatientId.value);
    formData.append('fileType', 'PRESCRIPTION');

    const config = useRuntimeConfig();
    const fileResponse = await $fetch<{ id: string }>(`${config.public.apiBaseUrl}/files/upload`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      },
      body: formData
    });

    const fileId = fileResponse.id;
    if (!fileId) throw new Error('Falha ao obter ID do arquivo após upload.');

    await useApiFetch('/prescriptions/workflows', {
      method: 'POST',
      body: {
        userId: selectedPatientId.value,
        fileId: fileId,
      }
    });

    successMsg.value = `Workflow iniciado com sucesso para ${selectedFile.value?.name}!`;
    selectedFile.value = null;
    selectedPatientId.value = "";

  } catch (error: any) {
    console.error('Falha ao iniciar workflow:', error);
    errorMsg.value = `Erro: ${error.data?.message || error.message || 'Não foi possível iniciar o processamento.'}`;
  } finally {
    isLoading.value = false;
  }
}

</script>

<style scoped>
.form-label { @apply mb-2 block text-sm font-semibold text-gray-700; }
.form-select, .form-input { @apply w-full rounded-lg border border-gray-300 px-3 py-2.5 text-sm shadow-sm transition focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary; }
.btn-primary { @apply flex items-center gap-2 rounded-lg bg-primary px-5 py-2 text-sm font-semibold text-white transition hover:bg-primary-dark disabled:opacity-50 disabled:cursor-not-allowed; }
.alert { @apply mb-4 flex gap-3 rounded-lg border-l-4 p-4; }
.alert-info { @apply border-primary bg-blue-50 text-blue-800; }
.alert-danger { @apply border-danger bg-red-50 text-red-800; }
.alert-success { @apply border-success bg-green-50 text-green-800; }
.upload-area { @apply cursor-pointer rounded-lg border-2 border-dashed border-gray-300 p-10 text-center transition; }
.upload-area:hover { @apply border-primary bg-gray-50; }
.upload-area.dragover { @apply border-primary bg-blue-50 ring-2 ring-primary; }
</style>