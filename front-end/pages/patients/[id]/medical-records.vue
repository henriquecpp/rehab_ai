<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h1 class="text-3xl font-bold">Registros Médicos</h1>
      <button
        v-if="authStore.isClinician"
        @click="showAddModal = true"
        class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
      >
        Adicionar Registro
      </button>
    </div>

    <!-- Tabs de Categorias -->
    <div class="bg-white rounded-lg shadow">
      <div class="flex border-b overflow-x-auto">
        <button
          v-for="section in sections"
          :key="section.key"
          @click="activeSection = section.key"
          :class="[
            'px-6 py-3 font-medium text-sm whitespace-nowrap transition-colors',
            activeSection === section.key
              ? 'border-b-2 border-blue-500 text-blue-600'
              : 'text-gray-600 hover:text-gray-800',
          ]"
        >
          {{ section.label }}
        </button>
      </div>

      <!-- Conteúdo -->
      <div class="p-6">
        <!-- Loading State -->
        <div v-if="loading" class="text-center py-8">
          <p class="text-gray-500">Carregando...</p>
        </div>

        <!-- Perfil -->
        <div v-else-if="activeSection === 'profile'">
          <h3 class="text-lg font-semibold mb-4">Perfil do Paciente</h3>
          <div v-if="profile" class="space-y-3">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="text-sm text-gray-600">Idioma Preferido</label>
                <p class="font-medium">
                  {{ profile.preferredLanguage || "Não informado" }}
                </p>
              </div>
              <div>
                <label class="text-sm text-gray-600">Sexo Biológico</label>
                <p class="font-medium">
                  {{ profile.biologicalSex || "Não informado" }}
                </p>
              </div>
              <div>
                <label class="text-sm text-gray-600">Data de Nascimento</label>
                <p class="font-medium">{{ formatDate(profile.dateOfBirth) }}</p>
              </div>
            </div>
            <div v-if="profile.notes" class="mt-4">
              <label class="text-sm text-gray-600">Notas</label>
              <p class="mt-1 text-gray-800">{{ profile.notes }}</p>
            </div>
          </div>
          <p v-else class="text-gray-500">Perfil não encontrado</p>
        </div>

        <!-- Notas Clínicas -->
        <div v-else-if="activeSection === 'notes'">
          <h3 class="text-lg font-semibold mb-4">Notas Clínicas</h3>
          <div v-if="notes.length" class="space-y-4">
            <div
              v-for="note in notes"
              :key="note.id"
              class="border-l-4 border-blue-500 bg-blue-50 p-4 rounded-r"
            >
              <div class="flex justify-between items-start mb-2">
                <span class="text-sm font-medium text-blue-900">
                  {{ formatDateTime(note.timestamp) }}
                </span>
                <button
                  v-if="authStore.isClinician"
                  @click="deleteNote(note.id)"
                  class="text-red-600 hover:text-red-800 text-sm"
                >
                  Excluir
                </button>
              </div>
              <p class="text-gray-800">{{ note.note }}</p>
              <p v-if="note.authorId" class="text-xs text-gray-600 mt-2">
                Autor: {{ note.authorId }}
              </p>
            </div>
          </div>
          <p v-else class="text-gray-500">Nenhuma nota clínica encontrada</p>
        </div>

        <!-- Condições Médicas -->
        <div v-else-if="activeSection === 'conditions'">
          <h3 class="text-lg font-semibold mb-4">Condições Médicas</h3>
          <div v-if="conditions.length" class="space-y-4">
            <div
              v-for="condition in conditions"
              :key="condition.id"
              class="bg-gray-50 p-4 rounded-lg"
            >
              <div class="flex justify-between items-start">
                <div class="flex-1">
                  <p class="font-medium text-lg">{{ condition.description }}</p>
                  <p class="text-sm text-gray-600 mt-1">
                    CID: {{ condition.code }}
                  </p>
                  <div class="mt-2 text-sm text-gray-700">
                    <p>Início: {{ formatDate(condition.onsetDate) }}</p>
                    <p v-if="condition.resolvedDate">
                      Resolução: {{ formatDate(condition.resolvedDate) }}
                    </p>
                    <span
                      v-else
                      class="inline-block mt-1 px-2 py-1 bg-yellow-100 text-yellow-800 rounded text-xs"
                    >
                      Em tratamento
                    </span>
                  </div>
                </div>
                <button
                  v-if="authStore.isClinician"
                  @click="deleteCondition(condition.id)"
                  class="text-red-600 hover:text-red-800"
                >
                  Excluir
                </button>
              </div>
            </div>
          </div>
          <p v-else class="text-gray-500">Nenhuma condição médica registrada</p>
        </div>

        <!-- Alergias -->
        <div v-else-if="activeSection === 'allergies'">
          <h3 class="text-lg font-semibold mb-4">Alergias</h3>
          <div v-if="allergies.length" class="space-y-4">
            <div
              v-for="allergy in allergies"
              :key="allergy.id"
              class="bg-red-50 border-l-4 border-red-500 p-4 rounded-r"
            >
              <div class="flex justify-between items-start">
                <div class="flex-1">
                  <div class="flex items-center gap-2">
                    <p class="font-medium text-lg">{{ allergy.substance }}</p>
                    <span
                      :class="[
                        'px-2 py-1 text-xs rounded',
                        allergy.severity === 'Grave'
                          ? 'bg-red-200 text-red-900'
                          : allergy.severity === 'Moderada'
                          ? 'bg-orange-200 text-orange-900'
                          : 'bg-yellow-200 text-yellow-900',
                      ]"
                    >
                      {{ allergy.severity }}
                    </span>
                  </div>
                  <p class="text-sm text-gray-700 mt-1">
                    Reação: {{ allergy.reaction }}
                  </p>
                  <p class="text-xs text-gray-600 mt-2">
                    Registrado em: {{ formatDateTime(allergy.recordedAt) }}
                  </p>
                </div>
                <button
                  v-if="authStore.isClinician"
                  @click="deleteAllergy(allergy.id)"
                  class="text-red-600 hover:text-red-800"
                >
                  Excluir
                </button>
              </div>
            </div>
          </div>
          <p v-else class="text-gray-500">Nenhuma alergia registrada</p>
        </div>

        <!-- Medicações -->
        <div v-else-if="activeSection === 'medications'">
          <h3 class="text-lg font-semibold mb-4">Medicações</h3>
          <div v-if="medications.length" class="space-y-4">
            <div
              v-for="med in medications"
              :key="med.id"
              class="bg-green-50 p-4 rounded-lg"
            >
              <div class="flex justify-between items-start">
                <div class="flex-1">
                  <p class="font-medium text-lg">{{ med.drugName }}</p>
                  <div class="mt-2 text-sm text-gray-700 space-y-1">
                    <p><span class="font-medium">Dose:</span> {{ med.dose }}</p>
                    <p><span class="font-medium">Via:</span> {{ med.route }}</p>
                    <p>
                      <span class="font-medium">Frequência:</span>
                      {{ med.frequency }}
                    </p>
                    <p>
                      <span class="font-medium">Período:</span>
                      {{ formatDate(med.startDate) }}
                      <span v-if="med.endDate">
                        até {{ formatDate(med.endDate) }}</span
                      >
                    </p>
                  </div>
                </div>
                <button
                  v-if="authStore.isClinician"
                  @click="deleteMedication(med.id)"
                  class="text-red-600 hover:text-red-800"
                >
                  Excluir
                </button>
              </div>
            </div>
          </div>
          <p v-else class="text-gray-500">Nenhuma medicação registrada</p>
        </div>

        <!-- Sinais Vitais -->
        <div v-else-if="activeSection === 'vitals'">
          <h3 class="text-lg font-semibold mb-4">Sinais Vitais</h3>
          <div v-if="vitals.length" class="space-y-4">
            <div
              v-for="vital in vitals"
              :key="vital.id"
              class="bg-purple-50 p-4 rounded-lg"
            >
              <div class="flex justify-between items-start">
                <div class="flex-1">
                  <p class="font-medium text-lg capitalize">{{ vital.type }}</p>
                  <div class="mt-2 text-sm text-gray-700">
                    <pre class="bg-white p-2 rounded">{{
                      formatVitalValue(vital.valueJson)
                    }}</pre>
                  </div>
                  <p class="text-xs text-gray-600 mt-2">
                    Registrado em: {{ formatDateTime(vital.recordedAt) }}
                  </p>
                </div>
              </div>
            </div>
          </div>
          <p v-else class="text-gray-500">Nenhum sinal vital registrado</p>
        </div>
      </div>
    </div>

    <!-- Modal de Adicionar -->
    <UIBaseModal v-if="showAddModal" @close="closeModal">
      <div class="p-6">
        <h2 class="text-2xl font-bold mb-4">Adicionar Registro</h2>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Tipo de Registro
            </label>
            <select
              v-model="newRecordType"
              class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Selecione o tipo</option>
              <option value="note">Nota Clínica</option>
              <option value="condition">Condição Médica</option>
              <option value="allergy">Alergia</option>
              <option value="medication">Medicação</option>
              <option value="vital">Sinal Vital</option>
              <option value="profile">Editar Perfil</option>
            </select>
          </div>

          <!-- Formulário: Nota Clínica -->
          <form
            v-if="newRecordType === 'note'"
            @submit.prevent="submitNote"
            class="space-y-4"
          >
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Nota Clínica *
              </label>
              <textarea
                v-model="noteForm.note"
                required
                rows="4"
                class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Descreva a nota clínica..."
              ></textarea>
            </div>
            <div class="flex justify-end gap-3">
              <button
                type="button"
                @click="closeModal"
                class="px-4 py-2 text-gray-700 bg-gray-200 rounded-lg hover:bg-gray-300"
              >
                Cancelar
              </button>
              <button
                type="submit"
                :disabled="saving"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                {{ saving ? "Salvando..." : "Salvar" }}
              </button>
            </div>
          </form>

          <!-- Formulário: Condição Médica -->
          <form
            v-if="newRecordType === 'condition'"
            @submit.prevent="submitCondition"
            class="space-y-4"
          >
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Descrição *
              </label>
              <input
                v-model="conditionForm.description"
                type="text"
                required
                class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Ex: Hipertensão arterial"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Código CID
              </label>
              <input
                v-model="conditionForm.code"
                type="text"
                class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Ex: I10"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Data de Início *
              </label>
              <input
                v-model="conditionForm.onsetDate"
                type="date"
                required
                class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div class="flex justify-end gap-3">
              <button
                type="button"
                @click="closeModal"
                class="px-4 py-2 text-gray-700 bg-gray-200 rounded-lg hover:bg-gray-300"
              >
                Cancelar
              </button>
              <button
                type="submit"
                :disabled="saving"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                {{ saving ? "Salvando..." : "Salvar" }}
              </button>
            </div>
          </form>

          <!-- Formulário: Alergia -->
          <form
            v-if="newRecordType === 'allergy'"
            @submit.prevent="submitAllergy"
            class="space-y-4"
          >
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Substância *
              </label>
              <input
                v-model="allergyForm.substance"
                type="text"
                required
                class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Ex: Penicilina"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Reação *
              </label>
              <input
                v-model="allergyForm.reaction"
                type="text"
                required
                class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Ex: Urticária"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Gravidade *
              </label>
              <select
                v-model="allergyForm.severity"
                required
                class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">Selecione</option>
                <option value="Leve">Leve</option>
                <option value="Moderada">Moderada</option>
                <option value="Grave">Grave</option>
              </select>
            </div>
            <div class="flex justify-end gap-3">
              <button
                type="button"
                @click="closeModal"
                class="px-4 py-2 text-gray-700 bg-gray-200 rounded-lg hover:bg-gray-300"
              >
                Cancelar
              </button>
              <button
                type="submit"
                :disabled="saving"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                {{ saving ? "Salvando..." : "Salvar" }}
              </button>
            </div>
          </form>

          <!-- Formulário: Medicação -->
          <form
            v-if="newRecordType === 'medication'"
            @submit.prevent="submitMedication"
            class="space-y-4"
          >
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Nome do Medicamento *
              </label>
              <input
                v-model="medicationForm.drugName"
                type="text"
                required
                class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Ex: Losartana"
              />
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Dose *
                </label>
                <input
                  v-model="medicationForm.dose"
                  type="text"
                  required
                  class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Ex: 50mg"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Via *
                </label>
                <input
                  v-model="medicationForm.route"
                  type="text"
                  required
                  class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Ex: Oral"
                />
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Frequência *
              </label>
              <input
                v-model="medicationForm.frequency"
                type="text"
                required
                class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Ex: 1x ao dia"
              />
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Data de Início *
                </label>
                <input
                  v-model="medicationForm.startDate"
                  type="date"
                  required
                  class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Data de Término
                </label>
                <input
                  v-model="medicationForm.endDate"
                  type="date"
                  class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>
            <div class="flex justify-end gap-3">
              <button
                type="button"
                @click="closeModal"
                class="px-4 py-2 text-gray-700 bg-gray-200 rounded-lg hover:bg-gray-300"
              >
                Cancelar
              </button>
              <button
                type="submit"
                :disabled="saving"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                {{ saving ? "Salvando..." : "Salvar" }}
              </button>
            </div>
          </form>

          <!-- Formulário: Sinal Vital -->
          <form
            v-if="newRecordType === 'vital'"
            @submit.prevent="submitVital"
            class="space-y-4"
          >
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Tipo de Sinal Vital *
              </label>
              <select
                v-model="vitalForm.type"
                required
                class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">Selecione</option>
                <option value="blood_pressure">Pressão Arterial</option>
                <option value="heart_rate">Frequência Cardíaca</option>
                <option value="temperature">Temperatura</option>
                <option value="respiratory_rate">
                  Frequência Respiratória
                </option>
                <option value="oxygen_saturation">Saturação de Oxigênio</option>
                <option value="weight">Peso</option>
                <option value="height">Altura</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Valor *
              </label>
              <input
                v-model="vitalForm.value"
                type="text"
                required
                class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Ex: 120/80 ou 36.5"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Unidade *
              </label>
              <input
                v-model="vitalForm.unit"
                type="text"
                required
                class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Ex: mmHg, bpm, °C, kg, cm"
              />
            </div>
            <div class="flex justify-end gap-3">
              <button
                type="button"
                @click="closeModal"
                class="px-4 py-2 text-gray-700 bg-gray-200 rounded-lg hover:bg-gray-300"
              >
                Cancelar
              </button>
              <button
                type="submit"
                :disabled="saving"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                {{ saving ? "Salvando..." : "Salvar" }}
              </button>
            </div>
          </form>

          <!-- Formulário: Editar Perfil -->
          <form
            v-if="newRecordType === 'profile'"
            @submit.prevent="submitProfile"
            class="space-y-4"
          >
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Idioma Preferido
              </label>
              <input
                v-model="profileForm.preferredLanguage"
                type="text"
                class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Ex: pt-BR"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Sexo Biológico
              </label>
              <select
                v-model="profileForm.biologicalSex"
                class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">Selecione</option>
                <option value="M">Masculino</option>
                <option value="F">Feminino</option>
                <option value="O">Outro</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Data de Nascimento
              </label>
              <input
                v-model="profileForm.dateOfBirth"
                type="date"
                class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Notas
              </label>
              <textarea
                v-model="profileForm.notes"
                rows="3"
                class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Notas adicionais sobre o perfil..."
              ></textarea>
            </div>
            <div class="flex justify-end gap-3">
              <button
                type="button"
                @click="closeModal"
                class="px-4 py-2 text-gray-700 bg-gray-200 rounded-lg hover:bg-gray-300"
              >
                Cancelar
              </button>
              <button
                type="submit"
                :disabled="saving"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                {{ saving ? "Salvando..." : "Salvar" }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </UIBaseModal>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from "~/store/auth";
import { $api } from "~/utils/api";

const authStore = useAuthStore();
const route = useRoute();

const patientId = computed(() => route.params.id as string);

const activeSection = ref("profile");
const loading = ref(false);
const showAddModal = ref(false);
const newRecordType = ref("");
const saving = ref(false);

const profile = ref<any>(null);
const notes = ref<any[]>([]);
const conditions = ref<any[]>([]);
const allergies = ref<any[]>([]);
const medications = ref<any[]>([]);
const vitals = ref<any[]>([]);

const noteForm = ref({ note: "" });
const conditionForm = ref({ description: "", code: "", onsetDate: "" });
const allergyForm = ref({ substance: "", reaction: "", severity: "" });
const medicationForm = ref({
  drugName: "",
  dose: "",
  route: "",
  frequency: "",
  startDate: "",
  endDate: "",
});
const vitalForm = ref({ type: "", value: "", unit: "" });
const profileForm = ref({
  preferredLanguage: "",
  biologicalSex: "",
  dateOfBirth: "",
  notes: "",
});

const sections = [
  { key: "profile", label: "Perfil" },
  { key: "notes", label: "Notas Clínicas" },
  { key: "conditions", label: "Condições" },
  { key: "allergies", label: "Alergias" },
  { key: "medications", label: "Medicações" },
  { key: "vitals", label: "Sinais Vitais" },
];

definePageMeta({
  middleware: ["auth-only"],
  layout: "default",
});

function closeModal() {
  showAddModal.value = false;
  newRecordType.value = "";

  noteForm.value = { note: "" };
  conditionForm.value = { description: "", code: "", onsetDate: "" };
  allergyForm.value = { substance: "", reaction: "", severity: "" };
  medicationForm.value = {
    drugName: "",
    dose: "",
    route: "",
    frequency: "",
    startDate: "",
    endDate: "",
  };
  vitalForm.value = { type: "", value: "", unit: "" };
  profileForm.value = {
    preferredLanguage: "",
    biologicalSex: "",
    dateOfBirth: "",
    notes: "",
  };
}

async function submitNote() {
  saving.value = true;
  try {
    await $api(`/patients/${patientId.value}/history/notes`, {
      method: "POST",
      body: { note: noteForm.value.note },
    });

    alert("Nota clínica adicionada com sucesso!");
    closeModal();
    await fetchMedicalRecords();
  } catch (error) {
    console.error("Erro ao adicionar nota:", error);
    alert("Erro ao adicionar nota clínica");
  } finally {
    saving.value = false;
  }
}

async function submitCondition() {
  saving.value = true;
  try {
    await $api(`/patients/${patientId.value}/conditions`, {
      method: "POST",
      body: {
        description: conditionForm.value.description,
        code: conditionForm.value.code || null,
        onsetDate: conditionForm.value.onsetDate,
      },
    });

    alert("Condição médica adicionada com sucesso!");
    closeModal();
    await fetchMedicalRecords();
  } catch (error) {
    console.error("Erro ao adicionar condição:", error);
    alert("Erro ao adicionar condição médica");
  } finally {
    saving.value = false;
  }
}

async function submitAllergy() {
  saving.value = true;
  try {
    await $api(`/patients/${patientId.value}/allergies`, {
      method: "POST",
      body: {
        substance: allergyForm.value.substance,
        reaction: allergyForm.value.reaction,
        severity: allergyForm.value.severity,
      },
    });

    alert("Alergia adicionada com sucesso!");
    closeModal();
    await fetchMedicalRecords();
  } catch (error) {
    console.error("Erro ao adicionar alergia:", error);
    alert("Erro ao adicionar alergia");
  } finally {
    saving.value = false;
  }
}

async function submitMedication() {
  saving.value = true;
  try {
    await $api(`/patients/${patientId.value}/medications`, {
      method: "POST",
      body: {
        drugName: medicationForm.value.drugName,
        dose: medicationForm.value.dose,
        route: medicationForm.value.route,
        frequency: medicationForm.value.frequency,
        startDate: medicationForm.value.startDate,
        endDate: medicationForm.value.endDate || null,
      },
    });

    alert("Medicação adicionada com sucesso!");
    closeModal();
    await fetchMedicalRecords();
  } catch (error) {
    console.error("Erro ao adicionar medicação:", error);
    alert("Erro ao adicionar medicação");
  } finally {
    saving.value = false;
  }
}

async function submitVital() {
  saving.value = true;
  try {
    const valueJson = JSON.stringify({
      value: vitalForm.value.value,
      unit: vitalForm.value.unit,
    });

    await $api(`/patients/${patientId.value}/vitals`, {
      method: "POST",
      body: {
        type: vitalForm.value.type,
        valueJson: valueJson,
      },
    });

    alert("Sinal vital adicionado com sucesso!");
    closeModal();
    await fetchMedicalRecords();
  } catch (error) {
    console.error("Erro ao adicionar sinal vital:", error);
    alert("Erro ao adicionar sinal vital");
  } finally {
    saving.value = false;
  }
}

async function submitProfile() {
  saving.value = true;
  try {
    await $api(`/patients/${patientId.value}/profile`, {
      method: "PUT",
      body: {
        preferredLanguage: profileForm.value.preferredLanguage || null,
        biologicalSex: profileForm.value.biologicalSex || null,
        dateOfBirth: profileForm.value.dateOfBirth || null,
        notes: profileForm.value.notes || null,
      },
    });

    alert("Perfil atualizado com sucesso!");
    closeModal();
    await fetchMedicalRecords();
  } catch (error) {
    console.error("Erro ao atualizar perfil:", error);
    alert("Erro ao atualizar perfil");
  } finally {
    saving.value = false;
  }
}

async function fetchMedicalRecords() {
  if (!patientId.value) return;

  loading.value = true;
  try {
    const [
      profileData,
      notesData,
      conditionsData,
      allergiesData,
      medicationsData,
      vitalsData,
    ] = await Promise.allSettled([
      $api(`/patients/${patientId.value}/profile`),
      $api(`/patients/${patientId.value}/history/notes`),
      $api(`/patients/${patientId.value}/conditions`),
      $api(`/patients/${patientId.value}/allergies`),
      $api(`/patients/${patientId.value}/medications`),
      $api(`/patients/${patientId.value}/vitals`),
    ]);

    profile.value =
      profileData.status === "fulfilled" ? profileData.value : null;
    notes.value = notesData.status === "fulfilled" ? notesData.value : [];
    conditions.value =
      conditionsData.status === "fulfilled" ? conditionsData.value : [];
    allergies.value =
      allergiesData.status === "fulfilled" ? allergiesData.value : [];
    medications.value =
      medicationsData.status === "fulfilled" ? medicationsData.value : [];
    vitals.value = vitalsData.status === "fulfilled" ? vitalsData.value : [];

    if (profile.value) {
      profileForm.value = {
        preferredLanguage: profile.value.preferredLanguage || "",
        biologicalSex: profile.value.biologicalSex || "",
        dateOfBirth: profile.value.dateOfBirth || "",
        notes: profile.value.notes || "",
      };
    }
  } catch (error) {
    console.error("Erro ao buscar registros médicos", error);
  } finally {
    loading.value = false;
  }
}

async function deleteNote(id: string) {
  if (!confirm("Tem certeza que deseja excluir esta nota?")) return;
  try {
    await $api(`/patients/${patientId.value}/history/notes/${id}`, {
      method: "DELETE",
    });
    notes.value = notes.value.filter((n) => n.id !== id);
    alert("Nota deletada com sucesso!");
  } catch (error) {
    console.error("Erro ao excluir nota", error);
    alert("Erro ao deletar nota");
  }
}

async function deleteCondition(id: string) {
  if (!confirm("Tem certeza que deseja excluir esta condição?")) return;
  try {
    await $api(`/patients/${patientId.value}/conditions/${id}`, {
      method: "DELETE",
    });
    conditions.value = conditions.value.filter((c) => c.id !== id);
    alert("Condição deletada com sucesso!");
  } catch (error) {
    console.error("Erro ao excluir condição", error);
    alert("Erro ao deletar condição");
  }
}

async function deleteAllergy(id: string) {
  if (!confirm("Tem certeza que deseja excluir esta alergia?")) return;
  try {
    await $api(`/patients/${patientId.value}/allergies/${id}`, {
      method: "DELETE",
    });
    allergies.value = allergies.value.filter((a) => a.id !== id);
    alert("Alergia deletada com sucesso!");
  } catch (error) {
    console.error("Erro ao excluir alergia", error);
    alert("Erro ao deletar alergia");
  }
}

async function deleteMedication(id: string) {
  if (!confirm("Tem certeza que deseja excluir esta medicação?")) return;
  try {
    await $api(`/patients/${patientId.value}/medications/${id}`, {
      method: "DELETE",
    });
    medications.value = medications.value.filter((m) => m.id !== id);
    alert("Medicação deletada com sucesso!");
  } catch (error) {
    console.error("Erro ao excluir medicação", error);
    alert("Erro ao deletar medicação");
  }
}

function formatDate(date: string | null) {
  if (!date) return "N/A";
  return new Date(date).toLocaleDateString("pt-BR");
}

function formatDateTime(date: string) {
  return new Date(date).toLocaleString("pt-BR");
}

function formatVitalValue(jsonString: string) {
  try {
    return JSON.stringify(JSON.parse(jsonString), null, 2);
  } catch {
    return jsonString;
  }
}

onMounted(async () => {
  await fetchMedicalRecords();
});
</script>
