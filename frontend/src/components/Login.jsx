import { useRef, useState, useEffect } from "react";
import useAuth from "../hooks/useAuth";
import { Link, useNavigate, useLocation } from "react-router-dom";

import axios from "../api/axios";
const LOGIN_URL = "/auth-service/auth/login";

const Login = () => {
  const { setAuth } = useAuth();

  const navigate = useNavigate();
  const location = useLocation();

  const userRef = useRef();
  const errRef = useRef();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [errMsg, setErrMsg] = useState("");

  useEffect(() => {
    userRef.current.focus();
  }, []);

  useEffect(() => {
    setErrMsg("");
  }, [username, password]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const response = await axios.post(
        LOGIN_URL,
        JSON.stringify({ username, password }),
        {
          headers: { "Content-Type": "application/json" },
          withCredentials: true,
        }
      );
      console.log(JSON.stringify(response?.data));
      //console.log(JSON.stringify(response));
      //   const accessToken = response?.data?.token;
      //   const roles = response?.data?.role;

      // setAuth({ username, password, roles, accessToken });

      //   const accessToken = response.headers["authorization"]?.split(" ")[1];
      //   const refreshToken = response.headers["x-refresh-token"];
      //   const userId = response.headers["x-user-id"];
      //   const roles = response?.data?.role;

      //   setAuth({  userId,username, roles, accessToken, refreshToken });

        const {
          accessToken,
          roles,
          username: resUsername,
        } = response.data;

        setAuth({
          username: resUsername,
          accessToken: accessToken,
          roles: Array.isArray(roles) ? roles : [roles], // <-- ensure roles is always an array
        });


    //   const { token, role, username: resUsername } = response.data;

    //   setAuth({
    //     username: resUsername,
    //     roles: role,
    //     accessToken: token,
    //   });

      setUsername("");
      setPassword("");

      // Get the intended destination from location state (calculated at submit time, not mount time)
      const from = location.state?.from?.pathname || "/";
      console.log("Location state:", location.state);
      console.log("Redirecting to:", from);
      navigate(from, { replace: true });
    } catch (err) {
      if (!err?.response) {
        setErrMsg("No Server Response");
      } else if (err.response?.status === 400) {
        setErrMsg("Missing Username or Password");
      }
      //  else if (err.response?.status === 401) {
      //     setErrMsg('Unauthorized');
      // }
      else {
        setErrMsg("Login Failed");
      }
      errRef.current.focus();
    }
  };

  return (
    <section>
      <p
        ref={errRef}
        className={errMsg ? "errmsg" : "offscreen"}
        aria-live="assertive"
      >
        {errMsg}
      </p>
      <h1>Sign In</h1>
      <form onSubmit={handleSubmit}>
        <label htmlFor="username">Username:</label>
        <input
          type="text"
          id="username"
          ref={userRef}
          autoComplete="off"
          onChange={(e) => setUsername(e.target.value)}
          value={username}
          required
        />

        <label htmlFor="password">Password:</label>
        <input
          type="password"
          id="password"
          onChange={(e) => setPassword(e.target.value)}
          value={password}
          required
        />
        <button>Sign In</button>
      </form>
      <p>
        Need an Account?
        <br />
        <span className="line">
          <Link to="/register">Sign Up</Link>
        </span>
      </p>
    </section>
  );
};

export default Login;
