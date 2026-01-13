const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const AUTH_BASE = `${API_BASE_URL}/auth-service/auth`;

const getAuthHeaders = () => {
  const token = localStorage.getItem("accessToken");
  return {
    "Content-Type": "application/json",
    ...(token && { Authorization: `Bearer ${token}` }),
  };
};

export const authService = {
  // ✅ REGISTER
  register: async (username, password) => {
    try {
      const response = await fetch(`${AUTH_BASE}/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Registration failed");
      }

      return response; // ⬅️ IMPORTANT
    } catch (error) {
      throw error;
    }
  },

  // ✅ LOGIN
  login: async (username, password) => {
    try {
      const response = await fetch(`${AUTH_BASE}/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Login failed");
      }

      return response; // ⬅️ IMPORTANT
    } catch (error) {
      throw error;
    }
  },

  // ✅ REFRESH (HEADERS ONLY)
  refresh: async () => {
    try {
      const refreshToken = localStorage.getItem("refreshToken");
      const userId = localStorage.getItem("userId");

      if (!refreshToken || !userId) {
        throw new Error("Missing refresh token or user ID");
      }

      const response = await fetch(`${AUTH_BASE}/refresh`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-Refresh-Token": refreshToken,
          "X-User-Id": userId,
        },
      });

      if (!response.ok) {
        throw new Error("Token refresh failed");
      }

      return response; // ⬅️ headers again
    } catch (error) {
      throw error;
    }
  },

  // ✅ LOGOUT
  logout: async () => {
    try {
      await fetch(`${AUTH_BASE}/logout`, {
        method: "POST",
        headers: getAuthHeaders(),
      });
    } catch (error) {
      console.error("Logout error:", error);
    }
  },

  // ✅ PROFILE (normal JSON)
  getProfile: async () => {
    try {
      const response = await fetch(
        `${API_BASE_URL}/api/user/profile`,
        {
          method: "GET",
          headers: getAuthHeaders(),
        }
      );

      if (!response.ok) {
        throw new Error("Failed to fetch profile");
      }

      return await response.json();
    } catch (error) {
      throw error;
    }
  },
};
