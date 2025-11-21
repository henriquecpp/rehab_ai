<template>
  <aside
    class="fixed flex h-full w-[280px] flex-col border-r border-gray-200 bg-white z-[100]"
  >
    <div class="flex items-center gap-3 border-b border-gray-200 p-6">
      <div
        class="flex h-10 w-10 items-center justify-center rounded-lg bg-gradient-to-br from-primary to-blue-500 text-xl font-bold text-white"
      >
        R
      </div>
      <span class="text-xl font-bold text-gray-900">RehabAI</span>
    </div>

    <nav class="flex-1 overflow-y-auto p-3">
      <NuxtLink
        v-for="item in userMenu"
        :key="item.id"
        :to="item.path"
        class="nav-item group"
        active-class="active"
      >
        <span class="flex h-5 w-5 items-center justify-center">{{
          item.icon
        }}</span>
        <span>{{ item.label }}</span>
      </NuxtLink>
    </nav>

    <div class="border-t border-gray-200 p-4">
      <button
        @click="authStore.logout()"
        class="btn-secondary w-full justify-center"
      >
        🚪 Sair
      </button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { useAuthStore } from "~/store/auth";
const authStore = useAuthStore();

const clinicianMenu = [
  { id: "dashboard", icon: "📊", label: "Dashboard", path: "/" },
  { id: "patients", icon: "👥", label: "Pacientes", path: "/patients" },
  {
    id: "prescribe",
    icon: "📋",
    label: "Nova Prescrição",
    path: "/prescriptions/prescribe",
  },
  {
    id: "prescriptions",
    icon: "🤖",
    label: "Prescrições IA",
    path: "/prescriptions",
  },
  {
    id: "workflows",
    icon: "🔄",
    label: "Workflows Ativos",
    path: "/workflows",
  }, // Página a ser criada
  { id: "plans", icon: "📑", label: "Planos de Reabilitação", path: "/plans" }, // Página a ser criada
  { id: "history", icon: "📝", label: "Históricos Médicos", path: "/history" }, // Página a ser criada
  { id: "analytics", icon: "📈", label: "Analytics", path: "/analytics" }, // Página a ser criada
];

const adminMenu = [
  { id: "dashboard", icon: "📊", label: "Dashboard", path: "/" },
  { id: "clinic", icon: "🏥", label: "Gestão da Clínica", path: "/clinic" }, // Página a ser criada
  { id: "users", icon: "👥", label: "Usuários", path: "/users" }, // Página a ser criada
  { id: "oauth", icon: "🔐", label: "Clientes OAuth", path: "/oauth" }, // Página a ser criada
  {
    id: "compliance",
    icon: "🔒",
    label: "LGPD & Compliance",
    path: "/compliance",
  }, // Página a ser criada
  { id: "reports", icon: "📈", label: "Relatórios", path: "/reports" }, // Página a ser criada
];

const patientMenu = [
  { id: "dashboard", icon: "🏠", label: "Meu Progresso", path: "/" },
  //{ id: "exercises", icon: "💪", label: "Meus Exercícios", path: "/exercises" }, // Página a ser criada
  {
    id: "medical-records",
    icon: "📋",
    label: "Meus Registros",
    path: "/medical-records",
  }, // Página a ser criada
  {
    id: "consents",
    icon: "✓",
    label: "Meus Consentimentos",
    path: "/consents",
  }, // Página a ser criada
  { id: "schedule", icon: "📅", label: "Agenda", path: "/schedule" }, // Página a ser criada
  { id: "messages", icon: "💬", label: "Mensagens", path: "/messages" }, // Página a ser criada
];

// O 'userMenu' é uma computed property que reage à mudança de 'authStore.user'
const userMenu = computed(() => {
  const role = authStore.user?.role;
  switch (role) {
    case "ADMIN":
      return adminMenu;
    case "PATIENT":
      return patientMenu;
    case "CLINICIAN":
    default:
      return clinicianMenu;
  }
});
</script>

<style scoped>
/* Estilos do NavItem e Botão */
.nav-item {
  @apply mx-0 my-1 flex cursor-pointer items-center gap-3 rounded-lg px-4 py-3 text-gray-700 transition-all;
}
.nav-item:hover {
  @apply bg-gray-100 text-primary;
}
.nav-item.active {
  @apply bg-primary text-white;
}

.btn-secondary {
  @apply flex items-center gap-2 rounded-lg bg-gray-200 px-5 py-2.5 text-sm font-semibold text-gray-700 transition hover:bg-gray-300;
}
</style>
