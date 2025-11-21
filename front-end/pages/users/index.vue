<template>
  <UICard title="Gestão de Usuários">
    <div class="mb-4 flex gap-4">
      <div class="flex-1">
        <label class="form-label">Filtrar por Função</label>
        <select v-model="selectedRole" class="form-input">
          <option value="">Todos</option>
          <option value="ADMIN">Admin</option>
          <option value="CLINICIAN">Clinician</option>
          <option value="PATIENT">Patient</option>
        </select>
      </div>
      <div class="flex-1">
        <label class="form-label">Filtrar por Status</label>
        <select v-model="selectedStatus" class="form-input">
          <option value="">Todos</option>
          <option value="true">Ativos</option>
          <option value="false">Inativos</option>
        </select>
      </div>
    </div>

    <div v-if="pending" class="py-10 text-center text-gray-600">
      Carregando usuários...
    </div>

    <div v-else-if="error" class="alert alert-danger">
      Erro ao carregar usuários: {{ error.message }}
    </div>

    <div v-else-if="users" class="grid gap-3">
      <div v-for="user in users" :key="user.id" class="user-card">
        <div class="flex items-center gap-4">
          <div class="user-avatar">
            {{ getInitials(user.fullName) }}
          </div>
          <div>
            <h3 class="text-base font-semibold text-gray-900">
              {{ user.fullName }}
            </h3>
            <p class="text-sm text-gray-600">{{ user.email }}</p>
          </div>
        </div>
        <div class="flex items-center gap-3 flex-wrap">
          <span class="badge" :class="getRoleClass(user.role)">
            {{ user.role }}
          </span>
          <span
            class="badge"
            :class="user.active ? 'badge-success' : 'badge-warning'"
          >
            {{ user.active ? "Ativo" : "Inativo" }}
          </span>
          <button
            v-if="user.role === 'PATIENT' && !userProfileStatus[user.id]"
            @click="openCreateProfileModal(user)"
            class="btn-create-profile"
            title="Criar perfil de paciente"
          >
            📋 Criar Perfil
          </button>
          <button
            v-if="user.role === 'PATIENT' && userProfileStatus[user.id]"
            @click="openViewProfileModal(user)"
            class="btn-view-profile"
            title="Ver perfil de paciente"
          >
            👁️ Ver Perfil
          </button>
        </div>
      </div>
    </div>

    <!-- Create Patient Profile Modal -->
    <UIBaseModal
      v-if="showCreateProfileModal"
      :model-value="showCreateProfileModal"
      @close="closeCreateProfileModal"
    >
      <div class="p-6">
        <h2 class="text-2xl font-bold mb-4">Criar Perfil de Paciente</h2>
        <p class="text-gray-600 mb-4">
          Criar perfil para: <strong>{{ selectedUser?.fullName }}</strong>
        </p>

        <form @submit.prevent="createPatientProfile" class="space-y-4">
          <div>
            <label class="form-label">Data de Nascimento</label>
            <input
              v-model="profileForm.dateOfBirth"
              type="date"
              class="form-input"
              required
            />
          </div>

          <div>
            <label class="form-label">Sexo Biológico</label>
            <select
              v-model="profileForm.biologicalSex"
              class="form-input"
              required
            >
              <option value="">Selecione...</option>
              <option value="MALE">Masculino</option>
              <option value="FEMALE">Feminino</option>
              <option value="OTHER">Outro</option>
            </select>
          </div>

          <div>
            <label class="form-label">Idioma Preferido</label>
            <select v-model="profileForm.preferredLanguage" class="form-input">
              <option value="pt-BR">Português (Brasil)</option>
              <option value="en">English</option>
              <option value="es">Español</option>
            </select>
          </div>

          <div>
            <label class="form-label">Observações</label>
            <textarea
              v-model="profileForm.notes"
              class="form-input"
              rows="3"
              placeholder="Notas adicionais sobre o paciente..."
            ></textarea>
          </div>

          <div class="flex gap-3 justify-end">
            <button
              type="button"
              @click="closeCreateProfileModal"
              class="btn-cancel"
              :disabled="creatingProfile"
            >
              Cancelar
            </button>
            <button
              type="submit"
              class="btn-primary"
              :disabled="creatingProfile"
            >
              {{ creatingProfile ? "Criando..." : "Criar Perfil" }}
            </button>
          </div>
        </form>
      </div>
    </UIBaseModal>

    <!-- View Patient Profile Modal -->
    <UIBaseModal
      v-if="showViewProfileModal"
      :model-value="showViewProfileModal"
      @close="closeViewProfileModal"
    >
      <div class="p-6">
        <h2 class="text-2xl font-bold mb-4">Perfil do Paciente</h2>
        <p class="text-gray-600 mb-4">
          <strong>{{ selectedUser?.fullName }}</strong>
        </p>

        <div v-if="loadingProfile" class="text-center py-8">
          <div
            class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"
          ></div>
          <p class="mt-2 text-gray-600">Carregando perfil...</p>
        </div>

        <div v-else-if="patientProfile" class="space-y-3">
          <div class="profile-field">
            <span class="profile-label">Data de Nascimento:</span>
            <span class="profile-value">{{
              formatDate(patientProfile.dateOfBirth)
            }}</span>
          </div>
          <div class="profile-field">
            <span class="profile-label">Idade:</span>
            <span class="profile-value"
              >{{ calculateAge(patientProfile.dateOfBirth) }} anos</span
            >
          </div>
          <div class="profile-field">
            <span class="profile-label">Sexo Biológico:</span>
            <span class="profile-value">{{
              getBiologicalSexLabel(patientProfile.biologicalSex)
            }}</span>
          </div>
          <div class="profile-field">
            <span class="profile-label">Idioma:</span>
            <span class="profile-value">{{
              patientProfile.preferredLanguage || "pt-BR"
            }}</span>
          </div>
          <div v-if="patientProfile.notes" class="profile-field">
            <span class="profile-label">Observações:</span>
            <span class="profile-value">{{ patientProfile.notes }}</span>
          </div>
        </div>

        <div class="flex justify-end mt-6">
          <button @click="closeViewProfileModal" class="btn-primary">
            Fechar
          </button>
        </div>
      </div>
    </UIBaseModal>
  </UICard>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: ["auth-only"],
  layout: "default",
});

