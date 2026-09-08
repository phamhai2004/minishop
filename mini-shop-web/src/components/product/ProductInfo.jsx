import { useEffect, useMemo, useRef, useState } from "react";

import { useAuth } from "../../contexts/AuthContext";
import ROLES from "../../components/constants/roles";
import cartApi from "../../api/cartApi";
import { useNavigate } from "react-router-dom";

import {
  PENDING_ACTION_TYPES,
  savePendingProductAction,
} from "../../utils/pendingProductAction";

import AuthRequiredModal from "../auth/AuthRequiredModal";

import "./ProductInfo.css";

export default function ProductInfo({ product }) {
  const { currentUser, isAuthenticated } = useAuth();

  const [showAuthModal, setShowAuthModal] = useState(false);

  const [showCartSuccess, setShowCartSuccess] = useState(false);

  const cartSuccessTimerRef = useRef(null);

  const [purchaseQuantity, setPurchaseQuantity] = useState(1);

  const userRole = currentUser?.role ?? null;

  const isGuest = !isAuthenticated;
  const isCustomer = userRole === ROLES.CUSTOMER;

  const variants = Array.isArray(product.variants) ? product.variants : [];

  const hasVariants = variants.length > 0;

  const navigate = useNavigate();

  const increaseQuantity = () => {
    if (purchaseQuantity < currentQuantity) {
      setPurchaseQuantity((prev) => prev + 1);
    }
  };

  const decreaseQuantity = () => {
    setPurchaseQuantity((prev) => Math.max(1, prev - 1));
  };

  const optionGroups = useMemo(() => {
    const groups = new Map();

    variants.forEach((variant) => {
      variant.options?.forEach((option) => {
        if (!groups.has(option.optionTypeId)) {
          groups.set(option.optionTypeId, {
            id: option.optionTypeId,
            name: option.optionTypeName,
            values: [],
          });
        }

        const group = groups.get(option.optionTypeId);

        const exists = group.values.some(
          (value) => value.id === option.optionValueId,
        );

        if (!exists) {
          group.values.push({
            id: option.optionValueId,
            name: option.optionValueName,
          });
        }
      });
    });

    return Array.from(groups.values());
  }, [variants]);

  const [selectedOptions, setSelectedOptions] = useState({});

  useEffect(() => {
    setSelectedOptions({});
  }, [product.id]);

  const selectedVariant = useMemo(() => {
    if (!hasVariants) {
      return null;
    }

    return variants.find((variant) => {
      return optionGroups.every((group) => {
        const selectedValueId = selectedOptions[group.id];

        return variant.options?.some(
          (option) =>
            option.optionTypeId === group.id &&
            option.optionValueId === selectedValueId,
        );
      });
    });
  }, [hasVariants, variants, optionGroups, selectedOptions]);

  useEffect(() => {
    setPurchaseQuantity(1);
  }, [selectedVariant]);

  useEffect(() => {
    return () => {
      if (cartSuccessTimerRef.current) {
        clearTimeout(cartSuccessTimerRef.current);
      }
    };
  }, []);

  const minVariantPrice = hasVariants
    ? variants.reduce(
        (min, variant) => Math.min(min, Number(variant.price ?? 0)),
        Infinity,
      )
    : null;

  const normalPrice = hasVariants
    ? (selectedVariant?.price ??
      (Number.isFinite(minVariantPrice) ? minVariantPrice : 0))
    : (product.price ?? 0);

  const hasFlashSale =
    Boolean(product.flashSale) &&
    product.salePrice !== null &&
    product.salePrice !== undefined &&
    Number(product.salePrice) < Number(normalPrice);

  const currentPrice = hasFlashSale
    ? Number(product.salePrice)
    : Number(normalPrice);

  const normalQuantity = hasVariants
    ? Number(selectedVariant?.quantity ?? 0)
    : Number(product.quantity ?? 0);

  const remainingFlashSaleQuantity =
    hasFlashSale &&
    product.flashSaleQuantity !== null &&
    product.flashSaleQuantity !== undefined
      ? Math.max(
          Number(product.flashSaleQuantity) -
            Number(product.flashSaleSold ?? 0),
          0,
        )
      : null;

  const currentQuantity =
    remainingFlashSaleQuantity !== null
      ? Math.min(normalQuantity, remainingFlashSaleQuantity)
      : normalQuantity;

  const formattedPrice = Number(currentPrice).toLocaleString("vi-VN", {
    style: "currency",
    currency: "VND",
  });

  const originalPrice = Number(normalPrice).toLocaleString("vi-VN", {
    style: "currency",
    currency: "VND",
  });

  const isOutOfStock = Number(currentQuantity) <= 0;

  const hasSelectedVariant = !hasVariants || selectedVariant != null;

  const canPurchase =
    hasSelectedVariant && !isOutOfStock && (isGuest || isCustomer);

  const handleOptionSelect = (optionTypeId, optionValueId) => {
    setSelectedOptions((prev) => ({
      ...prev,
      [optionTypeId]: optionValueId,
    }));
  };

  const handleBuyNow = () => {
    if (!canPurchase) {
      return;
    }

    if (isGuest) {
      savePendingProductAction(
        PENDING_ACTION_TYPES.BUY_NOW,
        product.id,
        selectedVariant?.id ?? null,
        purchaseQuantity,
      );

      setShowAuthModal(true);

      return;
    }

    navigate("/checkout", {
      state: {
        type: "BUY_NOW",
        items: [
          {
            productId: product.id,
            variantId: selectedVariant?.id ?? null,
            quantity: purchaseQuantity,
          },
        ],
      },
    });
  };

  const handleAddToCart = async () => {
    if (!canPurchase) {
      return;
    }

    if (isGuest) {
      savePendingProductAction(
        PENDING_ACTION_TYPES.ADD_TO_CART,
        product.id,
        selectedVariant?.id ?? null,
        purchaseQuantity,
      );

      setShowAuthModal(true);

      return;
    }

    try {
      const response = await cartApi.addToCart({
        productId: product.id,
        variantId: selectedVariant?.id ?? null,
        quantity: purchaseQuantity,
      });

      console.log("Add to cart success:", response.data);

      setShowCartSuccess(true);

      window.dispatchEvent(new Event("cart:added"));

      if (cartSuccessTimerRef.current) {
        clearTimeout(cartSuccessTimerRef.current);
      }

      cartSuccessTimerRef.current = setTimeout(() => {
        setShowCartSuccess(false);
      }, 1500);
    } catch (error) {
      console.error("Add to cart failed:", error);

      const message =
        error.response?.data?.message ??
        "Không thể thêm sản phẩm vào giỏ hàng.";

      console.error(message);
    }
  };

  return (
    <>
      <div className="product-info">
        <h1>{product.name}</h1>

        {product.categoryName && (
          <p>
            <strong>Danh mục:</strong> {product.categoryName}
          </p>
        )}

        {product.shopName && (
          <p>
            <strong>Shop:</strong> {product.shopName}
          </p>
        )}

        <div className="sale-price">{formattedPrice}</div>

        {hasFlashSale && <div className="original-price">{originalPrice}</div>}

        <p>
          <strong>Trạng thái:</strong> {isOutOfStock ? "Hết hàng" : "Còn hàng"}
        </p>

        {hasFlashSale && remainingFlashSaleQuantity !== null && (
          <div className="product-info__flash-sale">
            <strong>⚡ Flash Sale</strong>

            <span>Còn {remainingFlashSaleQuantity} sản phẩm giá ưu đãi</span>
          </div>
        )}

        {hasVariants && (
          <div className="product-options">
            {optionGroups.map((group) => (
              <div className="product-option-group" key={group.id}>
                <div className="product-option-title">{group.name}</div>

                <div className="product-option-values">
                  {group.values.map((value) => {
                    const isSelected = selectedOptions[group.id] === value.id;

                    return (
                      <button
                        key={value.id}
                        type="button"
                        className={`product-option-value ${
                          isSelected ? "selected" : ""
                        }`}
                        onClick={() => handleOptionSelect(group.id, value.id)}
                      >
                        {value.name}
                      </button>
                    );
                  })}
                </div>
              </div>
            ))}
          </div>
        )}

        {hasVariants && (
          <div className="variant-stock">
            <strong>Số lượng:</strong>{" "}
            {selectedVariant
              ? selectedVariant.quantity
              : "Vui lòng chọn đầy đủ phân loại"}
          </div>
        )}

        <div className="purchase-quantity">
          <span className="purchase-quantity__label">Số lượng</span>

          <div className="purchase-quantity__controls">
            <button
              type="button"
              onClick={decreaseQuantity}
              disabled={purchaseQuantity <= 1}
            >
              −
            </button>

            <span>{purchaseQuantity}</span>

            <button
              type="button"
              onClick={increaseQuantity}
              disabled={purchaseQuantity >= currentQuantity}
            >
              +
            </button>
          </div>
        </div>

        <div className="product-info__actions">
          <button
            type="button"
            className="product-info__buy-button"
            onClick={handleBuyNow}
            disabled={!hasSelectedVariant || isOutOfStock}
          >
            Mua ngay
          </button>

          <button
            type="button"
            className="product-info__cart-button"
            onClick={handleAddToCart}
            disabled={!hasSelectedVariant || isOutOfStock}
          >
            Thêm vào giỏ hàng
          </button>
        </div>

        {product.description && (
          <section className="product-info__description">
            <h2>MÔ TẢ SẢN PHẨM</h2>
            <div className="product-info__description-content">
              {product.description}
            </div>
          </section>
        )}
      </div>

      {showCartSuccess && (
        <div className="add-cart-success" role="status" aria-live="polite">
          <div className="add-cart-success__box">
            <div className="add-cart-success__check">✓</div>

            <div className="add-cart-success__message">
              Sản phẩm đã được thêm vào Giỏ hàng
            </div>
          </div>
        </div>
      )}

      <AuthRequiredModal
        isOpen={showAuthModal}
        onClose={() => setShowAuthModal(false)}
      />
    </>
  );
}
