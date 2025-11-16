<template>
  <div
    class="m-5 w-full max-w-md rounded-xl bg-white p-10 shadow-[0_20px_40px_rgba(0,0,0,0.1)]"
  >
    <div class="mb-8 text-center">
      <div class="mb-4 flex items-center justify-center gap-3">
        <div
          class="flex h-12 w-12 items-center justify-center rounded-lg bg-gradient-to-br from-primary to-blue-500 text-2xl font-bold text-white"
        >
          R
        </div>
        <span class="text-3xl font-bold text-gray-900">RehabAI</span>
      </div>
      <p class="text-base text-gray-600">
        Plataforma Inteligente de Prescrição Fisioterapêutica
      </p>
    </div>

    <div
      v-if="errorMsg"
      class="mb-4 rounded-lg bg-danger/10 p-3 text-center text-sm text-danger"
    >
      {{ errorMsg }}
    </div>

    <form @submit.prevent="handleLogin">
      <div class="mb-5">
        <label for="email" class="form-label">Email</label>
        <input
          v-model="email"
          id="email"
          type="email"
          class="form-input"
          placeholder="seu@email.com"
          required
        />
      </div>
      <div class="mb-5">
        <label for="password" class="form-label">Senha</label>
        <input
          v-model="password"
          id="password"
          type="password"
          class="form-input"
          placeholder="Sua senha"
          required
        />
      </div>
      <button
        type="submit"
        class="btn-primary mb-4 w-full justify-center py-2.5"
        :disabled="isLoading"
      >
        {{ isLoading ? "Entrando..." : "Entrar" }}
      </button>
    </form>

    <div class="mt-6 text-center">
      <p class="mb-4 text-gray-600">Não tem uma conta?</p>
      <button
        @click="isRegisterModalOpen = true"
        class="btn-secondary w-full justify-center py-2.5"
      >
        Criar uma conta
      </button>
    </div>
  </div>

  <AuthRegisterModal v-model="isRegisterModalOpen" />
</template>

<script setup lang="ts">
definePageMeta({
  layout: "login",
  middleware: ["guest-only"],
});

const authStore = useAuthStore();

const email = ref("");
const password = ref("");
const isLoading = ref(false);
const errorMsg = ref<string | null>(null);
const isRegisterModalOpen = ref(false);

async function handleLogin() {
  isLoading.value = true;
  errorMsg.value = null;
  try {
    await authStore.login(email.value, password.value);
  } catch (error: any) {
    console.error(error);
    if (error.data?.code === "invalid_credentials" || error.status === 401) {
      errorMsg.value = "Email ou senha inválidos.";
    } else {
      errorMsg.value = "Ocorreu um erro. Tente novamente.";
    }
  } finally {
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
