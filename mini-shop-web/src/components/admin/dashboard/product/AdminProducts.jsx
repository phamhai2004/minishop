import { useEffect, useState } from "react";

import dashboardApi from "../../../../api/dashboardApi";

import LoadingSpinner from "../../../common/LoadingSpinner";

import "./AdminProducts.css";

function formatCurrency(value) {
  return `${Number(value ?? 0).toLocaleString("vi-VN")} ₫`;
}

function AdminProducts({ dashboard }) {
  const [productAnalytics, setProductAnalytics] = useState([]);
  const [productPage, setProductPage] = useState(0);
  const [productTotalPages, setProductTotalPages] = useState(0);
  const [productLoading, setProductLoading] = useState(false);
  const [productError, setProductError] = useState("");

  useEffect(() => {
    let cancelled = false;

    const loadProducts = async () => {
      try {
        setProductLoading(true);
        setProductError("");

        const response = await dashboardApi.getAdminProducts({
          page: productPage,
          size: 5,
        });

        const apiResponse = response.data;

        if (!apiResponse?.success || !apiResponse?.data) {
          throw new Error(
            apiResponse?.message ?? "Không thể tải dữ liệu sản phẩm.",
          );
        }

        if (!cancelled) {
          setProductAnalytics(apiResponse.data.content ?? []);
          setProductTotalPages(apiResponse.data.totalPages ?? 0);
        }
      } catch (err) {
        console.error("Unable to load admin products:", err);

        if (!cancelled) {
          setProductError(
            err.response?.data?.message ??
              err.message ??
              "Không thể tải dữ liệu sản phẩm.",
          );
        }
      } finally {
        if (!cancelled) {
          setProductLoading(false);
        }
      }
    };

    loadProducts();

    return () => {
      cancelled = true;
    };
  }, [productPage]);

  if (!dashboard) {
    return null;
  }

  return (
    <section className="admin-products">
      <div className="admin-products__heading">
        <div>
          <h1>Sản phẩm</h1>
          <p>Phân tích hiệu quả sản phẩm trên toàn hệ thống.</p>
        </div>
      </div>

      <div className="admin-products__kpis">
        <article className="admin-products__kpi">
          <span>Tổng sản phẩm</span>

          <strong>{dashboard.totalProducts ?? 0}</strong>

          <small>Toàn bộ sản phẩm trong hệ thống</small>
        </article>

        <article className="admin-products__kpi">
          <span>Còn hàng</span>

          <strong>{dashboard.inStockProducts ?? 0}</strong>

          <small>Tồn kho lớn hơn 5</small>
        </article>

        <article className="admin-products__kpi">
          <span>Sắp hết hàng</span>

          <strong>{dashboard.lowStockProducts ?? 0}</strong>

          <small>Tồn kho từ 1 đến 5</small>
        </article>

        <article className="admin-products__kpi">
          <span>Hết hàng</span>

          <strong>{dashboard.outOfStockProducts ?? 0}</strong>

          <small>Tồn kho bằng 0</small>
        </article>
      </div>

      <div className="admin-products__sections">
        <div className="admin-products__analytics-grid">
          <section className="admin-products__panel">
            <div className="admin-products__panel-heading">
              <div>
                <h2>Bán chạy nhất</h2>
                <p>Xếp hạng sản phẩm theo số lượng đã bán.</p>
              </div>
            </div>

            <div className="admin-products__ranking-list">
              {Array.isArray(dashboard.topProducts) &&
              dashboard.topProducts.length > 0 ? (
                dashboard.topProducts.map((product, index) => (
                  <article
                    key={product.productId}
                    className="admin-products__ranking-item"
                  >
                    <span className="admin-products__ranking-number">
                      {index + 1}
                    </span>

                    <div className="admin-products__ranking-content">
                      <strong>{product.productName}</strong>

                      <small>Đã bán {product.sold ?? 0} sản phẩm</small>
                    </div>
                  </article>
                ))
              ) : (
                <div className="admin-products__empty">
                  Chưa có dữ liệu sản phẩm.
                </div>
              )}
            </div>
          </section>

          <section className="admin-products__panel">
            <div className="admin-products__panel-heading">
              <div>
                <h2>Doanh thu cao nhất</h2>
                <p>Xếp hạng sản phẩm theo doanh thu đã ghi nhận.</p>
              </div>
            </div>

            <div className="admin-products__ranking-list">
              {Array.isArray(dashboard.topProductsByRevenue) &&
              dashboard.topProductsByRevenue.length > 0 ? (
                dashboard.topProductsByRevenue.map((product, index) => (
                  <article
                    key={product.productId}
                    className="admin-products__ranking-item"
                  >
                    <span className="admin-products__ranking-number">
                      {index + 1}
                    </span>

                    <div className="admin-products__ranking-content">
                      <strong>{product.productName}</strong>

                      <small>
                        {product.sold ?? 0} đã bán ·{" "}
                        {formatCurrency(product.revenue)}
                      </small>
                    </div>
                  </article>
                ))
              ) : (
                <div className="admin-products__empty">
                  Chưa có dữ liệu doanh thu sản phẩm.
                </div>
              )}
            </div>
          </section>
        </div>

        <section className="admin-products__panel">
          <div className="admin-products__panel-heading">
            <div>
              <h2>Sản phẩm chưa bán được</h2>

              <p>Các sản phẩm chưa từng phát sinh đơn hàng đã thanh toán.</p>
            </div>
          </div>

          <div className="admin-products__product-list">
            {Array.isArray(dashboard.productsNeverSold) &&
            dashboard.productsNeverSold.length > 0 ? (
              dashboard.productsNeverSold.map((product) => (
                <article
                  key={product.productId}
                  className="admin-products__product-item"
                >
                  <div className="admin-products__product-main">
                    <strong>{product.productName}</strong>
                    <small>{product.shopName}</small>
                  </div>

                  <span className="admin-products__badge admin-products__badge--warning">
                    Chưa bán
                  </span>
                </article>
              ))
            ) : (
              <div className="admin-products__empty">
                Không có sản phẩm chưa bán.
              </div>
            )}
          </div>
        </section>

        <section className="admin-products__panel">
          <div className="admin-products__panel-heading">
            <div>
              <h2>Sản phẩm bán chậm</h2>

              <p>Sản phẩm đã bán từ 1 đến 2 nhưng còn tồn kho lớn.</p>
            </div>
          </div>

          <div className="admin-products__product-list">
            {Array.isArray(dashboard.slowMovingProducts) &&
            dashboard.slowMovingProducts.length > 0 ? (
              dashboard.slowMovingProducts.map((product) => (
                <article
                  key={product.productId}
                  className="admin-products__product-item"
                >
                  <div className="admin-products__product-main">
                    <strong>{product.productName}</strong>

                    <small>
                      {product.shopName} · Đã bán {product.sold ?? 0} ·{" "}
                      {formatCurrency(product.revenue)}
                    </small>
                  </div>

                  <div className="admin-products__product-meta">
                    <span>
                      Tồn kho: <strong>{product.effectiveStock ?? 0}</strong>
                    </span>

                    <span className="admin-products__badge admin-products__badge--slow">
                      Bán chậm
                    </span>
                  </div>
                </article>
              ))
            ) : (
              <div className="admin-products__empty">
                Không có sản phẩm bán chậm.
              </div>
            )}
          </div>
        </section>

        <section className="admin-products__panel">
          <div className="admin-products__panel-heading">
            <div>
              <h2>Phân tích toàn bộ sản phẩm</h2>

              <p>
                Tổng hợp doanh thu, số lượng bán và tồn kho của tất cả sản phẩm.
              </p>
            </div>
          </div>

          {productLoading && (
            <div className="admin-products__state">
              <LoadingSpinner size="medium" />
            </div>
          )}

          {!productLoading && productError && (
            <div className="admin-products__error">{productError}</div>
          )}

          {!productLoading && !productError && (
            <div className="admin-products__table-wrapper">
              <table className="admin-products__table">
                <thead>
                  <tr>
                    <th>Sản phẩm</th>
                    <th>Shop</th>
                    <th>Đã bán</th>
                    <th>Doanh thu</th>
                    <th>Tồn kho</th>
                    <th>Trạng thái</th>
                  </tr>
                </thead>

                <tbody>
                  {productAnalytics.length > 0 ? (
                    productAnalytics.map((product) => {
                      const sold = Number(product.sold ?? 0);
                      const stock = Number(product.effectiveStock ?? 0);

                      let status = "Bình thường";
                      let statusClass = "normal";

                      if (stock <= 0) {
                        status = "Hết hàng";
                        statusClass = "danger";
                      } else if (stock <= 5) {
                        status = "Sắp hết hàng";
                        statusClass = "warning";
                      } else if (sold === 0) {
                        status = "Chưa bán";
                        statusClass = "warning";
                      } else if (sold <= 2 && stock > 5) {
                        status = "Bán chậm";
                        statusClass = "slow";
                      }

                      return (
                        <tr key={product.productId}>
                          <td>
                            <strong className="admin-products__table-product-name">
                              {product.productName}
                            </strong>
                          </td>

                          <td>{product.shopName}</td>

                          <td>{sold}</td>

                          <td>{formatCurrency(product.revenue)}</td>

                          <td>{stock}</td>

                          <td>
                            <span
                              className={`admin-products__table-status admin-products__table-status--${statusClass}`}
                            >
                              {status}
                            </span>
                          </td>
                        </tr>
                      );
                    })
                  ) : (
                    <tr>
                      <td colSpan="6">
                        <div className="admin-products__empty">
                          Chưa có dữ liệu phân tích sản phẩm.
                        </div>
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          )}

          {!productLoading && !productError && productTotalPages > 1 && (
            <div className="admin-products__pagination">
              <button
                type="button"
                disabled={productPage === 0}
                onClick={() =>
                  setProductPage((current) => Math.max(current - 1, 0))
                }
              >
                Trang trước
              </button>

              <span>
                Trang {productPage + 1} / {productTotalPages}
              </span>

              <button
                type="button"
                disabled={productPage + 1 >= productTotalPages}
                onClick={() => setProductPage((current) => current + 1)}
              >
                Trang sau
              </button>
            </div>
          )}
        </section>
      </div>
    </section>
  );
}

export default AdminProducts;
