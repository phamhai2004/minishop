import { useEffect, useState } from "react";

import dashboardApi from "../../../../api/dashboardApi";

import LoadingSpinner from "../../../common/LoadingSpinner";

import "./SellerCustomers.css";

function formatCurrency(value) {
  return `${Number(value ?? 0).toLocaleString("vi-VN")} ₫`;
}

function formatDateTime(value) {
  if (!value) {
    return "-";
  }

  return new Date(value).toLocaleString("vi-VN");
}

function getCustomerTypeLabel(type) {
  const labels = {
    NEW: "Khách hàng mới",
    RETURNING: "Khách quay lại",
  };

  return labels[type] ?? type ?? "-";
}

function SellerCustomers({ dashboard }) {
  const [customers, setCustomers] = useState([]);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    const loadCustomers = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await dashboardApi.getSellerCustomers({
          page,
          size: 10,
        });

        const apiResponse = response.data;

        if (!apiResponse?.success || !apiResponse?.data) {
          throw new Error(
            apiResponse?.message ?? "Không thể tải dữ liệu khách hàng.",
          );
        }

        if (!cancelled) {
          setCustomers(apiResponse.data.content ?? []);

          setTotalPages(apiResponse.data.totalPages ?? 0);

          setTotalElements(apiResponse.data.totalElements ?? 0);
        }
      } catch (err) {
        console.error("Unable to load seller customers:", err);

        if (!cancelled) {
          setError(
            err.response?.data?.message ??
              err.message ??
              "Không thể tải dữ liệu khách hàng.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    loadCustomers();

    return () => {
      cancelled = true;
    };
  }, [page]);

  return (
    <section className="seller-customers">
      <div className="seller-customers__heading">
        <div>
          <h1>Khách hàng</h1>

          <p>Theo dõi khách hàng đã mua sản phẩm tại shop.</p>
        </div>
      </div>

      {dashboard && (
        <div className="seller-customers__kpis">
          <article className="seller-customers__kpi">
            <span>Đã mua hàng</span>

            <strong>{dashboard.customersPurchased ?? 0}</strong>

            <small>Khách hàng đã mua tại shop</small>
          </article>

          <article className="seller-customers__kpi">
            <span>Khách hàng mới</span>

            <strong>{dashboard.newCustomers ?? 0}</strong>

            <small>Mua lần đầu trong tháng hiện tại</small>
          </article>

          <article className="seller-customers__kpi">
            <span>Khách quay lại</span>

            <strong>{dashboard.returningCustomers ?? 0}</strong>

            <small>Đã mua từ 2 đơn trở lên</small>
          </article>
        </div>
      )}

      <section className="seller-customers__panel">
        <div className="seller-customers__panel-heading">
          <div>
            <h2>Danh sách khách hàng</h2>

            <p>Tổng cộng {totalElements} khách hàng đã mua tại shop.</p>
          </div>
        </div>

        {loading && (
          <div className="seller-customers__state">
            <LoadingSpinner size="medium" />
          </div>
        )}

        {!loading && error && (
          <div className="seller-customers__error">{error}</div>
        )}

        {!loading && !error && (
          <div className="seller-customers__table-wrapper">
            <table className="seller-customers__table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Khách hàng</th>
                  <th>Số điện thoại</th>
                  <th>Số đơn</th>
                  <th>Tổng chi tiêu</th>
                  <th>Lần mua gần nhất</th>
                  <th>Phân loại</th>
                </tr>
              </thead>

              <tbody>
                {customers.length > 0 ? (
                  customers.map((customer) => (
                    <tr key={customer.customerId}>
                      <td>
                        <strong>#{customer.customerId}</strong>
                      </td>

                      <td>
                        <div className="seller-customers__identity">
                          <strong>{customer.customerName ?? "-"}</strong>

                          <small>{customer.customerEmail ?? "-"}</small>
                        </div>
                      </td>

                      <td>{customer.customerPhone || "-"}</td>

                      <td>{customer.orderCount ?? 0}</td>

                      <td>{formatCurrency(customer.totalSpent)}</td>

                      <td>{formatDateTime(customer.lastOrderAt)}</td>

                      <td>
                        <span
                          className={`seller-customers__type seller-customers__type--${String(
                            customer.customerType ?? "",
                          ).toLowerCase()}`}
                        >
                          {getCustomerTypeLabel(customer.customerType)}
                        </span>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="7">
                      <div className="seller-customers__empty">
                        Chưa có khách hàng nào mua tại shop.
                      </div>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}

        {!loading && !error && totalPages > 1 && (
          <div className="seller-customers__pagination">
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

export default SellerCustomers;
