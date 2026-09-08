import { NavLink, useLocation, useNavigate } from "react-router-dom";

import { useAuth } from "../../contexts/AuthContext";

import ROLES from "../constants/roles";
import HeartIcon from "../icons/HeartIcon";
import UserIcon from "../icons/UserIcon";
import FilledBellIcon from "../icons/FilledBellIcon";
import HomeIcon from "../icons/HomeIcon";
import MenuIcon from "../icons/MenuIcon";
import VoucherIcon from "../icons/VoucherIcon";

import "./MobileBottomNav.css";

function ProductIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M4 7 12 3l8 4v10l-8 4-8-4z" />
      <path d="m4 7 8 4 8-4M12 11v10" />
    </svg>
  );
}

function OrderIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M6 3h12v18H6z" />
      <path d="M9 8h6M9 12h6M9 16h4" />
    </svg>
  );
}

function ShopIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M4 9h16l-1-5H5z" />
      <path d="M5 9v11h14V9M9 20v-6h6v6" />
    </svg>
  );
}

function CategoryIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <rect x="3" y="3" width="7" height="7" />
      <rect x="14" y="3" width="7" height="7" />
      <rect x="3" y="14" width="7" height="7" />
      <rect x="14" y="14" width="7" height="7" />
    </svg>
  );
}

function MobileBottomNav() {
  const navigate = useNavigate();
  const location = useLocation();

  const { currentUser, isAuthenticated } = useAuth();

  const role = currentUser?.role;

  const getClassName = ({ isActive }) =>
    `mobile-bottom-nav__item ${
      isActive ? "mobile-bottom-nav__item--active" : ""
    }`;

  const isPathActive = (path) =>
    location.pathname === path || location.pathname.startsWith(`${path}/`);

  const getButtonClassName = (path, activeForGuestLogin = false) => {
    const isActive = isAuthenticated
      ? isPathActive(path)
      : activeForGuestLogin && location.pathname === "/login";

    return `mobile-bottom-nav__item ${
      isActive ? "mobile-bottom-nav__item--active" : ""
    }`;
  };

  if (!isAuthenticated || role === ROLES.CUSTOMER) {
    return (
      <nav className="mobile-bottom-nav" aria-label="Điều hướng di động">
        <NavLink to="/" end className={getClassName}>
          <HomeIcon />
          <span>Home</span>
        </NavLink>

        <NavLink to="/products" className={getClassName}>
          <ProductIcon />
          <span>Sản phẩm</span>
        </NavLink>

        <button
          type="button"
          className={getButtonClassName("/customer/wishlist")}
          onClick={() =>
            navigate(isAuthenticated ? "/customer/wishlist" : "/login")
          }
        >
          <HeartIcon size={25} strokeWidth={1.8} />
          <span>Yêu thích</span>
        </button>

        <button
          type="button"
          className={getButtonClassName("/customer/notifications")}
          onClick={() =>
            navigate(isAuthenticated ? "/customer/notifications" : "/login")
          }
        >
          <FilledBellIcon size={25} />
          <span>Thông báo</span>
        </button>

        <button
          type="button"
          className={getButtonClassName("/customer/profile", true)}
          onClick={() =>
            navigate(isAuthenticated ? "/customer/profile" : "/login")
          }
        >
          <UserIcon size={25} strokeWidth={1.8} />

          <span>{isAuthenticated ? "Hồ sơ" : "Đăng nhập"}</span>
        </button>
      </nav>
    );
  }

  if (role === ROLES.SELLER) {
    return (
      <nav className="mobile-bottom-nav mobile-bottom-nav--seller">
        <NavLink to="/" end className={getClassName}>
          <HomeIcon />
          <span>Trang chủ</span>
        </NavLink>

        <NavLink to="/products" className={getClassName}>
          <ProductIcon />
          <span>Sản phẩm</span>
        </NavLink>

        <NavLink to="/seller/orders" className={getClassName}>
          <OrderIcon />
          <span>Đơn hàng</span>
        </NavLink>

        <NavLink to="/seller/manage" className={getClassName}>
          <MenuIcon />
          <span>Quản lý</span>
        </NavLink>
      </nav>
    );
  }

  return (
    <nav className="mobile-bottom-nav">
      <NavLink to="/" end className={getClassName}>
        <HomeIcon />
        <span>Tổng quan</span>
      </NavLink>

      <NavLink to="/admin/shops" className={getClassName}>
        <ShopIcon />
        <span>Shop</span>
      </NavLink>

      <NavLink to="/categories" className={getClassName}>
        <CategoryIcon />
        <span>Danh mục</span>
      </NavLink>

      <NavLink to="/admin/vouchers" className={getClassName}>
        <VoucherIcon />
        <span>Voucher</span>
      </NavLink>

      <NavLink to="/admin/manage" className={getClassName}>
        <MenuIcon />
        <span>Quản lý</span>
      </NavLink>
    </nav>
  );
}

export default MobileBottomNav;
