import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import sellerOrderApi from "../../../api/sellerOrderApi";
import LoadingSpinner from "../../../components/common/LoadingSpinner";

import "./SellerOrderListPage.css";

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

const TIME_RANGE_OPTIONS = [
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
      return "seller-order-list__status--pending";

    case "CONFIRMED":
      return "seller-order-list__status--confirmed";

    case "PACKING":
      return "seller-order-list__status--packing";

    case "SHIPPING":
      return "seller-order-list__status--shipping";

    case "DELIVERED":
      return "seller-order-list__status--delivered";

    case "COMPLETED":
      return "seller-order-list__status--completed";

    case "CANCELLED":
      return "seller-order-list__status--cancelled";

    default:
      return "";
  }
}

function getStatusText(order) {
  return order?.statusName ?? order?.status ?? "Chưa xác định";
}

function getTotalItems(order) {
  if (!Array.isArray(order?.items)) {
    return 0;
  }

  return order.items.reduce(
    (total, item) => total + Number(item.quantity ?? 0),
    0,
  );
}

function getPaymentStatusText(status) {
  const normalized = String(status ?? "").toUpperCase();

  if (normalized === "PAID") {
    return "Đã thanh toán";
  }

  if (normalized === "PENDING") {
    return "Chưa thanh toán";
  }

  if (normalized === "FAILED" || normalized === "CANCELLED") {
    return "Thanh toán thất bại";
  }

  return status || "Chưa xác định";
}

