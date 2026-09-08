import { useState } from "react";

import cartApi from "../../api/cartApi";

import LoadingSpinner from "../common/LoadingSpinner";

import "./CartItem.css";

function CartItem({ item, selected, onSelect, onUpdated }) {
  const [updating, setUpdating] = useState(false);
  const [removing, setRemoving] = useState(false);
  const [updateError, setUpdateError] = useState("");

  const options = Array.isArray(item?.variant?.options)
    ? item.variant.options
    : [];

  const itemKey = `${item.productId}-${item.variantId ?? "no-variant"}`;

  const updateQuantity = async (newQuantity) => {
    if (newQuantity < 1 || updating) {
      return;
    }

    try {
      setUpdating(true);
      setUpdateError("");

      const response = await cartApi.updateItem(
        item.productId,
        item.variantId,
        {
          quantity: newQuantity,
        },
      );

      const apiResponse = response.data;

      if (!apiResponse?.success || !apiResponse?.data) {
        throw new Error(
          apiResponse?.message ?? "Không thể cập nhật số lượng sản phẩm.",
        );
      }

      const updatedCart = apiResponse.data;

      const updatedItem = updatedCart.items?.find(
        (cartItem) =>
          cartItem.productId === item.productId &&
          (cartItem.variantId ?? null) === (item.variantId ?? null),
      );

      if (!updatedItem) {
        throw new Error("Không tìm thấy sản phẩm sau khi cập nhật giỏ hàng.");
      }

      onUpdated(updatedCart, updatedItem);
    } catch (error) {
      console.error("Unable to update cart item:", error);

      setUpdateError(
        error.response?.data?.message ??
          error.message ??
          "Không thể cập nhật số lượng.",
      );
    } finally {
      setUpdating(false);
    }
  };

  const handleDecrease = () => {
    updateQuantity(item.quantity - 1);
  };

  const handleIncrease = () => {
    updateQuantity(item.quantity + 1);
  };

  const handleRemove = async () => {
    if (updating || removing) {
      return;
    }

    try {
      setRemoving(true);
      setUpdateError("");

      const response = await cartApi.removeItem(item.productId, item.variantId);

      const apiResponse = response.data;

      if (!apiResponse?.success || !apiResponse?.data) {
        throw new Error(
          apiResponse?.message ?? "Không thể xóa sản phẩm khỏi giỏ hàng.",
        );
      }

      const updatedCart = apiResponse.data;

      onUpdated(updatedCart, null);
    } catch (error) {
      console.error("Unable to remove cart item:", error);

      setUpdateError(
        error.response?.data?.message ??
          error.message ??
          "Không thể xóa sản phẩm khỏi giỏ hàng.",
      );
    } finally {
      setRemoving(false);
    }
  };

  return (
    <article className="cart-item">
      <div className="cart-item__checkbox">
        <input
          type="checkbox"
          id={`cart-item-${itemKey}`}
          checked={selected}
          onChange={() => onSelect(itemKey)}
          aria-label={`Chọn ${item.productName}`}
        />
      </div>

      <div className="cart-item__info">
        <label htmlFor={`cart-item-${itemKey}`} className="cart-item__name">
          {item.productName}
        </label>

        {options.length > 0 && (
          <div className="cart-item__options">
            {options.map((option) => (
              <span
                key={`${option.optionTypeId}-${option.optionValueId}`}
                className="cart-item__option"
              >
                {option.optionTypeName}:{" "}
                <strong>{option.optionValueName}</strong>
              </span>
            ))}
          </div>
        )}

        {item.variant?.sku && (
          <p className="cart-item__sku">SKU: {item.variant.sku}</p>
        )}

        {updateError && (
          <p className="cart-item__update-error" role="alert">
            {updateError}
          </p>
        )}
      </div>

      <div className="cart-item__price">
        {Number(item.price).toLocaleString("vi-VN")} ₫
      </div>

      <div className="cart-item__quantity">
        <span className="cart-item__quantity-label">Số lượng:</span>

        <div className="cart-item__quantity-control">
          <button
            type="button"
            className="cart-item__quantity-button"
            onClick={handleDecrease}
            disabled={updating || item.quantity <= 1}
            aria-label="Giảm số lượng"
          >
            −
          </button>

          <span className="cart-item__quantity-value">{item.quantity}</span>

          <button
            type="button"
            className="cart-item__quantity-button"
            onClick={handleIncrease}
            disabled={updating}
            aria-label="Tăng số lượng"
          >
            +
          </button>
        </div>
      </div>

      <div className="cart-item__subtotal">
        {Number(item.subtotal).toLocaleString("vi-VN")} ₫
      </div>

      <div className="cart-item__actions">
        <button
          type="button"
          className="cart-item__remove-button"
          onClick={handleRemove}
          disabled={updating || removing}
        >
          {removing ? (
            <LoadingSpinner size="small" inline variant="light" />
          ) : (
            "Xóa"
          )}
        </button>
      </div>
    </article>
  );
}

export default CartItem;
