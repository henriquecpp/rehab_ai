<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <div class="max-w-4xl mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <div class="flex items-center gap-4 mb-4">
          <button
            @click="$router.push('/users')"
            class="text-gray-600 hover:text-gray-900"
          >
            ← Voltar para Usuários
          </button>
        </div>
        <h1 class="text-3xl font-bold text-gray-900 mb-2">Editar Usuário</h1>
        <p class="text-gray-600">
          Gerencie informações e permissões do usuário
        </p>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="text-center py-12">
        <div
          class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"
        ></div>
        <p class="mt-4 text-gray-600">Carregando usuário...</p>
      </div>

      <!-- User Edit Form -->
      <div v-else-if="user" class="space-y-6">
        <!-- User Info Card -->
        <div class="bg-white rounded-lg shadow-sm p-6">
          <h2 class="text-xl font-bold text-gray-900 mb-6">
            Informações do Usuário
          </h2>

          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >Nome Completo</label
              >
              <input
                v-model="editForm.name"
                type="text"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Nome do usuário"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >Email</label
              >
              <input
                v-model="editForm.email"
                type="email"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="email@exemplo.com"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >CPF</label
              >
              <input
                v-model="editForm.cpf"
                type="text"
                maxlength="14"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="000.000.000-00"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >Status</label
              >
              <div class="flex items-center gap-4">
                <span
                  class="px-3 py-1 inline-flex text-sm font-semibold rounded-full"
                  :class="
                    user.active
                      ? 'bg-green-100 text-green-800'
                      : 'bg-red-100 text-red-800'
                  "
                >
                  {{ user.active ? "✓ Ativo" : "✗ Inativo" }}
                </span>
                <span class="text-sm text-gray-500">
                  Criado em: {{ formatDate(user.createdAt) }}
                </span>
              </div>
            </div>

            <div class="flex gap-3 pt-4">
              <button
                @click="saveUser"
                :disabled="saving"
                class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
              >
                {{ saving ? "💾 Salvando..." : "💾 Salvar Alterações" }}
              </button>
            </div>
          </div>
        </div>

        <!-- Role Management Card -->
        <div class="bg-white rounded-lg shadow-sm p-6">
          <h2 class="text-xl font-bold text-gray-900 mb-6">
            Gerenciamento de Permissões
          </h2>

          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >Função Atual</label
              >
              <span
                class="px-4 py-2 inline-flex text-sm font-semibold rounded-lg"
                :class="getRoleClass(user.role)"
              >
                {{ getRoleLabel(user.role) }}
              </span>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >Alterar Função</label
              >
              <select
                v-model="newRole"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">-- Selecione uma função --</option>
                <option value="PATIENT">Paciente</option>
                <option value="CLINICIAN">Clínico</option>
                <option value="ADMIN">Administrador</option>
              </select>
            </div>

            <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
              <p class="text-sm text-blue-800">
                <strong>ℹ️ Permissões:</strong><br />
                <strong>PATIENT:</strong> Acesso básico, visualização de
                próprios dados<br />
                <strong>CLINICIAN:</strong> Gerenciar pacientes, criar
                prescrições e planos<br />
                <strong>ADMIN:</strong> Acesso total ao sistema, gerenciar
                usuários
              </p>
            </div>

            <button
              @click="changeRole"
              :disabled="!newRole || newRole === user.role || saving"
              class="px-6 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              {{ saving ? "⏳ Alterando..." : "🔄 Alterar Função" }}
            </button>
          </div>
        </div>

        <!-- Account Actions Card -->
        <div class="bg-white rounded-lg shadow-sm p-6">
          <h2 class="text-xl font-bold text-gray-900 mb-6">Ações da Conta</h2>

          <div class="space-y-4">
            <!-- Activate/Deactivate -->
            <div
              class="flex items-center justify-between p-4 border border-gray-200 rounded-lg"
            >
              <div>
                <h3 class="font-semibold text-gray-900">
                  {{ user.active ? "Desativar Conta" : "Ativar Conta" }}
                </h3>
                <p class="text-sm text-gray-600">
                  {{
                    user.active
                      ? "Usuário não poderá fazer login no sistema"
                      : "Usuário poderá fazer login normalmente"
                  }}
                </p>
              </div>
              <button
                @click="toggleUserActive"
                :disabled="saving"
                class="px-4 py-2 rounded-lg font-medium disabled:bg-gray-400 disabled:cursor-not-allowed"
                :class="
                  user.active
                    ? 'bg-red-100 text-red-700 hover:bg-red-200'
                    : 'bg-green-100 text-green-700 hover:bg-green-200'
                "
              >
                {{ user.active ? "🔒 Desativar" : "✓ Ativar" }}
              </button>
            </div>

            <!-- Reset Password -->
            <div
              class="flex items-center justify-between p-4 border border-gray-200 rounded-lg"
            >
              <div>
                <h3 class="font-semibold text-gray-900">Resetar Senha</h3>
                <p class="text-sm text-gray-600">
                  Definir uma nova senha temporária para o usuário
                </p>
              </div>
              <button
                @click="resetPassword"
                :disabled="saving"
                class="px-4 py-2 bg-orange-100 text-orange-700 rounded-lg hover:bg-orange-200 font-medium disabled:bg-gray-400 disabled:cursor-not-allowed"
              >
                🔑 Resetar Senha
              </button>
            </div>

            <!-- Delete User -->
            <div
              class="flex items-center justify-between p-4 border border-red-200 rounded-lg bg-red-50"
            >
              <div>
                <h3 class="font-semibold text-red-900">Deletar Usuário</h3>
                <p class="text-sm text-red-700">
                  ⚠️ Esta ação é irreversível e removerá todos os dados do
                  usuário
                </p>
              </div>
              <button
                @click="deleteUser"
                :disabled="saving"
                class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 font-medium disabled:bg-gray-400 disabled:cursor-not-allowed"
              >
                🗑️ Deletar
              </button>
            </div>
          </div>
        </div>

        <!-- Activity Log -->
        <div class="bg-white rounded-lg shadow-sm p-6">
          <h2 class="text-xl font-bold text-gray-900 mb-6">
            Registro de Atividades
          </h2>

          <div class="space-y-3">
            <div
              class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
            >
              <span class="text-sm text-gray-700">Último login</span>
              <span class="text-sm font-medium text-gray-900">
                {{ user.lastLogin ? formatDate(user.lastLogin) : "Nunca" }}
              </span>
            </div>
            <div
              class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
            >
              <span class="text-sm text-gray-700">Conta criada</span>
              <span class="text-sm font-medium text-gray-900">{{
                formatDate(user.createdAt)
              }}</span>
            </div>
            <div
              class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
            >
              <span class="text-sm text-gray-700">Última atualização</span>
              <span class="text-sm font-medium text-gray-900">{{
                formatDate(user.updatedAt)
              }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Error State -->
      <div v-else class="text-center py-12">
        <p class="text-xl text-gray-600">Usuário não encontrado</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useApiFetch } from "~/composables/useApiFetch";
