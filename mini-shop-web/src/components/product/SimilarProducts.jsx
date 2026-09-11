import { useEffect } from "react";

import useSimilarProducts from "../../hooks/useSimilarProducts";

import LoadingSpinner from "../common/LoadingSpinner";

import ProductCard from "./ProductCard";

import "./SimilarProducts.css";

export default function SimilarProducts({ productId }) {
  const { products, loading, load } = useSimilarProducts();

  useEffect(() => {
    if (productId) {
      load(productId);
    }
  }, [productId]);

  if (loading) {
    return (
      <div className="similar-products">
        <h3>Sản phẩm tương tự</h3>

        <LoadingSpinner size="medium" />
      </div>
    );
  }

  if (!products.length) {
    return null;
  }

  return (
    <div className="similar-products">
      <h3>Sản phẩm tương tự</h3>

      <div className="similar-products-grid">
        {products.map((product) => (
          <ProductCard key={product.id} product={product} />
        ))}
      </div>
    </div>
  );
}
