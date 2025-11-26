//store/auth.ts
import { defineStore } from "pinia";
import { decodeJwtPayload } from "~/utils/jwt";
import { $api } from "~/utils/api";

interface AuthResponse {
  token: string;
  refreshToken: string;
}

interface UserProfile {
  id: string;
  email: string;
  fullName: string;
  role: "PATIENT" | "CLINICIAN" | "ADMIN";
  active: boolean;
}

export const useAuthStore = defineStore("auth", () => {
  const user = ref<UserProfile | null>(null);
  const cookieOptions = {
    maxAge: 60 * 60 * 24 * 7,
    sameSite: "lax" as const,
    secure: false,
    path: "/",
  };

  const token = useCookie<string | null>("auth-token", cookieOptions);

  const refreshToken = useCookie<string | null>(
    "auth-refresh-token",
    cookieOptions
  );
  const isRefreshing = ref(false);
  const isAuthLoading = ref(true);

  const isAuthenticated = computed(() => !!token.value && !!user.value);
  const isAdmin = computed(() => user.value?.role === "ADMIN");
  const isClinician = computed(
    () => user.value?.role === "CLINICIAN" || isAdmin.value
  );
  const isPatient = computed(() => user.value?.role === "PATIENT");

  async function setLoginData(mainToken: string, newRefreshToken: string) {
    token.value = mainToken;
    refreshToken.value = newRefreshToken;

    const payload = decodeJwtPayload(mainToken);

    if (!payload || !payload.user_id) {
      throw new Error("Invalid JWT payload: missing user_id");
    }

    await fetchUser(payload.user_id);
  }

  async function fetchUser(userId: string) {
    try {
      const headers: Record<string, string> = {};

      if (token.value) {
        headers["Authorization"] = `Bearer ${token.value}`;
      }

      const data = await $api<UserProfile>(`/users/${userId}`, {
        method: "GET",
        headers: headers,
      });

      user.value = data || null;
    } catch (error) {
      console.error("Failed to fetch user data on SSR:", error);
      await performLogout();
    }
  }

  async function performLogout() {
    token.value = null;
    refreshToken.value = null;
    user.value = null;
    isRefreshing.value = false;
  }

  async function login(email: string, password: string) {
    try {
      const response = await $api<AuthResponse>("/auth/login", {
        method: "POST",
        body: { email, password },
      });

      if (!response.token) {
        throw new Error("API response missing 'token'");
      }

      await setLoginData(response.token, response.refreshToken);
      await navigateTo("/");
    } catch (error) {
      console.error("Login failed", error);
      await performLogout();
      throw error;
    }
  }

  async function register(data: {
    email: string;
    password: string;
    fullName: string;
    role: "PATIENT" | "CLINICIAN";
  }) {
    try {
      const response = await $api<AuthResponse>("/auth/register", {
        method: "POST",
        body: data,
      });
      if (!response.token) {
        throw new Error("API response missing 'token'");
      }
      await setLoginData(response.token, response.refreshToken);
      await navigateTo("/");
    } catch (error) {
      console.error("Registration failed", error);
      await performLogout();
      throw error;
    }
  }

  async function logout() {
    if (refreshToken.value) {
      try {
        await $api("/auth/logout", {
          method: "POST",
          body: { refreshToken: refreshToken.value },
        });
      } catch (error) {
        console.error("Failed to revoke refresh token", error);
      }
    }

    await performLogout();
    await navigateTo("/login");
  }

  async function refresh() {
    if (isRefreshing.value) return true;
    isRefreshing.value = true;

    const localRefreshToken = refreshToken.value;

    if (!localRefreshToken) {
      isRefreshing.value = false;
      await performLogout();
      return false;
    }

    try {
      const response = await $api<AuthResponse>("/auth/refresh", {
        method: "POST",
        body: { refreshToken: localRefreshToken },
      });

      if (!response.token) {
        throw new Error("Refresh response missing 'token'");
      }

      token.value = response.token;
      refreshToken.value = response.refreshToken;

      isRefreshing.value = false;
      return true;
    } catch (error) {
      console.error("Token refresh failed", error);
      await performLogout();
      isRefreshing.value = false;
      return false;
    }
  }

  async function initAuth() {
    isAuthLoading.value = true;

    // Verifica se o token existe antes de tentar decodificar
    if (token.value) {
      try {
        const payload = decodeJwtPayload(token.value);
        if (payload && payload.user_id) {
          await fetchUser(payload.user_id);
        } else {
          await performLogout();
        }
      } catch (e) {
        console.error("InitAuth Error:", e);
        await performLogout();
      }
    } else {
      // Se não tem token, garante que o estado está limpo, mas não precisa chamar API de logout
      user.value = null;
    }

    isAuthLoading.value = false;
  }

  return {
    user,
    token,
    refreshToken,
    isRefreshing,
    isAuthLoading,
    isAuthenticated,
    isAdmin,
    isClinician,
    isPatient,
    login,
    register,
    logout,
    refresh,
    initAuth,
  };
});
