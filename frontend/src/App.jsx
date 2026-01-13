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

// const ROLES = { User: 2001, Editor: 1984, Admin: 5150 };

function App() {
  return (
     <Routes>
      <Route path="/" element={<Layout />}>
        {/* public routes */}
        <Route path="login" element={<Login />} />
        <Route path="register" element={<Register />} />
        <Route path="linkpage" element={<LinkPage />} />
        <Route path="unauthorized" element={<Unauthorized />} />

        {/* we want to protect these routes */}
        <Route element={<RequireAuth allowedRoles={['USER','EDITOR','ADMIN']} />}>
          <Route path="/" element={<Home />} />
        </Route>

        <Route element={<RequireAuth allowedRoles={['EDITOR']} />}>
          <Route path="editor" element={<Editor />} />
        </Route>

        <Route element={<RequireAuth allowedRoles={['ADMIN']} />}>
          <Route path="admin" element={<Admin />} />
        </Route>

        <Route element={<RequireAuth allowedRoles={['EDITOR', 'ADMIN']} />}>
          <Route path="lounge" element={<Lounge />} />
        </Route>

        {/* catch all */}
        <Route path="*" element={<Missing />} />
      </Route>
    </Routes>
  );
}

export default App;
