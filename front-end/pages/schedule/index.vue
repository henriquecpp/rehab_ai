<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-3xl font-bold">Agenda</h1>
        <p class="text-gray-600 mt-1">{{ currentMonthYear }}</p>
      </div>
      <div class="flex gap-2">
        <button @click="previousMonth" class="p-2 hover:bg-gray-100 rounded-lg">
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
              d="M15 19l-7-7 7-7"
            />
          </svg>
        </button>
        <button @click="nextMonth" class="p-2 hover:bg-gray-100 rounded-lg">
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
              d="M9 5l7 7-7 7"
            />
          </svg>
        </button>
        <button
          @click="goToToday"
          class="px-4 py-2 bg-gray-100 hover:bg-gray-200 rounded-lg font-medium"
        >
          Hoje
        </button>
        <button
          v-if="authStore.isClinician"
          @click="showCreateModal = true"
          class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium"
        >
          Novo Agendamento
        </button>
      </div>
    </div>

    <!-- Calendário -->
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <!-- Cabeçalho dos dias da semana -->
      <div class="grid grid-cols-7 bg-gray-50 border-b">
        <div
          v-for="day in weekDays"
          :key="day"
          class="px-2 py-3 text-center text-sm font-semibold text-gray-700"
        >
          {{ day }}
        </div>
      </div>

      <!-- Dias do mês -->
      <div class="grid grid-cols-7 divide-x divide-y">
        <div
          v-for="(day, index) in calendarDays"
          :key="index"
          :class="[
            'min-h-[120px] p-2 cursor-pointer transition-colors',
            !day.isCurrentMonth && 'bg-gray-50 text-gray-400',
            day.isToday && 'bg-blue-50 ring-2 ring-blue-500 ring-inset',
            day.isSelected && 'bg-blue-100',
            'hover:bg-gray-100',
          ]"
          @click="selectDate(day)"
        >
          <div class="flex justify-between items-start mb-1">
            <span
              :class="[
                'text-sm font-medium',
                day.isToday &&
                  'bg-blue-500 text-white px-2 py-0.5 rounded-full',
              ]"
            >
              {{ day.date.getDate() }}
            </span>
            <span
              v-if="getEventsForDay(day).length"
              class="text-xs text-blue-600 font-medium"
            >
              {{ getEventsForDay(day).length }}
            </span>
          </div>

          <!-- Mini preview de eventos -->
          <div class="space-y-1">
            <div
              v-for="event in getEventsForDay(day).slice(0, 3)"
              :key="event.id"
              :class="[
                'text-xs p-1 rounded truncate',
                event.type === 'session' && 'bg-blue-100 text-blue-800',
                event.type === 'appointment' && 'bg-green-100 text-green-800',
                event.type === 'evaluation' && 'bg-purple-100 text-purple-800',
              ]"
              @click.stop="viewEvent(event)"
            >
              {{ event.time }} - {{ event.title }}
            </div>
            <div
              v-if="getEventsForDay(day).length > 3"
              class="text-xs text-gray-500 text-center"
            >
              +{{ getEventsForDay(day).length - 3 }} mais
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Detalhes do Dia Selecionado -->
    <Card v-if="selectedDay">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-xl font-semibold">
          Agendamentos para {{ formatFullDate(selectedDay.date) }}
        </h2>
        <button
          @click="selectedDay = null"
          class="text-gray-500 hover:text-gray-700"
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
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>
      </div>

      <div
        v-if="getEventsForDay(selectedDay).length === 0"
        class="text-center py-8 text-gray-500"
      >
        Nenhum agendamento para este dia
      </div>

      <div v-else class="space-y-3">
        <div
          v-for="event in getEventsForDay(selectedDay)"
          :key="event.id"
          class="p-4 rounded-lg border hover:shadow-md transition-shadow"
        >
          <div class="flex justify-between items-start">
            <div class="flex-1">
              <div class="flex items-center gap-2 mb-2">
                <span
                  :class="[
                    'px-2 py-1 text-xs rounded font-medium',
                    event.type === 'session' && 'bg-blue-100 text-blue-800',
                    event.type === 'appointment' &&
                      'bg-green-100 text-green-800',
                    event.type === 'evaluation' &&
                      'bg-purple-100 text-purple-800',
                  ]"
                >
                  {{ eventTypeLabels[event.type] }}
                </span>
                <span class="text-sm font-medium text-gray-900">{{
                  event.time
                }}</span>
              </div>
              <h3 class="font-semibold text-lg">{{ event.title }}</h3>
              <p class="text-sm text-gray-600 mt-1">{{ event.patient }}</p>
              <p v-if="event.description" class="text-sm text-gray-500 mt-2">
                {{ event.description }}
              </p>
            </div>
            <div class="flex gap-2">
              <button
                v-if="authStore.isClinician"
                @click="editEvent(event)"
                class="p-2 text-blue-600 hover:bg-blue-50 rounded"
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
                v-if="authStore.isClinician"
                @click="deleteEvent(event.id)"
                class="p-2 text-red-600 hover:bg-red-50 rounded"
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
      </div>
    </Card>

    <!-- Modal de Criar/Editar -->
    <UIBaseModal v-if="showCreateModal" @close="closeCreateModal">
      <div class="p-6">
        <h2 class="text-2xl font-bold mb-4">
          {{ editingEvent ? "Editar Agendamento" : "Novo Agendamento" }}
        </h2>
        <form @submit.prevent="handleSubmitEvent" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >Tipo</label
            >
            <select
              v-model="eventForm.type"
              class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            >
              <option value="session">Sessão de Terapia</option>
              <option value="appointment">Consulta</option>
              <option value="evaluation">Avaliação</option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >Título</label
            >
            <input
              v-model="eventForm.title"
              type="text"
              class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >Paciente</label
            >
            <input
              v-model="eventForm.patient"
              type="text"
              class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1"
                >Data</label
              >
              <input
                v-model="eventForm.date"
                type="date"
                class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1"
                >Horário</label
              >
              <input
                v-model="eventForm.time"
                type="time"
                class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >Descrição</label
            >
            <textarea
              v-model="eventForm.description"
              rows="3"
              class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            ></textarea>
          </div>

          <div class="flex gap-2 pt-4">
            <button
              type="submit"
              class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium"
            >
              {{ editingEvent ? "Atualizar" : "Criar" }}
            </button>
            <button
              type="button"
              @click="closeCreateModal"
              class="flex-1 px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded-lg font-medium"
            >
              Cancelar
            </button>
          </div>
        </form>
      </div>
    </UIBaseModal>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from "~/store/auth";

