import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import orderApi from "../../api/orderApi";
import LoadingSpinner from "../../components/common/LoadingSpinner";

import "./OrderListPage.css";

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
  const normalized = String(status ?? "").toUpperCase();

  if (normalized.includes("CANCEL") || normalized.includes("REJECT")) {
    return "order-list__status--cancelled";
  }

  if (
    normalized.includes("DELIVERED") ||
    normalized.includes("COMPLETED") ||
    normalized.includes("SUCCESS")
  ) {
    return "order-list__status--success";
  }

  if (normalized.includes("SHIPPING") || normalized.includes("DELIVERING")) {
    return "order-list__status--shipping";
  }

  if (normalized.includes("CONFIRM") || normalized.includes("PROCESS")) {
    return "order-list__status--processing";
  }

  return "order-list__status--pending";
}

function getStatusText(order) {
  return order?.statusName ?? order?.status ?? "Chưa xác định";
}

function getOrderTotal(order) {
  if (order?.finalAmount != null) {
    return order.finalAmount;
  }

  if (order?.totalAmount != null) {
    return order.totalAmount;
  }

  return 0;
}

function getPaymentStatusText(status) {
  const normalized = String(status ?? "").toUpperCase();

  if (normalized === "PAID") {
    return "Đã thanh toán";
  }

  if (normalized === "FAILED" || normalized === "CANCELLED") {
    return "Thanh toán thất bại";
  }

  if (normalized === "PENDING") {
    return "Chưa thanh toán";
  }

  return status || "Chưa xác định";
}

const STATUS_TABS = [
  {
    value: null,
    label: "Tất cả",
  },
  {
    value: "PENDING",
    label: "Chờ xác nhận",
  },
  {
    value: "CONFIRMED",
    label: "Đã xác nhận",
  },
  {
    value: "PACKING",
    label: "Đang đóng gói",
  },
  {
    value: "SHIPPING",
    label: "Đang vận chuyển",
  },
  {
    value: "DELIVERED",
    label: "Đã giao hàng",
  },
  {
    value: "COMPLETED",
    label: "Đã hoàn thành",
  },
  {
    value: "CANCELLED",
    label: "Đã hủy",
  },
];

const TIME_FILTERS = [
  {
    value: "ALL",
    label: "Tất cả thời gian",
  },
  {
    value: "TODAY",
    label: "Hôm nay",
  },
  {
    value: "LAST_7_DAYS",
    label: "7 ngày qua",
  },
  {
    value: "LAST_30_DAYS",
    label: "30 ngày qua",
  },
  {
    value: "LAST_3_MONTHS",
    label: "3 tháng qua",
  },
  {
    value: "LAST_6_MONTHS",
    label: "6 tháng qua",
  },
];

function isOrderInTimeRange(createdAt, timeRange) {
  if (timeRange === "ALL") {
    return true;
  }

  if (!createdAt) {
    return false;
  }

  const orderDate = new Date(createdAt);

  if (Number.isNaN(orderDate.getTime())) {
    return false;
  }

  const now = new Date();

  if (timeRange === "TODAY") {
    return (
      orderDate.getFullYear() === now.getFullYear() &&
      orderDate.getMonth() === now.getMonth() &&
      orderDate.getDate() === now.getDate()
    );
  }

  const startDate = new Date(now);

  switch (timeRange) {
    case "LAST_7_DAYS":
      startDate.setDate(startDate.getDate() - 7);
      break;

    case "LAST_30_DAYS":
      startDate.setDate(startDate.getDate() - 30);
      break;

    case "LAST_3_MONTHS":
      startDate.setMonth(startDate.getMonth() - 3);
      break;

    case "LAST_6_MONTHS":
      startDate.setMonth(startDate.getMonth() - 6);
      break;

    default:
      return true;
  }

  return orderDate >= startDate && orderDate <= now;
}

