import { useNavigate } from "react-router-dom";
import { useContext, useEffect } from "react";
import AuthContext from "../../context/AuthProvider";
import { jwtDecode } from "jwt-decode"; // Ensure this is installed: npm install jwt-decode
import '../../styles/Home.css'

const Home = () => {
    const { auth, setAuth } = useContext(AuthContext);
    const navigate = useNavigate();

    useEffect(() => {
        // Decode the token to check roles
        const decoded = auth?.accessToken ? jwtDecode(auth.accessToken) : null;
        const roles = decoded?.roles || [];

        if (roles.includes("ROLE_SYSTEM_ADMIN")) {
            // Redirect to the separate Spring Boot Admin Server
            window.location.href = "http://localhost:9000";
        } else if (roles.includes("ROLE_SECURITY_ANALYST")) {
            // Navigate to the internal React Analyst Dashboard
            navigate("/security_analyst");
        }
    }, [auth, navigate]);

    const logout = async () => {
        setAuth({});
        navigate('/login');
    };

   return (
  <div className="home-page">
    <div className="home-card">
      <section>
        <h1>Role not assigned yet</h1>
        <p>Please contact the backend team.</p>
        <button onClick={logout}>Sign Out</button>
      </section>
    </div>
  </div>
);
};

export default Home;