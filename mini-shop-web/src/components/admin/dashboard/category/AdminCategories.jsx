import { useEffect, useState } from "react";

import dashboardApi from "../../../../api/dashboardApi";

import LoadingSpinner from "../../../common/LoadingSpinner";

import "./AdminCategories.css";

function formatCurrency(value) {
  return `${Number(value ?? 0).toLocaleString("vi-VN")} ₫`;
}

function AdminCategories() {
  const [categories, setCategories] = useState([]);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    const loadCategories = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await dashboardApi.getAdminCategories({
          page,
          size: 10,
        });

        const apiResponse = response.data;

        if (!apiResponse?.success || !apiResponse?.data) {
          throw new Error(
            apiResponse?.message ?? "Không thể tải dữ liệu danh mục.",
          );
        }

        if (!cancelled) {
          setCategories(apiResponse.data.content ?? []);

          setTotalPages(apiResponse.data.totalPages ?? 0);

          setTotalElements(apiResponse.data.totalElements ?? 0);
        }
      } catch (err) {
        console.error("Unable to load admin categories:", err);

        if (!cancelled) {
          setError(
            err.response?.data?.message ??
              err.message ??
              "Không thể tải dữ liệu danh mục.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    loadCategories();

    return () => {
      cancelled = true;
    };
  }, [page]);

  return (
    <section className="admin-categories">
      <div className="admin-categories__heading">
        <div>
          <h1>Danh mục</h1>

          <p>
            Phân tích số lượng sản phẩm, doanh số và doanh thu theo từng danh
            mục.
          </p>
        </div>
      </div>

      <section className="admin-categories__panel">
        <div className="admin-categories__panel-heading">
          <div>
            <h2>Phân tích danh mục</h2>

            <p>Tổng cộng {totalElements} danh mục trong hệ thống.</p>
          </div>
        </div>

        {loading && (
          <div className="admin-categories__state">
            <LoadingSpinner size="medium" />
          </div>
        )}

        {!loading && error && (
          <div className="admin-categories__error">{error}</div>
        )}

        {!loading && !error && (
          <div className="admin-categories__table-wrapper">
            <table className="admin-categories__table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Danh mục</th>
                  <th>Sản phẩm</th>
                  <th>Đã bán</th>
                  <th>Doanh thu</th>
                  <th>Trạng thái</th>
                </tr>
              </thead>

              <tbody>
                {categories.length > 0 ? (
                  categories.map((category) => {
                    const productCount = Number(category.productCount ?? 0);

                    const sold = Number(category.sold ?? 0);

                    let status = "Đang hoạt động";
                    let statusClass = "normal";

                    if (productCount === 0) {
                      status = "Chưa có sản phẩm";
                      statusClass = "empty";
                    } else if (sold === 0) {
                      status = "Chưa có doanh số";
                      statusClass = "warning";
                    }

                    return (
                      <tr key={category.categoryId}>
                        <td>
                          <strong>#{category.categoryId}</strong>
                        </td>

                        <td>
                          <strong className="admin-categories__name">
                            {category.categoryName}
                          </strong>
                        </td>

                        <td>{productCount}</td>

                        <td>{sold}</td>

                        <td>{formatCurrency(category.revenue)}</td>

                        <td>
                          <span
                            className={`admin-categories__status admin-categories__status--${statusClass}`}
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
                      <div className="admin-categories__empty">
                        Chưa có dữ liệu danh mục.
                      </div>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}

        {!loading && !error && totalPages > 1 && (
          <div className="admin-categories__pagination">
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

export default AdminCategories;
