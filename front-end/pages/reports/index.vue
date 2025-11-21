<template>
  <UICard title="Relatórios">
    <div class="alert alert-info">
      <span>📊</span>
      <div>
        <strong>Página em Desenvolvimento</strong><br />
        Esta seção mostrará relatórios detalhados sobre pacientes, tratamentos e
        resultados.
      </div>
    </div>

    <div class="mt-6 grid gap-4 md:grid-cols-2">
      <div class="report-card">
        <h3 class="font-semibold text-lg mb-2">Relatório de Pacientes</h3>
        <p class="text-sm text-gray-600 mb-4">
          Visão geral de todos os pacientes ativos
        </p>
        <button class="btn-primary" disabled>Gerar Relatório</button>
      </div>

      <div class="report-card">
        <h3 class="font-semibold text-lg mb-2">Relatório de Prescrições</h3>
        <p class="text-sm text-gray-600 mb-4">
          Análise de prescrições processadas
        </p>
        <button class="btn-primary" disabled>Gerar Relatório</button>
      </div>

      <div class="report-card">
        <h3 class="font-semibold text-lg mb-2">Relatório de Conformidade</h3>
        <p class="text-sm text-gray-600 mb-4">
          Status de conformidade LGPD/GDPR
        </p>
        <button class="btn-primary" disabled>Gerar Relatório</button>
      </div>

      <div class="report-card">
        <h3 class="font-semibold text-lg mb-2">Relatório de Performance</h3>
        <p class="text-sm text-gray-600 mb-4">Métricas de performance da IA</p>
        <button class="btn-primary" disabled>Gerar Relatório</button>
      </div>
    </div>
  </UICard>
</template>

<script setup lang="ts">
definePageMeta({
  middleware: "auth-only",
  layout: "default",
});
</script>

<style scoped>
.alert {
  @apply mb-4 flex gap-3 rounded-lg border-l-4 p-4;
}
.alert-info {
  @apply border-primary bg-blue-50 text-blue-800;
}
.report-card {
  @apply rounded-lg border border-gray-200 bg-white p-6;
}
.btn-primary {
  @apply rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:opacity-50;
}
</style>
