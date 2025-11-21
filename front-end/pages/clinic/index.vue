<template>
  <UICard title="Gestão da Clínica">
    <div class="alert alert-info">
      <span>🏥</span>
      <div>
        <strong>Página em Desenvolvimento</strong><br />
        Esta seção permitirá gerenciar configurações da clínica, profissionais e
        estrutura organizacional.
      </div>
    </div>

    <div class="mt-6 space-y-6">
      <section>
        <h3 class="text-lg font-semibold mb-3">Informações da Clínica</h3>
        <div class="info-card">
          <div class="grid md:grid-cols-2 gap-4">
            <div>
              <p class="text-sm text-gray-600">Nome da Clínica</p>
              <p class="font-medium">RehabAI Clinic</p>
            </div>
            <div>
              <p class="text-sm text-gray-600">CNPJ</p>
              <p class="font-medium">00.000.000/0000-00</p>
            </div>
            <div>
              <p class="text-sm text-gray-600">Endereço</p>
              <p class="font-medium">Rua Exemplo, 123 - São Paulo, SP</p>
            </div>
            <div>
              <p class="text-sm text-gray-600">Telefone</p>
              <p class="font-medium">(11) 9999-9999</p>
            </div>
          </div>
          <button class="btn-secondary mt-4">Editar Informações</button>
        </div>
      </section>

      <section>
        <h3 class="text-lg font-semibold mb-3">Profissionais Ativos</h3>
        <div class="grid gap-3">
          <div class="professional-card">
            <div class="flex items-center gap-3">
              <div class="avatar">DS</div>
              <div>
                <p class="font-medium">Dr. Silva</p>
                <p class="text-sm text-gray-600">Fisioterapeuta</p>
              </div>
            </div>
            <span class="badge badge-success">Ativo</span>
          </div>
          <div class="professional-card">
            <div class="flex items-center gap-3">
              <div class="avatar">MP</div>
              <div>
                <p class="font-medium">Maria Pereira</p>
                <p class="text-sm text-gray-600">Terapeuta Ocupacional</p>
              </div>
            </div>
            <span class="badge badge-success">Ativo</span>
          </div>
        </div>
      </section>

      <section>
        <h3 class="text-lg font-semibold mb-3">Configurações</h3>
        <div class="space-y-3">
          <div class="config-item">
            <div>
              <p class="font-medium">Horário de Funcionamento</p>
              <p class="text-sm text-gray-600">Segunda a Sexta: 8h às 18h</p>
            </div>
            <button class="btn-secondary">Configurar</button>
          </div>
          <div class="config-item">
            <div>
              <p class="font-medium">Duração Padrão de Consulta</p>
              <p class="text-sm text-gray-600">60 minutos</p>
            </div>
            <button class="btn-secondary">Configurar</button>
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
.info-card {
  @apply rounded-lg border border-gray-200 bg-white p-6;
}
.professional-card {
  @apply flex items-center justify-between rounded-lg border border-gray-200 bg-white p-4;
}
.avatar {
  @apply flex h-12 w-12 items-center justify-center rounded-full bg-blue-100 font-semibold text-blue-800;
}
.config-item {
  @apply flex items-center justify-between rounded-lg border border-gray-200 bg-white p-4;
}
.badge {
  @apply inline-block rounded-full px-3 py-0.5 text-xs font-semibold;
}
.badge-success {
  @apply bg-green-100 text-green-800;
}
.btn-secondary {
  @apply rounded-lg bg-gray-200 px-4 py-2 text-sm font-semibold text-gray-700 transition hover:bg-gray-300;
}
</style>