function OrderListPage() {
  const navigate = useNavigate();

  const [orders, setOrders] = useState([]);
  const [selectedStatus, setSelectedStatus] = useState(null);
  const [selectedTimeRange, setSelectedTimeRange] = useState("ALL");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    const loadOrders = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await orderApi.getMyOrders();

        const apiResponse = response.data;

        if (!apiResponse?.success) {
          throw new Error(
            apiResponse?.message ?? "Không thể tải danh sách đơn hàng.",
          );
        }

        const orderList = Array.isArray(apiResponse.data)
          ? apiResponse.data
          : [];

        if (!cancelled) {
          setOrders(orderList);
        }
      } catch (err) {
        console.error("Unable to load orders:", err);

        if (!cancelled) {
          setOrders([]);

          setError(
            err.response?.data?.message ??
              err.message ??
              "Không thể tải danh sách đơn hàng.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    loadOrders();

    return () => {
      cancelled = true;
    };
  }, []);

  const filteredOrders = orders.filter((order) => {
    const matchStatus =
      selectedStatus === null ||
      String(order.status ?? "").toUpperCase() === selectedStatus;

    const matchTime = isOrderInTimeRange(order.createdAt, selectedTimeRange);

    return matchStatus && matchTime;
  });

  const handleStatusChange = (status) => {
    setSelectedStatus(status);
  };

  const handleTimeRangeChange = (timeRange) => {
    setSelectedTimeRange(timeRange);
  };

  if (loading) {
    return (
      <main className="order-list-page">
        <div className="order-list__container">
          <div className="order-list__loading">
            <LoadingSpinner size="large" />
          </div>
        </div>
      </main>
    );
  }

  if (error) {
    return (
      <main className="order-list-page">
        <div className="order-list__container">
          <header className="order-list__header">
            <button
              type="button"
              className="order-list__back-button"
              onClick={() => navigate(-1)}
              aria-label="Quay lại"
            >
              ←
            </button>

            <div>
              <h1>Đơn hàng của tôi</h1>
              <p>Theo dõi tất cả đơn hàng của bạn</p>
            </div>
          </header>

          <div className="order-list__error">
            <div className="order-list__error-icon">!</div>

            <h2>Không thể tải đơn hàng</h2>

            <p>{error}</p>

            <button
              type="button"
              className="order-list__primary-button"
              onClick={() => window.location.reload()}
            >
              Thử lại
            </button>
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="order-list-page">
      <div className="order-list__container">
        {/* HEADER */}
        <header className="order-list__header">
          <button
            type="button"
            className="order-list__back-button"
            onClick={() => navigate(-1)}
            aria-label="Quay lại"
          >
            ←
          </button>

          <div className="order-list__header-content">
            <h1>Đơn hàng của tôi</h1>

            <p>
              {filteredOrders.length > 0
                ? `${filteredOrders.length} đơn hàng`
                : "Không có đơn hàng trong trạng thái này"}
            </p>
          </div>
        </header>

        {/* STATUS TABS */}
        <section className="order-list__filters">
          <div className="order-list__tabs">
            {STATUS_TABS.map((tab) => {
              const isActive = selectedStatus === tab.value;

              return (
                <button
                  key={tab.value ?? "ALL"}
                  type="button"
                  className={
                    isActive
                      ? "order-list__tab order-list__tab--active"
                      : "order-list__tab"
                  }
                  onClick={() => handleStatusChange(tab.value)}
                >
                  {tab.label}
                </button>
              );
            })}
          </div>
        </section>

        {/* TIME FILTER */}
        <section className="order-list__time-filter">
          <span className="order-list__time-filter-label">Thời gian:</span>

          <select
            className="order-list__time-filter-select"
            value={selectedTimeRange}
            onChange={(event) => handleTimeRangeChange(event.target.value)}
          >
            {TIME_FILTERS.map((filter) => (
              <option key={filter.value} value={filter.value}>
                {filter.label}
              </option>
            ))}
          </select>
        </section>

        {/* EMPTY */}
        {filteredOrders.length === 0 ? (
          <section className="order-list__empty">
            <div className="order-list__empty-icon">📦</div>

            <h2>
              {selectedStatus === null
                ? "Chưa có đơn hàng"
                : "Không có đơn hàng"}
            </h2>

            <p>
              {selectedStatus === null
                ? "Bạn chưa có đơn hàng nào. Hãy khám phá sản phẩm và bắt đầu mua sắm nhé!"
                : "Bạn chưa có đơn hàng nào trong trạng thái này."}
            </p>

            {selectedStatus === null && (
              <button
                type="button"
                className="order-list__primary-button"
                onClick={() => navigate("/products")}
              >
                Tiếp tục mua sắm
              </button>
            )}
          </section>
        ) : (
          <section className="order-list">
            {filteredOrders.map((order) => {
              const items = Array.isArray(order.items) ? order.items : [];

              const statusText = getStatusText(order);

              const statusClass = getStatusClass(order.status);

              return (
                <article className="order-card" key={order.orderCode}>
                  {/* ORDER HEADER */}
                  <div className="order-card__header">
                    <div className="order-card__number">
                      <span>Mã đơn hàng</span>

                      <strong>#{order.orderCode}</strong>
                    </div>

                    <span className={`order-list__status ${statusClass}`}>
                      {statusText}
                    </span>
                  </div>

                  <div className="order-card__date">
                    <span>Shop</span>

                    <strong>{order.shopName ?? "—"}</strong>
                  </div>

                  {/* DATE */}
                  <div className="order-card__date">
                    <span>Ngày đặt</span>

                    <strong>{formatDate(order.createdAt)}</strong>
                  </div>

                  {/* ITEMS */}
                  <div className="order-card__items">
                    {items.length === 0 ? (
                      <div className="order-card__no-items">
                        Không có thông tin sản phẩm.
                      </div>
                    ) : (
                      items.map((item, index) => (
                        <div
                          className="order-item"
                          key={
                            item.id ??
                            `${order.orderCode}-${item.productId}-${index}`
                          }
                        >
                          <div className="order-item__image">
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

                          <div className="order-item__content">
                            <h3>
                              {item.productName ??
                                `Sản phẩm #${item.productId}`}
                            </h3>

                            <div className="order-item__meta">
                              <span>
                                Số lượng: <strong>x{item.quantity}</strong>
                              </span>

                              <span>
                                Đơn giá:{" "}
                                <strong>{formatCurrency(item.price)}</strong>
                              </span>
                            </div>
                          </div>

                          <strong className="order-item__subtotal">
                            {formatCurrency(item.subtotal)}
                          </strong>
                        </div>
                      ))
                    )}
                  </div>

                  {/* PAYMENT */}
                  <div className="order-card__payment">
                    <div>
                      <span>Phương thức thanh toán</span>

                      <strong>{order.paymentMethod ?? "—"}</strong>
                    </div>

                    <div>
                      <span>Trạng thái thanh toán</span>

                      <strong>
                        {getPaymentStatusText(order.paymentStatus)}
                      </strong>
                    </div>
                  </div>

                  {/* TOTAL */}
                  <div className="order-card__footer">
                    <div className="order-card__total">
                      <span>Tổng thanh toán</span>

                      <strong>{formatCurrency(getOrderTotal(order))}</strong>
                    </div>

                    <button
                      type="button"
                      className="order-card__detail-button"
                      onClick={() =>
                        navigate(`/customer/orders/${order.orderCode}`)
                      }
                    >
                      Xem chi tiết
                    </button>
                  </div>
                </article>
              );
            })}
          </section>
        )}
      </div>
    </main>
  );
}

export default OrderListPage;
