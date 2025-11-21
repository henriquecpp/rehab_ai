<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-3xl font-bold">Gerenciamento de Consentimentos</h1>
      <p class="text-gray-600 mt-2">
        Gerencie suas permissões de uso de dados (LGPD/GDPR)
      </p>
    </div>

    <!-- Aviso Importante -->
    <Card class="bg-blue-50 border-l-4 border-blue-500">
      <div class="flex gap-3">
        <svg
          class="w-6 h-6 text-blue-500 flex-shrink-0"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
        <div>
          <h3 class="font-semibold text-blue-900">
            Seus Direitos de Privacidade
          </h3>
          <p class="text-sm text-blue-800 mt-1">
            Você tem controle total sobre como seus dados são usados. Pode
            conceder ou revogar consentimentos a qualquer momento. A revogação
            não afeta processamentos já realizados.
          </p>
        </div>
      </div>
    </Card>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-8">
      <p class="text-gray-500">Carregando consentimentos...</p>
    </div>

    <!-- Lista de Consentimentos -->
    <div v-else class="grid gap-4">
      <Card
        v-for="consent in consents"
        :key="consent.type"
        class="hover:shadow-lg transition-shadow"
      >
        <div class="flex justify-between items-start gap-4">
          <div class="flex-1">
            <div class="flex items-start gap-3">
              <!-- Ícone -->
              <div
                :class="[
                  'w-12 h-12 rounded-full flex items-center justify-center flex-shrink-0',
                  consent.granted ? 'bg-green-100' : 'bg-gray-100',
                ]"
              >
                <svg
                  v-if="consent.granted"
                  class="w-6 h-6 text-green-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M5 13l4 4L19 7"
                  />
                </svg>
                <svg
                  v-else
                  class="w-6 h-6 text-gray-600"
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
              </div>

              <!-- Conteúdo -->
              <div class="flex-1">
                <h3 class="font-semibold text-lg text-gray-900">
                  {{ consentLabels[consent.type] }}
                </h3>
                <p class="text-sm text-gray-600 mt-1">
                  {{ consentDescriptions[consent.type] }}
                </p>

                <!-- Detalhes -->
                <div class="mt-3 space-y-1 text-xs text-gray-500">
                  <p>
                    <span class="font-medium">Status:</span>
                    <span
                      :class="[
                        'ml-2 px-2 py-0.5 rounded',
                        consent.granted
                          ? 'bg-green-100 text-green-800'
                          : 'bg-gray-100 text-gray-800',
                      ]"
                    >
                      {{ consent.granted ? "Concedido" : "Revogado" }}
                    </span>
                  </p>
                  <p>
                    <span class="font-medium">Última atualização:</span>
                    {{ formatDateTime(consent.timestamp) }}
                  </p>
                  <p v-if="consent.purpose">
                    <span class="font-medium">Finalidade:</span>
                    {{ consent.purpose }}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- Ação -->
          <div class="flex flex-col gap-2">
            <button
              @click="toggleConsent(consent)"
              :disabled="processingConsent === consent.type"
              :class="[
                'px-4 py-2 rounded-lg font-medium transition whitespace-nowrap',
                consent.granted
                  ? 'bg-red-100 text-red-700 hover:bg-red-200 disabled:bg-red-50'
                  : 'bg-green-100 text-green-700 hover:bg-green-200 disabled:bg-green-50',
                'disabled:cursor-not-allowed disabled:opacity-50',
              ]"
            >
              {{
                processingConsent === consent.type
                  ? "Processando..."
                  : consent.granted
                  ? "Revogar"
                  : "Conceder"
              }}
            </button>

            <button
              @click="showDetails(consent)"
              class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-lg transition"
            >
              Ver detalhes
            </button>
          </div>
        </div>
      </Card>
    </div>

    <!-- Estado Vazio -->
    <div
      v-if="!loading && consents.length === 0"
      class="text-center py-12 bg-gray-50 rounded-lg"
    >
      <svg
        class="w-16 h-16 mx-auto text-gray-400"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
        />
      </svg>
      <p class="text-gray-500 mt-4">Nenhum consentimento registrado</p>
      <button
        @click="initializeConsents"
        class="mt-4 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
      >
        Inicializar Consentimentos
      </button>
    </div>

    <!-- Modal de Detalhes -->
    <UIBaseModal v-if="selectedConsent" @close="selectedConsent = null">
      <div class="p-6">
        <h2 class="text-2xl font-bold mb-4">
          {{ consentLabels[selectedConsent.type] }}
        </h2>

        <div class="space-y-4">
          <div>
            <h3 class="font-semibold text-gray-900 mb-2">Descrição Completa</h3>
            <p class="text-gray-700">
              {{ consentDescriptions[selectedConsent.type] }}
            </p>
          </div>

          <div>
            <h3 class="font-semibold text-gray-900 mb-2">
              Como seus dados são usados
            </h3>
            <ul class="list-disc list-inside text-gray-700 space-y-1">
              <li
                v-for="usage in consentUsages[selectedConsent.type]"
                :key="usage"
              >
                {{ usage }}
              </li>
            </ul>
          </div>

          <div>
            <h3 class="font-semibold text-gray-900 mb-2">Informações Legais</h3>
            <p class="text-sm text-gray-600">
              Base legal: {{ consentLegalBasis[selectedConsent.type] }}
            </p>
            <p class="text-sm text-gray-600 mt-1">
              Você pode revogar este consentimento a qualquer momento sem
              justificativa.
            </p>
          </div>

          <div class="pt-4 border-t">
            <button
              @click="selectedConsent = null"
              class="w-full px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded-lg"
            >
              Fechar
            </button>
          </div>
        </div>
      </div>
    </UIBaseModal>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from "~/store/auth";
