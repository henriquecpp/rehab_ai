<template>
  <div class="h-[calc(100vh-120px)] flex gap-4">
    <!-- Lista de Conversas -->
    <div class="w-1/3 bg-white rounded-lg shadow flex flex-col">
      <div class="p-4 border-b">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-xl font-semibold">Mensagens</h2>
          <button
            v-if="authStore.isClinician"
            @click="showNewConversationModal = true"
            class="p-2 text-blue-600 hover:bg-blue-50 rounded-lg"
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
                d="M12 4v16m8-8H4"
              />
            </svg>
          </button>
        </div>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Buscar conversas..."
          class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
        />
      </div>

      <div class="flex-1 overflow-y-auto">
        <div
          v-if="filteredConversations.length === 0"
          class="p-8 text-center text-gray-500"
        >
          <svg
            class="w-16 h-16 mx-auto mb-4 text-gray-300"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
            />
          </svg>
          <p>Nenhuma conversa encontrada</p>
        </div>

        <div
          v-for="conversation in filteredConversations"
          :key="conversation.id"
          @click="selectConversation(conversation)"
          :class="[
            'p-4 cursor-pointer border-b hover:bg-gray-50 transition-colors',
            selectedConversation?.id === conversation.id &&
              'bg-blue-50 border-l-4 border-l-blue-500',
          ]"
        >
          <div class="flex items-start gap-3">
            <!-- Avatar -->
            <div
              :class="[
                'w-12 h-12 rounded-full flex items-center justify-center text-white font-semibold flex-shrink-0',
                conversation.online
                  ? 'ring-2 ring-green-500 ring-offset-2'
                  : '',
                'bg-gradient-to-br from-blue-500 to-purple-500',
              ]"
            >
              {{ getInitials(conversation.name) }}
            </div>

            <div class="flex-1 min-w-0">
              <div class="flex justify-between items-start mb-1">
                <h3 class="font-semibold text-gray-900 truncate">
                  {{ conversation.name }}
                </h3>
                <span class="text-xs text-gray-500 flex-shrink-0 ml-2">
                  {{ formatTime(conversation.lastMessageTime) }}
                </span>
              </div>

              <p class="text-sm text-gray-600 mb-1">{{ conversation.role }}</p>

              <div class="flex justify-between items-center">
                <p class="text-sm text-gray-500 truncate flex-1">
                  {{ conversation.lastMessage }}
                </p>
                <span
                  v-if="conversation.unread"
                  class="ml-2 bg-blue-500 text-white text-xs px-2 py-0.5 rounded-full font-medium flex-shrink-0"
                >
                  {{ conversation.unread }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Área de Mensagens -->
    <div class="flex-1 bg-white rounded-lg shadow flex flex-col">
      <div v-if="selectedConversation" class="flex-1 flex flex-col">
        <!-- Header da conversa -->
        <div class="p-4 border-b bg-gray-50">
          <div class="flex justify-between items-center">
            <div class="flex items-center gap-3">
              <div
                class="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-purple-500 flex items-center justify-center text-white font-semibold"
              >
                {{ getInitials(selectedConversation.name) }}
              </div>
              <div>
                <h3 class="font-semibold text-gray-900">
                  {{ selectedConversation.name }}
                </h3>
                <p class="text-sm text-gray-600">
                  {{ selectedConversation.role }}
                  <span
                    v-if="selectedConversation.online"
                    class="text-green-600"
                    >• Online</span
                  >
                </p>
              </div>
            </div>

            <div class="flex gap-2">
              <button
                class="p-2 hover:bg-gray-200 rounded-lg"
                title="Chamada de vídeo"
              >
                <svg
                  class="w-5 h-5 text-gray-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"
                  />
                </svg>
              </button>
              <button
                class="p-2 hover:bg-gray-200 rounded-lg"
                title="Mais opções"
              >
                <svg
                  class="w-5 h-5 text-gray-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z"
                  />
                </svg>
              </button>
            </div>
          </div>
        </div>

        <!-- Área de mensagens -->
        <div
          ref="messagesContainer"
          class="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50"
        >
          <!-- Data Separator -->
          <div class="flex items-center justify-center my-4">
            <span
              class="px-4 py-1 bg-gray-200 rounded-full text-xs text-gray-600"
            >
              Hoje
            </span>
          </div>

          <!-- Mensagens -->
          <div
            v-for="message in messages"
            :key="message.id"
            :class="[
              'flex',
              message.senderId === authStore.user?.id
                ? 'justify-end'
                : 'justify-start',
            ]"
          >
            <div
              :class="[
                'max-w-md px-4 py-3 rounded-2xl shadow-sm',
                message.senderId === authStore.user?.id
                  ? 'bg-blue-500 text-white rounded-br-none'
                  : 'bg-white text-gray-900 rounded-bl-none',
              ]"
            >
              <p class="text-sm whitespace-pre-wrap break-words">
                {{ message.text }}
              </p>
              <div
                :class="[
                  'flex items-center gap-2 mt-2 text-xs',
                  message.senderId === authStore.user?.id
                    ? 'text-blue-100'
                    : 'text-gray-500',
                ]"
              >
                <span>{{ formatMessageTime(message.timestamp) }}</span>
                <svg
                  v-if="message.senderId === authStore.user?.id && message.read"
                  class="w-4 h-4"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path
                    d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                  />
                </svg>
              </div>
            </div>
          </div>

          <!-- Typing indicator -->
          <div v-if="isTyping" class="flex justify-start">
            <div
              class="bg-white px-4 py-3 rounded-2xl rounded-bl-none shadow-sm"
            >
              <div class="flex gap-1">
                <span
                  class="w-2 h-2 bg-gray-400 rounded-full animate-bounce"
                ></span>
                <span
                  class="w-2 h-2 bg-gray-400 rounded-full animate-bounce"
                  style="animation-delay: 0.1s"
                ></span>
                <span
                  class="w-2 h-2 bg-gray-400 rounded-full animate-bounce"
                  style="animation-delay: 0.2s"
                ></span>
              </div>
            </div>
          </div>
        </div>

        <!-- Input de mensagem -->
        <div class="p-4 border-t bg-white">
          <div class="flex gap-2 items-end">
            <button class="p-2 hover:bg-gray-100 rounded-lg text-gray-600">
              <svg
                class="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13"
                />
              </svg>
            </button>

            <textarea
              v-model="newMessage"
              @keydown.enter.exact.prevent="sendMessage"
              @input="handleTyping"
              placeholder="Digite sua mensagem..."
              rows="1"
              class="flex-1 px-4 py-3 border rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none max-h-32"
            ></textarea>

            <button
              @click="sendMessage"
              :disabled="!newMessage.trim()"
              class="p-3 bg-blue-600 text-white rounded-xl hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
            >
              <svg
                class="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"
                />
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- Estado sem conversa selecionada -->
      <div
        v-else
        class="flex-1 flex flex-col items-center justify-center text-gray-500"
      >
        <svg
          class="w-24 h-24 mb-4 text-gray-300"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
          />
        </svg>
        <p class="text-lg">Selecione uma conversa para começar</p>
        <p class="text-sm mt-2">ou inicie uma nova conversa</p>
      </div>
    </div>

    <!-- Modal Nova Conversa -->
    <UIBaseModal
      v-if="showNewConversationModal"
      @close="showNewConversationModal = false"
    >
      <div class="p-6">
        <h2 class="text-2xl font-bold mb-4">Nova Conversa</h2>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >Selecione o paciente</label
            >
            <select
              v-model="newConversationUserId"
              class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Selecione...</option>
              <option
                v-for="patient in availablePatients"
                :key="patient.id"
                :value="patient.id"
              >
                {{ patient.fullName }} ({{ patient.email }})
              </option>
            </select>
          </div>
          <div class="flex gap-2">
            <button
              @click="startNewConversation"
              :disabled="!newConversationUserId"
              class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed"
            >
              Iniciar Conversa
            </button>
            <button
              @click="showNewConversationModal = false"
              class="flex-1 px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded-lg"
            >
              Cancelar
            </button>
          </div>
        </div>
      </div>
    </UIBaseModal>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from "~/store/auth";

