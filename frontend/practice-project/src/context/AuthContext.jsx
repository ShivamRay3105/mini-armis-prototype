
import { createContext, useContext, useState, useCallback, useEffect } from "react"

const AuthContext = createContext(null)

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    const checkAuth = () => {
      const token = localStorage.getItem("accessToken")
      const userStr = localStorage.getItem("user")

      if (token && userStr) {
        try {
          setUser(JSON.parse(userStr))
        } catch (e) {
          console.error("Failed to parse stored user:", e)
          localStorage.removeItem("user")
          localStorage.removeItem("accessToken")
        }
      }
      setLoading(false)
    }

    checkAuth()
  }, [])

  const setTokens = useCallback((accessToken, refreshToken) => {
    // Store tokens in localStorage
    localStorage.setItem("accessToken", accessToken)
    localStorage.setItem("refreshToken", refreshToken)

    // Also set in cookies (for redundancy)
    document.cookie = `accessToken=${accessToken}; path=/; max-age=900`
    document.cookie = `refreshToken=${refreshToken}; path=/; max-age=604800`
  }, [])

  const clearTokens = useCallback(() => {
    localStorage.removeItem("accessToken")
    localStorage.removeItem("refreshToken")
    localStorage.removeItem("user")

    // Clear cookies
    document.cookie = "accessToken=; path=/; max-age=0"
    document.cookie = "refreshToken=; path=/; max-age=0"
  }, [])

  const login = useCallback(
    (userData, accessToken, refreshToken) => {
      setUser(userData)
      localStorage.setItem("user", JSON.stringify(userData))
      setTokens(accessToken, refreshToken)
      setError(null)
    },
    [setTokens],
  )

  const logout = useCallback(() => {
    setUser(null)
    clearTokens()
    setError(null)
  }, [clearTokens])

  const updateError = useCallback((err) => {
    setError(err)
  }, [])

  const value = {
    user,
    loading,
    error,
    login,
    logout,
    setTokens,
    clearTokens,
    updateError,
    isAuthenticated: !!user,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider")
  }
  return context
}
