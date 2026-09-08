import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import sellerOrderApi from "../../../api/sellerOrderApi";
import LoadingSpinner from "../../../components/common/LoadingSpinner";

import "./SellerOrderDetailPage.css";

function formatCurrency(value) {
  return `${Number(value ?? 0).toLocaleString("vi-VN")} ₫`;
}

function formatDate(value) {
  if (!value) {
    return "—";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function getStatusClass(status) {
  switch (String(status ?? "").toUpperCase()) {
    case "PENDING":
      return "seller-order-detail__status--pending";

    case "CONFIRMED":
      return "seller-order-detail__status--confirmed";

    case "PACKING":
      return "seller-order-detail__status--packing";

    case "SHIPPING":
      return "seller-order-detail__status--shipping";

    case "DELIVERED":
      return "seller-order-detail__status--delivered";

    case "COMPLETED":
      return "seller-order-detail__status--completed";

    case "CANCELLED":
      return "seller-order-detail__status--cancelled";

    default:
      return "";
  }
}

function getPaymentStatusText(status) {
  const normalized = String(status ?? "").toUpperCase();

  switch (normalized) {
    case "PAID":
      return "Đã thanh toán";

    case "PENDING":
      return "Chưa thanh toán";

    case "FAILED":
      return "Thanh toán thất bại";

    case "CANCELLED":
      return "Đã hủy thanh toán";

    case "REFUNDED":
      return "Đã hoàn tiền";

    default:
      return status || "Chưa xác định";
  }
}

function getPaymentStatusClass(status) {
  const normalized = String(status ?? "").toUpperCase();

  if (normalized === "PAID") {
    return "seller-order-detail__payment-status--paid";
  }

  if (normalized === "PENDING") {
    return "seller-order-detail__payment-status--pending";
  }

  if (normalized === "FAILED" || normalized === "CANCELLED") {
    return "seller-order-detail__payment-status--failed";
  }

  return "";
}

function getTotalItems(items) {
  if (!Array.isArray(items)) {
    return 0;
  }

  return items.reduce((total, item) => total + Number(item.quantity ?? 0), 0);
}

function getVariantText(variant) {
  if (!variant) {
    return "";
  }

  if (!Array.isArray(variant.options)) {
    return "";
  }

  return variant.options
    .map((option) => {
      const typeName = option?.optionTypeName ?? "";

      const valueName = option?.optionValueName ?? "";

      if (typeName && valueName) {
        return `${typeName}: ${valueName}`;
      }

      return valueName;
    })
    .filter(Boolean)
    .join(" / ");
}

function SellerOrderDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [order, setOrder] = useState(null);

  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  const [error, setError] = useState("");
  const [actionError, setActionError] = useState("");

  const [showCancelModal, setShowCancelModal] = useState(false);

  const [cancelReason, setCancelReason] = useState("");

  const loadOrder = useCallback(async () => {
    if (!id) {
      setError("Không tìm thấy mã đơn hàng.");
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError("");

      const response = await sellerOrderApi.getById(id);

      const apiResponse = response.data;

      if (!apiResponse?.success) {
        throw new Error(
          apiResponse?.message ?? "Không thể tải thông tin đơn hàng.",
        );
      }

      if (!apiResponse.data) {
        throw new Error("Dữ liệu đơn hàng không hợp lệ.");
      }

      setOrder(apiResponse.data);
    } catch (err) {
      console.error("Unable to load seller order:", err);

      setOrder(null);

      setError(
        err.response?.data?.message ??
          err.message ??
          "Không thể tải thông tin đơn hàng.",
      );
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadOrder();
  }, [loadOrder]);

  const executeAction = async (action) => {
    if (!order?.shopOrderId) {
      return;
    }

    try {
      setActionLoading(true);
      setActionError("");

      switch (action) {
        case "confirm":
          await sellerOrderApi.confirm(order.shopOrderId);
          break;

        case "packing":
          await sellerOrderApi.startPacking(order.shopOrderId);
          break;

        case "shipping":
          await sellerOrderApi.startShipping(order.shopOrderId);
          break;

        case "delivered":
          await sellerOrderApi.delivered(order.shopOrderId);
          break;

        default:
          return;
      }

      await loadOrder();
    } catch (err) {
      console.error("Unable to update seller order:", err);

      setActionError(
        err.response?.data?.message ??
          err.message ??
          "Không thể cập nhật trạng thái đơn hàng.",
      );
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancelOrder = async () => {
    const reason = cancelReason.trim();

    if (!reason) {
      setActionError("Vui lòng nhập lý do hủy đơn.");
      return;
    }

    if (!order?.shopOrderId) {
      return;
    }

    try {
      setActionLoading(true);
      setActionError("");

      await sellerOrderApi.cancel(order.shopOrderId, reason);

      setShowCancelModal(false);
      setCancelReason("");

      await loadOrder();
    } catch (err) {
      console.error("Unable to cancel seller order:", err);

      setActionError(
        err.response?.data?.message ?? err.message ?? "Không thể hủy đơn hàng.",
      );
    } finally {
      setActionLoading(false);
    }
  };

  const handleBack = () => {
    navigate("/seller/orders");
  };

  const renderActionButtons = () => {
    if (!order) {
      return null;
    }

    const status = String(order.status ?? "").toUpperCase();

    const buttons = [];

    if (status === "PENDING") {
      buttons.push(
        <button
          key="confirm"
          type="button"
          className="seller-order-detail__action-button seller-order-detail__action-button--primary"
          disabled={actionLoading}
          onClick={() => executeAction("confirm")}
        >
          {actionLoading ? (
            <LoadingSpinner size="small" inline variant="light" />
          ) : (
            "✓ Xác nhận đơn"
          )}
        </button>,
      );
    }

    if (status === "CONFIRMED") {
      buttons.push(
        <button
          key="packing"
          type="button"
          className="seller-order-detail__action-button seller-order-detail__action-button--primary"
          disabled={actionLoading}
          onClick={() => executeAction("packing")}
        >
          {actionLoading ? (
            <LoadingSpinner size="small" inline variant="light" />
          ) : (
            "Bắt đầu đóng gói"
          )}
        </button>,
      );
    }

    if (status === "PACKING") {
      buttons.push(
        <button
          key="shipping"
          type="button"
          className="seller-order-detail__action-button seller-order-detail__action-button--primary"
          disabled={actionLoading}
          onClick={() => executeAction("shipping")}
        >
          {actionLoading ? (
            <LoadingSpinner size="small" inline variant="light" />
          ) : (
            "Bắt đầu giao hàng"
          )}
        </button>,
      );
    }

    if (status === "SHIPPING") {
      buttons.push(
        <button
          key="delivered"
          type="button"
          className="seller-order-detail__action-button seller-order-detail__action-button--success"
          disabled={actionLoading}
          onClick={() => executeAction("delivered")}
        >
          {actionLoading ? (
            <LoadingSpinner size="small" inline variant="light" />
          ) : (
            "Xác nhận đã giao hàng"
          )}
        </button>,
      );
    }

    if (status === "PENDING" || status === "CONFIRMED") {
      buttons.push(
        <button
          key="cancel"
          type="button"
          className="seller-order-detail__action-button seller-order-detail__action-button--danger"
          disabled={actionLoading}
          onClick={() => {
            setActionError("");
            setShowCancelModal(true);
          }}
        >
          Hủy đơn
        </button>,
      );
    }

    return buttons;
  };

  if (loading) {
    return (
      <main className="seller-order-detail-page">
        <div className="seller-order-detail__loading">
          <LoadingSpinner size="large" />
        </div>
      </main>
    );
  }

  if (error || !order) {
    return (
      <main className="seller-order-detail-page">
        <div className="seller-order-detail__container">
          <button
            type="button"
            className="seller-order-detail__back-button"
            onClick={handleBack}
          >
            ← Quay lại đơn hàng
          </button>

          <section className="seller-order-detail__error">
            <div className="seller-order-detail__error-icon">!</div>

            <h1>Không thể tải đơn hàng</h1>

            <p>{error || "Không tìm thấy đơn hàng."}</p>

            <button
              type="button"
              className="seller-order-detail__action-button seller-order-detail__action-button--primary"
              onClick={loadOrder}
            >
              Thử lại
            </button>
          </section>
        </div>
      </main>
    );
  }

  const items = Array.isArray(order.items) ? order.items : [];

  const status = String(order.status ?? "").toUpperCase();

  return (
    <main className="seller-order-detail-page">
      <div className="seller-order-detail__container">
        {/* BACK */}
        <button
          type="button"
          className="seller-order-detail__back-button"
          onClick={handleBack}
        >
          ← Quay lại danh sách đơn hàng
        </button>

        {/* HEADER */}
        <header className="seller-order-detail__header">
          <div>
            <div className="seller-order-detail__title-row">
              <h1>Đơn hàng #{order.orderCode ?? "—"}</h1>

              <span
                className={`seller-order-detail__status ${getStatusClass(
                  status,
                )}`}
              >
                {order.statusName ?? order.status ?? "Chưa xác định"}
              </span>
            </div>
          </div>
        </header>

        {/* ACTION ERROR */}
        {actionError && (
          <section className="seller-order-detail__action-error">
            <span>!</span>
            <p>{actionError}</p>

            <button type="button" onClick={() => setActionError("")}>
              ×
            </button>
          </section>
        )}

        {/* ACTIONS */}
        {renderActionButtons().length > 0 && (
          <section className="seller-order-detail__actions">
            <div className="seller-order-detail__actions-title">
              <span>Trạng thái xử lý</span>

              <strong>{order.statusName ?? order.status}</strong>
            </div>

            <div className="seller-order-detail__actions-buttons">
              {renderActionButtons()}
            </div>
          </section>
        )}

        {/* MAIN GRID */}
        <div className="seller-order-detail__grid">
          {/* CUSTOMER */}
          <section className="seller-order-detail__card">
            <div className="seller-order-detail__card-title">
              <h2>Thông tin khách hàng</h2>
            </div>

            <div className="seller-order-detail__customer-profile">
              <div className="seller-order-detail__customer-avatar">
                {order.customerAvatarUrl ? (
                  <img
                    src={order.customerAvatarUrl}
                    alt={order.customerName ?? "Khách hàng"}
                  />
                ) : (
                  <span>👤</span>
                )}
              </div>

              <div className="seller-order-detail__info-list">
                <div>
                  <span>Khách hàng</span>
                  <strong>{order.customerName ?? "—"}</strong>
                </div>

                <div>
                  <span>Số điện thoại</span>
                  <strong>{order.customerPhone ?? "—"}</strong>
                </div>
              </div>
            </div>
          </section>

          {/* RECEIVER */}
          <section className="seller-order-detail__card">
            <div className="seller-order-detail__card-title">
              <span>📍</span>
              <h2>Địa chỉ nhận hàng</h2>
            </div>

            <div className="seller-order-detail__info-list">
              <div>
                <span>Người nhận</span>
                <strong>{order.receiverName ?? "—"}</strong>
              </div>

              <div>
                <span>Số điện thoại</span>
                <strong>{order.receiverPhone ?? "—"}</strong>
              </div>

              <div className="seller-order-detail__info-full">
                <span>Địa chỉ</span>
                <strong>{order.shippingAddress ?? "—"}</strong>
              </div>
            </div>
          </section>

          {/* PAYMENT */}
          <section className="seller-order-detail__card">
            <div className="seller-order-detail__card-title">
              <span>💳</span>
              <h2>Thanh toán</h2>
            </div>

            <div className="seller-order-detail__info-list">
              <div>
                <span>Phương thức</span>
                <strong>{order.paymentMethod ?? "—"}</strong>
              </div>

              <div>
                <span>Trạng thái</span>

                <strong
                  className={`seller-order-detail__payment-status ${getPaymentStatusClass(
                    order.paymentStatus,
                  )}`}
                >
                  {getPaymentStatusText(order.paymentStatus)}
                </strong>
              </div>
            </div>
          </section>

          {/* DATE */}
          <section className="seller-order-detail__card">
            <div className="seller-order-detail__card-title">
              <span>🕐</span>
              <h2>Thời gian</h2>
            </div>

            <div className="seller-order-detail__info-list">
              <div>
                <span>Ngày đặt</span>
                <strong>{formatDate(order.createdAt)}</strong>
              </div>
            </div>
          </section>
        </div>

        {/* PRODUCTS */}
        <section className="seller-order-detail__card seller-order-detail__products">
          <div className="seller-order-detail__card-title">
            <span>🛍️</span>
            <h2>Sản phẩm</h2>

            <span className="seller-order-detail__item-count">
              {getTotalItems(items)} sản phẩm
            </span>
          </div>

          {items.length === 0 ? (
            <div className="seller-order-detail__no-items">
              Không có sản phẩm.
            </div>
          ) : (
            <div className="seller-order-detail__items">
              {items.map((item) => {
                const variantText = getVariantText(item.variant);

                return (
                  <div
                    className="seller-order-detail__item"
                    key={item.id ?? `${item.productId}-${item.variantId}`}
                  >
                    <div className="seller-order-detail__item-image">
                      {item.imageUrl ? (
                        <img
                          src={item.imageUrl}
                          alt={item.productName ?? "Sản phẩm"}
                        />
                      ) : (
                        <span>📦</span>
                      )}
                    </div>

                    <div className="seller-order-detail__item-content">
                      <h3>{item.productName ?? "Sản phẩm"}</h3>

                      {variantText && <p>Phân loại: {variantText}</p>}

                      {item.variantId && !variantText && (
                        <p>Phân loại #{item.variantId}</p>
                      )}

                      <div className="seller-order-detail__item-meta">
                        <span>SL: {item.quantity}</span>

                        <span>Đơn giá: {formatCurrency(item.price)}</span>
                      </div>
                    </div>

                    <div className="seller-order-detail__item-subtotal">
                      {formatCurrency(item.subtotal)}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </section>

        {/* PAYMENT SUMMARY */}
        <section className="seller-order-detail__card seller-order-detail__summary-card">
          <div className="seller-order-detail__card-title">
            <span>🧾</span>
            <h2>Chi tiết thanh toán</h2>
          </div>

          <div className="seller-order-detail__summary">
            <div>
              <span>Tạm tính</span>
              <strong>{formatCurrency(order.subtotal)}</strong>
            </div>

            <div>
              <span>Giảm giá</span>
              <strong>- {formatCurrency(order.discountAmount)}</strong>
            </div>

            <div>
              <span>Phí vận chuyển</span>
              <strong>{formatCurrency(order.shippingFee)}</strong>
            </div>

            <div className="seller-order-detail__summary-total">
              <span>Tổng thanh toán</span>
              <strong>{formatCurrency(order.finalAmount)}</strong>
            </div>
          </div>
        </section>
      </div>

      {/* CANCEL MODAL */}
      {showCancelModal && (
        <div
          className="seller-order-detail__modal-overlay"
          role="presentation"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) {
              if (!actionLoading) {
                setShowCancelModal(false);
              }
            }
          }}
        >
          <div
            className="seller-order-detail__modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="cancel-order-title"
          >
            <div className="seller-order-detail__modal-header">
              <div>
                <h2 id="cancel-order-title">Hủy đơn hàng</h2>

                <p>Đơn #{order.orderCode ?? "—"}</p>
              </div>

              <button
                type="button"
                className="seller-order-detail__modal-close"
                disabled={actionLoading}
                onClick={() => setShowCancelModal(false)}
              >
                ×
              </button>
            </div>

            <div className="seller-order-detail__modal-body">
              <label htmlFor="cancel-reason">Lý do hủy đơn</label>

              <textarea
                id="cancel-reason"
                value={cancelReason}
                onChange={(event) => setCancelReason(event.target.value)}
                placeholder="Nhập lý do hủy đơn..."
                rows={5}
                disabled={actionLoading}
                maxLength={500}
              />

              <div className="seller-order-detail__modal-counter">
                {cancelReason.length}/500
              </div>
            </div>

            <div className="seller-order-detail__modal-footer">
              <button
                type="button"
                className="seller-order-detail__modal-button seller-order-detail__modal-button--secondary"
                disabled={actionLoading}
                onClick={() => setShowCancelModal(false)}
              >
                Không hủy
              </button>

              <button
                type="button"
                className="seller-order-detail__modal-button seller-order-detail__modal-button--danger"
                disabled={actionLoading || !cancelReason.trim()}
                onClick={handleCancelOrder}
              >
                {actionLoading ? (
                  <LoadingSpinner size="small" inline variant="light" />
                ) : (
                  "Xác nhận hủy"
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  );
}

export default SellerOrderDetailPage;