const authStore = useAuthStore();

interface ApiUser {
  id: string;
  email: string;
  fullName: string;
  role: "PATIENT" | "CLINICIAN" | "ADMIN";
  active: boolean;
}

interface PatientProfile {
  userId: string;
  dateOfBirth: string;
  biologicalSex: "MALE" | "FEMALE" | "OTHER";
  preferredLanguage?: string;
  notes?: string;
}

interface ProfileForm {
  dateOfBirth: string;
  biologicalSex: "MALE" | "FEMALE" | "OTHER" | "";
  preferredLanguage: string;
  notes: string;
}

const selectedRole = ref<"PATIENT" | "CLINICIAN" | "ADMIN" | "">("");
const selectedStatus = ref<"true" | "false" | "">("");
const showCreateProfileModal = ref(false);
const showViewProfileModal = ref(false);
const selectedUser = ref<ApiUser | null>(null);
const creatingProfile = ref(false);
const loadingProfile = ref(false);
const patientProfile = ref<PatientProfile | null>(null);

const profileForm = ref<ProfileForm>({
  dateOfBirth: "",
  biologicalSex: "",
  preferredLanguage: "pt-BR",
  notes: "",
});

const queryParams = computed(() => {
  const params: Record<string, string> = {};
  if (selectedRole.value) {
    params.role = selectedRole.value;
  }
  if (selectedStatus.value) {
    params.activeOnly = selectedStatus.value;
  }
  return params;
});

const {
  data: users,
  pending,
  error,
  refresh: refreshUsers,
} = await useApiFetch<ApiUser[]>("/users", {
  query: queryParams,
  watch: [queryParams],
});

const userProfileStatus = ref<Record<string, boolean>>({});

