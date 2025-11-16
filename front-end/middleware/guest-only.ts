// middleware/guest-only.ts
import { useAuthStore } from '~/store/auth';

export default defineNuxtRouteMiddleware((to, from) => {
  const authStore = useAuthStore();

  if (authStore.isAuthenticated) {
    console.warn('GUEST-ONLY: Usuário já autenticado. Redirecionando para /.');
    return navigateTo('/', { replace: true });
  }
});