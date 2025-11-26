// front-end/layouts/default.vue
<template>
  <div class="flex min-h-screen">
    <AppSidebar />

    <main class="flex-1 bg-gray-50 ml-72">
      <AppHeader />

      <div class="p-8">
        <slot />
      </div>
    </main>
    <UIToastContainer />
  </div>
</template>

<script setup lang="ts"></script>
