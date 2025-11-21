<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h1 class="text-3xl font-bold">Exercícios</h1>
      <button
        v-if="authStore.isClinician"
        @click="showCreateModal = true"
        class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
      >
        Novo Exercício
      </button>
    </div>

    <!-- Filtros -->
    <div class="bg-white rounded-lg shadow p-4">
      <div class="flex gap-4">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Buscar exercícios..."
          class="flex-1 px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <select
          v-model="filterCategory"
          class="px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="">Todas as categorias</option>
          <option value="strength">Força</option>
          <option value="flexibility">Flexibilidade</option>
          <option value="balance">Equilíbrio</option>
          <option value="cardio">Cardio</option>
        </select>
      </div>
    </div>

    <!-- Lista de Exercícios -->
    <div v-if="loading" class="text-center py-8">
      <p class="text-gray-500">Carregando exercícios...</p>
    </div>

    <div v-else-if="filteredExercises.length === 0" class="text-center py-8">
      <p class="text-gray-500">Nenhum exercício encontrado</p>
    </div>

    <div v-else class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      <Card
        v-for="exercise in filteredExercises"
        :key="exercise.id"
        class="hover:shadow-lg transition-shadow"
      >
        <div class="flex justify-between items-start mb-2">
          <h3 class="text-lg font-semibold">{{ exercise.name }}</h3>
          <span class="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded">
            {{ exercise.category }}
          </span>
        </div>
        <p class="text-gray-600 text-sm mt-2 line-clamp-3">
          {{ exercise.description }}
        </p>
        <div class="mt-4 space-y-2 text-sm">
          <div class="flex justify-between">
            <span class="text-gray-600">Séries:</span>
            <span class="font-medium">{{ exercise.sets }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-600">Repetições:</span>
            <span class="font-medium">{{ exercise.repetitions }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-600">Duração:</span>
            <span class="font-medium">{{ exercise.duration }}s</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-600">Frequência:</span>
            <span class="font-medium">{{ exercise.frequency }}</span>
          </div>
        </div>
        <div v-if="authStore.isClinician" class="mt-4 flex gap-2">
          <button
            @click="editExercise(exercise)"
            class="flex-1 px-3 py-2 text-sm bg-gray-100 hover:bg-gray-200 rounded"
          >
            Editar
          </button>
          <button
            @click="deleteExercise(exercise.id)"
            class="flex-1 px-3 py-2 text-sm bg-red-100 text-red-700 hover:bg-red-200 rounded"
          >
            Excluir
          </button>
        </div>
      </Card>
    </div>

    <!-- Modal de Criação/Edição -->
    <UIBaseModal v-if="showCreateModal" @close="closeModal">
      <div class="p-6">
        <h2 class="text-2xl font-bold mb-4">
          {{ editingExercise ? "Editar Exercício" : "Novo Exercício" }}
        </h2>
        <ExerciseForm
          :initial-data="editingExercise"
          @submit="handleSubmit"
          @cancel="closeModal"
        />
      </div>
    </UIBaseModal>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from "~/store/auth";

const authStore = useAuthStore();
const { $api } = useNuxtApp();

const showCreateModal = ref(false);
const editingExercise = ref<any>(null);
const exercises = ref<any[]>([]);
const loading = ref(false);
const searchQuery = ref("");
const filterCategory = ref("");

definePageMeta({
  middleware: ["auth-only"],
  layout: "default",
});

const filteredExercises = computed(() => {
  let result = exercises.value;

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(
      (ex) =>
        ex.name.toLowerCase().includes(query) ||
        ex.description.toLowerCase().includes(query)
    );
  }

  if (filterCategory.value) {
    result = result.filter((ex) => ex.category === filterCategory.value);
  }

  return result;
});

async function fetchExercises() {
  loading.value = true;
  try {
    // TODO: Implementar endpoint de exercícios quando disponível
    // Por enquanto, dados mockados
    exercises.value = [
      {
        id: "1",
        name: "Alongamento de Quadríceps",
        description: "Em pé, segurar o pé e puxar em direção aos glúteos",
        category: "flexibility",
        sets: 3,
        repetitions: 15,
        duration: 30,
        frequency: "3x/semana",
      },
      {
        id: "2",
        name: "Agachamento",
        description: "Exercício para fortalecimento de membros inferiores",
        category: "strength",
        sets: 3,
        repetitions: 12,
        duration: 45,
        frequency: "3x/semana",
      },
    ];
  } catch (error) {
    console.error("Erro ao buscar exercícios", error);
  } finally {
    loading.value = false;
  }
}

function editExercise(exercise: any) {
  editingExercise.value = { ...exercise };
  showCreateModal.value = true;
}

async function deleteExercise(id: string) {
  if (!confirm("Tem certeza que deseja excluir este exercício?")) return;

  try {
    // TODO: Implementar exclusão quando API estiver disponível
    exercises.value = exercises.value.filter((ex) => ex.id !== id);
  } catch (error) {
    console.error("Erro ao excluir exercício", error);
  }
}

async function handleSubmit(data: any) {
  try {
    if (editingExercise.value) {
      // Atualizar
      const index = exercises.value.findIndex(
        (ex) => ex.id === editingExercise.value.id
      );
      if (index !== -1) {
        exercises.value[index] = { ...editingExercise.value, ...data };
      }
    } else {
      // Criar
      exercises.value.push({
        id: Date.now().toString(),
        ...data,
      });
    }
    closeModal();
  } catch (error) {
    console.error("Erro ao salvar exercício", error);
  }
}

function closeModal() {
  showCreateModal.value = false;
  editingExercise.value = null;
}

onMounted(() => {
  fetchExercises();
});
</script>
