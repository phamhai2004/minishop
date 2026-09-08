import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { useAuth } from "../../../contexts/AuthContext";

import shopApi from "../../../api/shopApi";

import "./SellerManagePage.css";

function ManageItem({ icon, title, subtitle, to }) {
  return (
    <Link to={to} className="seller-manage__item">
      <span className="seller-manage__icon">{icon}</span>

      <span className="seller-manage__content">
        <strong>{title}</strong>

        {subtitle && <small>{subtitle}</small>}
      </span>

      <span className="seller-manage__arrow">›</span>
    </Link>
  );
}

function SellerManagePage() {
  const navigate = useNavigate();
  const [shop, setShop] = useState(null);
  const { currentUser, logout } = useAuth();
  const displayName =
    currentUser?.fullName || currentUser?.email || "Người bán";

  useEffect(() => {
    const loadShop = async () => {
      try {
        const response = await shopApi.getMyShop();

        setShop(response.data?.data ?? null);
      } catch (err) {
        console.error("Không thể tải thông tin shop:", err);
      }
    };

    loadShop();
  }, []);
  const handleLogout = async () => {
    await logout();

    navigate("/login", {
      replace: true,
    });
  };

  return (
    <main className="seller-manage">
      <header className="seller-manage__header">
        <div className="seller-manage__avatar">
          {shop?.logoUrl ? (
            <img src={shop.logoUrl} alt={shop.name ?? displayName} />
          ) : currentUser?.avatarUrl ? (
            <img src={currentUser.avatarUrl} alt={displayName} />
          ) : (
            <span>{displayName.charAt(0).toUpperCase()}</span>
          )}
        </div>

        <div className="seller-manage__identity">
          <h1>{displayName}</h1>

          <p>{currentUser?.email}</p>
        </div>
      </header>

      <section className="seller-manage__group">
        <h2>Shop</h2>

        <ManageItem
          icon="🏪"
          title="Shop của tôi"
          subtitle="Thông tin và hình ảnh shop"
          to="/seller/shop"
        />

        <ManageItem
          icon="📦"
          title="Sản phẩm"
          subtitle="Quản lý sản phẩm của shop"
          to="/products"
        />

        <ManageItem
          icon="🧾"
          title="Đơn hàng"
          subtitle="Theo dõi và xử lý đơn"
          to="/seller/orders"
        />
      </section>

      <section className="seller-manage__group">
        <h2>Marketing</h2>

        <ManageItem icon="🎟️" title="Voucher" to="/seller/vouchers" />

        <ManageItem icon="⚡" title="Flash Sale" to="/seller/flash-sales" />
      </section>

      <section className="seller-manage__group">
        <h2>Kinh doanh</h2>

        <ManageItem
          icon="📊"
          title="Thống kê"
          subtitle="Doanh thu, khách hàng, tồn kho"
          to="/seller/dashboard"
        />

        <ManageItem icon="💬" title="Tin nhắn" to="/seller/chat" />
      </section>

      <button
        type="button"
        className="seller-manage__logout"
        onClick={handleLogout}
      >
        Đăng xuất
      </button>
    </main>
  );
}

export default SellerManagePage;
