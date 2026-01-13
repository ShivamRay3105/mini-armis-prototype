import { useNavigate } from "react-router-dom"
import { useAuth } from "../context/AuthContext"
import { authService } from "../services/authService"
import "./DashboardPage.css"

export default function DashboardPage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = async () => {
    try {
      await authService.logout()
      logout()
      navigate("/login")
    } catch (error) {
      console.error("Logout error:", error)
      // Still logout locally even if API call fails
      logout()
      navigate("/login")
    }
  }

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div className="header-content">
          <h1 className="app-title">Dashboard</h1>
          <button onClick={handleLogout} className="logout-button">
            Logout
          </button>
        </div>
      </header>

      <main className="dashboard-main">
        <div className="welcome-section">
          <h2>Welcome, {user?.name || user?.email || "User"}!</h2>
          <p>You are now logged in to your account.</p>
        </div>

        <div className="dashboard-content">
          <div className="card">
            <h3>User Information</h3>
            <div className="info-group">
              <p>
                <strong>Email:</strong> {user?.email}
              </p>
              <p>
                <strong>Name:</strong> {user?.name || "Not provided"}
              </p>
            </div>
          </div>

          <div className="card">
            <h3>Quick Actions</h3>
            <ul className="action-list">
              <li>Edit Profile</li>
              <li>Change Password</li>
              <li>View Activity</li>
              <li>Settings</li>
            </ul>
          </div>

          <div className="card">
            <h3>Account Status</h3>
            <p className="status-text">
              Status: <span className="status-badge">Active</span>
            </p>
            <p className="status-text">
              Session: <span className="session-badge">Secure</span>
            </p>
          </div>
        </div>
      </main>
    </div>
  )
}
