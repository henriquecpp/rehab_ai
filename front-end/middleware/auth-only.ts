// front-end/middleware/auth-only.ts
export default defineNuxtRouteMiddleware((to, from) => {
  const authStore = useAuthStore();

  if (!authStore.isAuthenticated) {
    console.warn(
      "AUTH-ONLY: Usuário não autenticado. Redirecionando para /login."
    );
    return navigateTo("/login", { replace: true });
  }
});
