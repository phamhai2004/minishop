import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import cartApi from "../../api/cartApi";

import CartItem from "../../components/cart/CartItem";
import LoadingSpinner from "../../components/common/LoadingSpinner";

import "./CartPage.css";

function CartPage() {
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedItemKeys, setSelectedItemKeys] = useState([]);

  const loadCart = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const response = await cartApi.getMyCart();

      const apiResponse = response.data;

      if (!apiResponse?.success || !apiResponse?.data) {
        throw new Error(apiResponse?.message ?? "Không thể tải giỏ hàng.");
      }

      const loadedCart = apiResponse.data;

      setCart(loadedCart);

      const availableKeys = (loadedCart.items ?? []).map(
        (item) => `${item.productId}-${item.variantId ?? "no-variant"}`,
      );

      setSelectedItemKeys((previousKeys) =>
        previousKeys.filter((key) => availableKeys.includes(key)),
      );
    } catch (requestError) {
      console.error("Unable to load cart:", requestError);

      setCart(null);

      setError(
        requestError.response?.data?.message ??
          requestError.message ??
          "Không thể tải giỏ hàng. Vui lòng thử lại.",
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadCart();
  }, [loadCart]);

  const items = Array.isArray(cart?.items) ? cart.items : [];

  const getItemKey = (item) =>
    `${item.productId}-${item.variantId ?? "no-variant"}`;

  const handleSelectItem = (itemKey) => {
    setSelectedItemKeys((previousKeys) => {
      if (previousKeys.includes(itemKey)) {
        return previousKeys.filter((key) => key !== itemKey);
      }

      return [...previousKeys, itemKey];
    });
  };

  const allItemKeys = items.map(getItemKey);

  const allSelected =
    items.length > 0 && selectedItemKeys.length === items.length;

  const handleSelectAll = () => {
    if (allSelected) {
      setSelectedItemKeys([]);
      return;
    }

    setSelectedItemKeys(allItemKeys);
  };

  const handleItemUpdated = (updatedCart, updatedItem) => {
    setCart(updatedCart);
  };

  const selectedItems = items.filter((item) =>
    selectedItemKeys.includes(getItemKey(item)),
  );

  const selectedTotal = selectedItems.reduce(
    (total, item) => total + Number(item.subtotal ?? 0),
    0,
  );

  if (loading) {
    return (
      <section className="cart-page">
        <header className="cart-page__header">
          <h1 className="cart-page__title">Giỏ hàng</h1>
          <p className="cart-page__description">
            Danh sách các sản phẩm trong giỏ hàng.
          </p>
        </header>

        <div className="cart-page__status" role="status">
          <LoadingSpinner size="large" />
        </div>
      </section>
    );
  }

  if (error) {
    return (
      <section className="cart-page">
        <header className="cart-page__header">
          <h1 className="cart-page__title">Giỏ hàng</h1>
          <p className="cart-page__description">
            Danh sách các sản phẩm trong giỏ hàng.
          </p>
        </header>

        <div
          className="cart-page__status cart-page__status--error"
          role="alert"
        >
          <span className="cart-page__status-icon" aria-hidden="true">
            ⚠️
          </span>

          <h2>Không thể tải giỏ hàng</h2>

          <p>{error}</p>

          <button
            type="button"
            className="cart-page__retry-button"
            onClick={loadCart}
          >
            Thử lại
          </button>
        </div>
      </section>
    );
  }

  if (items.length === 0) {
    return (
      <section className="cart-page">
        <header className="cart-page__header">
          <h1 className="cart-page__title">Giỏ hàng</h1>
          <p className="cart-page__description">
            Danh sách các sản phẩm trong giỏ hàng.
          </p>
        </header>

        <div className="cart-page__empty">
          <span className="cart-page__empty-icon" aria-hidden="true">
            🛒
          </span>

          <h2>Giỏ hàng đang trống</h2>

          <p>Bạn chưa có sản phẩm nào trong giỏ hàng.</p>
        </div>
      </section>
    );
  }

  return (
    <section className="cart-page">
      <header className="cart-page__header">
        <div>
          <h1 className="cart-page__title">Giỏ hàng</h1>

          <p className="cart-page__description">
            Kiểm tra các sản phẩm bạn đã thêm vào giỏ hàng.
          </p>
        </div>

        <span className="cart-page__count">{items.length} sản phẩm</span>
      </header>

      <div className="cart-page__select-all">
        <label className="cart-page__select-all-label">
          <input
            type="checkbox"
            checked={allSelected}
            onChange={handleSelectAll}
          />

          <span>Chọn tất cả</span>
        </label>

        <span className="cart-page__selected-count">
          Đã chọn: {selectedItems.length}
        </span>
      </div>

      <div className="cart-page__content">
        <div className="cart-page__items">
          {items.map((item) => {
            const itemKey = getItemKey(item);

            return (
              <CartItem
                key={itemKey}
                item={item}
                selected={selectedItemKeys.includes(itemKey)}
                onSelect={handleSelectItem}
                onUpdated={handleItemUpdated}
              />
            );
          })}
        </div>

        <aside className="cart-page__summary">
          <h2 className="cart-page__summary-title">Tóm tắt đơn hàng</h2>

          <div className="cart-page__summary-row">
            <span>Đã chọn</span>
            <strong>{selectedItems.length} sản phẩm</strong>
          </div>

          <div className="cart-page__summary-divider" />

          <div className="cart-page__summary-total">
            <span>Tổng tiền</span>

            <strong>{selectedTotal.toLocaleString("vi-VN")} ₫</strong>
          </div>

          <button
            type="button"
            className="cart-page__checkout-button"
            disabled={selectedItems.length === 0}
            onClick={() => {
              // eslint-disable-next-line no-undef
              navigate("/checkout", {
                state: {
                  type: "CART",
                  items: selectedItems.map((item) => ({
                    productId: item.productId,
                    variantId: item.variantId ?? null,
                    quantity: item.quantity,
                  })),
                },
              });
            }}
          >
            Mua hàng ({selectedItems.length})
          </button>
        </aside>
      </div>
    </section>
  );
}

export default CartPage;
