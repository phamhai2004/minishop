import ProductCard from "./ProductCard";

import "./ProductGrid.css";

function ProductGrid({ products }) {
  if (!Array.isArray(products) || products.length === 0) {
    return null;
  }

  return (
    <section className="product-grid" aria-label="Danh sách sản phẩm">
      {products.map((product) => (
        <ProductCard key={product.id} product={product} />
      ))}
    </section>
  );
}

export default ProductGrid;
