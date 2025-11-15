<template>
  <UIBaseModal title="Criar Conta" :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)">
    <div v-if="errorMsg" class="mb-4 rounded-lg bg-danger/10 p-3 text-center text-sm text-danger">
      {{ errorMsg }}
    </div>

    <form @submit.prevent="handleRegister">
      <div class="mb-4">
        <label class="form-label">Nome Completo</label>
        <input v-model="form.fullName" type="text" class="form-input" required>
      </div>
      <div class="mb-4">
        <label class="form-label">Email</label>
        <input v-model="form.email" type="email" class="form-input" required>
      </div>
      <div class="mb-4">
        <label class="form-label">Senha</label>
        <input v-model="form.password" type="password" class="form-input" placeholder="Mínimo 8 caracteres" required>
      </div>
      <div class="mb-4">
        <label class="form-label">Tipo de Conta</label>
        <select v-model="form.role" class="form-input">
          <option value="PATIENT">Paciente</option>
          <option value="CLINICIAN">Fisioterapeuta</option>
        </select>
      </div>
      <button type="submit" class="btn-primary w-full justify-center py-2.5" :disabled="isLoading">
        {{ isLoading ? 'Criando conta...' : 'Criar Conta' }}
      </button>
    </form>
  </UIBaseModal>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/store/auth';

// Setup para v-model
const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue'])

const authStore = useAuthStore();
const isLoading = ref(false)
const errorMsg = ref<string | null>(null)

const form = ref({
  fullName: '',
  email: '',
  password: '',
  role: 'PATIENT' as 'PATIENT' | 'CLINICIAN'
})

async function handleRegister() {
  isLoading.value = true;
  errorMsg.value = null;
  try {
    // Chama a action do store (Seção 1.1)
    await authStore.register(form.value);
    // O redirecionamento é feito no store
    emit('update:modelValue', false) // Fecha o modal
  } catch (error: any) {
    console.error(error)
    if (error.data?.code === 'email_already_exists') {
      errorMsg.value = 'Este email já está cadastrado.'
    } else {
      errorMsg.value = 'Ocorreu um erro no registro.'
    }
  } finally {
    isLoading.value = false;
  }
}
</script>

<style scoped>
/* ... (estilos dos forms) ... */
.form-label { @apply mb-2 block text-sm font-semibold text-gray-700; }
.form-input { @apply w-full rounded-lg border border-gray-300 px-3 py-2.5 text-sm shadow-sm transition focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary; }
.btn-primary { @apply flex items-center gap-2 rounded-lg bg-primary px-5 py-2 text-sm font-semibold text-white transition hover:bg-primary-dark disabled:opacity-70; }
</style>