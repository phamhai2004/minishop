import { useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";

import userApi from "../../../api/userApi";
import { useAuth } from "../../../contexts/AuthContext";

import "./CustomerProfilePage.css";

function ProfileRow({ icon, title, subtitle, to }) {
  return (
    <Link to={to} className="mobile-profile__row">
      <span className="mobile-profile__row-icon">{icon}</span>

      <span className="mobile-profile__row-content">
        <strong>{title}</strong>

        {subtitle && <small>{subtitle}</small>}
      </span>

      <span className="mobile-profile__chevron">›</span>
    </Link>
  );
}

function CustomerProfilePage() {
  const navigate = useNavigate();

  const { currentUser, logout, updateCurrentUser } = useAuth();

  const displayName =
    currentUser?.fullName || currentUser?.email || "Khách hàng";

  useEffect(() => {
    const loadCurrentUser = async () => {
      try {
        const response = await userApi.getCurrentUser();

        const user = response.data?.data;

        if (user) {
          updateCurrentUser(user);
        }
      } catch (err) {
        console.error("Không thể tải thông tin người dùng:", err);
      }
    };

    loadCurrentUser();
  }, [updateCurrentUser]);

  const handleLogout = async () => {
    await logout();

    navigate("/login", {
      replace: true,
    });
  };

  return (
    <main className="mobile-profile">
      <section className="mobile-profile__hero">
        <div className="mobile-profile__avatar">
          {currentUser?.avatarUrl ? (
            <img src={currentUser.avatarUrl} alt={displayName} />
          ) : (
            <span>{displayName.charAt(0).toUpperCase()}</span>
          )}
        </div>

        <div className="mobile-profile__identity">
          <h1>{displayName}</h1>

          <p>{currentUser?.email}</p>
        </div>
      </section>

      <section className="mobile-profile__card">
        <div className="mobile-profile__section-heading">
          <h2>Đơn mua</h2>

          <Link to="/customer/orders">Xem tất cả ›</Link>
        </div>

        <div className="mobile-profile__order-shortcuts">
          <Link to="/customer/orders">
            <span>🧾</span>
            <small>Chờ xác nhận</small>
          </Link>

          <Link to="/customer/orders">
            <span>📦</span>
            <small>Chờ lấy hàng</small>
          </Link>

          <Link to="/customer/orders">
            <span>🚚</span>
            <small>Đang giao</small>
          </Link>

          <Link to="/customer/reviews">
            <span>⭐</span>
            <small>Đánh giá</small>
          </Link>
        </div>
      </section>

      <section className="mobile-profile__group">
        <h2>Tài khoản của tôi</h2>

        <ProfileRow icon="👤" title="Thông tin tài khoản" to="/account" />

        <ProfileRow icon="📍" title="Địa chỉ" to="/addresses" />
      </section>

      <section className="mobile-profile__group">
        <h2>Mua sắm</h2>

        <ProfileRow
          icon="♡"
          title="Sản phẩm yêu thích"
          to="/customer/wishlist"
        />

        <ProfileRow icon="🏪" title="Đang theo dõi" to="/customer/following" />

        <ProfileRow icon="🎟️" title="Kho Voucher" to="/customer/my-vouchers" />

        <ProfileRow icon="⭐" title="Đánh giá của tôi" to="/customer/reviews" />
      </section>

      <section className="mobile-profile__group">
        <h2>Tiện ích</h2>

        <ProfileRow icon="💬" title="Tin nhắn" to="/customer/chat" />

        <ProfileRow icon="🔔" title="Thông báo" to="/customer/notifications" />

        <ProfileRow
          icon="🏬"
          title="Đăng ký shop"
          to="/customer/shop/register"
        />
      </section>

      <button
        type="button"
        className="mobile-profile__logout"
        onClick={handleLogout}
      >
        Đăng xuất
      </button>
    </main>
  );
}

export default CustomerProfilePage;