import { $api } from "~/utils/api";

const authStore = useAuthStore();

const consents = ref<any[]>([]);
const loading = ref(false);
const processingConsent = ref<string | null>(null);
const selectedConsent = ref<any>(null);

const consentLabels: Record<string, string> = {
  data_processing: "📊 Processamento de Dados",
  data_sharing: "🤝 Compartilhamento de Dados",
  marketing: "📧 Comunicações de Marketing",
  analytics: "📈 Análise de Dados",
};

const consentDescriptions: Record<string, string> = {
  data_processing:
    "Permitir o processamento de seus dados pessoais e de saúde para fins de tratamento, reabilitação e acompanhamento clínico.",
  data_sharing:
    "Compartilhar seus dados com profissionais de saúde autorizados (fisioterapeutas, médicos) envolvidos no seu tratamento.",
  marketing:
    "Receber comunicações sobre novos serviços, funcionalidades e conteúdo educacional relacionado à sua reabilitação.",
  analytics:
    "Usar seus dados de forma anonimizada para análises estatísticas, pesquisas e melhoria da plataforma.",
};

const consentUsages: Record<string, string[]> = {
  data_processing: [
    "Criar e atualizar seu perfil médico",
    "Registrar histórico clínico e evolução",
    "Gerar planos de tratamento personalizados",
    "Acompanhar progresso e resultados",
  ],
  data_sharing: [
    "Acesso de fisioterapeutas aos seus dados",
    "Compartilhamento com médicos referenciadores",
    "Troca de informações entre profissionais da equipe",
    "Relatórios para convênios (quando aplicável)",
  ],
  marketing: [
    "Envio de newsletters educacionais",
    "Notificações sobre novos recursos",
    "Ofertas de serviços complementares",
    "Pesquisas de satisfação",
  ],
  analytics: [
    "Análise de efetividade de tratamentos",
    "Estudos de melhoria da plataforma",
    "Estatísticas agregadas e anonimizadas",
    "Pesquisas científicas (dados anonimizados)",
  ],
};

const consentLegalBasis: Record<string, string> = {
  data_processing: "Art. 7º, I - Consentimento do titular (LGPD)",
  data_sharing:
    "Art. 7º, I e Art. 11, II, f - Consentimento e tutela da saúde (LGPD)",
  marketing: "Art. 7º, I - Consentimento do titular (LGPD)",
  analytics: "Art. 7º, I e IX - Consentimento e estudos (LGPD)",
};

definePageMeta({
  middleware: ["auth-only"],
  layout: "default",
});

async function fetchConsents() {
  loading.value = true;
  try {
    const data = await $api<any[]>("/users/me/consents");
    consents.value = data;
  } catch (error) {
    console.error("Erro ao buscar consentimentos", error);
  } finally {
    loading.value = false;
  }
}

async function toggleConsent(consent: any) {
  if (
    !confirm(
      consent.granted
        ? `Tem certeza que deseja REVOGAR o consentimento de "${
            consentLabels[consent.type]
          }"?`
        : `Deseja CONCEDER o consentimento de "${consentLabels[consent.type]}"?`
    )
  )
    return;

  processingConsent.value = consent.type;

  try {
    if (consent.granted) {
      await $api("/users/me/consents/revoke", {
        method: "POST",
        body: { type: consent.type },
      });
    } else {
      await $api("/users/me/consents", {
        method: "POST",
        body: {
          type: consent.type,
          granted: true,
          purpose: consentDescriptions[consent.type],
        },
      });
    }
    await fetchConsents();
  } catch (error) {
    console.error("Erro ao atualizar consentimento", error);
    alert("Erro ao atualizar consentimento. Tente novamente.");
  } finally {
    processingConsent.value = null;
  }
}

async function initializeConsents() {
  loading.value = true;
  try {
    // Criar consentimentos padrão
    for (const type of Object.keys(consentLabels)) {
      await $api("/users/me/consents", {
        method: "POST",
        body: {
          type,
          granted: type === "data_processing" || type === "data_sharing", // Padrão: essenciais concedidos
          purpose: consentDescriptions[type],
        },
      });
    }
    await fetchConsents();
  } catch (error) {
    console.error("Erro ao inicializar consentimentos", error);
  } finally {
    loading.value = false;
  }
}

function showDetails(consent: any) {
  selectedConsent.value = consent;
}

function formatDateTime(date: string) {
  return new Date(date).toLocaleString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

onMounted(() => {
  fetchConsents();
});
</script>
