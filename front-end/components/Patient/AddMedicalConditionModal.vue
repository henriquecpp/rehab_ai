<template>
  <UIBaseModal @close="$emit('close')">
    <template #header>
      <h2 class="text-xl font-semibold text-gray-900">
        Adicionar Condição Médica
      </h2>
    </template>

    <form @submit.prevent="handleSubmit" class="space-y-4">
      <div>
        <label for="code" class="block text-sm font-medium text-gray-700 mb-1">
          Código CID-10 *
        </label>
        <input
          id="code"
          v-model="form.code"
          type="text"
          required
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="Ex: M54.5"
        />
      </div>

      <div>
        <label
          for="description"
          class="block text-sm font-medium text-gray-700 mb-1"
        >
          Descrição *
        </label>
        <input
          id="description"
          v-model="form.description"
          type="text"
          required
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="Ex: Dor lombar baixa"
        />
      </div>

      <div>
        <label
          for="onsetDate"
          class="block text-sm font-medium text-gray-700 mb-1"
        >
          Data de Início *
        </label>
        <input
          id="onsetDate"
          v-model="form.onsetDate"
          type="date"
          required
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      <div>
        <label
          for="resolutionDate"
          class="block text-sm font-medium text-gray-700 mb-1"
        >
          Data de Resolução (Opcional)
        </label>
        <input
          id="resolutionDate"
          v-model="form.resolutionDate"
          type="date"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
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
  code: "",
  description: "",
  onsetDate: "",
  resolutionDate: "",
});

const loading = ref(false);

const handleSubmit = async () => {
  loading.value = true;
  try {
    const payload = {
      code: form.value.code,
      description: form.value.description,
      onsetDate: form.value.onsetDate,
      ...(form.value.resolutionDate && {
        resolutionDate: form.value.resolutionDate,
      }),
    };

    await useApiFetch(`/patients/${props.patientId}/conditions`, {
      method: "POST",
      body: payload,
    });
    emit("success");
    emit("close");
  } catch (error) {
    console.error("Erro ao criar condição médica:", error);
    alert("Erro ao criar condição médica. Tente novamente.");
  } finally {
    loading.value = false;
  }
};
</script>
