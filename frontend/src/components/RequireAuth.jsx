// import { useLocation, Navigate, Outlet } from "react-router-dom";
// import useAuth from "../hooks/useAuth";

// const RequireAuth = ({ allowedRoles }) => {
//     const { auth } = useAuth();
//     const location = useLocation();

//      if (auth?.accessToken) {
//         if (auth?.roles?.some(role => allowedRoles?.includes(role))) {
//             // User is authenticated and has the correct role, allow access to the protected route
//             return <Outlet />;
//         } else {
//             // User is authenticated but does not have the correct role, redirect to unauthorized page
//             return <Navigate to="/unauthorized" state={{ from: location }} replace />;
//         }
//     } else {
//         // User is not authenticated, redirect to login page and remember the current location
//         return <Navigate to="/login" state={{ from: location }} replace />;
//     }

//     // return (
//     //     auth?.roles?.find(role => allowedRoles?.includes(role))
//     //         ? <Outlet />
//     //         : auth?.user
//     //             ? <Navigate to="/unauthorized" state={{ from: location }} replace />
//     //             : <Navigate to="/login" state={{ from: location }} replace />
//     // );
// }

// export default RequireAuth;

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
