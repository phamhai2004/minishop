import { useState } from "react";
import { useNavigate } from "react-router-dom";

import shopChatApi from "../../api/shopChatApi";
import { useAuth } from "../../contexts/AuthContext";
import ROLES from "../constants/roles";

import AuthRequiredModal from "../auth/AuthRequiredModal";
import MessageModal from "../common/MessageModal";
import LoadingSpinner from "../common/LoadingSpinner";

import "./ShopSummaryCard.css";

function formatJoinedTime(value) {
  if (!value) {
    return "—";
  }

  const createdAt = new Date(value);

  if (Number.isNaN(createdAt.getTime())) {
    return "—";
  }

  const now = new Date();

  let months =
    (now.getFullYear() - createdAt.getFullYear()) * 12 +
    (now.getMonth() - createdAt.getMonth());

  if (now.getDate() < createdAt.getDate()) {
    months -= 1;
  }

  if (months >= 12) {
    const years = Math.floor(months / 12);

    return `${years} năm trước`;
  }

  if (months > 0) {
    return `${months} tháng trước`;
  }

  const diffMs = now.getTime() - createdAt.getTime();
  const days = Math.max(0, Math.floor(diffMs / (1000 * 60 * 60 * 24)));

  if (days > 0) {
    return `${days} ngày trước`;
  }

  return "Hôm nay";
}

function ShopLogo({ logoUrl, shopName }) {
  if (logoUrl) {
    return <img className="shop-summary__logo" src={logoUrl} alt={shopName} />;
  }

  return (
    <div className="shop-summary__logo shop-summary__logo--empty">
      {shopName?.trim()?.charAt(0)?.toUpperCase() || "S"}
    </div>
  );
}

function ShopSummaryCard({ shop, product, loading = false }) {
  const navigate = useNavigate();
  const { currentUser, isAuthenticated } = useAuth();
  const [chatLoading, setChatLoading] = useState(false);
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [chatNotice, setChatNotice] = useState({
    open: false,
    title: "",
    message: "",
    type: "error",
  });

  if (loading) {
    return (
      <section className="shop-summary shop-summary--loading">
        <LoadingSpinner size="medium" />
      </section>
    );
  }

  if (!shop) {
    return null;
  }

  const rating = Number(shop.rating ?? 0);
  const totalReviews = Number(shop.totalReviews ?? 0);
  const totalProducts = Number(shop.totalProducts ?? 0);
  const totalFollowers = Number(shop.totalFollowers ?? 0);

  const handleViewShop = () => {
    navigate(`/shops/${shop.id}`, {
      state: {
        sourceProductId: product?.id ?? null,
      },
    });
  };

  const handleChat = async () => {
    if (!isAuthenticated) {
      setShowAuthModal(true);
      return;
    }

    if (currentUser?.role !== ROLES.CUSTOMER) {
      return;
    }

    if (chatLoading || !product?.id) {
      return;
    }

    try {
      setChatLoading(true);

      const response = await shopChatApi.openConversationWithProduct(
        shop.id,
        product.id,
      );

      const conversation = response.data?.data;

      if (!conversation?.id) {
        throw new Error("Không lấy được conversationId");
      }

      navigate("/customer/chat", {
        state: {
          conversationId: conversation.id,

          returnTo: `/products/${product.id}`,
        },
      });
    } catch (error) {
      console.error("Không thể mở chat:", error);

      setChatNotice({
        open: true,

        title: "Không thể mở cuộc trò chuyện",

        message:
          error.response?.data?.message ||
          "Không thể mở cuộc trò chuyện. Vui lòng thử lại.",

        type: "error",
      });
    } finally {
      setChatLoading(false);
    }
  };

  return (
    <>
      <section className="shop-summary">
        {/* LEFT */}
        <div className="shop-summary__identity">
          <ShopLogo logoUrl={shop.logoUrl} shopName={shop.name} />

          <div className="shop-summary__identity-content">
            <div className="shop-summary__name-row">
              <h2>{shop.name}</h2>

              {shop.verified && (
                <span
                  className="shop-summary__verified"
                  title="Shop đã xác minh"
                >
                  ✓
                </span>
              )}
            </div>

            <span className="shop-summary__status">
              <span className="shop-summary__status-dot" />

              {shop.statusName || "Đang hoạt động"}
            </span>

            <div className="shop-summary__actions">
              <button
                type="button"
                className="shop-summary__view-button"
                onClick={handleViewShop}
              >
                <span>▣</span>
                Xem shop
              </button>

              <button
                type="button"
                className="shop-summary__chat-button"
                onClick={handleChat}
                disabled={
                  chatLoading ||
                  (isAuthenticated && currentUser?.role !== ROLES.CUSTOMER)
                }
              >
                <span>◌</span>

                {chatLoading ? (
                  <LoadingSpinner size="small" inline variant="light" />
                ) : (
                  "Chat"
                )}
              </button>
            </div>
          </div>
        </div>

        {/* DESKTOP STATS */}
        <div className="shop-summary__stats shop-summary__stats--desktop">
          <div className="shop-summary__stat">
            <span>Đánh giá</span>
            <strong>{rating.toFixed(1)}</strong>
          </div>

          <div className="shop-summary__stat">
            <span>Sản phẩm</span>
            <strong>{totalProducts}</strong>
          </div>

          <div className="shop-summary__stat">
            <span>Tham gia</span>
            <strong>{formatJoinedTime(shop.createdAt)}</strong>
          </div>

          <div className="shop-summary__stat">
            <span>Người theo dõi</span>
            <strong>{totalFollowers}</strong>
          </div>
        </div>

        {/* MOBILE STATS */}
        <div className="shop-summary__stats shop-summary__stats--mobile">
          <div className="shop-summary__stat">
            <strong>{rating.toFixed(1)}</strong>
            <span>Đánh giá</span>
          </div>

          <div className="shop-summary__stat">
            <strong>{totalProducts}</strong>
            <span>Sản phẩm</span>
          </div>
        </div>
      </section>

      <AuthRequiredModal
        isOpen={showAuthModal}
        onClose={() => setShowAuthModal(false)}
      />

      <MessageModal
        open={chatNotice.open}
        title={chatNotice.title}
        message={chatNotice.message}
        type={chatNotice.type}
        onClose={() =>
          setChatNotice((current) => ({
            ...current,
            open: false,
          }))
        }
      />
    </>
  );
}

export default ShopSummaryCard;
