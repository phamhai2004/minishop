import { Navigate, Outlet, useLocation } from "react-router-dom";

import { useAuth } from "../contexts/AuthContext";

function RequireRole({ allowedRoles }) {
  const { currentUser, isAuthenticated } = useAuth();

  const location = useLocation();

  if (!isAuthenticated) {
    return (
      <Navigate
        to="/login"
        replace
        state={{
          from: location,
        }}
      />
    );
  }

  const userRole = currentUser?.role;

  const hasPermission = Boolean(userRole) && allowedRoles.includes(userRole);

  if (!hasPermission) {
    return (
      <Navigate
        to="/forbidden"
        replace
        state={{
          from: location,
        }}
      />
    );
  }

  return <Outlet />;
}

export default RequireRole;