const authStore = useAuthStore();

const currentDate = ref(new Date());
const selectedDay = ref<any>(null);
const showCreateModal = ref(false);
const editingEvent = ref<any>(null);
const events = ref<any[]>([
  // Dados mockados para demonstração
  {
    id: "1",
    type: "session",
    title: "Fisioterapia - João Silva",
    patient: "João Silva",
    date: new Date().toISOString().split("T")[0],
    time: "09:00",
    description: "Sessão de fortalecimento",
  },
  {
    id: "2",
    type: "appointment",
    title: "Consulta - Maria Santos",
    patient: "Maria Santos",
    date: new Date().toISOString().split("T")[0],
    time: "14:00",
    description: "Avaliação inicial",
  },
]);

const eventForm = ref({
  type: "session",
  title: "",
  patient: "",
  date: "",
  time: "",
  description: "",
});

const weekDays = ["Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"];

const eventTypeLabels: Record<string, string> = {
  session: "Sessão",
  appointment: "Consulta",
  evaluation: "Avaliação",
};

definePageMeta({
  middleware: ["auth-only"],
  layout: "default",
});

const currentMonthYear = computed(() => {
  return currentDate.value.toLocaleDateString("pt-BR", {
    month: "long",
    year: "numeric",
  });
});

const calendarDays = computed(() => {
  const year = currentDate.value.getFullYear();
  const month = currentDate.value.getMonth();
  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const days = [];

  // Dias do mês anterior
  const firstDayOfWeek = firstDay.getDay();
  for (let i = firstDayOfWeek - 1; i >= 0; i--) {
    const date = new Date(year, month, -i);
    days.push({
      date,
      isCurrentMonth: false,
      isToday: date.getTime() === today.getTime(),
      isSelected: selectedDay.value?.date.getTime() === date.getTime(),
    });
  }

  // Dias do mês atual
  for (let i = 1; i <= lastDay.getDate(); i++) {
    const date = new Date(year, month, i);
    days.push({
      date,
      isCurrentMonth: true,
      isToday: date.getTime() === today.getTime(),
      isSelected: selectedDay.value?.date.getTime() === date.getTime(),
    });
  }

  // Dias do próximo mês
  const remainingDays = 42 - days.length; // 6 semanas x 7 dias
  for (let i = 1; i <= remainingDays; i++) {
    const date = new Date(year, month + 1, i);
    days.push({
      date,
      isCurrentMonth: false,
      isToday: date.getTime() === today.getTime(),
      isSelected: selectedDay.value?.date.getTime() === date.getTime(),
    });
  }

  return days;
});

function getEventsForDay(day: any) {
  const dateStr = day.date.toISOString().split("T")[0];
  return events.value.filter((e) => e.date === dateStr);
}

function selectDate(day: any) {
  selectedDay.value = day;
}

function previousMonth() {
  currentDate.value = new Date(
    currentDate.value.getFullYear(),
    currentDate.value.getMonth() - 1,
    1
  );
}

function nextMonth() {
  currentDate.value = new Date(
    currentDate.value.getFullYear(),
    currentDate.value.getMonth() + 1,
    1
  );
}

function goToToday() {
  currentDate.value = new Date();
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  selectedDay.value = {
    date: today,
    isCurrentMonth: true,
    isToday: true,
  };
}

function formatFullDate(date: Date) {
  return date.toLocaleDateString("pt-BR", {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

function viewEvent(event: any) {
  // Implementar visualização detalhada
  console.log("View event:", event);
}

function editEvent(event: any) {
  editingEvent.value = event;
  eventForm.value = { ...event };
  showCreateModal.value = true;
}

function deleteEvent(id: string) {
  if (!confirm("Tem certeza que deseja excluir este agendamento?")) return;
  events.value = events.value.filter((e) => e.id !== id);
}

function handleSubmitEvent() {
  if (editingEvent.value) {
    // Atualizar
    const index = events.value.findIndex((e) => e.id === editingEvent.value.id);
    if (index !== -1) {
      events.value[index] = { ...eventForm.value, id: editingEvent.value.id };
    }
  } else {
    // Criar
    events.value.push({
      ...eventForm.value,
      id: Date.now().toString(),
    });
  }
  closeCreateModal();
}

function closeCreateModal() {
  showCreateModal.value = false;
  editingEvent.value = null;
  eventForm.value = {
    type: "session",
    title: "",
    patient: "",
    date: "",
    time: "",
    description: "",
  };
}

// TODO: Integrar com API quando disponível
// async function fetchEvents() {}
</script>
