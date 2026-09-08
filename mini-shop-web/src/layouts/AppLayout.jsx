import { useLayoutEffect, useRef } from "react";

import { Outlet } from "react-router-dom";

import Footer from "../components/footer/Footer";
import Header from "../components/header/Header";
import NavigationMenu from "../components/navigation/NavigationMenu";
import ChatHub from "../components/chat/ChatHub";
import MobileTopBar from "../components/mobile/MobileTopBar";
import MobileBottomNav from "../components/mobile/MobileBottomNav";

import { useAuth } from "../contexts/AuthContext";
import ROLES from "../components/constants/roles";

import "./AppLayout.css";

function AppLayout() {
  const { currentUser } = useAuth();

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
    </div>
  );
}

export default AppLayout;
