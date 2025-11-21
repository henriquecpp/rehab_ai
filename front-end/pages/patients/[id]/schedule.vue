<template>
  <UICard>
    <template #title>
      <div class="flex justify-between items-center">
        <span>Agenda do Paciente</span>
        <button
          v-if="authStore.isClinician"
          @click="showCreateModal = true"
          class="btn-primary"
        >
          Novo Agendamento
        </button>
      </div>
    </template>

    <!-- Stats -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
      <div class="stat-card">
        <div class="stat-icon bg-blue-100 text-blue-600">📅</div>
        <div>
          <div class="stat-label">Próximos Agendamentos</div>
          <div class="stat-value">{{ upcomingEvents }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon bg-green-100 text-green-600">✓</div>
        <div>
          <div class="stat-label">Realizados</div>
          <div class="stat-value">{{ completedEvents }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon bg-purple-100 text-purple-600">📊</div>
        <div>
          <div class="stat-label">Total</div>
          <div class="stat-value">{{ events.length }}</div>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="filters-section mb-6">
      <div class="flex flex-col sm:flex-row gap-3">
        <select v-model="statusFilter" class="input-field">
          <option value="">Todos os Status</option>
          <option value="upcoming">Próximos</option>
          <option value="completed">Realizados</option>
          <option value="cancelled">Cancelados</option>
        </select>
        <select v-model="typeFilter" class="input-field">
          <option value="">Todos os Tipos</option>
          <option value="session">Sessão de Terapia</option>
          <option value="appointment">Consulta</option>
          <option value="evaluation">Avaliação</option>
        </select>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p class="mt-2 text-gray-600">Carregando agenda...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="filteredEvents.length === 0" class="empty-state">
      <p class="text-xl mb-2">📆</p>
      <p>Nenhum agendamento encontrado</p>
    </div>

    <!-- Events List -->
    <div v-else class="space-y-3">
      <div v-for="event in filteredEvents" :key="event.id" class="event-card">
        <div class="flex-1">
          <div class="flex items-center gap-2 mb-2">
            <span class="badge" :class="getTypeClass(event.type)">
              {{ eventTypeLabels[event.type] }}
            </span>
            <span class="text-sm font-medium text-gray-900">
              {{ formatDate(event.date) }} - {{ event.time }}
            </span>
            <span
              v-if="event.status"
              class="badge"
              :class="getStatusClass(event.status)"
            >
              {{ statusLabels[event.status] }}
            </span>
          </div>
          <h3 class="font-semibold text-lg">{{ event.title }}</h3>
          <p v-if="event.description" class="text-sm text-gray-500 mt-2">
            {{ event.description }}
          </p>
          <p v-if="event.location" class="text-sm text-gray-600 mt-1">
            📍 {{ event.location }}
          </p>
        </div>
        <div v-if="authStore.isClinician" class="flex gap-2">
          <button
            @click="editEvent(event)"
            class="action-btn text-blue-600 hover:bg-blue-50"
            title="Editar"
          >
            <svg
              class="w-5 h-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
              />
            </svg>
          </button>
          <button
            @click="deleteEvent(event.id)"
            class="action-btn text-red-600 hover:bg-red-50"
            title="Deletar"
          >
            <svg
              class="w-5 h-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
              />
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <UIBaseModal v-if="showCreateModal" @close="closeCreateModal">
      <div class="p-6">
        <h2 class="text-2xl font-bold mb-4">
          {{ editingEvent ? "Editar Agendamento" : "Novo Agendamento" }}
        </h2>
        <form @submit.prevent="handleSubmitEvent" class="space-y-4">
          <div>
            <label class="form-label">Tipo</label>
            <select v-model="eventForm.type" class="input-field" required>
              <option value="session">Sessão de Terapia</option>
              <option value="appointment">Consulta</option>
              <option value="evaluation">Avaliação</option>
            </select>
          </div>

          <div>
            <label class="form-label">Título</label>
            <input
              v-model="eventForm.title"
              type="text"
              class="input-field"
              required
            />
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="form-label">Data</label>
              <input
                v-model="eventForm.date"
                type="date"
                class="input-field"
                required
              />
            </div>
            <div>
              <label class="form-label">Horário</label>
              <input
                v-model="eventForm.time"
                type="time"
                class="input-field"
                required
              />
            </div>
          </div>

          <div>
            <label class="form-label">Local</label>
            <input
              v-model="eventForm.location"
              type="text"
              class="input-field"
              placeholder="Ex: Sala 2, Clínica XYZ"
            />
          </div>

          <div>
            <label class="form-label">Descrição</label>
            <textarea
              v-model="eventForm.description"
              rows="3"
              class="input-field"
              placeholder="Detalhes do agendamento..."
            ></textarea>
          </div>

          <div>
            <label class="form-label">Status</label>
            <select v-model="eventForm.status" class="input-field">
              <option value="upcoming">Próximo</option>
              <option value="completed">Realizado</option>
              <option value="cancelled">Cancelado</option>
            </select>
          </div>

          <div class="flex gap-2 pt-4">
            <button type="submit" class="btn-primary flex-1">
              {{ editingEvent ? "Atualizar" : "Criar" }}
            </button>
            <button
              type="button"
              @click="closeCreateModal"
              class="btn-secondary flex-1"
            >
              Cancelar
            </button>
          </div>
        </form>
      </div>
    </UIBaseModal>
  </UICard>
</template>

<script setup lang="ts">
import { useAuthStore } from "~/store/auth";

definePageMeta({ middleware: "auth-only" });

const authStore = useAuthStore();
const route = useRoute();
const patientId = computed(() => route.params.id as string);

interface ScheduleEvent {
  id: string;
  userId: string;
  type: "session" | "appointment" | "evaluation";
  title: string;
  date: string;
  time: string;
  description?: string;
  location?: string;
  status: "upcoming" | "completed" | "cancelled";
}

const events = ref<ScheduleEvent[]>([]);
const loading = ref(true);
const statusFilter = ref("");
const typeFilter = ref("");
const showCreateModal = ref(false);
const editingEvent = ref<ScheduleEvent | null>(null);

const eventForm = ref({
  type: "session" as "session" | "appointment" | "evaluation",
  title: "",
  date: "",
  time: "",
  description: "",
  location: "",
  status: "upcoming" as "upcoming" | "completed" | "cancelled",
});

const eventTypeLabels: Record<string, string> = {
  session: "Sessão",
  appointment: "Consulta",
  evaluation: "Avaliação",
};

const statusLabels: Record<string, string> = {
  upcoming: "Próximo",
  completed: "Realizado",
  cancelled: "Cancelado",
};

const filteredEvents = computed(() => {
  return events.value.filter((event) => {
    const matchesStatus =
      !statusFilter.value || event.status === statusFilter.value;
    const matchesType = !typeFilter.value || event.type === typeFilter.value;
    return matchesStatus && matchesType;
  });
});

const upcomingEvents = computed(() => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return events.value.filter(
    (e) => e.status === "upcoming" && new Date(e.date) >= today
  ).length;
});

const completedEvents = computed(() => {
  return events.value.filter((e) => e.status === "completed").length;
});

const fetchEvents = async () => {
  loading.value = true;
  try {
    const { data } = await useApiFetch<ScheduleEvent[]>(
      `/schedule/patient/${patientId.value}`,
      {
        method: "GET",
      }
    );
    if (data.value) {
      events.value = data.value.sort((a, b) => {
        const dateA = new Date(`${a.date} ${a.time}`);
        const dateB = new Date(`${b.date} ${b.time}`);
        return dateB.getTime() - dateA.getTime();
      });
    }
  } catch (error) {
    console.error("Erro ao carregar agenda:", error);
    // Use mock data for now
    events.value = [];
  } finally {
    loading.value = false;
  }
};

const handleSubmitEvent = async () => {
  try {
    if (editingEvent.value) {
      // Update
      const { data } = await useApiFetch(`/schedule/${editingEvent.value.id}`, {
        method: "PUT",
        body: {
          ...eventForm.value,
          userId: patientId.value,
        },
      });
      if (data.value) {
        const index = events.value.findIndex(
          (e) => e.id === editingEvent.value!.id
        );
        if (index !== -1) {
          events.value[index] = data.value as ScheduleEvent;
        }
        alert("Agendamento atualizado com sucesso!");
      }
    } else {
      // Create
      const { data } = await useApiFetch<ScheduleEvent>("/schedule", {
        method: "POST",
        body: {
          ...eventForm.value,
          userId: patientId.value,
        },
      });
      if (data.value) {
        events.value.unshift(data.value);
        alert("Agendamento criado com sucesso!");
      }
    }
    closeCreateModal();
    await fetchEvents();
  } catch (error) {
    console.error("Erro ao salvar agendamento:", error);
    alert("Erro ao salvar agendamento");
  }
};

const editEvent = (event: ScheduleEvent) => {
  editingEvent.value = event;
  eventForm.value = {
    type: event.type,
    title: event.title,
    date: event.date,
    time: event.time,
    description: event.description || "",
    location: event.location || "",
    status: event.status,
  };
  showCreateModal.value = true;
};

const deleteEvent = async (id: string) => {
  if (!confirm("Tem certeza que deseja excluir este agendamento?")) return;

  try {
    await useApiFetch(`/schedule/${id}`, {
      method: "DELETE",
    });
    events.value = events.value.filter((e) => e.id !== id);
    alert("Agendamento excluído com sucesso!");
  } catch (error) {
    console.error("Erro ao excluir agendamento:", error);
    alert("Erro ao excluir agendamento");
  }
};

const closeCreateModal = () => {
  showCreateModal.value = false;
  editingEvent.value = null;
  eventForm.value = {
    type: "session",
    title: "",
    date: "",
    time: "",
    description: "",
    location: "",
    status: "upcoming",
  };
};

const getTypeClass = (type: string): string => {
  const classes = {
    session: "bg-blue-100 text-blue-800",
    appointment: "bg-green-100 text-green-800",
    evaluation: "bg-purple-100 text-purple-800",
  };
  return classes[type as keyof typeof classes] || "bg-gray-100 text-gray-800";
};

const getStatusClass = (status: string): string => {
  const classes = {
    upcoming: "bg-yellow-100 text-yellow-800",
    completed: "bg-green-100 text-green-800",
    cancelled: "bg-red-100 text-red-800",
  };
  return classes[status as keyof typeof classes] || "bg-gray-100 text-gray-800";
};

const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  return date.toLocaleDateString("pt-BR", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
};

onMounted(() => {
  fetchEvents();
});
</script>

<style scoped>
.stat-card {
  @apply flex items-center gap-4 bg-white border border-gray-200 rounded-lg p-4;
}
.stat-icon {
  @apply w-12 h-12 flex items-center justify-center rounded-lg text-2xl;
}
.stat-label {
  @apply text-xs text-gray-600 uppercase tracking-wide;
}
.stat-value {
  @apply text-2xl font-bold text-gray-900;
}

.filters-section {
  @apply bg-white border border-gray-200 rounded-lg p-4;
}

.input-field {
  @apply w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent;
}

.loading-state {
  @apply py-10 text-center;
}

.spinner {
  @apply inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600;
}

.empty-state {
  @apply py-10 text-center text-gray-500;
}

.event-card {
  @apply flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3 p-4 rounded-lg border border-gray-200 hover:shadow-md transition-shadow bg-white;
}

.badge {
  @apply px-2 py-1 text-xs rounded font-medium inline-block;
}

.action-btn {
  @apply p-2 rounded transition;
}

.btn-primary {
  @apply px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium transition;
}

.btn-secondary {
  @apply px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded-lg font-medium transition;
}

.form-label {
  @apply block text-sm font-medium text-gray-700 mb-1;
}
</style>
