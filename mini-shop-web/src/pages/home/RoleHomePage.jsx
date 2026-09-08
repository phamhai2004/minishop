import { useAuth } from "../../contexts/AuthContext";

import ROLES from "../../components/constants/roles";

import HomePage from "./HomePage";
import SellerDashboardPage from "../seller/dashboard/SellerDashboardPage";
import AdminDashboardPage from "../admin/dashboard/AdminDashboardPage";

function RoleHomePage() {
  const { currentUser, isAuthenticated, authReady } = useAuth();

  if (!authReady) {
    return (
      <main
        style={{
          padding: "40px 20px",
          textAlign: "center",
        }}
      >
        Đang tải...
      </main>
    );
  }

  if (!isAuthenticated) {
    return <HomePage />;
  }

  switch (currentUser?.role) {
    case ROLES.SELLER:
      return <SellerDashboardPage />;

    case ROLES.ADMIN:
      return <AdminDashboardPage />;

    case ROLES.CUSTOMER:
    default:
      return <HomePage />;
  }
}

export default RoleHomePage;
