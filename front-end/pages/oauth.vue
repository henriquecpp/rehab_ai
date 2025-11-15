<template>
  <UICard title="Clientes OAuth">
    <p>Página de Clientes OAuth (em construção).</p>
    <p class="mt-4 text-sm text-gray-600">
      Consultar documentação para mais detalhes.
    </p>
  </UICard>
</template>
<script setup lang="ts">
definePageMeta({ middleware: 'auth-only' })
</script>