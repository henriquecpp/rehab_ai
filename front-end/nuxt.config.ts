// nuxt.config.ts
export default defineNuxtConfig({
  devtools: { enabled: true },

  // Registra os módulos que instalamos
  modules: [
    '@nuxtjs/tailwindcss',
    '@pinia/nuxt',
  ],

  // Adiciona o CSS global
  css: [
    '~/assets/css/main.css'
  ],

  // CRÍTICO: Expõe a URL base da API para o app
  runtimeConfig: {
    public: {
      // Pega a URL base da sua documentação
      apiBaseUrl: process.env.API_BASE_URL || 'http://localhost:8080'
    }
  },

  // Configuração do Pinia (para persistir o token)
  pinia: {
    storesDirs: ['./store/**'],
  },
})