const authStore = useAuthStore();

const searchQuery = ref("");
const selectedConversation = ref<any>(null);
const messages = ref<any[]>([]);
const newMessage = ref("");
const isTyping = ref(false);
const messagesContainer = ref<HTMLElement | null>(null);
const showNewConversationModal = ref(false);
const newConversationUserId = ref("");
const availablePatients = ref<any[]>([]);

// Dados mockados
const conversations = ref([
  {
    id: "1",
    name: "João Silva",
    role: "Paciente",
    lastMessage: "Obrigado pela sessão de hoje!",
    lastMessageTime: new Date().toISOString(),
    unread: 2,
    online: true,
  },
  {
    id: "2",
    name: "Maria Santos",
    role: "Paciente",
    lastMessage: "Quando é minha próxima consulta?",
    lastMessageTime: new Date(Date.now() - 3600000).toISOString(),
    unread: 0,
    online: false,
  },
]);

definePageMeta({
  middleware: ["auth-only"],
  layout: "default",
});

const filteredConversations = computed(() => {
  if (!searchQuery.value) return conversations.value;

  const query = searchQuery.value.toLowerCase();
  return conversations.value.filter(
    (c) =>
      c.name.toLowerCase().includes(query) ||
      c.lastMessage.toLowerCase().includes(query)
  );
});

