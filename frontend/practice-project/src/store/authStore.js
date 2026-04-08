import { create } from "zustand"
import { persist } from "zustand/middleware"

export const useAuthStore = create(
  persist(
    (set) => ({
      user: null,
      token: null,
      refreshToken: null,
      userId: null,
      username: null,
      userRole: null,

      setUser: (userData) =>
        set((state) => ({
          ...state,
          user: userData,
          userId: userData?.userId,
          username: userData?.username,
          userRole: userData?.userRole,
        })),

      setTokens: (token, refreshToken) =>
        set({
          token,
          refreshToken,
        }),

      logout: () =>
        set({
          user: null,
          token: null,
          refreshToken: null,
          userId: null,
          username: null,
          userRole: null,
        }),
    }),
    {
      name: "auth-store",
      storage: typeof window !== "undefined" ? localStorage : undefined,
    },
  ),
)