function SellerOrderListPage() {
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [selectedStatus, setSelectedStatus] = useState(null);
  const [selectedTimeRange, setSelectedTimeRange] = useState("ALL");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);

  const PAGE_SIZE = 20;

  useEffect(() => {
    let cancelled = false;

    const loadOrders = async () => {
      try {
        setLoading(true);
        setError("");

        let response;

        if (selectedStatus) {
          response = await sellerOrderApi.getByStatus(selectedStatus, {
            page,
            size: PAGE_SIZE,
            timeRange: selectedTimeRange,
          });
        } else {
          response = await sellerOrderApi.getAll({
            page,
            size: PAGE_SIZE,
            direction: "desc",
            timeRange: selectedTimeRange,
          });
        }

        const apiResponse = response.data;

        if (!apiResponse?.success) {
          throw new Error(
            apiResponse?.message ?? "Không thể tải danh sách đơn hàng.",
          );
        }

        const pageData = apiResponse.data;

        if (!pageData) {
          throw new Error("Dữ liệu danh sách đơn hàng không hợp lệ.");
        }

        if (!cancelled) {
          setOrders(Array.isArray(pageData.content) ? pageData.content : []);

          setTotalPages(Number(pageData.totalPages ?? 0));

          setTotalElements(Number(pageData.totalElements ?? 0));
        }
      } catch (err) {
        console.error("Unable to load seller orders:", err);

        if (!cancelled) {
          setOrders([]);
          setTotalPages(0);
          setTotalElements(0);

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
  }, [selectedStatus, selectedTimeRange, page, reloadKey]);

  const handleStatusChange = (status) => {
    setSelectedStatus(status);
    setPage(0);
  };

  const handleTimeRangeChange = (timeRange) => {
    setSelectedTimeRange(timeRange);
    setPage(0);
  };

  const handlePreviousPage = () => {
    if (page <= 0) {
      return;
    }

    setPage((current) => current - 1);
  };

  const handleNextPage = () => {
    if (page >= totalPages - 1) {
      return;
    }

    setPage((current) => current + 1);
  };

  return (
    <main className="seller-order-list-page">
      <div className="seller-order-list__container">
        {/* HEADER */}
        <header className="seller-order-list__header">
          <div className="seller-order-list__header-content">
            <h1>Đơn hàng</h1>

            <p>Quản lý các đơn hàng thuộc shop của bạn</p>
          </div>
        </header>

        {/* STATUS TABS */}
        <section className="seller-order-list__filters">
          <div className="seller-order-list__tabs">
            {STATUS_TABS.map((tab) => {
              const isActive = selectedStatus === tab.value;

              return (
                <button
                  key={tab.value ?? "ALL"}
                  type="button"
                  className={
                    isActive
                      ? "seller-order-list__tab seller-order-list__tab--active"
                      : "seller-order-list__tab"
                  }
                  onClick={() => handleStatusChange(tab.value)}
                >
                  {tab.label}
                </button>
              );
            })}
          </div>
        </section>

        <div className="seller-order-list__time-filter">
          <label htmlFor="seller-order-time-range">Thời gian:</label>

          <select
            id="seller-order-time-range"
            value={selectedTimeRange}
            onChange={(event) => handleTimeRangeChange(event.target.value)}
          >
            {TIME_RANGE_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        {/* SUMMARY */}
        <div className="seller-order-list__summary">
          <span>
            {selectedStatus
              ? STATUS_TABS.find((tab) => tab.value === selectedStatus)?.label
              : "Tất cả đơn hàng"}

            {" · "}

            {
              TIME_RANGE_OPTIONS.find(
                (option) => option.value === selectedTimeRange,
              )?.label
            }
          </span>

          <strong>{totalElements} đơn hàng</strong>
        </div>

        {/* ERROR */}
        {error && (
          <section className="seller-order-list__error">
            <div className="seller-order-list__error-icon">!</div>

            <h2>Không thể tải đơn hàng</h2>

            <p>{error}</p>

            <button
              type="button"
              className="seller-order-list__retry-button"
              onClick={() => setReloadKey((value) => value + 1)}
            >
              Thử lại
            </button>
          </section>
        )}

        {/* LOADING */}
        {loading && (
          <section className="seller-order-list__loading">
            <LoadingSpinner size="large" />
          </section>
        )}

        {/* EMPTY */}
        {!loading && !error && orders.length === 0 && (
          <section className="seller-order-list__empty">
            <div className="seller-order-list__empty-icon">📦</div>

            <h2>Chưa có đơn hàng</h2>

            <p>Hiện tại chưa có đơn hàng nào trong trạng thái này.</p>
          </section>
        )}

        {/* ORDER LIST */}
        {!loading && !error && orders.length > 0 && (
          <section className="seller-order-list">
            {orders.map((order) => {
              const itemCount = getTotalItems(order);

              const statusClass = getStatusClass(order.status);

              return (
                <article className="seller-order-card" key={order.shopOrderId}>
                  {/* CARD HEADER */}
                  <div className="seller-order-card__header">
                    <div className="seller-order-card__ids">
                      <div>
                        <span>Mã đơn hàng</span>

                        <strong>#{order.orderCode ?? "—"}</strong>
                      </div>
                    </div>
                    <span
                      className={`seller-order-list__status ${statusClass}`}
                    >
                      {getStatusText(order)}
                    </span>
                  </div>

                  {/* CUSTOMER */}
                  <div className="seller-order-card__customer">
                    <div className="seller-order-card__customer-icon">
                      {order.customerAvatarUrl ? (
                        <img
                          src={order.customerAvatarUrl}
                          alt={order.customerName ?? "Khách hàng"}
                        />
                      ) : (
                        <span>👤</span>
                      )}
                    </div>

                    <div>
                      <span>Khách hàng</span>

                      <strong>{order.customerName ?? "—"}</strong>

                      {order.customerPhone && (
                        <small>{order.customerPhone}</small>
                      )}
                    </div>
                  </div>

                  {/* ORDER INFO */}
                  <div className="seller-order-card__info">
                    <div>
                      <span>Sản phẩm</span>

                      <strong>{itemCount} sản phẩm</strong>
                    </div>

                    <div>
                      <span>Thanh toán</span>

                      <strong>
                        {getPaymentStatusText(order.paymentStatus)}
                      </strong>
                    </div>

                    <div>
                      <span>Ngày đặt</span>

                      <strong>{formatDate(order.createdAt)}</strong>
                    </div>
                  </div>

                  {/* FOOTER */}
                  <div className="seller-order-card__footer">
                    <div className="seller-order-card__total">
                      <span>Tổng thanh toán</span>

                      <strong>{formatCurrency(order.finalAmount)}</strong>
                    </div>

                    <button
                      type="button"
                      className="seller-order-card__detail-button"
                      onClick={() =>
                        navigate(`/seller/orders/${order.shopOrderId}`)
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

        {/* PAGINATION */}
        {!loading && !error && totalPages > 0 && (
          <nav
            className="seller-order-list__pagination"
            aria-label="Phân trang đơn hàng"
          >
            <button
              type="button"
              className="seller-order-list__page-button"
              disabled={page <= 0}
              onClick={handlePreviousPage}
            >
              ← Trước
            </button>

            <span>
              Trang <strong>{page + 1}</strong>
              {" / "}
              <strong>{totalPages}</strong>
            </span>

            <button
              type="button"
              className="seller-order-list__page-button"
              disabled={page >= totalPages - 1}
              onClick={handleNextPage}
            >
              Sau →
            </button>
          </nav>
        )}
      </div>
    </main>
  );
}

export default SellerOrderListPage;
