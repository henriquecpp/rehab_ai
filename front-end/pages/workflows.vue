<template>
  <UICard title="Workflows de Prescrição">
    <div class="alert alert-info">
      <span>ℹ️</span>
      <div>
        <strong>Página em Desenvolvimento</strong><br />
        Esta seção listará o status de todos os workflows de processamento de IA
        em tempo real.
      </div>
    </div>

    <div class="mt-6">
      <h3 class="text-lg font-semibold mb-3">Workflows Ativos (Exemplo)</h3>

      <div class="workflow-status status-running">
        <span>🔄</span>
        <div class="flex-1">
          <strong>Processando laudo de Maria Silva</strong>
          <div class="stage-progress">
            <span>Extração: ✓</span>
            <span>Normalização: ⏳</span>
            <span>Geração: ⏳</span>
          </div>
        </div>
        <button class="btn-secondary" disabled>Detalhes</button>
      </div>

      <div class="workflow-status status-completed">
        <span>✅</span>
        <div class="flex-1">
          <strong>Plano gerado para João Santos</strong>
          <div class="stage-progress">
            <span>Concluído há 2 horas</span>
          </div>
        </div>
        <button class="btn-secondary" disabled>Ver Plano</button>
      </div>

      <div class="alert alert-warning mt-6">
        <span>⚠️</span>
        <div>
          <strong>Nota de Implementação:</strong> A API (Seções 8 e 9) não
          parece fornecer um endpoint para listar *todos* os workflows (ex: `GET
          /prescriptions/workflows`). Um endpoint assim será necessário para
          tornar esta página funcional.
        </div>
      </div>
    </div>
  </UICard>
</template>

<script setup lang="ts">
definePageMeta({ middleware: "auth-only" });
</script>

<style scoped>
.alert {
  @apply mb-4 flex gap-3 rounded-lg border-l-4 p-4;
}
.alert-info {
  @apply border-primary bg-blue-50 text-blue-800;
}
.alert-warning {
  @apply border-warning bg-yellow-50 text-yellow-800;
}
.btn-secondary {
  @apply flex items-center gap-2 rounded-lg bg-gray-200 px-4 py-2 text-xs font-semibold text-gray-700 transition hover:bg-gray-300 disabled:opacity-70;
}

.workflow-status {
  @apply mb-3 flex items-center gap-3 rounded-lg p-3;
}
.status-running {
  @apply bg-blue-50 text-blue-800;
}
.status-completed {
  @apply bg-green-50 text-green-800;
}
.stage-progress {
  @apply flex items-center gap-2 text-xs opacity-80;
}
</style>
