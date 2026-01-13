import { useState } from "react"
import { useNavigate, Link } from "react-router-dom"
import { useAuth } from "../context/AuthContext"
import { authService } from "../services/authService"
import "./SignupPage.css"

export default function SignupPage() {
  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(false)

  const { login } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError("")
    setLoading(true)

    try {
      if (!username || !password || !confirmPassword) {
        throw new Error("Please fill in all fields")
      }

      if (password !== confirmPassword) {
        throw new Error("Passwords do not match")
      }

      const response = await authService.register(username, password)

      // ⬇️ READ FROM HEADERS (CRITICAL)
      const accessToken =
        response.headers.get("Authorization")?.replace("Bearer ", "")
      const refreshToken = response.headers.get("X-Refresh-Token")

      const user = {
        id: response.headers.get("X-User-Id"),
        username: response.headers.get("X-Username"),
        role: response.headers.get("X-User-Role"),
      }

      if (!accessToken || !refreshToken) {
        throw new Error("Token missing in response")
      }

      login(user, accessToken, refreshToken)
      navigate("/dashboard")
    } catch (err) {
      setError(err.message || "Registration failed")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="signup-container">
      <div className="signup-card">
        <h1 className="signup-title">Create Account</h1>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit} className="signup-form">
          <div className="form-group">
            <label>Username</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label>Confirm Password</label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              disabled={loading}
            />
          </div>

          <button type="submit" disabled={loading}>
            {loading ? "Creating Account..." : "Sign Up"}
          </button>
        </form>

        <p>
          Already have an account? <Link to="/login">Login</Link>
        </p>
      </div>
    </div>
  )
}


// import { useState } from "react";
// import { useNavigate } from "react-router-dom";
// import { authService } from "../services/authService";

// export default function SignupPage() {
//   const [username, setUsername] = useState("");
//   const [password, setPassword] = useState("");
//   const [error, setError] = useState("");
//   const [loading, setLoading] = useState(false);

//   const navigate = useNavigate();

//   const handleSubmit = async (e) => {
//     e.preventDefault();
//     setError("");
//     setLoading(true);

//     try {
//       if (!username || !password) {
//         throw new Error("Username and password required");
//       }

//       const response = await authService.register(username, password);

//       if (!response.ok) {
//         const msg = await response.text();
//         throw new Error(msg || "Registration failed");
//       }

//       // ✅ DONE. Backend handled it.
//       navigate("/login");

//     } catch (err) {
//       setError(err.message);
//     } finally {
//       setLoading(false);
//     }
//   };

//   return (
//     <form onSubmit={handleSubmit}>
//       <h2>Sign Up</h2>

//       {error && <p style={{ color: "red" }}>{error}</p>}

//       <input
//         type="text"
//         placeholder="Username"
//         value={username}
//         onChange={(e) => setUsername(e.target.value)}
//         disabled={loading}
//       />

//       <input
//         type="password"
//         placeholder="Password"
//         value={password}
//         onChange={(e) => setPassword(e.target.value)}
//         disabled={loading}
//       />

//       <button type="submit" disabled={loading}>
//         {loading ? "Submitting..." : "Register"}
//       </button>
//     </form>
//   );
// }
