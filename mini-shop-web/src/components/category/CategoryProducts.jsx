import { useEffect, useState } from "react";

import productApi from "../../api/productApi";

import ProductCard from "../product/ProductCard";

import HomePagination from "../home/HomePagination";

import LoadingSpinner from "../common/LoadingSpinner";

import "./CategoryProducts.css";

function CategoryProducts({ categoryId, categoryName }) {
  const [products, setProducts] = useState([]);

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

    const loadProducts = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await productApi.getByCategory(categoryId, {
          page,
          size: 12,
        });

        const data = response.data?.data;

        if (cancelled) {
          return;
        }

        setProducts(Array.isArray(data?.content) ? data.content : []);

        setTotalPages(Number(data?.totalPages ?? 0));
      } catch (err) {
        console.error("Unable to load category products:", err);

        if (!cancelled) {
          setError(err.response?.data?.message ?? "Không thể tải sản phẩm.");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    void loadProducts();

    return () => {
      cancelled = true;
    };
  }, [categoryId, page]);

  if (!categoryId) {
    return null;
  }

  return (
    <section className="home-section category-products">
      <div className="home-section__heading">
        <h2>SẢN PHẨM {categoryName ? `• ${categoryName}` : ""}</h2>
      </div>

      {loading ? (
        <div className="home-section__state">
          <LoadingSpinner size="medium" />
        </div>
      ) : error ? (
        <p className="home-section__error">{error}</p>
      ) : products.length === 0 ? (
        <p className="home-section__empty">Chưa có sản phẩm phù hợp.</p>
      ) : (
        <>
          <div className="home-product-grid">
            {products.map((product) => (
              <ProductCard key={product.id} product={product} />
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

export default CategoryProducts;
