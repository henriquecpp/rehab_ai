// middleware/0.auth.global.ts
export default defineNuxtRouteMiddleware(async (to, from) => {
  const authStore = useAuthStore();
  
  const token = useCookie('auth-token');

  if (token.value && !authStore.user) {
    await authStore.initAuth();
  }
});