function selectConversation(conversation: any) {
  selectedConversation.value = conversation;
  loadMessages(conversation.id);

  // Marcar como lido
  conversation.unread = 0;
}

function loadMessages(conversationId: string) {
  // Dados mockados
  messages.value = [
    {
      id: "1",
      senderId: selectedConversation.value.id,
      text: "Olá! Tudo bem?",
      timestamp: new Date(Date.now() - 7200000).toISOString(),
      read: true,
    },
    {
      id: "2",
      senderId: authStore.user?.id,
      text: "Olá! Tudo ótimo, e você?",
      timestamp: new Date(Date.now() - 7100000).toISOString(),
      read: true,
    },
    {
      id: "3",
      senderId: selectedConversation.value.id,
      text: "Bem também! Gostaria de agendar uma sessão.",
      timestamp: new Date(Date.now() - 3600000).toISOString(),
      read: true,
    },
  ];

  nextTick(() => {
    scrollToBottom();
  });
}

function sendMessage() {
  if (!newMessage.value.trim()) return;

  const message = {
    id: Date.now().toString(),
    senderId: authStore.user?.id,
    text: newMessage.value,
    timestamp: new Date().toISOString(),
    read: false,
  };

  messages.value.push(message);

  // Atualizar última mensagem na conversa
  if (selectedConversation.value) {
    const conv = conversations.value.find(
      (c) => c.id === selectedConversation.value.id
    );
    if (conv) {
      conv.lastMessage = newMessage.value;
      conv.lastMessageTime = message.timestamp;
    }
  }

  newMessage.value = "";

  nextTick(() => {
    scrollToBottom();
  });

  // TODO: Enviar para API
}

function handleTyping() {
  // TODO: Emitir evento de "digitando" via WebSocket
}

function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
  }
}

function getInitials(name: string) {
  return name
    .split(" ")
    .map((n) => n[0])
    .join("")
    .toUpperCase()
    .slice(0, 2);
}

function formatTime(timestamp: string) {
  const date = new Date(timestamp);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (days === 0) {
    return date.toLocaleTimeString("pt-BR", {
      hour: "2-digit",
      minute: "2-digit",
    });
  } else if (days === 1) {
    return "Ontem";
  } else if (days < 7) {
    return date.toLocaleDateString("pt-BR", { weekday: "short" });
  } else {
    return date.toLocaleDateString("pt-BR", {
      day: "2-digit",
      month: "2-digit",
    });
  }
}

function formatMessageTime(timestamp: string) {
  return new Date(timestamp).toLocaleTimeString("pt-BR", {
    hour: "2-digit",
    minute: "2-digit",
  });
}

async function startNewConversation() {
  // TODO: Implementar criação de conversa
  showNewConversationModal.value = false;
  newConversationUserId.value = "";
}

// TODO: Integrar com WebSocket para mensagens em tempo real
// TODO: Buscar conversas e mensagens da API
</script>