import { useAuthStore } from "~/store/auth";

definePageMeta({
  middleware: ["auth-only"],
  layout: "default",
});

interface User {
  id: string;
  name: string;
  email: string;
  cpf?: string;
  role: "PATIENT" | "CLINICIAN" | "ADMIN";
  active: boolean;
  createdAt: string;
  updatedAt: string;
  lastLogin?: string;
}

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const user = ref<User | null>(null);
const loading = ref(true);
const saving = ref(false);
const newRole = ref("");

const editForm = reactive({
  name: "",
  email: "",
  cpf: "",
});

const fetchUser = async () => {
  loading.value = true;
  try {
    const { data } = await useApiFetch<User>(`/users/${route.params.id}`, {
      method: "GET",
    });
    if (data.value) {
      user.value = data.value;
      editForm.name = data.value.name;
      editForm.email = data.value.email;
      editForm.cpf = data.value.cpf || "";
    }
  } catch (error) {
    console.error("Erro ao carregar usuário:", error);
    alert("Erro ao carregar usuário");
  } finally {
    loading.value = false;
  }
};

const saveUser = async () => {
  if (!editForm.name || !editForm.email) {
    alert("Preencha todos os campos obrigatórios");
    return;
  }

  saving.value = true;
  try {
    await useApiFetch(`/users/${route.params.id}`, {
      method: "PUT",
      body: {
        name: editForm.name,
        email: editForm.email,
        cpf: editForm.cpf || undefined,
      },
    });
    alert("Usuário atualizado com sucesso!");
    await fetchUser();
  } catch (error) {
    console.error("Erro ao salvar usuário:", error);
    alert("Erro ao salvar usuário");
  } finally {
    saving.value = false;
  }
};

