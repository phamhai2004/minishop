import { useEffect, useLayoutEffect, useRef, useState } from "react";

import { Outlet, useNavigate } from "react-router-dom";

import Footer from "../components/footer/Footer";
import Header from "../components/header/Header";
import NavigationMenu from "../components/navigation/NavigationMenu";
import ChatHub from "../components/chat/ChatHub";
import MobileTopBar from "../components/mobile/MobileTopBar";
import MobileBottomNav from "../components/mobile/MobileBottomNav";

import SellerApprovedModal from "../components/common/SellerApprovedModal";

import { useAuth } from "../contexts/AuthContext";
import ROLES from "../components/constants/roles";

import shopApi from "../api/shopApi";

import "./AppLayout.css";

function AppLayout() {
  const navigate = useNavigate();

  const { currentUser, logout } = useAuth();

  const [sellerApproved, setSellerApproved] = useState(false);

  const [confirming, setConfirming] = useState(false);

  const canUseShopChat =
    currentUser?.role === ROLES.CUSTOMER || currentUser?.role === ROLES.SELLER;

  const layoutRef = useRef(null);
  const topbarRef = useRef(null);

  useLayoutEffect(() => {
    const layout = layoutRef.current;
    const topbar = topbarRef.current;

    if (!layout || !topbar) {
      return;
    }

    const updateTopbarHeight = () => {
      const height = topbar.getBoundingClientRect().height;

      layout.style.setProperty("--app-topbar-height", `${height}px`);
    };

    updateTopbarHeight();

    const resizeObserver = new ResizeObserver(updateTopbarHeight);

    resizeObserver.observe(topbar);

    window.addEventListener("resize", updateTopbarHeight);

    return () => {
      resizeObserver.disconnect();

      window.removeEventListener("resize", updateTopbarHeight);
    };
  }, []);

  useEffect(() => {
    if (!currentUser || currentUser.role !== ROLES.CUSTOMER) {
      setSellerApproved(false);
      return;
    }

    let cancelled = false;

    const checkShopApproval = async () => {
      try {
        const response = await shopApi.getMyShop();

        const shop = response.data?.data;

        if (
          !cancelled &&
          shop?.status === "ACTIVE" &&
          shop?.verified === true
        ) {
          setSellerApproved(true);
        }
      } catch (error) {
        if (error.response?.status !== 404) {
          console.error("Không thể kiểm tra trạng thái shop:", error);
        }
      }
    };

    void checkShopApproval();

    const intervalId = window.setInterval(checkShopApproval, 15000);

    const handleWindowFocus = () => {
      void checkShopApproval();
    };

    window.addEventListener("focus", handleWindowFocus);

    return () => {
      cancelled = true;

      window.clearInterval(intervalId);

      window.removeEventListener("focus", handleWindowFocus);
    };
  }, [currentUser]);

  const handleSellerApprovedConfirm = async () => {
    try {
      setConfirming(true);

      await logout();

      navigate("/login", {
        replace: true,
        state: {
          message:
            "Tài khoản đã được nâng cấp lên Người bán. Vui lòng đăng nhập lại.",
        },
      });
    } finally {
      setConfirming(false);
    }
  };

  return (
    <div ref={layoutRef} className="app-layout">
      <div ref={topbarRef} className="app-layout__topbar">
        <div className="app-layout__desktop-topbar">
          <Header />
          <NavigationMenu />
        </div>

        <MobileTopBar />
      </div>

      <main className="app-layout__main">
        <div className="app-layout__container">
          <Outlet />
        </div>
      </main>

      {canUseShopChat && <ChatHub />}

      <div className="app-layout__desktop-footer">
        <Footer />
      </div>

      <MobileBottomNav />

      <SellerApprovedModal
        open={sellerApproved}
        confirming={confirming}
        onConfirm={handleSellerApprovedConfirm}
      />
    </div>
  );
}

export default AppLayout;
