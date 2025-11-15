<template>
  <UICard title="Analytics e Métricas">
    <div class="alert alert-info">
      <span>ℹ️</span>
      <div>
        <strong>Página em Desenvolvimento</strong><br />
        Esta seção mostrará métricas agregadas sobre o desempenho da plataforma
        e da IA.
      </div>
    </div>

    <div class="mt-6">
      <h3 class="text-lg font-semibold mb-3">Métricas de IA (Exemplo)</h3>

      <div class="confidence-bar">
        <span class="confidence-label">Precisão de Extração</span>
        <div class="confidence-track">
          <div class="confidence-fill" style="width: 94%"></div>
        </div>
        <span class="confidence-value">94%</span>
      </div>

      <div class="confidence-bar">
        <span class="confidence-label">Confiança Média</span>
        <div class="confidence-track">
          <div class="confidence-fill" style="width: 89%"></div>
        </div>
        <span class="confidence-value">89%</span>
      </div>

      <div class="confidence-bar">
        <span class="confidence-label">Taxa de Aprovação</span>
        <div class="confidence-track">
          <div class="confidence-fill" style="width: 82%"></div>
        </div>
        <span class="confidence-value">82%</span>
      </div>

      <div class="alert alert-warning mt-6">
        <span>⚠️</span>
        <div>
          <strong>Nota de Implementação:</strong> A API atual não fornece
          endpoints para buscar essas métricas de forma agregada. Novos
          endpoints de analytics seriam necessários no back-end.
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

.confidence-bar {
  @apply my-3 flex items-center gap-3;
}
.confidence-label {
  @apply w-36 text-sm text-gray-700;
}
.confidence-track {
  @apply h-2 flex-1 overflow-hidden rounded-full bg-gray-200;
}
.confidence-fill {
  @apply h-full bg-success;
}
.confidence-value {
  @apply w-12 text-right font-semibold text-gray-900;
}
</style>
