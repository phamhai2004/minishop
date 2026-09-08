import { useEffect, useState } from "react";

import dashboardApi from "../../../../api/dashboardApi";

import LoadingSpinner from "../../../common/LoadingSpinner";

import "./SellerPerformance.css";

function formatCurrency(value) {
  return `${Number(value ?? 0).toLocaleString("vi-VN")} ₫`;
}

function formatPercent(value) {
  const number = Number(value ?? 0);

  return `${number.toLocaleString("vi-VN", {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  })}%`;
}

function getGrowthClass(value) {
  const number = Number(value ?? 0);

  if (number > 0) {
    return "positive";
  }

  if (number < 0) {
    return "negative";
  }

  return "neutral";
}

function getGrowthIcon(value) {
  const number = Number(value ?? 0);

  if (number > 0) {
    return "↑";
  }

  if (number < 0) {
    return "↓";
  }

  return "→";
}

function SellerPerformance() {
  const [performance, setPerformance] = useState(null);
  const [products, setProducts] = useState([]);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    const loadPerformance = async () => {
      try {
        setLoading(true);
        setError("");

        const [performanceResponse, productsResponse] = await Promise.all([
          dashboardApi.getSellerPerformance(),
          dashboardApi.getSellerPerformanceProducts(),
        ]);

        const performanceApi = performanceResponse.data;
        const productsApi = productsResponse.data;

        if (!performanceApi?.success || !performanceApi?.data) {
          throw new Error(
            performanceApi?.message ?? "Không thể tải hiệu quả kinh doanh.",
          );
        }

        if (!productsApi?.success || !Array.isArray(productsApi?.data)) {
          throw new Error(
            productsApi?.message ?? "Không thể tải hiệu quả sản phẩm.",
          );
        }

        if (!cancelled) {
          setPerformance(performanceApi.data);
          setProducts(productsApi.data);
        }
      } catch (err) {
        console.error("Unable to load seller performance:", err);

        if (!cancelled) {
          setError(
            err.response?.data?.message ??
              err.message ??
              "Không thể tải dữ liệu hiệu quả kinh doanh.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    loadPerformance();

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <section className="seller-performance">
      <div className="seller-performance__heading">
        <div>
          <h1>Hiệu quả kinh doanh</h1>

          <p>
            Theo dõi doanh thu, đơn hàng và đóng góp doanh thu của từng sản
            phẩm.
          </p>
        </div>
      </div>

      {loading && (
        <div className="seller-performance__state">
          <LoadingSpinner size="medium" />
        </div>
      )}

      {!loading && error && (
        <div className="seller-performance__error">{error}</div>
      )}

      {!loading && !error && performance && (
        <>
          <div className="seller-performance__kpis">
            <article className="seller-performance__kpi">
              <span>Doanh thu tháng này</span>

              <strong>{formatCurrency(performance.currentMonthRevenue)}</strong>

              <div
                className={`seller-performance__growth seller-performance__growth--${getGrowthClass(
                  performance.revenueGrowthRate,
                )}`}
              >
                <span>{getGrowthIcon(performance.revenueGrowthRate)}</span>

                <span>{formatPercent(performance.revenueGrowthRate)}</span>

                <small>so với tháng trước</small>
              </div>
            </article>

            <article className="seller-performance__kpi">
              <span>Doanh thu tháng trước</span>

              <strong>
                {formatCurrency(performance.previousMonthRevenue)}
              </strong>

              <small>Doanh thu đã thanh toán tháng trước</small>
            </article>

            <article className="seller-performance__kpi">
              <span>Đơn đã thanh toán tháng này</span>

              <strong>{performance.currentMonthPaidOrders ?? 0}</strong>

              <div
                className={`seller-performance__growth seller-performance__growth--${getGrowthClass(
                  performance.paidOrderGrowthRate,
                )}`}
              >
                <span>{getGrowthIcon(performance.paidOrderGrowthRate)}</span>

                <span>{formatPercent(performance.paidOrderGrowthRate)}</span>

                <small>so với tháng trước</small>
              </div>
            </article>

            <article className="seller-performance__kpi">
              <span>Đơn đã thanh toán tháng trước</span>

              <strong>{performance.previousMonthPaidOrders ?? 0}</strong>

              <small>Tổng đơn đã thanh toán tháng trước</small>
            </article>
          </div>

          <div className="seller-performance__sections">
            <section className="seller-performance__panel">
              <div className="seller-performance__panel-heading">
                <div>
                  <h2>So sánh theo tháng</h2>

                  <p>
                    So sánh doanh thu và số đơn đã thanh toán giữa tháng hiện
                    tại và tháng trước.
                  </p>
                </div>
              </div>

              <div className="seller-performance__comparison-grid">
                <article className="seller-performance__comparison-card">
                  <div className="seller-performance__comparison-heading">
                    <span>Doanh thu</span>

                    <span
                      className={`seller-performance__comparison-growth seller-performance__comparison-growth--${getGrowthClass(
                        performance.revenueGrowthRate,
                      )}`}
                    >
                      {getGrowthIcon(performance.revenueGrowthRate)}{" "}
                      {formatPercent(performance.revenueGrowthRate)}
                    </span>
                  </div>

                  <div className="seller-performance__comparison-values">
                    <div>
                      <span>Tháng này</span>

                      <strong>
                        {formatCurrency(performance.currentMonthRevenue)}
                      </strong>
                    </div>

                    <div>
                      <span>Tháng trước</span>

                      <strong>
                        {formatCurrency(performance.previousMonthRevenue)}
                      </strong>
                    </div>
                  </div>
                </article>

                <article className="seller-performance__comparison-card">
                  <div className="seller-performance__comparison-heading">
                    <span>Đơn đã thanh toán</span>

                    <span
                      className={`seller-performance__comparison-growth seller-performance__comparison-growth--${getGrowthClass(
                        performance.paidOrderGrowthRate,
                      )}`}
                    >
                      {getGrowthIcon(performance.paidOrderGrowthRate)}{" "}
                      {formatPercent(performance.paidOrderGrowthRate)}
                    </span>
                  </div>

                  <div className="seller-performance__comparison-values">
                    <div>
                      <span>Tháng này</span>

                      <strong>{performance.currentMonthPaidOrders ?? 0}</strong>
                    </div>

                    <div>
                      <span>Tháng trước</span>

                      <strong>
                        {performance.previousMonthPaidOrders ?? 0}
                      </strong>
                    </div>
                  </div>
                </article>
              </div>
            </section>

            <section className="seller-performance__panel">
              <div className="seller-performance__panel-heading">
                <div>
                  <h2>Đóng góp doanh thu theo sản phẩm</h2>

                  <p>
                    Tỷ trọng doanh thu của từng sản phẩm trong tổng doanh thu đã
                    ghi nhận.
                  </p>
                </div>
              </div>

              {products.length > 0 ? (
                <div className="seller-performance__product-shares">
                  {products.map((product) => {
                    const share = Number(product.revenueShare ?? 0);

                    const safeShare = Math.min(Math.max(share, 0), 100);

                    return (
                      <article
                        key={product.productId}
                        className="seller-performance__share-item"
                      >
                        <div className="seller-performance__share-heading">
                          <div className="seller-performance__share-product">
                            <strong>{product.productName}</strong>

                            <small>Đã bán {product.sold ?? 0} sản phẩm</small>
                          </div>

                          <div className="seller-performance__share-value">
                            <strong>{formatPercent(share)}</strong>

                            <small>{formatCurrency(product.revenue)}</small>
                          </div>
                        </div>

                        <div className="seller-performance__progress">
                          <div
                            className="seller-performance__progress-bar"
                            style={{
                              width: `${safeShare}%`,
                            }}
                          />
                        </div>
                      </article>
                    );
                  })}
                </div>
              ) : (
                <div className="seller-performance__empty">
                  Chưa có sản phẩm phát sinh doanh thu.
                </div>
              )}
            </section>

            <section className="seller-performance__panel">
              <div className="seller-performance__panel-heading">
                <div>
                  <h2>Chi tiết hiệu quả sản phẩm</h2>

                  <p>
                    Doanh số, doanh thu và tỷ trọng doanh thu của từng sản phẩm.
                  </p>
                </div>
              </div>

              <div className="seller-performance__table-wrapper">
                <table className="seller-performance__table">
                  <thead>
                    <tr>
                      <th>Hạng</th>
                      <th>Sản phẩm</th>
                      <th>Đã bán</th>
                      <th>Doanh thu</th>
                      <th>Tỷ trọng</th>
                    </tr>
                  </thead>

                  <tbody>
                    {products.length > 0 ? (
                      products.map((product, index) => (
                        <tr key={product.productId}>
                          <td>
                            <span className="seller-performance__rank">
                              {index + 1}
                            </span>
                          </td>

                          <td>
                            <strong className="seller-performance__product-name">
                              {product.productName}
                            </strong>
                          </td>

                          <td>{product.sold ?? 0}</td>

                          <td>{formatCurrency(product.revenue)}</td>

                          <td>
                            <strong>
                              {formatPercent(product.revenueShare)}
                            </strong>
                          </td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="5">
                          <div className="seller-performance__empty">
                            Chưa có dữ liệu hiệu quả sản phẩm.
                          </div>
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </section>
          </div>
        </>
      )}
    </section>
  );
}

export default SellerPerformance;
