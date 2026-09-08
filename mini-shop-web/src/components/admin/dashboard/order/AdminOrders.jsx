import { useEffect, useState } from "react";

import dashboardApi from "../../../../api/dashboardApi";

import LoadingSpinner from "../../../common/LoadingSpinner";

import "./AdminOrders.css";

function formatCurrency(value) {
  return `${Number(value ?? 0).toLocaleString("vi-VN")} ₫`;
}

function formatDateTime(value) {
  if (!value) {
    return "-";
  }

  return new Date(value).toLocaleString("vi-VN");
}

function normalizeDateTimeLocal(value) {
  if (!value) {
    return undefined;
  }

  return value.length === 16 ? `${value}:00` : value;
}

function getOrderStatusLabel(status) {
  const labels = {
    PENDING: "Chờ xử lý",
    WAITING_PAYMENT: "Chờ thanh toán",
    PAID: "Đã thanh toán",
    CONFIRMED: "Đã xác nhận",
    PACKING: "Đang đóng gói",
    SHIPPING: "Đang vận chuyển",
    DELIVERED: "Đã giao hàng",
    COMPLETED: "Hoàn thành",
    CANCELLED: "Đã hủy",
    RETURNED: "Đã trả hàng",
  };

  return labels[status] ?? status ?? "-";
}

function getPaymentStatusLabel(status) {
  const labels = {
    PENDING: "Chờ thanh toán",
    PROCESSING: "Đang xử lý",
    PAID: "Đã thanh toán",
    FAILED: "Thất bại",
    CANCELLED: "Đã hủy",
    EXPIRED: "Hết hạn",
    REFUNDED: "Đã hoàn tiền",
  };

  return labels[status] ?? status ?? "-";
}

const EMPTY_FILTERS = {
  keyword: "",
  orderStatus: "",
  paymentMethod: "",
  paymentStatus: "",
  fromDate: "",
  toDate: "",
};

