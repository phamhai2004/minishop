import { useEffect, useState } from "react";

import { Link } from "react-router-dom";

import shopApi from "../../api/shopApi";

import HomePagination from "../home/HomePagination";

import LoadingSpinner from "../common/LoadingSpinner";

import "./CategoryShops.css";

function CategoryShops({ categoryId, categoryName }) {
  const [shops, setShops] = useState([]);
  const [page, setPage] = useState(0);

  const [totalPages, setTotalPages] = useState(0);

  const [loading, setLoading] = useState(false);

  const [error, setError] = useState("");

  useEffect(() => {
    setPage(0);
  }, [categoryId]);

  useEffect(() => {
    if (!categoryId) {
      return;
    }

    let cancelled = false;

    const loadShops = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await shopApi.getByCategory(categoryId, {
          page,
          size: 6,
        });

        const data = response.data?.data;

        if (cancelled) {
          return;
        }

        setShops(Array.isArray(data?.content) ? data.content : []);

        setTotalPages(Number(data?.totalPages ?? 0));
      } catch (err) {
        console.error("Unable to load category shops:", err);

        if (!cancelled) {
          setError(err.response?.data?.message ?? "Không thể tải shop.");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    void loadShops();

    return () => {
      cancelled = true;
    };
  }, [categoryId, page]);

  if (!categoryId) {
    return null;
  }

  return (
    <section className="home-section category-shops">
      <div className="home-section__heading">
        <h2>SHOP {categoryName ? `• ${categoryName}` : ""}</h2>
      </div>

      {loading ? (
        <div className="home-section__state">
          <LoadingSpinner size="medium" />
        </div>
      ) : error ? (
        <p className="home-section__error">{error}</p>
      ) : shops.length === 0 ? (
        <p className="home-section__empty">Chưa có shop phù hợp.</p>
      ) : (
        <>
          <div className="category-shops__grid">
            {shops.map((shop) => (
              <article key={shop.id} className="category-shop-card">
                <div className="category-shop-card__logo">
                  {shop.logoUrl ? (
                    <img src={shop.logoUrl} alt={shop.name} />
                  ) : (
                    <span>{shop.name?.charAt(0)?.toUpperCase() ?? "S"}</span>
                  )}
                </div>

                <div className="category-shop-card__content">
                  <div className="category-shop-card__name-row">
                    <h3>{shop.name}</h3>

                    {shop.verified && (
                      <span
                        className="category-shop-card__verified"
                        title="Shop đã xác minh"
                      >
                        ✓
                      </span>
                    )}
                  </div>

                  <p>⭐ {Number(shop.rating ?? 0).toFixed(1)}</p>

                  <span>{shop.totalProducts ?? 0} sản phẩm</span>
                </div>

                <Link to={`/shops/${shop.id}`}>Xem shop</Link>
              </article>
            ))}
          </div>

          <HomePagination
            page={page}
            totalPages={totalPages}
            onChange={setPage}
          />
        </>
      )}
    </section>
  );
}

export default CategoryShops;
