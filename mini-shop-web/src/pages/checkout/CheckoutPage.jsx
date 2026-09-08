import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import productApi from "../../api/productApi";
import orderApi from "../../api/orderApi";
import cartApi from "../../api/cartApi";
import addressApi from "../../api/addressApi";
import paymentApi from "../../api/paymentApi";
import voucherApi from "../../api/voucherApi";

import AddressSelectorModal from "../../components/address/AddressSelectorModal";
import AddAddressModal from "../../components/address/AddAddressModal";
import OrderSuccessModal from "../../components/order/OrderSuccessModal";

import "./CheckoutPage.css";

function formatCurrency(value) {
  return `${Number(value ?? 0).toLocaleString("vi-VN")} ₫`;
}

function getProductImage(product) {
  const images = Array.isArray(product?.images) ? product.images : [];

  if (images.length === 0) {
    return null;
  }

  const firstImage = images[0];

  if (typeof firstImage === "string") {
    return firstImage;
  }

  return (
    firstImage?.url ??
    firstImage?.imageUrl ??
    firstImage?.src ??
    firstImage?.path ??
    null
  );
}

function getVariant(product, variantId) {
  if (variantId == null) {
    return null;
  }

  const variants = Array.isArray(product?.variants) ? product.variants : [];

  return (
    variants.find((variant) => Number(variant.id) === Number(variantId)) ?? null
  );
}

function getVariantOptions(variant) {
  if (!Array.isArray(variant?.options)) {
    return "";
  }

  return variant.options
    .map((option) => option.optionValueName)
    .filter(Boolean)
    .join(", ");
}

function getItemPrice(product, variant) {
  if (variant?.price != null) {
    return Number(variant.price);
  }

  if (product?.salePrice != null) {
    return Number(product.salePrice);
  }

  return Number(product?.price ?? 0);
}

function getVoucherEligibleAmount(voucher, checkoutItems, totalProductAmount) {
  if (!voucher) {
    return 0;
  }

  if (voucher.scope === "PLATFORM") {
    return totalProductAmount;
  }

  if (voucher.scope === "SHOP") {
    return checkoutItems
      .filter((item) => Number(item.product?.shopId) === Number(voucher.shopId))
      .reduce((total, item) => total + item.subtotal, 0);
  }

  return 0;
}

function calculateVoucherDiscount(voucher, eligibleAmount) {
  if (!voucher || eligibleAmount <= 0) {
    return 0;
  }

  const minOrderAmount = Number(voucher.minOrderAmount ?? 0);

  if (eligibleAmount < minOrderAmount) {
    return 0;
  }

  // eslint-disable-next-line no-useless-assignment
  let discount = 0;

  if (voucher.discountType === "PERCENTAGE") {
    discount = (eligibleAmount * Number(voucher.discountValue ?? 0)) / 100;
  } else {
    discount = Number(voucher.discountValue ?? 0);
  }

  if (voucher.maxDiscountAmount != null) {
    discount = Math.min(discount, Number(voucher.maxDiscountAmount));
  }

  return Math.min(discount, eligibleAmount);
}

function CheckoutPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const checkoutData = location.state;
  const items = Array.isArray(checkoutData?.items) ? checkoutData.items : [];
  const checkoutType = checkoutData?.type ?? null;
  const [productDetails, setProductDetails] = useState({});
  const [loadingProducts, setLoadingProducts] = useState(true);
  const [productError, setProductError] = useState("");
  const [paymentMethod, setPaymentMethod] = useState("COD");
  const [placingOrder, setPlacingOrder] = useState(false);
  const [orderError, setOrderError] = useState("");
  const [createdOrder, setCreatedOrder] = useState(null);
  const [address, setAddress] = useState(null);
  const [loadingAddress, setLoadingAddress] = useState(true);
  const [addressError, setAddressError] = useState("");
  const [addresses, setAddresses] = useState([]);
  const [showAddressModal, setShowAddressModal] = useState(false);
  const [showAddAddressModal, setShowAddAddressModal] = useState(false);
  const [availableVouchers, setAvailableVouchers] = useState([]);
  const [selectedVoucherCode, setSelectedVoucherCode] = useState("");
  const [loadingVouchers, setLoadingVouchers] = useState(true);
  const [voucherError, setVoucherError] = useState("");
  const [voucherSelectionInitialized, setVoucherSelectionInitialized] =
    useState(false);

  useEffect(() => {
    if (items.length === 0) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setLoadingProducts(false);
      return;
    }

    let cancelled = false;

    const loadProducts = async () => {
      try {
        setLoadingProducts(true);
        setProductError("");

        const uniqueProductIds = [
          ...new Set(items.map((item) => item.productId)),
        ];

        const responses = await Promise.all(
          uniqueProductIds.map(async (productId) => {
            const response = await productApi.getById(productId);

            return {
              productId,
              product: response.data?.data ?? null,
            };
          }),
        );

        if (cancelled) {
          return;
        }

        const productMap = {};

        responses.forEach(({ productId, product }) => {
          if (product) {
            productMap[productId] = product;
          }
        });

        setProductDetails(productMap);
      } catch (error) {
        console.error("Unable to load checkout products:", error);

        if (!cancelled) {
          setProductError(
            error.response?.data?.message ??
              "Không thể tải thông tin sản phẩm. Vui lòng thử lại.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoadingProducts(false);
        }
      }
    };

    loadProducts();

    return () => {
      cancelled = true;
    };
  }, [items]);

  useEffect(() => {
    let cancelled = false;

    const loadAddresses = async () => {
      try {
        setLoadingAddress(true);
        setAddressError("");

        const response = await addressApi.getMyAddresses();

        const apiResponse = response.data;

        if (!apiResponse?.success) {
          throw new Error(
            apiResponse?.message ?? "Không thể tải địa chỉ nhận hàng.",
          );
        }

        const addressList = Array.isArray(apiResponse.data)
          ? apiResponse.data
          : [];

        if (cancelled) {
          return;
        }

        setAddresses(addressList);

        const defaultAddress =
          addressList.find((item) => item.defaultAddress === true) ??
          addressList[0] ??
          null;

        setAddress(defaultAddress);
      } catch (error) {
        console.error("Unable to load addresses:", error);

        if (!cancelled) {
          setAddresses([]);
          setAddress(null);

          setAddressError(
            error.response?.data?.message ??
              error.message ??
              "Không thể tải địa chỉ nhận hàng.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoadingAddress(false);
        }
      }
    };

    loadAddresses();

    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    let cancelled = false;

    const loadAvailableVouchers = async () => {
      try {
        setLoadingVouchers(true);
        setVoucherError("");

        const response = await voucherApi.getAvailable();

        const apiResponse = response.data;

        if (!apiResponse?.success) {
          throw new Error(apiResponse?.message || "Không thể tải voucher.");
        }

        if (!cancelled) {
          setAvailableVouchers(
            Array.isArray(apiResponse.data) ? apiResponse.data : [],
          );
        }
      } catch (error) {
        console.error("Không thể tải voucher checkout:", error);

        if (!cancelled) {
          setAvailableVouchers([]);

          setVoucherError(
            error.response?.data?.message ||
              error.message ||
              "Không thể tải voucher.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoadingVouchers(false);
        }
      }
    };

    void loadAvailableVouchers();

    return () => {
      cancelled = true;
    };
  }, []);

  const checkoutItems = useMemo(() => {
    return items.map((item) => {
      const product = productDetails[item.productId] ?? null;

      const variant = getVariant(product, item.variantId);

      const price = getItemPrice(product, variant);

      const quantity = Number(item.quantity ?? 0);

      return {
        ...item,
        product,
        variant,
        price,
        quantity,
        subtotal: price * quantity,
        image: getProductImage(product),
        variantOptions: getVariantOptions(variant),
      };
    });
  }, [items, productDetails]);

  const totalProductAmount = checkoutItems.reduce(
    (total, item) => total + item.subtotal,
    0,
  );

  const checkoutVouchers = useMemo(() => {
    return availableVouchers
      .map((voucher) => {
        const eligibleAmount = getVoucherEligibleAmount(
          voucher,
          checkoutItems,
          totalProductAmount,
        );

        const discountAmount = calculateVoucherDiscount(
          voucher,
          eligibleAmount,
        );

        const minOrderAmount = Number(voucher.minOrderAmount ?? 0);

        return {
          ...voucher,

          eligibleAmount,

          discountAmount,

          applicable:
            eligibleAmount > 0 &&
            eligibleAmount >= minOrderAmount &&
            discountAmount > 0,
        };
      })
      .sort((first, second) => second.discountAmount - first.discountAmount);
  }, [availableVouchers, checkoutItems, totalProductAmount]);

  useEffect(() => {
    if (voucherSelectionInitialized || loadingVouchers || loadingProducts) {
      return;
    }

    const bestVoucher = checkoutVouchers.find((voucher) => voucher.applicable);

    // eslint-disable-next-line react-hooks/set-state-in-effect
    setSelectedVoucherCode(bestVoucher?.code ?? "");

    setVoucherSelectionInitialized(true);
  }, [
    voucherSelectionInitialized,
    loadingVouchers,
    loadingProducts,
    checkoutVouchers,
  ]);

  const selectedVoucher = useMemo(
    () =>
      checkoutVouchers.find(
        (voucher) => voucher.code === selectedVoucherCode,
      ) ?? null,
    [checkoutVouchers, selectedVoucherCode],
  );

  const shippingFee = 0;

  const shippingDiscount = 0;

  const voucherDiscount = selectedVoucher?.applicable
    ? selectedVoucher.discountAmount
    : 0;

  const totalPayment =
    totalProductAmount + shippingFee - shippingDiscount - voucherDiscount;

  const handleBack = () => {
    if (checkoutType === "BUY_NOW") {
      const firstItem = items[0];

      if (firstItem?.productId != null) {
        navigate(`/products/${firstItem.productId}`);
        return;
      }

      navigate("/products");
      return;
    }

    if (checkoutType === "CART") {
      navigate("/cart");
      return;
    }

    navigate(-1);
  };

  const handleOpenAddressModal = () => {
    setShowAddressModal(true);
  };

  const handleCloseAddressModal = () => {
    setShowAddressModal(false);
  };

  const handleSelectAddress = (selectedAddress) => {
    setAddress(selectedAddress);
    setShowAddressModal(false);
  };

  const handleAddAddress = () => {
    setShowAddressModal(false);
    setShowAddAddressModal(true);
  };

  const handleAddressCreated = (createdAddress) => {
    setAddresses((previous) => {
      if (createdAddress.defaultAddress) {
        return [
          ...previous.map((item) => ({
            ...item,
            defaultAddress: false,
          })),
          createdAddress,
        ];
      }

      return [...previous, createdAddress];
    });

    setAddress(createdAddress);

    setShowAddAddressModal(false);
  };

  const clearPurchasedCartItems = async () => {
    if (checkoutType !== "CART") {
      return;
    }

    if (items.length === 0) {
      return;
    }

    const results = await Promise.allSettled(
      items.map((item) =>
        cartApi.removeItem(item.productId, item.variantId ?? null),
      ),
    );

    const failedItems = results.filter(
      (result) => result.status === "rejected",
    );

    if (failedItems.length > 0) {
      console.error(
        "Một số sản phẩm chưa được xóa khỏi giỏ hàng:",
        failedItems,
      );
    }
  };

  const handlePlaceOrder = async () => {
    if (placingOrder) {
      return;
    }

    try {
      setPlacingOrder(true);
      setOrderError("");

      const request = {
        items: checkoutItems.map((item) => ({
          productId: item.productId,
          variantId: item.variantId ?? null,
          quantity: item.quantity,
        })),

        addressId: address?.id ?? null,

        voucherCode: selectedVoucher?.applicable ? selectedVoucher.code : null,

        autoApplyBestVoucher: false,

        paymentMethod,
      };

      console.log("CREATE ORDER REQUEST:", request);

      const orderResponse = await orderApi.createOrder(request);

      console.log("CREATE ORDER RESPONSE:", orderResponse.data);

      const orderApiResponse = orderResponse.data;

      if (!orderApiResponse?.success || !orderApiResponse?.data) {
        throw new Error(
          orderApiResponse?.message ?? "Đặt hàng không thành công.",
        );
      }

      const createdOrder = orderApiResponse.data;

      console.log("ORDER CREATED:", createdOrder);

      if (paymentMethod === "COD") {
        await clearPurchasedCartItems();

        setCreatedOrder(createdOrder);

        return;
      }

      if (paymentMethod === "VNPAY") {
        console.log("CREATE VNPAY PAYMENT:", createdOrder.id);

        const paymentResponse = await paymentApi.createVnPayPayment(
          createdOrder.id,
        );

        console.log("CREATE VNPAY PAYMENT RESPONSE:", paymentResponse.data);

        const paymentData = paymentResponse.data;

        if (!paymentData?.paymentUrl) {
          throw new Error(
            paymentData?.responseMessage ??
              "VNPay không trả về đường dẫn thanh toán.",
          );
        }

        const paymentUrl = paymentData.paymentUrl;

        if (!paymentUrl) {
          throw new Error("VNPay không trả về đường dẫn thanh toán.");
        }

        console.log("VNPAY PAYMENT URL:", paymentUrl);

        await clearPurchasedCartItems();

        window.location.href = paymentUrl;

        return;
      }

      throw new Error(
        `Phương thức thanh toán "${paymentMethod}" chưa được hỗ trợ.`,
      );
    } catch (error) {
      console.error("Unable to create order/payment:", error);

      setOrderError(
        error.response?.data?.message ??
          error.message ??
          "Đặt hàng thất bại. Vui lòng thử lại.",
      );
    } finally {
      setPlacingOrder(false);
    }
  };

  if (!checkoutData || items.length === 0) {
    return (
      <main className="checkout-page">
        <div className="checkout-empty">
          <h1>Không có sản phẩm thanh toán</h1>

          <p>
            Vui lòng chọn sản phẩm hoặc sản phẩm trong giỏ hàng trước khi thanh
            toán.
          </p>

          <button type="button" onClick={() => navigate("/products")}>
            Tiếp tục mua sắm
          </button>
        </div>
      </main>
    );
  }

  return (
    <main className="checkout-page">
      <div className="checkout-container">
        {/* HEADER */}
        <header className="checkout-header">
          <button
            type="button"
            className="checkout-back-button"
            onClick={handleBack}
            aria-label="Quay lại"
          >
            ←
          </button>

          <h1 className="checkout-title">Thanh toán</h1>
        </header>

        <section className="checkout-section checkout-address-section">
          <div className="checkout-section__header">
            <h2> Địa chỉ nhận hàng</h2>

            <button
              type="button"
              className="checkout-section__arrow"
              aria-label="Chọn địa chỉ nhận hàng"
              onClick={handleOpenAddressModal}
            >
              ›
            </button>
          </div>

          {loadingAddress ? (
            <div className="checkout-loading">
              Đang tải địa chỉ nhận hàng...
            </div>
          ) : addressError ? (
            <div className="checkout-error" role="alert">
              {addressError}
            </div>
          ) : address ? (
            <div className="checkout-address">
              <div className="checkout-address__top">
                <strong>{address.receiverName}</strong>

                <span>{address.phone}</span>
              </div>

              <p>
                {address.detail}, {address.ward}, {address.province}
              </p>

              {address.defaultAddress && (
                <span className="checkout-address__default">Mặc định</span>
              )}
            </div>
          ) : (
            <div className="checkout-address-empty">
              <p>Chưa có địa chỉ nhận hàng.</p>

              <button type="button" onClick={handleOpenAddressModal}>
                + Chọn địa chỉ
              </button>
            </div>
          )}
        </section>

        {/* SẢN PHẨM */}
        <section className="checkout-section">
          <div className="checkout-section__header">
            <h2>Sản phẩm</h2>
          </div>

          {loadingProducts ? (
            <div className="checkout-loading">
              Đang tải thông tin sản phẩm...
            </div>
          ) : productError ? (
            <div className="checkout-error" role="alert">
              {productError}
            </div>
          ) : (
            <div className="checkout-items">
              {checkoutItems.map((item, index) => {
                const shopName = item.product?.shopName ?? "Shop Hair";

                return (
                  <article
                    className="checkout-shop"
                    key={`${item.productId}-${item.variantId ?? "default"}-${index}`}
                  >
                    <div className="checkout-shop__header">
                      <span className="checkout-shop__icon">🏪</span>

                      <strong>{shopName}</strong>
                    </div>

                    <div className="checkout-product">
                      <div className="checkout-product__image-wrapper">
                        {item.image ? (
                          <img
                            className="checkout-product__image"
                            src={item.image}
                            alt={item.product?.name ?? "Sản phẩm"}
                          />
                        ) : (
                          <div className="checkout-product__image-placeholder">
                            📦
                          </div>
                        )}
                      </div>

                      <div className="checkout-product__content">
                        <h3 className="checkout-product__name">
                          {item.product?.name ?? `Sản phẩm #${item.productId}`}
                        </h3>

                        {item.variantOptions && (
                          <p className="checkout-product__variant">
                            Phân loại: {item.variantOptions}
                          </p>
                        )}

                        {item.variant?.sku && (
                          <p className="checkout-product__sku">
                            SKU: {item.variant.sku}
                          </p>
                        )}

                        <div className="checkout-product__bottom">
                          <span className="checkout-product__price">
                            {formatCurrency(item.price)}
                          </span>

                          <span className="checkout-product__quantity">
                            x{item.quantity}
                          </span>

                          <strong className="checkout-product__subtotal">
                            {formatCurrency(item.subtotal)}
                          </strong>
                        </div>
                      </div>
                    </div>

                    <div className="checkout-shop__total">
                      <span>Tổng số tiền ({item.quantity} sản phẩm)</span>

                      <strong>{formatCurrency(item.subtotal)}</strong>
                    </div>
                  </article>
                );
              })}
            </div>
          )}
        </section>

        {/* VOUCHER */}
        <section className="checkout-section">
          <div className="checkout-section__header">
            <h2>Voucher</h2>
          </div>

          {loadingVouchers ? (
            <div className="checkout-loading">Đang tải voucher...</div>
          ) : voucherError ? (
            <div className="checkout-error" role="alert">
              {voucherError}
            </div>
          ) : availableVouchers.length === 0 ? (
            <p>Bạn chưa có voucher có thể sử dụng.</p>
          ) : (
            <div className="checkout-voucher">
              <select
                value={selectedVoucherCode}
                onChange={(event) => setSelectedVoucherCode(event.target.value)}
              >
                <option value="">Không sử dụng voucher</option>

                {checkoutVouchers.map((voucher) => (
                  <option
                    key={voucher.voucherId}
                    value={voucher.code}
                    disabled={!voucher.applicable}
                  >
                    {voucher.scope === "PLATFORM"
                      ? "Hair"
                      : voucher.shopName || "Shop"}
                    {" - "}
                    {voucher.code}
                    {" - "}

                    {voucher.applicable
                      ? `Giảm ${formatCurrency(voucher.discountAmount)}`
                      : "Không đủ điều kiện"}
                  </option>
                ))}
              </select>

              {selectedVoucher && (
                <div className="checkout-voucher__selected">
                  <strong>{selectedVoucher.code}</strong>

                  <span>Tiết kiệm {formatCurrency(voucherDiscount)}</span>
                </div>
              )}
            </div>
          )}
        </section>

        {/* PHƯƠNG THỨC THANH TOÁN */}
        <section className="checkout-section">
          <div className="checkout-section__header">
            <h2>Phương thức thanh toán</h2>
          </div>

          <div className="checkout-payment-methods">
            <label className="checkout-payment-option">
              <input
                type="radio"
                name="paymentMethod"
                value="COD"
                checked={paymentMethod === "COD"}
                onChange={(event) => setPaymentMethod(event.target.value)}
              />

              <span>Thanh toán khi nhận hàng (COD)</span>
            </label>

            <label className="checkout-payment-option">
              <input
                type="radio"
                name="paymentMethod"
                value="VNPAY"
                checked={paymentMethod === "VNPAY"}
                onChange={(event) => setPaymentMethod(event.target.value)}
              />

              <span>Thanh toán qua VNPay</span>
            </label>
          </div>
        </section>

        {/* CHI TIẾT THANH TOÁN */}
        <section className="checkout-summary">
          <h2>Chi tiết thanh toán</h2>

          <div className="checkout-summary__row">
            <span>Tổng tiền hàng</span>

            <strong>{formatCurrency(totalProductAmount)}</strong>
          </div>

          <div className="checkout-summary__row">
            <span>Tổng tiền phí vận chuyển</span>

            <strong>{formatCurrency(shippingFee)}</strong>
          </div>

          <div className="checkout-summary__row checkout-summary__discount">
            <span>Giảm giá phí vận chuyển</span>

            <strong>-{formatCurrency(shippingDiscount)}</strong>
          </div>

          <div className="checkout-summary__row checkout-summary__discount">
            <span>Tổng cộng Voucher giảm giá</span>

            <strong>-{formatCurrency(voucherDiscount)}</strong>
          </div>

          <div className="checkout-summary__divider" />

          <div className="checkout-summary__row checkout-summary__total">
            <span>Tổng thanh toán</span>

            <strong>{formatCurrency(totalPayment)}</strong>
          </div>
        </section>

        {/* ĐIỀU KHOẢN */}
        <p className="checkout-terms">
          Nhấn "Đặt hàng" đồng nghĩa với việc bạn đồng ý đặt hàng tại Hair.
        </p>
      </div>

      {/* FOOTER ĐẶT HÀNG */}
      {orderError && (
        <div className="checkout-error" role="alert">
          {orderError}
        </div>
      )}
      <div className="checkout-footer">
        <div className="checkout-footer__total">
          <span>Tổng cộng</span>

          <strong>{formatCurrency(totalPayment)}</strong>
        </div>

        <button
          type="button"
          className="checkout-submit"
          disabled={
            loadingProducts ||
            !!productError ||
            loadingAddress ||
            !!addressError ||
            !address ||
            placingOrder
          }
          onClick={handlePlaceOrder}
        >
          {placingOrder ? "Đang đặt hàng..." : "Đặt hàng"}
        </button>
      </div>

      <AddressSelectorModal
        open={showAddressModal}
        addresses={addresses}
        selectedAddressId={address?.id ?? null}
        onSelect={handleSelectAddress}
        onClose={handleCloseAddressModal}
        onAddAddress={handleAddAddress}
      />
      <AddAddressModal
        open={showAddAddressModal}
        onClose={() => setShowAddAddressModal(false)}
        onCreated={handleAddressCreated}
      />

      <OrderSuccessModal
        open={!!createdOrder}
        onContinueShopping={() => {
          setCreatedOrder(null);
          navigate("/products");
        }}
        onViewOrder={() => {
          setCreatedOrder(null);
          navigate("/customer/orders");
        }}
      />
    </main>
  );
}

export default CheckoutPage;
