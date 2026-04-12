import { useLocation, Navigate, Outlet } from "react-router-dom";
import useAuth from "../hooks/useAuth";

const RequireAuth = ({ allowedRoles }) => {
  const { auth } = useAuth();
  const location = useLocation();

  // If auth is still loading, you can show a spinner here
  if (auth === null) return <p>Loading...</p>; // or spinner component

  // Not authenticated, redirect to login
  if (!auth?.accessToken) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Authenticated but check role
  //   if (auth?.roles?.some(role => allowedRoles?.includes(role))) {
  const rolesArray = Array.isArray(auth?.roles)
    ? auth.roles
    : auth?.roles
    ? [auth.roles]
    : [];
  if (rolesArray.some((role) => allowedRoles?.includes(role))) {
    return <Outlet />;
  } else {
    return <Navigate to="/unauthorized" state={{ from: location }} replace />;
  }
};

export default RequireAuth;
