// middleware/0.auth.global.ts
export default defineNuxtRouteMiddleware(async (to, from) => {
  const authStore = useAuthStore();
  if (authStore.isAuthLoading) {
    await authStore.initAuth();
  }
});