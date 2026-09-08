import { Link, useNavigate } from "react-router-dom";

import { useAuth } from "../../../contexts/AuthContext.jsx";

import "./AdminManagePage.css";

function ManageItem({ icon, title, subtitle, to }) {
  return (
    <Link to={to} className="admin-manage__item">
      <span className="admin-manage__icon">{icon}</span>

      <span className="admin-manage__content">
        <strong>{title}</strong>

        {subtitle && <small>{subtitle}</small>}
      </span>

      <span className="admin-manage__arrow">›</span>
    </Link>
  );
}

function AdminManagePage() {
  const navigate = useNavigate();

  const { currentUser, logout } = useAuth();

  const displayName =
    currentUser?.fullName || currentUser?.email || "Quản trị viên";

  const handleLogout = async () => {
    await logout();

    navigate("/login", {
      replace: true,
    });
  };

  return (
    <main className="admin-manage">
      <header className="admin-manage__header">
        <div className="admin-manage__avatar">
          {currentUser?.avatarUrl ? (
            <img src={currentUser.avatarUrl} alt={displayName} />
          ) : (
            <span>{displayName.charAt(0).toUpperCase()}</span>
          )}
        </div>

        <div className="admin-manage__identity">
          <h1>{displayName}</h1>

          <p>{currentUser?.email}</p>
        </div>
      </header>

      <section className="admin-manage__group">
        <h2>Hệ thống</h2>

        <ManageItem
          icon="📊"
          title="Tổng quan"
          subtitle="Theo dõi toàn bộ hệ thống"
          to="/admin/dashboard"
        />

        <ManageItem
          icon="🏪"
          title="Duyệt shop"
          subtitle="Xử lý đăng ký shop"
          to="/admin/shops"
        />

        <ManageItem
          icon="📁"
          title="Danh mục"
          subtitle="Quản lý danh mục sản phẩm"
          to="/categories"
        />

        <ManageItem
          icon="🧩"
          title="Phân loại sản phẩm"
          subtitle="Màu sắc, kích thước và tùy chọn"
          to="/admin/product-options"
        />

        <ManageItem
          icon="🎟️"
          title="Voucher"
          subtitle="Quản lý voucher nền tảng"
          to="/admin/vouchers"
        />

        <ManageItem
          icon="📦"
          title="Sản phẩm"
          subtitle="Xem sản phẩm trên hệ thống"
          to="/products"
        />
      </section>

      <button
        type="button"
        className="admin-manage__logout"
        onClick={handleLogout}
      >
        Đăng xuất
      </button>
    </main>
  );
}

export default AdminManagePage;
