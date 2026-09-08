import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import orderApi from "../../api/orderApi";
import ReviewModal from "../../components/review/ReviewModal";
import LoadingSpinner from "../../components/common/LoadingSpinner";

import "./OrderDetailPage.css";

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
  const value = String(status ?? "").toUpperCase();

  if (value.includes("CANCEL") || value.includes("REJECT")) {
    return "order-detail__status--cancelled";
  }

  if (
    value.includes("DELIVERED") ||
    value.includes("COMPLETED") ||
    value.includes("SUCCESS")
  ) {
    return "order-detail__status--success";
  }

  if (value.includes("SHIPPING") || value.includes("DELIVERING")) {
    return "order-detail__status--shipping";
  }

  if (value.includes("CONFIRM") || value.includes("PROCESS")) {
    return "order-detail__status--processing";
  }

  return "order-detail__status--pending";
}

function getStatusText(order) {
  return order?.statusName ?? order?.status ?? "Chưa xác định";
}

function getPaymentStatusText(status) {
  const value = String(status ?? "").toUpperCase();

  if (value === "PAID") {
    return "Đã thanh toán";
  }

  if (value === "PENDING") {
    return "Chưa thanh toán";
  }

  if (value === "FAILED" || value === "CANCELLED") {
    return "Thanh toán thất bại";
  }

  return status || "Chưa xác định";
}

function getTotal(order) {
  if (order?.finalAmount != null) {
    return order.finalAmount;
  }

  if (order?.totalAmount != null) {
    return order.totalAmount;
  }

  return 0;
}

