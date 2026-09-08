import { BrowserRouter, Route, Routes } from "react-router-dom";

import ROLES from "../components/constants/roles";

import AppLayout from "../layouts/AppLayout";

import AdminDashboardPage from "../pages/admin/dashboard/AdminDashboardPage";
import CategoryManagementPage from "../pages/admin/category/CategoryManagementPage";
import ProductOptionManagementPage from "../pages/admin/classify/ProductOptionManagementPage";
import LoginPage from "../pages/auth/LoginPage";
import RegisterPage from "../pages/auth/RegisterPage";
import VerifyRegistrationPage from "../pages/auth/VerifyRegistrationPage";
import EmailVerificationPage from "../pages/auth/EmailVerificationPage";
import OAuth2CallbackPage from "../pages/auth/OAuth2CallbackPage";
import ForbiddenPage from "../pages/common/ForbiddenPage";
import NotFoundPage from "../pages/common/NotFoundPage";
import CustomerReviewsPage from "../pages/customer/review/CustomerReviewsPage";
import FollowingShopsPage from "../pages/customer/follow/FollowingShopsPage";
import ShopRegistrationPage from "../pages/customer/shop/ShopRegistrationPage";
import CartPage from "../pages/cart/CartPage";
import WishlistPage from "../pages/customer/wishlist/WishlistPage";
import CheckoutPage from "../pages/checkout/CheckoutPage";
import OrderListPage from "../pages/order/OrderListPage";
import OrderDetailPage from "../pages/order/OrderDetailPage";
import PaymentResultPage from "../pages/payment/PaymentResultPage";
import RoleHomePage from "../pages/home/RoleHomePage";
import AccountInfoPage from "../pages/account/AccountInfoPage";
import AddressManagementPage from "../pages/address/AddressManagementPage";
import ProductDetailPage from "../pages/product/ProductDetailPage";
import ProductListPage from "../pages/product/ProductListPage";
import SellerDashboardPage from "../pages/seller/dashboard/SellerDashboardPage";
import SellerOrderListPage from "../pages/seller/order/SellerOrderListPage";
import SellerShopPage from "../pages/seller/shop/SellerShopPage";
import ShopPublicPage from "../pages/shop/ShopPublicPage";
import SellerOrderDetailPage from "../pages/seller/order/SellerOrderDetailPage";
import ShopChatPage from "../pages/message/ShopChatPage";
import VouchersPage from "../pages/voucher/VouchersPage";
import MyVouchersPage from "../pages/voucher/MyVoucherPage";
import SellerVoucherPage from "../pages/seller/voucher/SellerVoucherPage";
import SellerFlashSalePage from "../pages/seller/flashsale/SellerFlashSalePage";
import AdminVoucherPage from "../pages/admin/voucher/AdminVoucherPage";
import AdminShopManagementPage from "../pages/admin/shop/AdminShopManagementPage";
import FlashSalePage from "../pages/flashsale/FlashSalePage";
import CategoryPage from "../pages/category/CategoryPage";
import CustomerProfilePage from "../pages/customer/profile/CustomerProfilePage";
import NotificationPage from "../pages/notification/NotificationPage";
import AssistantChatPage from "../pages/customer/assistant/AssistantChatPage";
import SellerManagePage from "../pages/seller/manage/SellerManagePage";
import AdminManagePage from "../pages/admin/manage/AdminManagePage";

import GuestRoute from "./GuestRoute";
import RequireRole from "./RequireRole";

function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Guest routes: không sử dụng AppLayout */}
        <Route element={<GuestRoute />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/register/verify" element={<VerifyRegistrationPage />} />
          <Route path="/email/verify" element={<EmailVerificationPage />} />
        </Route>

        <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />

        <Route element={<AppLayout />}>
          {/* Public routes */}
          <Route index element={<RoleHomePage />} />
          <Route path="products" element={<ProductListPage />} />
          <Route path="products/:id" element={<ProductDetailPage />} />
          <Route path="shops/:shopId" element={<ShopPublicPage />} />
          <Route path="forbidden" element={<ForbiddenPage />} />
          <Route path="flash-sales" element={<FlashSalePage />} />
          <Route path="categories/:categoryId" element={<CategoryPage />} />

          {/* PAYMENT */}
          <Route path="payment-result" element={<PaymentResultPage />} />

          {/* CUSTOMER routes */}
          <Route element={<RequireRole allowedRoles={[ROLES.CUSTOMER]} />}>
            <Route path="account" element={<AccountInfoPage />} />
            <Route path="addresses" element={<AddressManagementPage />} />
            <Route path="customer/orders" element={<OrderListPage />} />
            <Route path="customer/following" element={<FollowingShopsPage />} />
            <Route path="customer/chat" element={<ShopChatPage />} />
            <Route path="customer/reviews" element={<CustomerReviewsPage />} />
            <Route path="customer/vouchers" element={<VouchersPage />} />
            <Route path="customer/my-vouchers" element={<MyVouchersPage />} />
            <Route path="customer/wishlist" element={<WishlistPage />} />
            <Route
              path="customer/shop/register"
              element={<ShopRegistrationPage />}
            />
            <Route
              path="customer/orders/:orderCode"
              element={<OrderDetailPage />}
            />
            <Route path="cart" element={<CartPage />} />
            <Route path="checkout" element={<CheckoutPage />} />

            <Route path="customer/profile" element={<CustomerProfilePage />} />

            <Route
              path="customer/notifications"
              element={<NotificationPage />}
            />
            <Route path="customer/assistant" element={<AssistantChatPage />} />
          </Route>

          {/* SELLER routes */}
          <Route element={<RequireRole allowedRoles={[ROLES.SELLER]} />}>
            <Route path="seller/dashboard" element={<SellerDashboardPage />} />
            <Route path="seller/orders" element={<SellerOrderListPage />} />
            <Route
              path="seller/orders/:id"
              element={<SellerOrderDetailPage />}
            />
            <Route path="seller/vouchers" element={<SellerVoucherPage />} />
            <Route
              path="seller/flash-sales"
              element={<SellerFlashSalePage />}
            />
            <Route path="seller/shop" element={<SellerShopPage />} />
            <Route path="seller/chat" element={<ShopChatPage />} />
            <Route path="seller/manage" element={<SellerManagePage />} />
          </Route>

          {/* ADMIN routes */}
          <Route element={<RequireRole allowedRoles={[ROLES.ADMIN]} />}>
            <Route path="admin/dashboard" element={<AdminDashboardPage />} />
            <Route path="categories" element={<CategoryManagementPage />} />
            <Route
              path="admin/product-options"
              element={<ProductOptionManagementPage />}
            />
            <Route path="admin/vouchers" element={<AdminVoucherPage />} />
            <Route path="admin/shops" element={<AdminShopManagementPage />} />
            <Route path="admin/manage" element={<AdminManagePage />} />
          </Route>

          {/* Not found */}
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default AppRouter;
