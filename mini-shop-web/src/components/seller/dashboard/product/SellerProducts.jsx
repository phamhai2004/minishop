import { useEffect, useState } from "react";

import dashboardApi from "../../../../api/dashboardApi";

import LoadingSpinner from "../../../common/LoadingSpinner";

import "./SellerProducts.css";

function formatCurrency(value) {
  return `${Number(value ?? 0).toLocaleString("vi-VN")} ₫`;
}

function SellerProducts({ dashboard }) {
  const [products, setProducts] = useState([]);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    const loadProducts = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await dashboardApi.getSellerProducts({
          page,
          size: 5,
        });

        const apiResponse = response.data;

        if (!apiResponse?.success || !apiResponse?.data) {
          throw new Error(
            apiResponse?.message ?? "Không thể tải dữ liệu sản phẩm.",
          );
        }

        if (!cancelled) {
          setProducts(apiResponse.data.content ?? []);
          setTotalPages(apiResponse.data.totalPages ?? 0);
          setTotalElements(apiResponse.data.totalElements ?? 0);
        }
      } catch (err) {
        console.error("Unable to load seller products:", err);

        if (!cancelled) {
          setError(
            err.response?.data?.message ??
              err.message ??
              "Không thể tải dữ liệu sản phẩm.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    loadProducts();

    return () => {
      cancelled = true;
    };
  }, [page]);

  return (
    <section className="seller-products">
      <div className="seller-products__heading">
        <div>
          <h1>Sản phẩm</h1>

          <p>Theo dõi doanh số, doanh thu và tồn kho sản phẩm của shop.</p>
        </div>
      </div>

      {dashboard && (
        <div className="seller-products__kpis">
          <article className="seller-products__kpi">
            <span>Tổng sản phẩm</span>

            <strong>{dashboard.totalProducts ?? 0}</strong>

            <small>Sản phẩm thuộc shop</small>
          </article>

          <article className="seller-products__kpi">
            <span>Còn hàng</span>

            <strong>{dashboard.inStockProducts ?? 0}</strong>

            <small>Tồn kho lớn hơn 5</small>
          </article>

          <article className="seller-products__kpi">
            <span>Sắp hết hàng</span>

            <strong>{dashboard.lowStockProducts ?? 0}</strong>

            <small>Tồn kho từ 1 đến 5</small>
          </article>

          <article className="seller-products__kpi">
            <span>Hết hàng</span>

            <strong>{dashboard.outOfStockProducts ?? 0}</strong>

            <small>Tồn kho bằng 0</small>
          </article>
        </div>
      )}

      <div className="seller-products__sections">
        {dashboard && (
          <section className="seller-products__panel">
            <div className="seller-products__panel-heading">
              <div>
                <h2>Sản phẩm bán chạy</h2>

                <p>Những sản phẩm có số lượng bán cao nhất của shop.</p>
              </div>
            </div>

            <div className="seller-products__ranking-list">
              {Array.isArray(dashboard.topProducts) &&
              dashboard.topProducts.length > 0 ? (
                dashboard.topProducts.map((product, index) => (
                  <article
                    key={product.productId}
                    className="seller-products__ranking-item"
                  >
                    <span className="seller-products__ranking-number">
                      {index + 1}
                    </span>

                    <div className="seller-products__ranking-content">
                      <strong>{product.productName}</strong>

                      <small>Đã bán {product.sold ?? 0} sản phẩm</small>
                    </div>
                  </article>
                ))
              ) : (
                <div className="seller-products__empty">
                  Chưa có dữ liệu sản phẩm bán chạy.
                </div>
              )}
            </div>
          </section>
        )}

        <section className="seller-products__panel">
          <div className="seller-products__panel-heading">
            <div>
              <h2>Phân tích sản phẩm</h2>

              <p>Tổng cộng {totalElements} sản phẩm của shop.</p>
            </div>
          </div>

          {loading && (
            <div className="seller-products__state">
              <LoadingSpinner size="medium" />
            </div>
          )}

          {!loading && error && (
            <div className="seller-products__error">{error}</div>
          )}

          {!loading && !error && (
            <div className="seller-products__table-wrapper">
              <table className="seller-products__table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Sản phẩm</th>
                    <th>Đã bán</th>
                    <th>Doanh thu</th>
                    <th>Tồn kho</th>
                    <th>Trạng thái</th>
                  </tr>
                </thead>

                <tbody>
                  {products.length > 0 ? (
                    products.map((product) => {
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
                            <strong>#{product.productId}</strong>
                          </td>

                          <td>
                            <strong className="seller-products__name">
                              {product.productName}
                            </strong>
                          </td>

                          <td>{sold}</td>

                          <td>{formatCurrency(product.revenue)}</td>

                          <td>{stock}</td>

                          <td>
                            <span
                              className={`seller-products__status seller-products__status--${statusClass}`}
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
                        <div className="seller-products__empty">
                          Chưa có dữ liệu sản phẩm.
                        </div>
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          )}

          {!loading && !error && totalPages > 1 && (
            <div className="seller-products__pagination">
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
      </div>
    </section>
  );
}

export default SellerProducts;
