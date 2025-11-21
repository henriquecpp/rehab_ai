<template>
  <UIBaseModal
    title="Cadastrar Novo Paciente"
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <form @submit.prevent="handleSubmit">
      <div
        v-if="errorMsg"
        class="mb-4 rounded-lg bg-danger/10 p-3 text-center text-sm text-danger"
      >
        {{ errorMsg }}
      </div>

      <h3 class="mb-3 text-lg font-semibold text-gray-800">Dados de Acesso</h3>
      <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
        <div class="md:col-span-2">
          <label for="fullName" class="form-label">Nome Completo</label>
          <input
            id="fullName"
            v-model="form.fullName"
            type="text"
            class="form-input"
            required
          />
        </div>
        <div>
          <label for="email" class="form-label">Email</label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            class="form-input"
            placeholder="paciente@email.com"
            required
          />
        </div>
        <div>
          <label for="password" class="form-label">Senha Provisória</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            class="form-input"
            required
          />
        </div>
      </div>

      <h3 class="mb-3 mt-6 text-lg font-semibold text-gray-800">
        Dados do Perfil
      </h3>
      <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
        <div>
          <label for="dateOfBirth" class="form-label">Data de Nascimento</label>
          <input
            id="dateOfBirth"
            v-model="form.dateOfBirth"
            type="date"
            class="form-input"
            required
          />
        </div>
        <div>
          <label for="biologicalSex" class="form-label">Sexo Biológico</label>
          <select
            id="biologicalSex"
            v-model="form.biologicalSex"
            class="form-input"
            required
          >
            <option value="M">Masculino</option>
            <option value="F">Feminino</option>
            <option value="O">Outro</option>
          </select>
        </div>
        <div class="md:col-span-2">
          <label for="notes" class="form-label">Notas Adicionais</label>
          <textarea
            id="notes"
            v-model="form.notes"
            class="form-input"
            rows="3"
            placeholder="Informações relevantes (ex: CPF, telefone...)"
          ></textarea>
        </div>
      </div>

      <div class="mt-8 flex justify-end gap-3">
        <button
          type="button"
          class="btn-secondary"
          @click="$emit('update:modelValue', false)"
          :disabled="isLoading"
        >
          Cancelar
        </button>
        <button type="submit" class="btn-primary" :disabled="isLoading">
          {{ isLoading ? "Cadastrando..." : "Cadastrar Paciente" }}
        </button>
      </div>
    </form>
  </UIBaseModal>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { $api } from "~/utils/api";

import { decodeJwtPayload } from "~/utils/jwt"; //

interface AuthResponse {
  token: string;
  refreshToken: string;
}


defineProps<{
  modelValue: boolean;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", value: boolean): void;
  (e: "patientCreated"): void;
}>();

const form = reactive({
  fullName: "",
  email: "",
  password: "",
  dateOfBirth: "",
  biologicalSex: "M" as "M" | "F" | "O",
  notes: "",
});

const isLoading = ref(false);
const errorMsg = ref<string | null>(null);

async function handleSubmit() {
  isLoading.value = true;
  errorMsg.value = null;
  let newUserId = "";

  try {
    const response = await $api<AuthResponse>("/auth/register", {
      method: "POST",
      body: {
        email: form.email,
        password: form.password,
        fullName: form.fullName,
        role: "PATIENT",
      },
    });

    const payload: any = decodeJwtPayload(response.token);

    if (!payload || !payload.user_id) {
      throw new Error("Não foi possível obter o ID do novo usuário no token.");
    }
    newUserId = payload.user_id;

    await $api(`/patients/${newUserId}/profile`, {
      method: "PUT",
      body: {
        dateOfBirth: form.dateOfBirth,
        biologicalSex: form.biologicalSex,
        notes: form.notes,
        preferredLanguage: "pt-BR",
      },
    });

    isLoading.value = false;
    emit("patientCreated");
    emit("update:modelValue", false);
  } catch (error: any) {
    console.error("Falha ao cadastrar paciente:", error);
    if (error.data?.code === "email_already_exists") {
      //
      errorMsg.value = "Este email já está cadastrado.";
    } else if (newUserId) {
      errorMsg.value =
        "O usuário foi criado, mas houve um erro ao salvar o perfil. Edite o perfil manualmente.";
    } else {
      errorMsg.value = "Ocorreu um erro. Tente novamente.";
    }
    isLoading.value = false;
  }
}
</script>

<style scoped>
.form-label {
  @apply mb-2 block text-sm font-semibold text-gray-700;
}
.form-input {
  @apply w-full rounded-lg border border-gray-300 px-3 py-2.5 text-sm shadow-sm transition focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary;
}
.btn-primary {
  @apply flex items-center gap-2 rounded-lg bg-primary px-5 py-2 text-sm font-semibold text-white transition hover:bg-primary-dark disabled:opacity-70;
}
.btn-secondary {
  @apply flex items-center gap-2 rounded-lg bg-gray-200 px-5 py-2 text-sm font-semibold text-gray-700 transition hover:bg-gray-300;
}
</style>
