import { Navigate, Outlet } from "react-router-dom";

import { useAuth } from "../contexts/AuthContext";

function GuestRoute() {
  const { isAuthenticated } = useAuth();

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}

export default GuestRoute;
