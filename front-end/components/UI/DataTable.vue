<template>
  <div class="data-table-container">
    <!-- Search Bar -->
    <div v-if="searchable" class="mb-4">
      <input
        v-model="searchQuery"
        type="text"
        :placeholder="searchPlaceholder"
        class="w-full rounded-lg border border-gray-300 px-4 py-2 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
      />
    </div>

    <!-- Table -->
    <div
      class="overflow-x-auto rounded-lg border border-gray-200 bg-white shadow-sm"
    >
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th
              v-for="column in columns"
              :key="column.key"
              scope="col"
              class="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500"
              :class="column.sortable ? 'cursor-pointer hover:bg-gray-100' : ''"
              @click="column.sortable ? handleSort(column.key) : null"
            >
              <div class="flex items-center gap-2">
                <span>{{ column.label }}</span>
                <span
                  v-if="column.sortable && sortKey === column.key"
                  class="text-blue-600"
                >
                  {{ sortOrder === "asc" ? "↑" : "↓" }}
                </span>
              </div>
            </th>
            <th
              v-if="actions.length > 0"
              scope="col"
              class="px-6 py-3 text-right text-xs font-medium uppercase tracking-wider text-gray-500"
            >
              Ações
            </th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200 bg-white">
          <!-- Loading State -->
          <tr v-if="loading">
            <td
              :colspan="columns.length + (actions.length > 0 ? 1 : 0)"
              class="px-6 py-12 text-center"
            >
              <div class="flex items-center justify-center gap-2">
                <div
                  class="h-5 w-5 animate-spin rounded-full border-2 border-gray-300 border-t-blue-600"
                ></div>
                <span class="text-sm text-gray-500">Carregando...</span>
              </div>
            </td>
          </tr>

          <!-- Empty State -->
          <tr v-else-if="filteredData.length === 0">
            <td
              :colspan="columns.length + (actions.length > 0 ? 1 : 0)"
              class="px-6 py-12 text-center"
            >
              <div class="flex flex-col items-center gap-2">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-12 w-12 text-gray-300"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
                  />
                </svg>
                <p class="text-sm font-medium text-gray-900">
                  {{ emptyMessage }}
                </p>
                <p v-if="searchQuery" class="text-xs text-gray-500">
                  Tente ajustar sua busca
                </p>
              </div>
            </td>
          </tr>

          <tr
            v-else
            v-for="(row, index) in filteredData"
            :key="index"
            class="hover:bg-gray-50 transition-colors"
          >
            <td
              v-for="column in columns"
              :key="column.key"
              class="px-6 py-4 whitespace-nowrap text-sm text-gray-900"
            >
              <slot
                :name="`cell-${column.key}`"
                :row="row"
                :value="getNestedValue(row, column.key)"
              >
                {{ formatValue(row, column) }}
              </slot>
            </td>
            <td
              v-if="actions.length > 0"
              class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium"
            >
              <div class="flex items-center justify-end gap-2">
                <button
                  v-for="action in actions"
                  :key="action.label"
                  @click="action.handler(row)"
                  :class="[
                    'rounded-md px-3 py-1 text-xs font-medium transition-colors',
                    action.variant === 'danger'
                      ? 'bg-red-100 text-red-700 hover:bg-red-200'
                      : action.variant === 'success'
                      ? 'bg-green-100 text-green-700 hover:bg-green-200'
                      : action.variant === 'warning'
                      ? 'bg-yellow-100 text-yellow-700 hover:bg-yellow-200'
                      : 'bg-blue-100 text-blue-700 hover:bg-blue-200',
                  ]"
                  :title="action.label"
                >
                  {{ action.label }}
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div
      v-if="paginated && filteredData.length > 0"
      class="mt-4 flex items-center justify-between"
    >
      <div class="text-sm text-gray-700">
        Mostrando <span class="font-medium">{{ startIndex + 1 }}</span> até
        <span class="font-medium">{{
          Math.min(endIndex, filteredData.length)
        }}</span>
        de <span class="font-medium">{{ filteredData.length }}</span> resultados
      </div>
      <div class="flex gap-2">
        <button
          @click="previousPage"
          :disabled="currentPage === 1"
          class="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
        >
          Anterior
        </button>
        <button
          @click="nextPage"
          :disabled="currentPage === totalPages"
          class="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
        >
          Próxima
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";

interface Column {
  key: string;
  label: string;
  sortable?: boolean;
  format?: (value: any) => string;
}

interface Action {
  label: string;
  handler: (row: any) => void;
  variant?: "primary" | "success" | "warning" | "danger";
}

interface Props {
  columns: Column[];
  data: any[];
  actions?: Action[];
  loading?: boolean;
  searchable?: boolean;
  searchPlaceholder?: string;
  emptyMessage?: string;
  paginated?: boolean;
  pageSize?: number;
}

const props = withDefaults(defineProps<Props>(), {
  actions: () => [],
  loading: false,
  searchable: false,
  searchPlaceholder: "Buscar...",
  emptyMessage: "Nenhum dado encontrado",
  paginated: false,
  pageSize: 10,
});

const searchQuery = ref("");
const sortKey = ref<string | null>(null);
const sortOrder = ref<"asc" | "desc">("asc");
const currentPage = ref(1);

function getNestedValue(obj: any, path: string): any {
  return path.split(".").reduce((value, key) => value?.[key], obj);
}

function formatValue(row: any, column: Column): string {
  const value = getNestedValue(row, column.key);
  if (column.format) {
    return column.format(value);
  }
  return value ?? "-";
}

const filteredData = computed(() => {
  let result = [...props.data];

  if (props.searchable && searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter((row) => {
      return props.columns.some((column) => {
        const value = getNestedValue(row, column.key);
        return String(value).toLowerCase().includes(query);
      });
    });
  }

  if (sortKey.value) {
    result.sort((a, b) => {
      const aVal = getNestedValue(a, sortKey.value!);
      const bVal = getNestedValue(b, sortKey.value!);

      if (aVal === bVal) return 0;

      const comparison = aVal < bVal ? -1 : 1;
      return sortOrder.value === "asc" ? comparison : -comparison;
    });
  }

  return result;
});

const totalPages = computed(() => {
  if (!props.paginated) return 1;
  return Math.ceil(filteredData.value.length / props.pageSize);
});

const startIndex = computed(() => {
  if (!props.paginated) return 0;
  return (currentPage.value - 1) * props.pageSize;
});

const endIndex = computed(() => {
  if (!props.paginated) return filteredData.value.length;
  return startIndex.value + props.pageSize;
});

const paginatedData = computed(() => {
  if (!props.paginated) return filteredData.value;
  return filteredData.value.slice(startIndex.value, endIndex.value);
});

function handleSort(key: string) {
  if (sortKey.value === key) {
    sortOrder.value = sortOrder.value === "asc" ? "desc" : "asc";
  } else {
    sortKey.value = key;
    sortOrder.value = "asc";
  }
}

function previousPage() {
  if (currentPage.value > 1) {
    currentPage.value--;
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
}

watch(searchQuery, () => {
  currentPage.value = 1;
});
</script>

<style scoped>
.data-table-container {
  @apply w-full;
}
</style>
