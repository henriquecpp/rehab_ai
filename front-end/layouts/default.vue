<template>
  <div class="flex min-h-screen">
    <AppSidebar />
    
    <main class="flex-1 bg-gray-50" style="margin-left: 280px;">
      
      <AppHeader />
      
      <div class="p-8">
        <slot />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
// Este layout não precisa de script, mas se você precisar
// buscar dados que valem para todo o layout, pode fazer aqui.
</script>