function OrderDetailPage() {
  const { orderCode } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [cancelling, setCancelling] = useState(false);
  const [cancelError, setCancelError] = useState("");
  const [confirmingReceived, setConfirmingReceived] = useState(false);
  const [confirmReceivedError, setConfirmReceivedError] = useState("");
  const [reviewModal, setReviewModal] = useState({
    open: false,
    product: null,
  });
  const [reviewedProductIds, setReviewedProductIds] = useState([]);
  const [confirmModal, setConfirmModal] = useState({
    open: false,
    title: "",
    message: "",
    confirmText: "Xác nhận",
    cancelText: "Hủy",
    type: "default",
    onConfirm: null,
  });

  useEffect(() => {
    let cancelled = false;

    const loadOrder = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await orderApi.getByOrderCode(orderCode);

        const apiResponse = response.data;

        if (!apiResponse?.success || !apiResponse?.data) {
          throw new Error(
            apiResponse?.message ?? "Không thể tải thông tin đơn hàng.",
          );
        }

        if (!cancelled) {
          setOrder(apiResponse.data);
        }
      } catch (err) {
        console.error("Unable to load order detail:", err);

        if (!cancelled) {
          setError(
            err.response?.data?.message ??
              err.message ??
              "Không thể tải thông tin đơn hàng.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    if (orderCode) {
      loadOrder();
    }

    return () => {
      cancelled = true;
    };
  }, [orderCode]);

  const openConfirmModal = ({
    title,
    message,
    confirmText = "Xác nhận",
    cancelText = "Hủy",
    type = "default",
    onConfirm,
  }) => {
    setConfirmModal({
      open: true,
      title,
      message,
      confirmText,
      cancelText,
      type,
      onConfirm,
    });
  };

  const closeConfirmModal = () => {
    setConfirmModal((prev) => ({
      ...prev,
      open: false,
    }));
  };

  const handleCancelOrder = () => {
    if (!order?.orderCode || cancelling) {
      return;
    }

    openConfirmModal({
      title: "Xác nhận hủy đơn",
      message: `Bạn có chắc muốn hủy đơn hàng #${order.orderCode}?`,
      confirmText: "Hủy đơn",
      cancelText: "Quay lại",
      type: "danger",
      onConfirm: async () => {
        try {
          setCancelling(true);
          setCancelError("");

          const response = await orderApi.cancelOrder(order.orderCode);

          const apiResponse = response.data;

          if (!apiResponse?.success) {
            throw new Error(apiResponse?.message ?? "Không thể hủy đơn hàng.");
          }

          if (apiResponse.data) {
            setOrder(apiResponse.data);
          } else {
            const detailResponse = await orderApi.getByOrderCode(
              order.orderCode,
            );

            if (detailResponse.data?.data) {
              setOrder(detailResponse.data.data);
            }
          }

          closeConfirmModal();
        } catch (err) {
          console.error("Unable to cancel order:", err);

          setCancelError(
            err.response?.data?.message ??
              err.message ??
              "Không thể hủy đơn hàng.",
          );

          closeConfirmModal();
        } finally {
          setCancelling(false);
        }
      },
    });
  };

  const handleConfirmReceived = () => {
    if (!order?.orderCode || confirmingReceived) {
      return;
    }

    openConfirmModal({
      title: "Xác nhận nhận hàng",
      message: `Bạn xác nhận đã nhận được đơn hàng #${order.orderCode}?`,
      confirmText: "Xác nhận",
      cancelText: "Hủy",
      type: "success",
      onConfirm: async () => {
        try {
          setConfirmingReceived(true);
          setConfirmReceivedError("");

          const response = await orderApi.confirmReceived(order.orderCode);

          const apiResponse = response.data;

          if (!apiResponse?.success) {
            throw new Error(
              apiResponse?.message ?? "Không thể xác nhận đã nhận hàng.",
            );
          }

          if (apiResponse.data) {
            setOrder(apiResponse.data);
          } else {
            const detailResponse = await orderApi.getByOrderCode(
              order.orderCode,
            );

            if (detailResponse.data?.data) {
              setOrder(detailResponse.data.data);
            }
          }

          closeConfirmModal();
        } catch (err) {
          console.error("Unable to confirm received order:", err);

          setConfirmReceivedError(
            err.response?.data?.message ??
              err.message ??
              "Không thể xác nhận đã nhận hàng.",
          );

          closeConfirmModal();
        } finally {
          setConfirmingReceived(false);
        }
      },
    });
  };

  const openReviewModal = (item) => {
    setReviewModal({
      open: true,
      product: {
        productId: item.productId,
        productName: item.productName,
        imageUrl: item.imageUrl,
      },
    });
  };

  const closeReviewModal = () => {
    setReviewModal({
      open: false,
      product: null,
    });
  };

  if (loading) {
    return (
      <main className="order-detail-page">
        <div className="order-detail__container">
          <div className="order-detail__loading">
            <LoadingSpinner size="large" />
          </div>
        </div>
      </main>
    );
  }

  if (error || !order) {
    return (
      <main className="order-detail-page">
        <div className="order-detail__container">
          <header className="order-detail__header">
            <button
              type="button"
              className="order-detail__back"
              onClick={() => navigate(-1)}
              aria-label="Quay lại"
            >
              ←
            </button>

            <h1>Chi tiết đơn hàng</h1>
          </header>

          <section className="order-detail__error">
            <div className="order-detail__error-icon">!</div>

            <h2>Không tìm thấy đơn hàng</h2>

            <p>
              {error ||
                "Đơn hàng không tồn tại hoặc bạn không có quyền xem đơn hàng này."}
            </p>

            <button
              type="button"
              className="order-detail__primary-button"
              onClick={() => navigate("/customer/orders")}
            >
              Về danh sách đơn hàng
            </button>
          </section>
        </div>
      </main>
    );
  }

  const items = Array.isArray(order.items) ? order.items : [];

  const statusText = getStatusText(order);

  const statusClass = getStatusClass(order.status);

  const normalizedStatus = String(order.status ?? "").toUpperCase();

  const canCancel =
    ["PENDING", "CONFIRMED"].includes(normalizedStatus) &&
    String(order.paymentMethod).toUpperCase() !== "VNPAY";

  const canConfirmReceived = normalizedStatus === "DELIVERED";

  return (
    <main className="order-detail-page">
      <div className="order-detail__container">
        {/* HEADER */}
        <header className="order-detail__header">
          <button
            type="button"
            className="order-detail__back"
            onClick={() => navigate(-1)}
            aria-label="Quay lại"
          >
            ←
          </button>

          <div className="order-detail__header-content">
            <h1>Chi tiết đơn hàng</h1>

            <p>
              Mã đơn hàng: <strong>#{order.orderCode}</strong>
            </p>

            <p>
              Shop: <strong>{order.shopName ?? "—"}</strong>
            </p>
          </div>
        </header>

        {/* STATUS */}
        <section className="order-detail__status-card">
          <div>
            <span className="order-detail__label">Trạng thái đơn hàng</span>

            <strong className={`order-detail__status ${statusClass}`}>
              {statusText}
            </strong>
          </div>

          <div>
            <span className="order-detail__label">Ngày đặt</span>

            <strong className="order-detail__value">
              {formatDate(order.createdAt)}
            </strong>
          </div>
        </section>

        {/* CUSTOMER / ADDRESS */}
        <section className="order-detail__section">
          <div className="order-detail__section-title">
            <h2>Địa chỉ nhận hàng</h2>
          </div>

          <div className="order-detail__address">
            <div className="order-detail__address-row">
              <strong>{order.receiverName ?? order.customerName ?? "—"}</strong>

              <span>{order.receiverPhone ?? order.customerPhone ?? "—"}</span>
            </div>

            <p>{order.shippingAddress ?? "—"}</p>
          </div>
        </section>

        {/* PRODUCTS */}
        <section className="order-detail__section">
          <div className="order-detail__section-title">
            <h2>Sản phẩm</h2>

            <span>{items.length} sản phẩm</span>
          </div>

          <div className="order-detail__items">
            {items.length === 0 ? (
              <div className="order-detail__no-items">
                Không có thông tin sản phẩm.
              </div>
            ) : (
              items.map((item, index) => (
                <article
                  className="order-detail-item"
                  key={
                    item.id ?? `${order.orderCode}-${item.productId}-${index}`
                  }
                >
                  <div className="order-detail-item__image">
                    {item.imageUrl ? (
                      <img
                        src={item.imageUrl}
                        alt={item.productName}
                        loading="lazy"
                      />
                    ) : (
                      <span>📦</span>
                    )}
                  </div>

                  <div className="order-detail-item__content">
                    <h3>{item.productName ?? `Sản phẩm #${item.productId}`}</h3>

                    <div className="order-detail-item__info">
                      <span>
                        Đơn giá: <strong>{formatCurrency(item.price)}</strong>
                      </span>

                      <span>
                        Số lượng: <strong>x{item.quantity}</strong>
                      </span>
                    </div>

                    {normalizedStatus === "COMPLETED" && (
                      <div className="order-detail__review-action">
                        {reviewedProductIds.includes(item.productId) ? (
                          <span className="order-detail__reviewed">
                            ✓ Đã đánh giá
                          </span>
                        ) : (
                          <button
                            type="button"
                            className="order-detail__review-button"
                            onClick={() => openReviewModal(item)}
                          >
                            ⭐ Đánh giá sản phẩm
                          </button>
                        )}
                      </div>
                    )}
                  </div>

                  <strong className="order-detail-item__subtotal">
                    {formatCurrency(item.subtotal)}
                  </strong>
                </article>
              ))
            )}
          </div>
        </section>

        {/* PAYMENT */}
        <section className="order-detail__section">
          <div className="order-detail__section-title">
            <h2>Thanh toán</h2>
          </div>

          <div className="order-detail__payment">
            <div>
              <span>Phương thức</span>

              <strong>{order.paymentMethod ?? "—"}</strong>
            </div>

            <div>
              <span>Trạng thái</span>

              <strong>{getPaymentStatusText(order.paymentStatus)}</strong>
            </div>

            {order.paidAt && (
              <div>
                <span>Thời gian thanh toán</span>

                <strong>{formatDate(order.paidAt)}</strong>
              </div>
            )}
          </div>
        </section>

        {/* SUMMARY */}
        <section className="order-detail__section">
          <div className="order-detail__section-title">
            <h2>Chi tiết thanh toán</h2>
          </div>

          <div className="order-detail__summary">
            <div>
              <span>Tổng tiền hàng</span>

              <strong>
                {formatCurrency(order.subtotal ?? order.totalAmount ?? 0)}
              </strong>
            </div>

            <div>
              <span>Phí vận chuyển</span>

              <strong>{formatCurrency(order.shippingFee ?? 0)}</strong>
            </div>

            {Number(order.discountAmount ?? 0) > 0 && (
              <div>
                <span>
                  Voucher giảm giá
                  {order.voucherCode ? ` (${order.voucherCode})` : ""}
                </span>

                <strong className="order-detail__discount">
                  - {formatCurrency(order.discountAmount)}
                </strong>
              </div>
            )}

            <div className="order-detail__summary-total">
              <span>Tổng thanh toán</span>

              <strong>{formatCurrency(getTotal(order))}</strong>
            </div>
          </div>
        </section>

        {/* CANCEL ERROR */}
        {cancelError && (
          <div className="order-detail__cancel-error" role="alert">
            {cancelError}
          </div>
        )}

        {confirmReceivedError && (
          <div className="order-detail__confirm-error" role="alert">
            {confirmReceivedError}
          </div>
        )}

        <div className="order-detail__actions">
          <div className="order-detail__actions-right">
            {canCancel && (
              <button
                type="button"
                className="order-detail__cancel-button"
                onClick={handleCancelOrder}
                disabled={cancelling}
              >
                {cancelling ? (
                  <LoadingSpinner size="small" inline variant="light" />
                ) : (
                  "Hủy đơn hàng"
                )}
              </button>
            )}

            {canConfirmReceived && (
              <button
                type="button"
                className="order-detail__confirm-received-button"
                onClick={handleConfirmReceived}
                disabled={confirmingReceived}
              >
                {confirmingReceived ? (
                  <LoadingSpinner size="small" inline variant="light" />
                ) : (
                  "✓ Đã nhận hàng"
                )}
              </button>
            )}
          </div>
        </div>

        {confirmModal.open && (
          <div
            className="order-confirm-modal__overlay"
            onMouseDown={(event) => {
              if (event.target === event.currentTarget) {
                closeConfirmModal();
              }
            }}
          >
            <div
              className="order-confirm-modal"
              role="dialog"
              aria-modal="true"
              aria-labelledby="order-confirm-modal-title"
            >
              <div
                className={`order-confirm-modal__icon order-confirm-modal__icon--${confirmModal.type}`}
              >
                {confirmModal.type === "danger" ? "⚠️" : "📦"}
              </div>

              <h2 id="order-confirm-modal-title">{confirmModal.title}</h2>

              <p>{confirmModal.message}</p>

              <div className="order-confirm-modal__actions">
                <button
                  type="button"
                  className="order-confirm-modal__cancel"
                  onClick={closeConfirmModal}
                  disabled={cancelling || confirmingReceived}
                >
                  {confirmModal.cancelText}
                </button>

                <button
                  type="button"
                  className={`order-confirm-modal__confirm order-confirm-modal__confirm--${confirmModal.type}`}
                  onClick={confirmModal.onConfirm}
                  disabled={cancelling || confirmingReceived}
                >
                  {cancelling || confirmingReceived ? (
                    <LoadingSpinner size="small" inline variant="light" />
                  ) : (
                    confirmModal.confirmText
                  )}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
      <ReviewModal
        open={reviewModal.open}
        product={reviewModal.product}
        onClose={closeReviewModal}
        onSuccess={() => {
          const productId = reviewModal.product?.productId;

          if (productId == null) {
            return;
          }

          setReviewedProductIds((currentIds) => {
            if (currentIds.includes(productId)) {
              return currentIds;
            }

            return [...currentIds, productId];
          });
        }}
      />
    </main>
  );
}

export default OrderDetailPage;
