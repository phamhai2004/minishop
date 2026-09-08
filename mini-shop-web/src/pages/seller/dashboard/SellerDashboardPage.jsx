import { useEffect, useState } from "react";

import dashboardApi from "../../../api/dashboardApi";

import SellerProducts from "../../../components/seller/dashboard/product/SellerProducts";
import SellerOrders from "../../../components/seller/dashboard/order/SellerOrders";
import SellerCustomers from "../../../components/seller/dashboard/customer/SellerCustomers";
import SellerPerformance from "../../../components/seller/dashboard/performance/SellerPerformance";
import RevenueChart from "../../../components/revenue/RevenueChart";
import LoadingSpinner from "../../../components/common/LoadingSpinner";

import "./SellerDashboardPage.css";

function formatCurrency(value) {
  return `${Number(value ?? 0).toLocaleString("vi-VN")} ₫`;
}

const loadSellerRevenue = (period) => dashboardApi.getSellerRevenue(period);

function SellerDashboardPage() {
  const [activeSection, setActiveSection] = useState("overview");
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    const loadDashboard = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await dashboardApi.getSellerDashboard();
        const apiResponse = response.data;

        if (!apiResponse?.success || !apiResponse?.data) {
          throw new Error(
            apiResponse?.message ?? "Không thể tải dữ liệu dashboard.",
          );
        }

        if (!cancelled) {
          setDashboard(apiResponse.data);
        }
      } catch (err) {
        console.error("Unable to load seller dashboard:", err);

        if (!cancelled) {
          setError(
            err.response?.data?.message ??
              err.message ??
              "Không thể tải dữ liệu dashboard.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    loadDashboard();

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <main className="seller-dashboard">
      <nav className="seller-dashboard__nav">
        <button
          type="button"
          className={activeSection === "overview" ? "active" : ""}
          onClick={() => setActiveSection("overview")}
        >
          Tổng quan
        </button>

        <button
          type="button"
          className={activeSection === "products" ? "active" : ""}
          onClick={() => setActiveSection("products")}
        >
          Sản phẩm
        </button>

        <button
          type="button"
          className={activeSection === "orders" ? "active" : ""}
          onClick={() => setActiveSection("orders")}
        >
          Đơn hàng
        </button>

        <button
          type="button"
          className={activeSection === "customers" ? "active" : ""}
          onClick={() => setActiveSection("customers")}
        >
          Khách hàng
        </button>

        <button
          type="button"
          className={activeSection === "performance" ? "active" : ""}
          onClick={() => setActiveSection("performance")}
        >
          Hiệu quả kinh doanh
        </button>
      </nav>
      {activeSection === "overview" && (
        <>
          <div className="seller-dashboard__heading">
            <div>
              <h1>{dashboard?.shopName ?? "Seller Dashboard"}</h1>
              <p>Theo dõi hoạt động kinh doanh của shop.</p>
            </div>
          </div>
          {loading && (
            <div className="seller-dashboard__state">
              <LoadingSpinner size="medium" />
            </div>
          )}

          {!loading && error && (
            <div className="seller-dashboard__error">{error}</div>
          )}

          {!loading && !error && dashboard && (
            <>
              <div className="seller-dashboard__kpis">
                <article className="seller-dashboard__kpi">
                  <span>Doanh thu</span>

                  <strong>{formatCurrency(dashboard.revenue)}</strong>

                  <small>Doanh thu đã ghi nhận</small>
                </article>

                <article className="seller-dashboard__kpi">
                  <span>Đơn hàng</span>

                  <strong>{dashboard.totalOrders ?? 0}</strong>

                  <small>Tổng đơn của shop</small>
                </article>

                <article className="seller-dashboard__kpi">
                  <span>Sản phẩm</span>

                  <strong>{dashboard.totalProducts ?? 0}</strong>

                  <small>Sản phẩm của shop</small>
                </article>

                <article className="seller-dashboard__kpi">
                  <span>Khách hàng</span>

                  <strong>{dashboard.customersPurchased ?? 0}</strong>

                  <small>Khách đã mua tại shop</small>
                </article>
              </div>

              <div className="seller-dashboard__sections">
                <section className="seller-dashboard__panel">
                  <div className="seller-dashboard__panel-heading">
                    <div>
                      <h2>Trạng thái đơn hàng</h2>
                      <p>Tình trạng xử lý đơn hàng của shop.</p>
                    </div>
                  </div>

                  <div className="seller-dashboard__status-grid">
                    <article className="seller-dashboard__status-card">
                      <span>Chờ xác nhận</span>
                      <strong>{dashboard.pendingOrders ?? 0}</strong>
                    </article>

                    <article className="seller-dashboard__status-card">
                      <span>Đã xác nhận</span>
                      <strong>{dashboard.confirmedOrders ?? 0}</strong>
                    </article>

                    <article className="seller-dashboard__status-card">
                      <span>Đang đóng gói</span>
                      <strong>{dashboard.packingOrders ?? 0}</strong>
                    </article>

                    <article className="seller-dashboard__status-card">
                      <span>Đang vận chuyển</span>
                      <strong>{dashboard.shippingOrders ?? 0}</strong>
                    </article>

                    <article className="seller-dashboard__status-card">
                      <span>Đã giao hàng</span>
                      <strong>{dashboard.deliveredOrders ?? 0}</strong>
                    </article>

                    <article className="seller-dashboard__status-card">
                      <span>Đã hoàn thành</span>
                      <strong>{dashboard.completedOrders ?? 0}</strong>
                    </article>

                    <article className="seller-dashboard__status-card">
                      <span>Đã hủy</span>
                      <strong>{dashboard.cancelledOrders ?? 0}</strong>
                    </article>

                    <article className="seller-dashboard__status-card">
                      <span>Đã thanh toán</span>
                      <strong>{dashboard.paidOrders ?? 0}</strong>
                    </article>
                  </div>
                </section>

                <section className="seller-dashboard__panel">
                  <div className="seller-dashboard__panel-heading">
                    <div>
                      <h2>Tồn kho</h2>
                      <p>Tình trạng tồn kho của sản phẩm trong shop.</p>
                    </div>
                  </div>

                  <div className="seller-dashboard__inventory-grid">
                    <article className="seller-dashboard__inventory-card">
                      <span>Còn hàng</span>
                      <strong>{dashboard.inStockProducts ?? 0}</strong>
                      <small>Tồn kho lớn hơn 5</small>
                    </article>

                    <article className="seller-dashboard__inventory-card">
                      <span>Sắp hết hàng</span>
                      <strong>{dashboard.lowStockProducts ?? 0}</strong>
                      <small>Tồn kho từ 1 đến 5</small>
                    </article>

                    <article className="seller-dashboard__inventory-card">
                      <span>Hết hàng</span>
                      <strong>{dashboard.outOfStockProducts ?? 0}</strong>
                      <small>Tồn kho bằng 0</small>
                    </article>
                  </div>
                </section>

                <section className="seller-dashboard__panel">
                  <div className="seller-dashboard__panel-heading">
                    <div>
                      <h2>Khách hàng</h2>
                      <p>Thống kê khách hàng đã mua tại shop.</p>
                    </div>
                  </div>

                  <div className="seller-dashboard__customer-grid">
                    <article className="seller-dashboard__customer-card">
                      <span>Đã mua hàng</span>
                      <strong>{dashboard.customersPurchased ?? 0}</strong>
                      <small>Tổng khách hàng của shop</small>
                    </article>

                    <article className="seller-dashboard__customer-card">
                      <span>Khách hàng mới</span>
                      <strong>{dashboard.newCustomers ?? 0}</strong>
                      <small>Mua lần đầu trong tháng hiện tại</small>
                    </article>

                    <article className="seller-dashboard__customer-card">
                      <span>Khách quay lại</span>
                      <strong>{dashboard.returningCustomers ?? 0}</strong>
                      <small>Đã mua từ 2 đơn trở lên</small>
                    </article>
                  </div>
                </section>

                <section className="seller-dashboard__panel">
                  <RevenueChart
                    loadRevenue={loadSellerRevenue}
                    initialPeriod="MONTH"
                  />
                </section>

                <section className="seller-dashboard__panel">
                  <div className="seller-dashboard__panel-heading">
                    <div>
                      <h2>Sản phẩm bán chạy</h2>
                      <p>Sản phẩm bán tốt nhất của shop.</p>
                    </div>
                  </div>

                  <div className="seller-dashboard__ranking-list">
                    {Array.isArray(dashboard.topProducts) &&
                    dashboard.topProducts.length > 0 ? (
                      dashboard.topProducts.map((product, index) => (
                        <article
                          key={product.productId}
                          className="seller-dashboard__ranking-item"
                        >
                          <span className="seller-dashboard__ranking-number">
                            {index + 1}
                          </span>

                          <div className="seller-dashboard__ranking-content">
                            <strong>{product.productName}</strong>

                            <small>Đã bán {product.sold ?? 0} sản phẩm</small>
                          </div>
                        </article>
                      ))
                    ) : (
                      <div className="seller-dashboard__empty">
                        Chưa có dữ liệu sản phẩm.
                      </div>
                    )}
                  </div>
                </section>
              </div>
            </>
          )}
        </>
      )}
      {activeSection === "products" && <SellerProducts dashboard={dashboard} />}

      {activeSection === "orders" && <SellerOrders dashboard={dashboard} />}

      {activeSection === "customers" && (
        <SellerCustomers dashboard={dashboard} />
      )}

      {activeSection === "performance" && <SellerPerformance />}
    </main>
  );
}

export default SellerDashboardPage;
