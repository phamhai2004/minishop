import { useEffect, useRef } from "react";

import { Link, useNavigate } from "react-router-dom";

import { useAuth } from "../../contexts/AuthContext";

import ROLES from "../constants/roles";

import NotificationDropdown from "../notification/NotificationDropdown";
import ShoppingCartIcon from "../icons/ShoppingCartIcon";
import UserIcon from "../icons/UserIcon";
import AddressIcon from "../icons/AddressIcon";
import LogoutIcon from "../icons/LogoutIcon";

import LetterHIcon from "../icons/LetterHIcon";
import BrandAnthropicIcon from "../icons/BrandAnthropicIcon";
import LetterRIcon from "../icons/LetterRIcon";
import HeartIcon from "../icons/HeartIcon";

import "./Header.css";

function Header() {
  const navigate = useNavigate();

  const { currentUser, isAuthenticated, logout } = useAuth();

  const cartIconRef = useRef(null);

  const displayName =
    currentUser?.fullName ?? currentUser?.email ?? "người dùng";

  useEffect(() => {
    const handleCartAdded = () => {
      cartIconRef.current?.startAnimation();
    };

    window.addEventListener("cart:added", handleCartAdded);

    return () => {
      window.removeEventListener("cart:added", handleCartAdded);
    };
  }, []);

  const handleLogout = async () => {
    await logout();

    navigate("/login", {
      replace: true,
    });
  };

  return (
    <header className="site-header">
      <div className="site-header__container">
        <Link
          to="/"
          className="site-header__brand"
          aria-label="Về trang chủ Hair"
        >
          <span className="site-header__brand-icons">
            <LetterHIcon size={38} strokeWidth={2.2} />
            <BrandAnthropicIcon size={38} />
            <LetterRIcon size={38} strokeWidth={2.2} />
          </span>
        </Link>

        <div className="site-header__account">
          {isAuthenticated ? (
            <>
              <div className="site-header__user">
                <span className="site-header__greeting">Xin chào</span>

                <strong className="site-header__username">{displayName}</strong>
              </div>

              <NotificationDropdown />

              {currentUser?.role === ROLES.CUSTOMER && (
                <Link
                  to="/cart"
                  className="site-header__cart-link"
                  aria-label="Giỏ hàng"
                  title="Giỏ hàng"
                >
                  <ShoppingCartIcon
                    ref={cartIconRef}
                    size={27}
                    strokeWidth={2}
                    className="site-header__cart-icon"
                  />
                </Link>
              )}

              {currentUser?.role === ROLES.CUSTOMER && (
                <Link
                  to="/customer/wishlist"
                  className="site-header__wishlist-link"
                  aria-label="Sản phẩm yêu thích"
                  title="Sản phẩm yêu thích"
                >
                  <HeartIcon
                    size={25}
                    strokeWidth={2}
                    className="site-header__wishlist-icon"
                  />
                </Link>
              )}

              {/* Account menu */}
              <div className="site-header__account-menu">
                <button
                  type="button"
                  className="site-header__account-button"
                  aria-label="Tài khoản"
                  title="Tài khoản"
                >
                  <UserIcon
                    size={24}
                    strokeWidth={2}
                    className="site-header__account-icon"
                  />
                </button>

                <div className="site-header__dropdown">
                  {/* CUSTOMER */}
                  {currentUser?.role === ROLES.CUSTOMER && (
                    <>
                      <button
                        type="button"
                        className="site-header__dropdown-item"
                        onClick={() => navigate("/account")}
                      >
                        <UserIcon
                          size={20}
                          strokeWidth={2}
                          className="site-header__dropdown-icon"
                        />

                        <span>Thông tin tài khoản</span>
                      </button>

                      <button
                        type="button"
                        className="site-header__dropdown-item"
                        onClick={() => navigate("/addresses")}
                      >
                        <AddressIcon
                          size={20}
                          strokeWidth={2}
                          className="site-header__dropdown-icon"
                        />

                        <span>Địa chỉ</span>
                      </button>
                    </>
                  )}

                  {/* SELLER */}
                  {currentUser?.role === ROLES.SELLER && (
                    <button
                      type="button"
                      className="site-header__dropdown-item"
                      onClick={() => navigate("/seller/shop")}
                    >
                      <UserIcon
                        size={20}
                        strokeWidth={2}
                        className="site-header__dropdown-icon"
                      />

                      <span>Thông tin shop</span>
                    </button>
                  )}

                  <button
                    type="button"
                    className="site-header__dropdown-item site-header__dropdown-item--logout"
                    onClick={handleLogout}
                  >
                    <LogoutIcon
                      size={20}
                      strokeWidth={2}
                      className="site-header__dropdown-icon"
                    />

                    <span>Đăng xuất</span>
                  </button>
                </div>
              </div>
            </>
          ) : (
            <Link to="/login" className="site-header__login-link">
              Đăng nhập
            </Link>
          )}
        </div>
      </div>
    </header>
  );
}

export default Header;
