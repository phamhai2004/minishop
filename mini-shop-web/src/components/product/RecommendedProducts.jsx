import { useEffect, useState } from "react";

import { useAuth } from "../../contexts/AuthContext";

import ROLES from "../constants/roles";

import recommendationApi from "../../api/recommendationApi";
import productApi from "../../api/productApi";

import ProductCard from "../product/ProductCard";

import LoadingSpinner from "../common/LoadingSpinner";

import "./RecommendedProducts.css";

function RecommendedProducts() {
  const { currentUser, isAuthenticated } = useAuth();

  const [products, setProducts] = useState([]);

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState("");

  const isCustomer = isAuthenticated && currentUser?.role === ROLES.CUSTOMER;

  useEffect(() => {
    let cancelled = false;

    const loadProducts = async () => {
      try {
        setLoading(true);
        setError("");

        let data;

        if (isCustomer) {
          const response = await recommendationApi.getMine();

          data = response.data?.data;
        } else {
          const response = await productApi.getAll({
            page: 0,
            size: 12,
            sort: "id",
            direction: "desc",
          });

          data = response.data?.data?.content;
        }

        if (cancelled) {
          return;
        }

        setProducts(Array.isArray(data) ? data.slice(0, 12) : []);
      } catch (err) {
        console.error("Unable to load recommendations:", err);

        if (!cancelled) {
          setError(
            err.response?.data?.message ?? "Không thể tải gợi ý sản phẩm.",
          );
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
  }, [isCustomer]);

  return (
    <section className="home-section recommended-products">
      <div className="home-section__heading">
        <h2>GỢI Ý HÔM NAY</h2>
      </div>

      {loading ? (
        <div className="home-section__state">
          <LoadingSpinner size="medium" />
        </div>
      ) : error ? (
        <p className="home-section__error">{error}</p>
      ) : products.length === 0 ? (
        <p className="home-section__empty">Chưa có sản phẩm gợi ý.</p>
      ) : (
        <div className="home-product-grid">
          {products.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      )}
    </section>
  );
}

export default RecommendedProducts;
