import { useEffect, useState } from "react";

import dashboardApi from "../../../api/dashboardApi";

import AdminOrders from "../../../components/admin/dashboard/order/AdminOrders";
import AdminProducts from "../../../components/admin/dashboard/product/AdminProducts";
import AdminOverview from "../../../components/admin/dashboard/overview/AdminOverview";
import AdminUsers from "../../../components/admin/dashboard/user/AdminUsers";
import AdminCategories from "../../../components/admin/dashboard/category/AdminCategories";

import "./AdminDashboardPage.css";

function AdminDashboardPage() {
  const [activeSection, setActiveSection] = useState("overview");
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    const loadDashboard = async () => {
      try {
        setLoading(true);
        setError("");
        const response = await dashboardApi.getAdminDashboard();
        const apiResponse = response.data;

        if (!apiResponse?.success || !apiResponse?.data) {
          throw new Error(
            apiResponse?.message ?? "Không thể tải dữ liệu dashboard.",
          );
        }

        if (!cancelled) {
          setDashboard(apiResponse.data);
        }
      } catch (err) {
        console.error("Unable to load admin dashboard:", err);

        if (!cancelled) {
          setError(
            err.response?.data?.message ??
              err.message ??
              "Không thể tải dữ liệu dashboard.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    loadDashboard();

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div className="admin-dashboard">
      <aside className="admin-dashboard__sidebar">
        <nav className="admin-dashboard__nav">
          <button
            type="button"
            className={activeSection === "overview" ? "active" : ""}
            onClick={() => setActiveSection("overview")}
          >
            Tổng quan
          </button>

          <button
            type="button"
            className={activeSection === "categories" ? "active" : ""}
            onClick={() => setActiveSection("categories")}
          >
            Danh mục
          </button>

          <button
            type="button"
            className={activeSection === "products" ? "active" : ""}
            onClick={() => setActiveSection("products")}
          >
            Sản phẩm
          </button>

          <button
            type="button"
            className={activeSection === "users" ? "active" : ""}
            onClick={() => setActiveSection("users")}
          >
            Người dùng
          </button>

          <button
            type="button"
            className={activeSection === "orders" ? "active" : ""}
            onClick={() => setActiveSection("orders")}
          >
            Đơn hàng
          </button>
        </nav>
      </aside>

      <main className="admin-dashboard__content">
        {activeSection === "overview" && (
          <AdminOverview
            dashboard={dashboard}
            loading={loading}
            error={error}
          />
        )}

        {activeSection === "categories" && <AdminCategories />}

        {activeSection === "products" && (
          <AdminProducts dashboard={dashboard} />
        )}

        {activeSection === "users" && <AdminUsers />}

        {activeSection === "orders" && <AdminOrders />}
      </main>
    </div>
  );
}

export default AdminDashboardPage;
