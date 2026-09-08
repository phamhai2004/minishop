import { useEffect, useState } from "react";

import { Link } from "react-router-dom";

import flashSaleApi from "../../api/flashSaleApi";

import ProductCard from "../product/ProductCard";

import LoadingSpinner from "../common/LoadingSpinner";

import "./HomeFlashSale.css";

function mapFlashSaleToProduct(flashSale) {
  return {
    id: flashSale.productId,
    name: flashSale.productName,

    price: flashSale.originalPrice,

    salePrice: flashSale.salePrice,

    images: flashSale.productImage
      ? [
          {
            imageUrl: flashSale.productImage,
          },
        ]
      : [],

    quantity: flashSale.remainingQuantity,

    flashSale: true,

    flashSaleQuantity: flashSale.quantity,

    flashSaleSold: flashSale.sold,
  };
}

function HomeFlashSale() {
  const [items, setItems] = useState([]);

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    const loadFlashSale = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await flashSaleApi.getActive({
          page: 0,
          size: 6,
        });

        const data = response.data?.data;

        if (cancelled) {
          return;
        }

        setItems(Array.isArray(data?.content) ? data.content : []);
      } catch (err) {
        console.error("Unable to load flash sale:", err);

        if (!cancelled) {
          setError(err.response?.data?.message ?? "Không thể tải Flash Sale.");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    void loadFlashSale();

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <section className="home-section home-flash-sale">
      <div className="home-section__heading home-flash-sale__heading">
        <h2>⚡ FLASH SALE</h2>

        <Link to="/flash-sales" className="home-section__heading-link">
          Xem tất cả ›
        </Link>
      </div>

      {loading ? (
        <div className="home-section__state">
          <LoadingSpinner size="medium" />
        </div>
      ) : error ? (
        <p className="home-section__error">{error}</p>
      ) : items.length === 0 ? (
        <p className="home-section__empty">
          Hiện chưa có Flash Sale đang diễn ra.
        </p>
      ) : (
        <div className="home-product-grid">
          {items.map((item) => (
            <ProductCard key={item.id} product={mapFlashSaleToProduct(item)} />
          ))}
        </div>
      )}
    </section>
  );
}

export default HomeFlashSale;
