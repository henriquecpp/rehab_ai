<template>
  <UIBaseModal @close="$emit('close')">
    <template #header>
      <h2 class="text-xl font-semibold text-gray-900">Adicionar Alergia</h2>
    </template>

    <form @submit.prevent="handleSubmit" class="space-y-4">
      <div>
        <label
          for="substance"
          class="block text-sm font-medium text-gray-700 mb-1"
        >
          Substância *
        </label>
        <input
          id="substance"
          v-model="form.substance"
          type="text"
          required
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="Ex: Penicilina"
        />
      </div>

      <div>
        <label
          for="reaction"
          class="block text-sm font-medium text-gray-700 mb-1"
        >
          Reação *
        </label>
        <input
          id="reaction"
          v-model="form.reaction"
          type="text"
          required
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="Ex: Erupção cutânea"
        />
      </div>

      <div>
        <label
          for="severity"
          class="block text-sm font-medium text-gray-700 mb-1"
        >
          Severidade *
        </label>
        <select
          id="severity"
          v-model="form.severity"
          required
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="">Selecione a severidade</option>
          <option value="LOW">Baixa</option>
          <option value="MEDIUM">Média</option>
          <option value="HIGH">Alta</option>
          <option value="CRITICAL">Crítica</option>
        </select>
      </div>

      <div class="flex justify-end gap-3 pt-4">
        <button
          type="button"
          @click="$emit('close')"
          class="px-4 py-2 text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200"
        >
          Cancelar
        </button>
        <button
          type="submit"
          :disabled="loading"
          class="px-4 py-2 text-white bg-blue-600 rounded-md hover:bg-blue-700 disabled:bg-gray-400"
        >
          {{ loading ? "Salvando..." : "Salvar" }}
        </button>
      </div>
    </form>
  </UIBaseModal>
</template>

<script setup lang="ts">
const props = defineProps<{
  patientId: string;
}>();

const emit = defineEmits<{
  close: [];
  success: [];
}>();

const form = ref({
  substance: "",
  reaction: "",
  severity: "",
});

const loading = ref(false);

const handleSubmit = async () => {
  loading.value = true;
  try {
    await useApiFetch(`/patients/${props.patientId}/allergies`, {
      method: "POST",
      body: form.value,
    });
    emit("success");
    emit("close");
  } catch (error) {
    console.error("Erro ao criar alergia:", error);
    alert("Erro ao criar alergia. Tente novamente.");
  } finally {
    loading.value = false;
  }
};
</script>
