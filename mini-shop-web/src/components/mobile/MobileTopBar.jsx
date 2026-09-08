import { useNavigate } from "react-router-dom";

import { useAuth } from "../../contexts/AuthContext";
import ROLES from "../constants/roles";

import ShoppingCartIcon from "../icons/ShoppingCartIcon";
import MessageCircleIcon from "../icons/MessageCircleIcon";
import ProductSearchBox from "../product/ProductSearchBox";

import { apiSearchByImage } from "../../api/productApi";

import "./MobileTopBar.css";

function MobileTopBar() {
  const navigate = useNavigate();
  const { currentUser, isAuthenticated } = useAuth();
  const role = currentUser?.role;
  const isMarketplaceUser = !isAuthenticated || role === ROLES.CUSTOMER;

  const handleCart = () => {
    if (isAuthenticated && role === ROLES.CUSTOMER) {
      navigate("/cart");
      return;
    }

    navigate("/login");
  };

  const handleChat = () => {
    if (!isAuthenticated) {
      navigate("/login");
      return;
    }

    if (role === ROLES.CUSTOMER) {
      window.dispatchEvent(new Event("chat-hub:toggle"));

      return;
    }

    if (role === ROLES.SELLER) {
      navigate("/seller/chat");
    }
  };

  const handleSearch = (keyword) => {
    const value = keyword?.trim();

    navigate({
      pathname: "/products",
      search: value ? `?keyword=${encodeURIComponent(value)}` : "",
    });
  };

  const handleImageSearch = async (file) => {
    try {
      const result = await apiSearchByImage(file);

      navigate("/products", {
        state: {
          imageSearchResults: result,
        },
      });
    } catch (error) {
      console.error("Không thể tìm bằng hình ảnh:", error);
    }
  };

  if (!isMarketplaceUser) {
    return (
      <div className="mobile-topbar mobile-topbar--management">
        <button
          type="button"
          className="mobile-topbar__brand"
          onClick={() => navigate("/")}
        >
          HAIR
        </button>

        <strong className="mobile-topbar__management-title">
          {role === ROLES.SELLER ? "Kênh người bán" : "Quản trị Hair"}
        </strong>

        {role === ROLES.SELLER && (
          <button
            type="button"
            className="mobile-topbar__icon-button"
            onClick={handleChat}
            aria-label="Tin nhắn"
          >
            <MessageCircleIcon />
          </button>
        )}
      </div>
    );
  }

  return (
    <div className="mobile-topbar">
      <div className="mobile-topbar__search">
        <ProductSearchBox
          compact
          placeholder="Tìm sản phẩm..."
          onSearch={handleSearch}
          onImageSearch={handleImageSearch}
        />
      </div>

      <button
        type="button"
        className="mobile-topbar__icon-button"
        onClick={handleCart}
        aria-label="Giỏ hàng"
      >
        <ShoppingCartIcon size={28} strokeWidth={1.9} />
      </button>

      <button
        type="button"
        className="mobile-topbar__icon-button"
        onClick={handleChat}
        aria-label="Chat"
      >
        <MessageCircleIcon />
      </button>
    </div>
  );
}

export default MobileTopBar;
