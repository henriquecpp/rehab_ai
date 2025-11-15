<template>
  <div
    v-if="modelValue"
    class="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-4"
  >
    <div class="form-group md:col-span-2">
      <label class="form-label">Nome do Exercício</label>
      <input
        type="text"
        :value="modelValue.name"
        @input="update('name', $event)"
        class="form-input"
      />
    </div>
    <div class="form-group md:col-span-2">
      <label class="form-label">Descrição / Instruções</label>
      <textarea
        :value="modelValue.description"
        @input="update('description', $event)"
        class="form-input"
        rows="2"
      ></textarea>
    </div>

    <div class="form-group">
      <label class="form-label">Séries</label>
      <input
        type="number"
        :value="modelValue.sets"
        @input="update('sets', $event, true)"
        class="form-input"
      />
    </div>
    <div class="form-group">
      <label class="form-label">Frequência</label>
      <input
        type="text"
        :value="modelValue.frequency"
        @input="update('frequency', $event)"
        class="form-input"
      />
    </div>

    <div class="form-group">
      <label class="form-label">Repetições</label>
      <input
        type="number"
        :value="modelValue.repetitions"
        @input="update('repetitions', $event, true)"
        class="form-input"
        :disabled="modelValue.duration !== null"
      />
      <span
        v-if="modelValue.duration !== null"
        class="text-xs text-gray-500 mt-1"
        >Desativado (exercício por duração)</span
      >
    </div>

    <div class="form-group">
      <label class="form-label">Duração (segundos)</label>
      <input
        type="number"
        :value="modelValue.duration"
        @input="update('duration', $event, true)"
        class="form-input"
        :disabled="modelValue.repetitions !== null"
      />
      <span
        v-if="modelValue.repetitions !== null"
        class="text-xs text-gray-500 mt-1"
        >Desativado (exercício por repetição)</span
      >
    </div>
  </div>
</template>

<script setup lang="ts">
// --- Tipagem ---
interface Exercise {
  name: string;
  description: string;
  sets: number;
  repetitions: number | null;
  duration: number | null;
  frequency: string;
}

// --- v-model (props e emits) ---
const props = defineProps<{
  // --- CORREÇÃO 2: A prop 'modelValue' agora é opcional ---
  modelValue?: Exercise;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", value: Exercise): void;
}>();

// --- Lógica de Atualização ---
function update(field: keyof Exercise, event: Event, isNumber = false) {
  // --- CORREÇÃO 3: Adicionamos uma "guarda" ---
  // Se o modelValue não existir por algum motivo, não faz nada.
  if (!props.modelValue) return;

  const target = event.target as HTMLInputElement;
  let value: string | number | null = target.value;

  if (isNumber) {
    value = value === "" ? null : parseInt(value, 10);
  }

  const newExercise = { ...props.modelValue, [field]: value };
  if (field === "repetitions" && value !== null) {
    newExercise.duration = null;
  }
  if (field === "duration" && value !== null) {
    newExercise.repetitions = null;
  }

  emit("update:modelValue", newExercise);
}
</script>

<style scoped>
.form-input {
  @apply w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm shadow-sm disabled:bg-gray-100;
}
.form-label {
  @apply mb-1 block text-sm font-semibold text-gray-700;
}
.form-group {
  @apply flex flex-col;
}
</style>
