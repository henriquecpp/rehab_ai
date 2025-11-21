<template>
  <UIBaseModal @close="$emit('close')">
    <template #header>
      <h2 class="text-xl font-semibold text-gray-900">Adicionar Medicação</h2>
    </template>

    <form @submit.prevent="handleSubmit" class="space-y-4">
      <div>
        <label
          for="medication"
          class="block text-sm font-medium text-gray-700 mb-1"
        >
          Nome do Medicamento *
        </label>
        <input
          id="medication"
          v-model="form.medication"
          type="text"
          required
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="Ex: Ibuprofeno"
        />
      </div>

      <div>
        <label
          for="dosage"
          class="block text-sm font-medium text-gray-700 mb-1"
        >
          Dose *
        </label>
        <input
          id="dosage"
          v-model="form.dosage"
          type="text"
          required
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="Ex: 400mg"
        />
      </div>

      <div>
        <label for="route" class="block text-sm font-medium text-gray-700 mb-1">
          Via de Administração *
        </label>
        <select
          id="route"
          v-model="form.route"
          required
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="">Selecione a via</option>
          <option value="oral">Oral</option>
          <option value="intravenous">Intravenosa</option>
          <option value="intramuscular">Intramuscular</option>
          <option value="subcutaneous">Subcutânea</option>
          <option value="topical">Tópica</option>
          <option value="inhalation">Inalação</option>
        </select>
      </div>

      <div>
        <label
          for="frequency"
          class="block text-sm font-medium text-gray-700 mb-1"
        >
          Frequência *
        </label>
        <input
          id="frequency"
          v-model="form.frequency"
          type="text"
          required
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="Ex: 3x ao dia"
        />
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <label
            for="startDate"
            class="block text-sm font-medium text-gray-700 mb-1"
          >
            Data de Início *
          </label>
          <input
            id="startDate"
            v-model="form.startDate"
            type="date"
            required
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label
            for="endDate"
            class="block text-sm font-medium text-gray-700 mb-1"
          >
            Data de Término (Opcional)
          </label>
          <input
            id="endDate"
            v-model="form.endDate"
            type="date"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
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
  medication: "",
  dosage: "",
  route: "",
  frequency: "",
  startDate: "",
  endDate: "",
});

const loading = ref(false);

const handleSubmit = async () => {
  loading.value = true;
  try {
    const payload = {
      medication: form.value.medication,
      dosage: form.value.dosage,
      route: form.value.route,
      frequency: form.value.frequency,
      startDate: form.value.startDate,
      ...(form.value.endDate && { endDate: form.value.endDate }),
    };

    await useApiFetch(`/patients/${props.patientId}/medications`, {
      method: "POST",
      body: payload,
    });
    emit("success");
    emit("close");
  } catch (error) {
    console.error("Erro ao criar medicação:", error);
    alert("Erro ao criar medicação. Tente novamente.");
  } finally {
    loading.value = false;
  }
};
</script>
