import RevenueChart from "../../../revenue/RevenueChart";
import LoadingSpinner from "../../../common/LoadingSpinner";

import dashboardApi from "../../../../api/dashboardApi";

import "./AdminOverview.css";

function formatCurrency(value) {
  return `${Number(value ?? 0).toLocaleString("vi-VN")} ₫`;
}
const loadAdminRevenue = (period) => dashboardApi.getAdminRevenue(period);
function AdminOverview({ dashboard, loading, error }) {
  return (
    <section className="admin-overview">
      <div className="admin-overview__heading">
        <div>
          <h1>Tổng quan</h1>
          <p>Theo dõi hoạt động của hệ thống Hair.</p>
        </div>
      </div>

      {loading && (
        <div className="admin-overview__state">
          <LoadingSpinner size="medium" />
        </div>
      )}

      {!loading && error && (
        <div className="admin-overview__error">{error}</div>
      )}

      {!loading && !error && dashboard && (
        <>
          <div className="admin-overview__kpis">
            <article className="admin-overview__kpi">
              <span>Doanh thu</span>

              <strong>{formatCurrency(dashboard.revenue)}</strong>

              <small>Doanh thu đã ghi nhận</small>
            </article>

            <article className="admin-overview__kpi">
              <span>Đơn hàng</span>

              <strong>{dashboard.totalOrders ?? 0}</strong>

              <small>Tổng đơn toàn hệ thống</small>
            </article>

            <article className="admin-overview__kpi">
              <span>Khách hàng</span>

              <strong>{dashboard.totalCustomers ?? 0}</strong>

              <small>Tài khoản CUSTOMER</small>
            </article>

            <article className="admin-overview__kpi">
              <span>Người bán</span>

              <strong>{dashboard.totalSellers ?? 0}</strong>

              <small>Tài khoản SELLER</small>
            </article>
          </div>

          <div className="admin-overview__sections">
            <section className="admin-overview__panel">
              <div className="admin-overview__panel-heading">
                <div>
                  <h2>Trạng thái đơn hàng</h2>

                  <p>Tình trạng xử lý đơn hàng trên toàn hệ thống.</p>
                </div>
              </div>

              <div className="admin-overview__status-grid">
                <article className="admin-overview__status-card">
                  <span>Chờ xử lý</span>
                  <strong>{dashboard.pendingOrders ?? 0}</strong>
                </article>

                <article className="admin-overview__status-card">
                  <span>Chờ thanh toán</span>
                  <strong>{dashboard.waitingPaymentOrders ?? 0}</strong>
                </article>

                <article className="admin-overview__status-card">
                  <span>Đã thanh toán</span>
                  <strong>{dashboard.paidStatusOrders ?? 0}</strong>
                </article>

                <article className="admin-overview__status-card">
                  <span>Đã xác nhận</span>
                  <strong>{dashboard.confirmedOrders ?? 0}</strong>
                </article>

                <article className="admin-overview__status-card">
                  <span>Đang đóng gói</span>
                  <strong>{dashboard.packingOrders ?? 0}</strong>
                </article>

                <article className="admin-overview__status-card">
                  <span>Đang vận chuyển</span>
                  <strong>{dashboard.shippingOrders ?? 0}</strong>
                </article>

                <article className="admin-overview__status-card">
                  <span>Đã giao hàng</span>
                  <strong>{dashboard.deliveredOrders ?? 0}</strong>
                </article>

                <article className="admin-overview__status-card">
                  <span>Đã hoàn thành</span>
                  <strong>{dashboard.completedOrders ?? 0}</strong>
                </article>

                <article className="admin-overview__status-card">
                  <span>Đã hủy</span>
                  <strong>{dashboard.cancelledOrders ?? 0}</strong>
                </article>

                <article className="admin-overview__status-card">
                  <span>Đã trả hàng</span>
                  <strong>{dashboard.returnedOrders ?? 0}</strong>
                </article>
              </div>
            </section>

            <section className="admin-overview__panel">
              <div className="admin-overview__panel-heading">
                <div>
                  <h2>Tồn kho</h2>

                  <p>Tình trạng tồn kho của sản phẩm trên toàn hệ thống.</p>
                </div>
              </div>

              <div className="admin-overview__inventory-grid">
                <article className="admin-overview__inventory-card">
                  <span>Còn hàng</span>

                  <strong>{dashboard.inStockProducts ?? 0}</strong>

                  <small>Tồn kho lớn hơn 5</small>
                </article>

                <article className="admin-overview__inventory-card">
                  <span>Sắp hết hàng</span>

                  <strong>{dashboard.lowStockProducts ?? 0}</strong>

                  <small>Tồn kho từ 1 đến 5</small>
                </article>

                <article className="admin-overview__inventory-card">
                  <span>Hết hàng</span>

                  <strong>{dashboard.outOfStockProducts ?? 0}</strong>

                  <small>Tồn kho bằng 0</small>
                </article>
              </div>
            </section>

            <section className="admin-overview__panel">
              <RevenueChart
                loadRevenue={loadAdminRevenue}
                initialPeriod="MONTH"
              />
            </section>

            <div className="admin-overview__analytics-grid">
              <section className="admin-overview__panel">
                <div className="admin-overview__panel-heading">
                  <div>
                    <h2>Sản phẩm bán chạy</h2>
                    <p>Sản phẩm có số lượng bán cao nhất.</p>
                  </div>
                </div>

                <div className="admin-overview__ranking-list">
                  {Array.isArray(dashboard.topProducts) &&
                  dashboard.topProducts.length > 0 ? (
                    dashboard.topProducts.map((product, index) => (
                      <article
                        key={product.productId}
                        className="admin-overview__ranking-item"
                      >
                        <span className="admin-overview__ranking-number">
                          {index + 1}
                        </span>

                        <div className="admin-overview__ranking-content">
                          <strong>{product.productName}</strong>

                          <small>Đã bán {product.sold ?? 0} sản phẩm</small>
                        </div>
                      </article>
                    ))
                  ) : (
                    <div className="admin-overview__empty">
                      Chưa có dữ liệu sản phẩm.
                    </div>
                  )}
                </div>
              </section>

              <section className="admin-overview__panel">
                <div className="admin-overview__panel-heading">
                  <div>
                    <h2>Người bán hàng đầu</h2>

                    <p>Xếp hạng shop theo doanh thu.</p>
                  </div>
                </div>

                <div className="admin-overview__ranking-list">
                  {Array.isArray(dashboard.topSellers) &&
                  dashboard.topSellers.length > 0 ? (
                    dashboard.topSellers.map((seller, index) => (
                      <article
                        key={seller.shopId}
                        className="admin-overview__ranking-item"
                      >
                        <span className="admin-overview__ranking-number">
                          {index + 1}
                        </span>

                        <div className="admin-overview__ranking-content">
                          <strong>{seller.shopName}</strong>

                          <small>
                            {seller.orderCount ?? 0} đơn ·{" "}
                            {formatCurrency(seller.revenue)}
                          </small>
                        </div>
                      </article>
                    ))
                  ) : (
                    <div className="admin-overview__empty">
                      Chưa có dữ liệu người bán.
                    </div>
                  )}
                </div>
              </section>
            </div>

            <section className="admin-overview__panel">
              <div className="admin-overview__panel-heading">
                <div>
                  <h2>Thanh toán</h2>
                  <p>Thống kê phương thức và trạng thái thanh toán.</p>
                </div>
              </div>

              <div className="admin-overview__payment-groups">
                <div>
                  <h3>Phương thức thanh toán</h3>

                  <div className="admin-overview__payment-grid">
                    <article className="admin-overview__payment-card">
                      <span>COD</span>

                      <strong>{dashboard.codOrders ?? 0}</strong>

                      <small>Thanh toán khi nhận hàng</small>
                    </article>

                    <article className="admin-overview__payment-card">
                      <span>VNPay</span>

                      <strong>{dashboard.vnpayOrders ?? 0}</strong>

                      <small>Thanh toán trực tuyến</small>
                    </article>
                  </div>
                </div>

                <div>
                  <h3>Trạng thái thanh toán</h3>

                  <div className="admin-overview__payment-status-grid">
                    <article className="admin-overview__payment-card">
                      <span>Chờ thanh toán</span>

                      <strong>
                        {dashboard.pendingPaymentStatusOrders ?? 0}
                      </strong>
                    </article>

                    <article className="admin-overview__payment-card">
                      <span>Đang xử lý</span>

                      <strong>{dashboard.processingPaymentOrders ?? 0}</strong>
                    </article>

                    <article className="admin-overview__payment-card">
                      <span>Đã thanh toán</span>

                      <strong>{dashboard.paidOrders ?? 0}</strong>
                    </article>

                    <article className="admin-overview__payment-card">
                      <span>Thất bại</span>

                      <strong>{dashboard.failedPaymentOrders ?? 0}</strong>
                    </article>

                    <article className="admin-overview__payment-card">
                      <span>Đã hủy</span>

                      <strong>{dashboard.cancelledPaymentOrders ?? 0}</strong>
                    </article>

                    <article className="admin-overview__payment-card">
                      <span>Hết hạn</span>

                      <strong>{dashboard.expiredPaymentOrders ?? 0}</strong>
                    </article>

                    <article className="admin-overview__payment-card">
                      <span>Đã hoàn tiền</span>

                      <strong>{dashboard.refundedPaymentOrders ?? 0}</strong>
                    </article>
                  </div>
                </div>
              </div>
            </section>
          </div>
        </>
      )}
    </section>
  );
}

export default AdminOverview;