function AdminOrders() {
  const [orders, setOrders] = useState([]);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [appliedFilters, setAppliedFilters] = useState(EMPTY_FILTERS);

  useEffect(() => {
    let cancelled = false;

    const loadOrders = async () => {
      try {
        setLoading(true);
        setError("");

        const params = {
          page,
          size: 10,
        };

        const keyword = appliedFilters.keyword.trim();

        if (keyword) {
          params.keyword = keyword;
        }

        if (appliedFilters.orderStatus) {
          params.orderStatus = appliedFilters.orderStatus;
        }

        if (appliedFilters.paymentMethod) {
          params.paymentMethod = appliedFilters.paymentMethod;
        }

        if (appliedFilters.paymentStatus) {
          params.paymentStatus = appliedFilters.paymentStatus;
        }

        if (appliedFilters.fromDate) {
          params.fromDate = normalizeDateTimeLocal(appliedFilters.fromDate);
        }

        if (appliedFilters.toDate) {
          params.toDate = normalizeDateTimeLocal(appliedFilters.toDate);
        }

        const response = await dashboardApi.getAdminOrders(params);
        const apiResponse = response.data;

        if (!apiResponse?.success || !apiResponse?.data) {
          throw new Error(
            apiResponse?.message ?? "Không thể tải dữ liệu đơn hàng.",
          );
        }

        if (!cancelled) {
          setOrders(apiResponse.data.content ?? []);
          setTotalPages(apiResponse.data.totalPages ?? 0);
        }
      } catch (err) {
        console.error("Unable to load admin orders:", err);

        if (!cancelled) {
          setError(
            err.response?.data?.message ??
              err.message ??
              "Không thể tải dữ liệu đơn hàng.",
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
  }, [page, appliedFilters]);

  const handleFilterChange = (event) => {
    const { name, value } = event.target;

    setFilters((current) => ({
      ...current,
      [name]: value,
    }));
  };

  const handleFilterSubmit = (event) => {
    event.preventDefault();

    setPage(0);

    setAppliedFilters({
      ...filters,
    });
  };

  const handleFilterReset = () => {
    const resetFilters = {
      ...EMPTY_FILTERS,
    };

    setFilters(resetFilters);
    setAppliedFilters(resetFilters);
    setPage(0);
  };

  return (
    <section className="admin-orders">
      <div className="admin-orders__heading">
        <div>
          <h1>Đơn hàng</h1>
          <p>Theo dõi, tìm kiếm và phân tích đơn hàng trên toàn hệ thống.</p>
        </div>
      </div>

      <section className="admin-orders__panel">
        <div className="admin-orders__panel-heading">
          <div>
            <h2>Bộ lọc đơn hàng</h2>
            <p>Tìm theo khách hàng, trạng thái, thanh toán và thời gian.</p>
          </div>
        </div>

        <form className="admin-orders__filters" onSubmit={handleFilterSubmit}>
          <div className="admin-orders__filter-field">
            <label htmlFor="order-keyword">Tìm kiếm</label>

            <input
              id="order-keyword"
              type="text"
              name="keyword"
              value={filters.keyword}
              onChange={handleFilterChange}
              placeholder="Tên, email hoặc mã đơn..."
            />
          </div>

          <div className="admin-orders__filter-field">
            <label htmlFor="order-status">Trạng thái đơn</label>

            <select
              id="order-status"
              name="orderStatus"
              value={filters.orderStatus}
              onChange={handleFilterChange}
            >
              <option value="">Tất cả</option>
              <option value="PENDING">Chờ xử lý</option>
              <option value="WAITING_PAYMENT">Chờ thanh toán</option>
              <option value="PAID">Đã thanh toán</option>
              <option value="CONFIRMED">Đã xác nhận</option>
              <option value="PACKING">Đang đóng gói</option>
              <option value="SHIPPING">Đang vận chuyển</option>
              <option value="DELIVERED">Đã giao hàng</option>
              <option value="COMPLETED">Hoàn thành</option>
              <option value="CANCELLED">Đã hủy</option>
              <option value="RETURNED">Đã trả hàng</option>
            </select>
          </div>

          <div className="admin-orders__filter-field">
            <label htmlFor="payment-method">Phương thức thanh toán</label>

            <select
              id="payment-method"
              name="paymentMethod"
              value={filters.paymentMethod}
              onChange={handleFilterChange}
            >
              <option value="">Tất cả</option>
              <option value="COD">COD</option>
              <option value="VNPAY">VNPay</option>
            </select>
          </div>

          <div className="admin-orders__filter-field">
            <label htmlFor="payment-status">Trạng thái thanh toán</label>

            <select
              id="payment-status"
              name="paymentStatus"
              value={filters.paymentStatus}
              onChange={handleFilterChange}
            >
              <option value="">Tất cả</option>
              <option value="PENDING">Chờ thanh toán</option>
              <option value="PROCESSING">Đang xử lý</option>
              <option value="PAID">Đã thanh toán</option>
              <option value="FAILED">Thất bại</option>
              <option value="CANCELLED">Đã hủy</option>
              <option value="EXPIRED">Hết hạn</option>
              <option value="REFUNDED">Đã hoàn tiền</option>
            </select>
          </div>

          <div className="admin-orders__filter-field">
            <label htmlFor="from-date">Từ ngày</label>

            <input
              id="from-date"
              type="datetime-local"
              name="fromDate"
              value={filters.fromDate}
              onChange={handleFilterChange}
            />
          </div>

          <div className="admin-orders__filter-field">
            <label htmlFor="to-date">Đến ngày</label>

            <input
              id="to-date"
              type="datetime-local"
              name="toDate"
              value={filters.toDate}
              onChange={handleFilterChange}
            />
          </div>

          <div className="admin-orders__filter-actions">
            <button type="submit" className="admin-orders__filter-submit">
              Áp dụng
            </button>

            <button
              type="button"
              className="admin-orders__filter-reset"
              onClick={handleFilterReset}
            >
              Đặt lại
            </button>
          </div>
        </form>
      </section>

      <section className="admin-orders__panel admin-orders__list-panel">
        <div className="admin-orders__panel-heading">
          <div>
            <h2>Danh sách đơn hàng</h2>
            <p>Dữ liệu đơn hàng từ toàn bộ khách hàng trong hệ thống.</p>
          </div>
        </div>

        {loading && (
          <div className="admin-orders__state">
            <LoadingSpinner size="medium" />
          </div>
        )}

        {!loading && error && (
          <div className="admin-orders__error">{error}</div>
        )}

        {!loading && !error && (
          <div className="admin-orders__table-wrapper">
            <table className="admin-orders__table">
              <thead>
                <tr>
                  <th>Mã đơn</th>
                  <th>Khách hàng</th>
                  <th>Tổng tiền</th>
                  <th>Trạng thái đơn</th>
                  <th>Phương thức</th>
                  <th>Thanh toán</th>
                  <th>Ngày tạo</th>
                  <th>Ngày thanh toán</th>
                </tr>
              </thead>

              <tbody>
                {orders.length > 0 ? (
                  orders.map((order) => (
                    <tr key={order.orderId}>
                      <td>
                        <strong>#{order.orderId}</strong>
                      </td>

                      <td>
                        <div className="admin-orders__customer">
                          <strong>{order.customerName ?? "-"}</strong>

                          <small>{order.customerEmail ?? "-"}</small>
                        </div>
                      </td>

                      <td>{formatCurrency(order.finalAmount)}</td>

                      <td>
                        <span
                          className={`admin-orders__order-status admin-orders__order-status--${String(
                            order.orderStatus ?? "",
                          ).toLowerCase()}`}
                        >
                          {getOrderStatusLabel(order.orderStatus)}
                        </span>
                      </td>

                      <td>{order.paymentMethod ?? "-"}</td>

                      <td>
                        <span
                          className={`admin-orders__payment-status admin-orders__payment-status--${String(
                            order.paymentStatus ?? "",
                          ).toLowerCase()}`}
                        >
                          {getPaymentStatusLabel(order.paymentStatus)}
                        </span>
                      </td>

                      <td>{formatDateTime(order.createdAt)}</td>

                      <td>{formatDateTime(order.paidAt)}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="8">
                      <div className="admin-orders__empty">
                        Không tìm thấy đơn hàng phù hợp.
                      </div>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}

        {!loading && !error && totalPages > 1 && (
          <div className="admin-orders__pagination">
            <button
              type="button"
              disabled={page === 0}
              onClick={() => setPage((current) => Math.max(current - 1, 0))}
            >
              Trang trước
            </button>

            <span>
              Trang {page + 1} / {totalPages}
            </span>

            <button
              type="button"
              disabled={page + 1 >= totalPages}
              onClick={() => setPage((current) => current + 1)}
            >
              Trang sau
            </button>
          </div>
        )}
      </section>
    </section>
  );
}

export default AdminOrders;
