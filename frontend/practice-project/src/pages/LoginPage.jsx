import { useState } from "react"
import { useNavigate, Link } from "react-router-dom"
import { useAuth } from "../context/AuthContext"
import { authService } from "../services/authService"
import "./LoginPage.css"

export default function LoginPage() {
  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(false)

  const { login } = useAuth()
  const navigate = useNavigate()

  const handleLogin = async (e) => {
    e.preventDefault();
  
    try {
      const response = await authService.login(username, password);
  
      // 🔑 Tokens come from headers
      const accessToken =
        response.headers.get("Authorization")?.replace("Bearer ", "");
      const refreshToken =
        response.headers.get("X-Refresh-Token");
  
      if (!accessToken || !refreshToken) {
        throw new Error("Missing tokens from server");
      }
  
      // 🔐 Save tokens
      localStorage.setItem("accessToken", accessToken);
      localStorage.setItem("refreshToken", refreshToken);
  
      // 🔑 USER INFO (BODY)
      const user = await response.json();
  
      // ⬅️ THIS LINE YOU ASKED ABOUT
      localStorage.setItem("userId", user.id);
  
      // optional
      localStorage.setItem("username", user.username);
  
      // navigate("/dashboard");
    } catch (err) {
      console.error("Login failed:", err.message);
    }
  };
  
  return (
    <div className="login-container">
      <div className="login-card">
        <h1>Login</h1>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleLogin}>
          <div>
            <label>Username</label>
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={loading}
            />
          </div>

          <div>
            <label>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={loading}
            />
          </div>

          <button type="submit" disabled={loading}>
            {loading ? "Logging in..." : "Login"}
          </button>
        </form>

        <p>
          Don’t have an account? <Link to="/signup">Sign up</Link>
        </p>
      </div>
    </div>
  )
}
