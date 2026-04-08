import "./App.css";
import { Route, Routes } from "react-router-dom";
import Layout from "./layout/Layout";
import Login from "./components/Login";
import Register from "./components/Register";
import LinkPage from "./components/testing/LinkPage";
import Unauthorized from "./components/testing/Unauthorised";
import Editor from "./components/testing/Editor";
import RequireAuth from "./components/RequireAuth";
import Admin from "./components/testing/Admin";
import Lounge from "./components/testing/Lounge";
import Missing from "./components/testing/Missing";
import Home from "./components/testing/Home";
import AnalystDashboard from "./components/Dashboard/AnalystDashboard";

// Standard Spring Security roles usually come with the 'ROLE_' prefix.
// Your backend log showed: "roles":["ROLE_USER"]
const ROLES = {
    'User': 'ROLE_USER',
    'Editor': 'ROLE_EDITOR',
    'Admin': 'ROLE_ADMIN',
    'Analyst': 'ROLE_SECURITY_ANALYST'
    
};

function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        {/* public routes */}
        <Route path="login" element={<Login />} />
        <Route path="register" element={<Register />} />
        <Route path="linkpage" element={<LinkPage />} />
        <Route path="unauthorized" element={<Unauthorized />} />

        {/* Protected Routes */}
        {/* Home is accessible by anyone with a valid role */}
        <Route element={<RequireAuth allowedRoles={[ROLES.User, ROLES.Editor, ROLES.Admin]} />}>
          <Route path="/" element={<Home />} />
        </Route>

        <Route element={<RequireAuth allowedRoles={[ROLES.Editor]} />}>
          <Route path="editor" element={<Editor />} />
        </Route>

        <Route element={<RequireAuth allowedRoles={[ROLES.Admin]} />}>
          <Route path="admin" element={<Admin />} />
        </Route>

        <Route element={<RequireAuth allowedRoles={[ROLES.Analyst]} />}>
          <Route path="lounge" element={<Lounge />} />
          <Route path="security_analyst" element={<AnalystDashboard />} />
        </Route>

        {/* catch all */}
        <Route path="*" element={<Missing />} />
      </Route>
    </Routes>
  );
}

export default App;