const changeRole = async () => {
  if (!newRole.value) return;

  if (
    !confirm(
      `Tem certeza que deseja alterar a função para ${getRoleLabel(
        newRole.value
      )}?`
    )
  ) {
    return;
  }

  saving.value = true;
  try {
    await useApiFetch(`/users/${route.params.id}/role`, {
      method: "POST",
      body: { role: newRole.value },
    });
    alert("Função alterada com sucesso!");
    newRole.value = "";
    await fetchUser();
  } catch (error) {
    console.error("Erro ao alterar função:", error);
    alert("Erro ao alterar função");
  } finally {
    saving.value = false;
  }
};

const toggleUserActive = async () => {
  if (!user.value) return;

  const action = user.value.active ? "desativar" : "ativar";
  if (!confirm(`Tem certeza que deseja ${action} este usuário?`)) {
    return;
  }

  saving.value = true;
  try {
    const endpoint = user.value.active ? "deactivate" : "activate";
    await useApiFetch(`/users/${route.params.id}/${endpoint}`, {
      method: "POST",
    });
    alert(
      `Usuário ${
        action === "desativar" ? "desativado" : "ativado"
      } com sucesso!`
    );
    await fetchUser();
  } catch (error) {
    console.error(`Erro ao ${action} usuário:`, error);
    alert(`Erro ao ${action} usuário`);
  } finally {
    saving.value = false;
  }
};

const resetPassword = async () => {
  const newPassword = prompt(
    "Digite a nova senha temporária (mínimo 8 caracteres):"
  );
  if (!newPassword) return;

  if (newPassword.length < 8) {
    alert("A senha deve ter no mínimo 8 caracteres");
    return;
  }

  saving.value = true;
  try {
    await useApiFetch(`/users/${route.params.id}/password`, {
      method: "POST",
      body: { newPassword },
    });
    alert(
      "Senha resetada com sucesso! Informe ao usuário a nova senha temporária."
    );
  } catch (error) {
    console.error("Erro ao resetar senha:", error);
    alert("Erro ao resetar senha");
  } finally {
    saving.value = false;
  }
};

const deleteUser = async () => {
  if (
    !confirm(
      "⚠️ ATENÇÃO: Esta ação é IRREVERSÍVEL!\n\nTem certeza que deseja deletar este usuário permanentemente?"
    )
  ) {
    return;
  }

  const confirmation = prompt('Digite "DELETAR" em maiúsculas para confirmar:');
  if (confirmation !== "DELETAR") {
    return;
  }

  saving.value = true;
  try {
    await useApiFetch(`/users/${route.params.id}`, {
      method: "DELETE",
    });
    alert("Usuário deletado com sucesso!");
    router.push("/users");
  } catch (error) {
    console.error("Erro ao deletar usuário:", error);
    alert("Erro ao deletar usuário");
  } finally {
    saving.value = false;
  }
};

const getRoleClass = (role: string): string => {
  const classes = {
    PATIENT: "bg-blue-100 text-blue-800",
    CLINICIAN: "bg-green-100 text-green-800",
    ADMIN: "bg-purple-100 text-purple-800",
  };
  return classes[role as keyof typeof classes] || "bg-gray-100 text-gray-800";
};

const getRoleLabel = (role: string): string => {
  const labels = {
    PATIENT: "Paciente",
    CLINICIAN: "Clínico",
    ADMIN: "Administrador",
  };
  return labels[role as keyof typeof labels] || role;
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
  if (authStore.user?.role !== "ADMIN") {
    alert("Apenas administradores podem editar usuários");
    router.push("/users");
    return;
  }

  fetchUser();
});
</script>
