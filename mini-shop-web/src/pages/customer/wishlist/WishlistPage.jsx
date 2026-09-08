import { useCallback, useEffect, useState } from "react";

import { Link } from "react-router-dom";

import wishlistApi from "../../../api/wishlistApi";
import HeartIcon from "../../../components/icons/HeartIcon";
import LoadingSpinner from "../../../components/common/LoadingSpinner";

import "./WishlistPage.css";

function formatCurrency(value) {
  return Number(value ?? 0).toLocaleString("vi-VN", {
    style: "currency",
    currency: "VND",
  });
}

function WishlistPage() {
  const [items, setItems] = useState([]);

  const [loading, setLoading] = useState(true);

  const [removingId, setRemovingId] = useState(null);

  const [error, setError] = useState("");

  const loadWishlist = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const response = await wishlistApi.getMyWishlist();

      const data = response.data?.data;

      setItems(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error("Unable to load wishlist:", err);

      setError(
        err.response?.data?.message ?? "Không thể tải danh sách yêu thích.",
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadWishlist();
  }, [loadWishlist]);

  const handleRemove = async (productId) => {
    try {
      setRemovingId(productId);

      setError("");

      await wishlistApi.remove(productId);

      setItems((current) =>
        current.filter((item) => Number(item.productId) !== Number(productId)),
      );

      window.dispatchEvent(
        new CustomEvent("wishlist:changed", {
          detail: {
            productId,
            liked: false,
          },
        }),
      );
    } catch (err) {
      console.error("Unable to remove wishlist:", err);

      setError(
        err.response?.data?.message ??
          "Không thể xóa sản phẩm khỏi danh sách yêu thích.",
      );
    } finally {
      setRemovingId(null);
    }
  };

  if (loading) {
    return (
      <main className="wishlist-page">
        <div className="wishlist-page__state">
          <LoadingSpinner size="large" />
        </div>
      </main>
    );
  }

  return (
    <main className="wishlist-page">
      <div className="wishlist-page__header">
        <div>
          <h1>Sản phẩm yêu thích</h1>

          <p>Những sản phẩm bạn đã lưu để xem lại sau.</p>
        </div>

        <span className="wishlist-page__count">{items.length} sản phẩm</span>
      </div>

      {error && <div className="wishlist-page__error">{error}</div>}

      {items.length === 0 ? (
        <div className="wishlist-page__empty">
          <HeartIcon size={54} strokeWidth={1.5} />

          <h2>Chưa có sản phẩm yêu thích</h2>

          <p>Hãy thêm những sản phẩm bạn quan tâm vào danh sách yêu thích.</p>

          <Link to="/products">Khám phá sản phẩm</Link>
        </div>
      ) : (
        <div className="wishlist-page__grid">
          {items.map((item) => (
            <article
              key={item.id}
              className={`wishlist-card ${
                item.available === false ? "wishlist-card--unavailable" : ""
              }`}
            >
              <div className="wishlist-card__image-wrapper">
                {item.imageUrl ? (
                  <img
                    src={item.imageUrl}
                    alt={item.productName}
                    className="wishlist-card__image"
                  />
                ) : (
                  <div className="wishlist-card__no-image">No image</div>
                )}

                <button
                  type="button"
                  className="wishlist-card__heart"
                  aria-label="Xóa khỏi yêu thích"
                  title="Xóa khỏi yêu thích"
                  disabled={removingId === item.productId}
                  onClick={() => handleRemove(item.productId)}
                >
                  {removingId === item.productId ? (
                    <LoadingSpinner size="small" inline />
                  ) : (
                    <HeartIcon size={24} strokeWidth={1.8} filled />
                  )}
                </button>
              </div>

              <div className="wishlist-card__body">
                <h2>{item.productName}</h2>

                {item.shopName && (
                  <p className="wishlist-card__shop">{item.shopName}</p>
                )}

                <div className="wishlist-card__price">
                  {formatCurrency(item.price)}
                </div>

                {item.available === false && (
                  <div className="wishlist-card__unavailable">
                    Sản phẩm hiện không khả dụng
                  </div>
                )}

                <div className="wishlist-card__actions">
                  {item.available !== false ? (
                    <Link to={`/products/${item.productId}`}>Xem sản phẩm</Link>
                  ) : (
                    <button type="button" disabled>
                      Không khả dụng
                    </button>
                  )}
                </div>
              </div>
            </article>
          ))}
        </div>
      )}
    </main>
  );
}

export default WishlistPage;
