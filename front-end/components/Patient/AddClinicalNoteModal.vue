<template>
  <UIBaseModal @close="$emit('close')">
    <template #header>
      <h2 class="text-xl font-semibold text-gray-900">
        Adicionar Nota Clínica
      </h2>
    </template>

    <form @submit.prevent="handleSubmit" class="space-y-4">
      <div>
        <label for="note" class="block text-sm font-medium text-gray-700 mb-1">
          Nota *
        </label>
        <textarea
          id="note"
          v-model="form.note"
          rows="6"
          required
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="Descreva a observação clínica..."
        />
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
  note: "",
});

const loading = ref(false);

const handleSubmit = async () => {
  loading.value = true;
  try {
    await useApiFetch(`/patients/${props.patientId}/history/notes`, {
      method: "POST",
      body: form.value,
    });
    emit("success");
    emit("close");
  } catch (error) {
    console.error("Erro ao criar nota clínica:", error);
    alert("Erro ao criar nota clínica. Tente novamente.");
  } finally {
    loading.value = false;
  }
};
</script>
