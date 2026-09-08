import { NavLink } from "react-router-dom";

import ROLES from "../constants/roles";
import { useAuth } from "../../contexts/AuthContext";

import "./NavigationMenu.css";

function NavigationMenu() {
  const { currentUser, isAuthenticated } = useAuth();

  const userRole = currentUser?.role;

  const getNavLinkClassName = ({ isActive }) => {
    return isActive
      ? "main-navigation__link main-navigation__link--active"
      : "main-navigation__link";
  };

  return (
    <nav className="main-navigation" aria-label="Điều hướng chính">
      <div className="main-navigation__container">
        <ul className="main-navigation__list">
          <li className="main-navigation__item">
            <NavLink to="/" end className={getNavLinkClassName}>
              Trang chủ
            </NavLink>
          </li>

          <li className="main-navigation__item">
            <NavLink to="/products" className={getNavLinkClassName}>
              {userRole === ROLES.SELLER ? "Quản lý sản phẩm" : "Sản phẩm"}
            </NavLink>
          </li>

          {userRole === ROLES.CUSTOMER && (
            <li className="main-navigation__item">
              <NavLink to="/customer/orders" className={getNavLinkClassName}>
                Đơn hàng
              </NavLink>
            </li>
          )}

          {userRole === ROLES.CUSTOMER && (
            <li className="main-navigation__item">
              <NavLink to="/customer/reviews" className={getNavLinkClassName}>
                Đánh giá
              </NavLink>
            </li>
          )}

          {userRole === ROLES.CUSTOMER && (
            <li className="main-navigation__item">
              <NavLink to="/customer/following" className={getNavLinkClassName}>
                Đang theo dõi
              </NavLink>
            </li>
          )}

          {userRole === ROLES.CUSTOMER && (
            <li className="main-navigation__item">
              <NavLink
                to="/customer/my-vouchers"
                className={getNavLinkClassName}
              >
                Kho Voucher
              </NavLink>
            </li>
          )}

          {!isAuthenticated && (
            <li className="main-navigation__item">
              <NavLink to="/login" className={getNavLinkClassName}>
                Đăng nhập
              </NavLink>
            </li>
          )}

          {userRole === ROLES.CUSTOMER && (
            <li className="main-navigation__item">
              <NavLink
                to="/customer/shop/register"
                className={getNavLinkClassName}
              >
                Đăng ký shop
              </NavLink>
            </li>
          )}

          {userRole === ROLES.SELLER && (
            <>
              <li className="main-navigation__item">
                <NavLink to="/seller/orders" className={getNavLinkClassName}>
                  Đơn hàng
                </NavLink>
              </li>

              <li className="main-navigation__item">
                <NavLink to="/seller/vouchers" className={getNavLinkClassName}>
                  Voucher
                </NavLink>
              </li>

              <li className="main-navigation__item">
                <NavLink
                  to="/seller/flash-sales"
                  className={getNavLinkClassName}
                >
                  Flash Sale
                </NavLink>
              </li>
            </>
          )}

          {userRole === ROLES.ADMIN && (
            <>
              <li className="main-navigation__item">
                <NavLink to="/categories" className={getNavLinkClassName}>
                  Danh mục
                </NavLink>
              </li>

              <li className="main-navigation__item">
                <NavLink
                  to="/admin/product-options"
                  className={getNavLinkClassName}
                >
                  Phân loại
                </NavLink>
              </li>

              <li className="main-navigation__item">
                <NavLink to="/admin/vouchers" className={getNavLinkClassName}>
                  Voucher
                </NavLink>
              </li>

              <li className="main-navigation__item">
                <NavLink to="/admin/shops" className={getNavLinkClassName}>
                  Duyệt shop
                </NavLink>
              </li>
            </>
          )}
        </ul>
      </div>
    </nav>
  );
}

export default NavigationMenu;
