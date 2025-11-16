// utils/api.ts
import { ofetch } from "ofetch";
import { useAuthStore } from "~/store/auth";

// Lista de rotas públicas que NUNCA devem receber o token
const publicRoutes = [
  '/auth/login',
  '/auth/register',
  '/auth/refresh'
];

export const $api = ofetch.create({

  onRequest({ options, request }) {
    const config = useRuntimeConfig();
    const authStore = useAuthStore();

    options.baseURL = config.public.apiBaseUrl;
    
    const isPublicRoute = publicRoutes.some(route => 
      String(request).endsWith(route)
    );

    if (!isPublicRoute && authStore.token) {
      const headers = new Headers(options.headers as HeadersInit);
      headers.set('Authorization', `Bearer ${authStore.token}`);
      options.headers = headers;
    }
  },

  async onResponseError({ request, response, options }) {
    const authStore = useAuthStore();

    if (
      response.status !== 401 ||
      String(request).endsWith("/auth/refresh") ||
      authStore.isRefreshing
    ) {
      return; 
    }

    const refreshSuccess = await authStore.refresh();

    if (refreshSuccess) {

      const newHeaders = new Headers(options.headers as HeadersInit);
      newHeaders.set("Authorization", `Bearer ${authStore.token}`);
      options.headers = newHeaders; 
      return;
    }
  },
});