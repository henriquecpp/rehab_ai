<template>
  <UICard title="LGPD & Compliance">
    <div class="alert alert-info">
      <span>🔒</span>
      <div>
        <strong>Gestão de Conformidade</strong><br />
        Monitore e gerencie conformidade com LGPD/GDPR
      </div>
    </div>

    <div class="mt-6 space-y-6">
      <section>
        <h3 class="text-lg font-semibold mb-3">Status de Conformidade</h3>
        <div class="grid gap-4 md:grid-cols-3">
          <div class="stat-card">
            <div class="stat-value text-green-600">✓</div>
            <div class="stat-label">Consentimentos Ativos</div>
            <div class="stat-number">245</div>
          </div>

          <div class="stat-card">
            <div class="stat-value text-blue-600">📄</div>
            <div class="stat-label">Políticas Atualizadas</div>
            <div class="stat-number">100%</div>
          </div>

          <div class="stat-card">
            <div class="stat-value text-yellow-600">⚠️</div>
            <div class="stat-label">Revisões Pendentes</div>
            <div class="stat-number">3</div>
          </div>
        </div>
      </section>

      <section>
        <h3 class="text-lg font-semibold mb-3">Ações Necessárias</h3>
        <div class="space-y-3">
          <div class="action-item">
            <span class="action-icon bg-yellow-100 text-yellow-800">⚠️</span>
            <div class="flex-1">
              <p class="font-medium">Revisar política de privacidade</p>
              <p class="text-sm text-gray-600">Última atualização há 6 meses</p>
            </div>
            <button class="btn-secondary">Revisar</button>
          </div>

          <div class="action-item">
            <span class="action-icon bg-blue-100 text-blue-800">ℹ️</span>
            <div class="flex-1">
              <p class="font-medium">Exportar dados de usuários inativos</p>
              <p class="text-sm text-gray-600">12 solicitações pendentes</p>
            </div>
            <button class="btn-secondary">Processar</button>
          </div>
        </div>
      </section>

      <section>
        <h3 class="text-lg font-semibold mb-3">Registros de Auditoria</h3>
        <div class="audit-log">
          <div class="audit-item">
            <span class="audit-date">15/11/2025 14:30</span>
            <span>Consentimento de dados concedido - Usuário #1234</span>
          </div>
          <div class="audit-item">
            <span class="audit-date">14/11/2025 10:15</span>
            <span>Política de privacidade aceita - Usuário #5678</span>
          </div>
          <div class="audit-item">
            <span class="audit-date">13/11/2025 16:45</span>
            <span
              >Solicitação de exclusão de dados processada - Usuário #9012</span
            >
          </div>
        </div>
      </section>
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
.stat-card {
  @apply rounded-lg border border-gray-200 bg-white p-4 text-center;
}
.stat-value {
  @apply text-3xl mb-2;
}
.stat-label {
  @apply text-sm text-gray-600 mb-1;
}
.stat-number {
  @apply text-2xl font-bold text-gray-900;
}
.action-item {
  @apply flex items-center gap-4 rounded-lg border border-gray-200 bg-white p-4;
}
.action-icon {
  @apply flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-lg;
}
.btn-secondary {
  @apply rounded-lg bg-gray-200 px-4 py-2 text-sm font-semibold text-gray-700 transition hover:bg-gray-300;
}
.audit-log {
  @apply space-y-2 rounded-lg border border-gray-200 bg-white p-4;
}
.audit-item {
  @apply flex gap-4 text-sm;
}
.audit-date {
  @apply font-medium text-gray-500;
}
</style>