async function checkProfileStatus(userId: string): Promise<boolean> {
  try {
    const token = authStore.token;
    if (!token) {
      return false;
    }

    const response = await $fetch(`/patients/${userId}/profile`, {
      method: "GET",
      baseURL: "http://localhost:8080",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return !!response;
  } catch (error: any) {
    if (
      error?.statusCode === 404 ||
      error?.statusCode === 400 ||
      error?.statusCode === 401
    ) {
      return false;
    }
    if (error?.statusCode && ![404, 400, 401].includes(error.statusCode)) {
      console.warn(`Unexpected error checking profile for ${userId}:`, error);
    }
    return false;
  }
}

async function refreshProfileStatuses() {
  if (users.value) {
    const statusMap: Record<string, boolean> = {};
    await Promise.all(
      users.value
        .filter((u) => u.role === "PATIENT")
        .map(async (user) => {
          statusMap[user.id] = await checkProfileStatus(user.id);
        })
    );
    userProfileStatus.value = statusMap;
  }
}

onMounted(async () => {
  await refreshProfileStatuses();
});

watch(users, async () => {
  await refreshProfileStatuses();
});

function openCreateProfileModal(user: ApiUser) {
  selectedUser.value = user;
  showCreateProfileModal.value = true;
  profileForm.value = {
    dateOfBirth: "",
    biologicalSex: "",
    preferredLanguage: "pt-BR",
    notes: "",
  };
}

function closeCreateProfileModal() {
  showCreateProfileModal.value = false;
  selectedUser.value = null;
}

async function createPatientProfile() {
  if (!selectedUser.value || !profileForm.value.biologicalSex) return;

  creatingProfile.value = true;
  try {
    const payload: any = {
      dateOfBirth: profileForm.value.dateOfBirth,
      biologicalSex: profileForm.value.biologicalSex,
    };

    if (profileForm.value.preferredLanguage) {
      payload.preferredLanguage = profileForm.value.preferredLanguage;
    }
    if (profileForm.value.notes) {
      payload.notes = profileForm.value.notes;
    }

    console.log("Creating profile with payload:", payload);

    const { data, error } = await useApiFetch(
      `/patients/${selectedUser.value.id}/profile`,
      {
        method: "PUT",
        body: payload,
      }
    );

    if (error.value) {
      console.error("API error:", error.value);
      throw new Error(error.value.message || "Erro ao criar perfil");
    }

    console.log("Profile created successfully:", data.value);
    alert("Perfil de paciente criado com sucesso!");

    if (selectedUser.value) {
      userProfileStatus.value[selectedUser.value.id] = true;
    }

    closeCreateProfileModal();
    await refreshUsers();
    await refreshProfileStatuses();
  } catch (error: any) {
    console.error("Erro ao criar perfil:", error);
    const errorMessage =
      error?.data?.detail || error?.message || "Erro desconhecido";
    alert(`Erro ao criar perfil de paciente: ${errorMessage}`);
  } finally {
    creatingProfile.value = false;
  }
}

async function openViewProfileModal(user: ApiUser) {
  selectedUser.value = user;
  showViewProfileModal.value = true;
  loadingProfile.value = true;
  patientProfile.value = null;

  try {
    const { data } = await useApiFetch<PatientProfile>(
      `/patients/${user.id}/profile`,
      {
        method: "GET",
      }
    );
    if (data.value) {
      patientProfile.value = data.value;
    }
  } catch (error) {
    console.error("Erro ao carregar perfil:", error);
    alert("Erro ao carregar perfil de paciente.");
  } finally {
    loadingProfile.value = false;
  }
}

function closeViewProfileModal() {
  showViewProfileModal.value = false;
  selectedUser.value = null;
  patientProfile.value = null;
}

function getInitials(name: string) {
  if (!name) return "";
  return name
    .split(" ")
    .map((n) => n[0])
    .join("")
    .toUpperCase();
}

function getRoleClass(role: string) {
  switch (role) {
    case "ADMIN":
      return "badge-danger";
    case "CLINICIAN":
      return "badge-info";
    case "PATIENT":
      return "badge-secondary";
    default:
      return "badge-secondary";
  }
}

function formatDate(dateString: string): string {
  if (!dateString) return "-";
  const date = new Date(dateString);
  return date.toLocaleDateString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
}

function calculateAge(dateOfBirth: string): number {
  if (!dateOfBirth) return 0;
  const today = new Date();
  const birthDate = new Date(dateOfBirth);
  let age = today.getFullYear() - birthDate.getFullYear();
  const monthDiff = today.getMonth() - birthDate.getMonth();
  if (
    monthDiff < 0 ||
    (monthDiff === 0 && today.getDate() < birthDate.getDate())
  ) {
    age--;
  }
  return age;
}

function getBiologicalSexLabel(sex: string): string {
  const labels = {
    MALE: "Masculino",
    FEMALE: "Feminino",
    OTHER: "Outro",
  };
  return labels[sex as keyof typeof labels] || sex;
}
</script>

<style scoped>
.form-input {
  @apply w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm shadow-sm;
}
.form-label {
  @apply mb-1 block text-xs font-semibold text-gray-600;
}
.user-card {
  @apply flex items-center justify-between rounded-lg border border-gray-200 bg-white p-4;
}
.user-avatar {
  @apply flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-gray-200 font-semibold text-gray-700;
}
.badge {
  @apply shrink-0 rounded-full px-3 py-0.5 text-xs font-semibold;
}
.badge-success {
  @apply bg-green-100 text-green-800;
}
.badge-warning {
  @apply bg-yellow-100 text-yellow-800;
}
.badge-danger {
  @apply bg-red-100 text-red-800;
}
.badge-info {
  @apply bg-blue-100 text-blue-800;
}
.badge-secondary {
  @apply bg-gray-100 text-gray-700;
}
.alert-danger {
  @apply mb-4 flex gap-3 rounded-lg border-l-4 border-danger bg-red-50 p-4 text-red-800;
}
.btn-create-profile {
  @apply px-3 py-1.5 text-xs bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition;
}
.btn-view-profile {
  @apply px-3 py-1.5 text-xs bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition;
}
.btn-primary {
  @apply px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed;
}
.btn-cancel {
  @apply px-4 py-2 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300;
}
.profile-field {
  @apply flex flex-col sm:flex-row sm:gap-2 py-2 border-b border-gray-200;
}
.profile-label {
  @apply text-sm font-semibold text-gray-600 sm:w-40;
}
.profile-value {
  @apply text-sm text-gray-900;
}
</style>
