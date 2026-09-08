import { useCallback, useEffect, useState } from "react";

import { useNavigate } from "react-router-dom";

import reviewApi from "../../../api/reviewApi";
import ReviewModal from "../../../components/review/ReviewModal";
import LoadingSpinner from "../../../components/common/LoadingSpinner";

import "./CustomerReviewsPage.css";

function formatDate(value) {
  if (!value) {
    return "—";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function StarRating({ rating }) {
  const value = Number(rating) || 0;

  return (
    <div className="my-review-stars">
      {[1, 2, 3, 4, 5].map((star) => (
        <span
          key={star}
          className={
            star <= value
              ? "my-review-star my-review-star--active"
              : "my-review-star"
          }
        >
          ★
        </span>
      ))}
    </div>
  );
}

function ProductImage({ imageUrl, productName }) {
  return (
    <div className="my-review-card__image">
      {imageUrl ? (
        <img src={imageUrl} alt={productName} loading="lazy" />
      ) : (
        <span>📦</span>
      )}
    </div>
  );
}

function CustomerReviewsPage() {
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState("pending");

  const [pendingReviews, setPendingReviews] = useState([]);

  const [myReviews, setMyReviews] = useState([]);

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState("");

  const [reviewModal, setReviewModal] = useState({
    open: false,
    product: null,
  });

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const [pendingResponse, reviewedResponse] = await Promise.all([
        reviewApi.getMyPendingReviews(),
        reviewApi.getMyReviews(),
      ]);

      setPendingReviews(pendingResponse.data?.data ?? []);

      setMyReviews(reviewedResponse.data?.data ?? []);
    } catch (err) {
      console.error("Không thể tải đánh giá:", err);

      setError(
        err.response?.data?.message ||
          err.message ||
          "Không thể tải danh sách đánh giá.",
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadData();
  }, [loadData]);

  const openReviewModal = (item) => {
    setReviewModal({
      open: true,

      product: {
        productId: item.productId,
        productName: item.productName,
        imageUrl: item.imageUrl,
      },
    });
  };

  const closeReviewModal = () => {
    setReviewModal({
      open: false,
      product: null,
    });
  };

  const handleReviewSuccess = async () => {
    closeReviewModal();

    await loadData();

    setActiveTab("reviewed");
  };

  return (
    <main className="customer-reviews-page">
      <div className="customer-reviews-page__container">
        <header className="customer-reviews-page__header">
          <h1>Đánh giá của tôi</h1>

          <p>
            Quản lý những sản phẩm chưa đánh giá và những đánh giá bạn đã gửi.
          </p>
        </header>

        <section className="customer-reviews-box">
          <div className="customer-reviews-tabs">
            <button
              type="button"
              className={
                activeTab === "pending"
                  ? "customer-reviews-tab customer-reviews-tab--active"
                  : "customer-reviews-tab"
              }
              onClick={() => setActiveTab("pending")}
            >
              Chưa đánh giá
              <span>{pendingReviews.length}</span>
            </button>

            <button
              type="button"
              className={
                activeTab === "reviewed"
                  ? "customer-reviews-tab customer-reviews-tab--active"
                  : "customer-reviews-tab"
              }
              onClick={() => setActiveTab("reviewed")}
            >
              Đã đánh giá
              <span>{myReviews.length}</span>
            </button>
          </div>

          {error && <div className="customer-reviews-error">{error}</div>}

          {loading ? (
            <div className="customer-reviews-status">
              <LoadingSpinner size="medium" />
            </div>
          ) : (
            <>
              {activeTab === "pending" && (
                <div className="customer-reviews-list">
                  {pendingReviews.length === 0 ? (
                    <div className="customer-reviews-empty">
                      <span>⭐</span>

                      <h2>Không có sản phẩm cần đánh giá</h2>

                      <p>
                        Sản phẩm đã hoàn thành đơn hàng nhưng chưa đánh giá sẽ
                        xuất hiện tại đây.
                      </p>
                    </div>
                  ) : (
                    pendingReviews.map((item) => (
                      <article key={item.productId} className="my-review-card">
                        <div className="my-review-card__shop">
                          <strong>🏪 {item.shopName || "Hair"}</strong>

                          {item.orderCode && (
                            <span>Đơn hàng #{item.orderCode}</span>
                          )}
                        </div>

                        <div className="my-review-card__body">
                          <button
                            type="button"
                            className="my-review-card__product"
                            onClick={() =>
                              navigate(`/products/${item.productId}`)
                            }
                          >
                            <ProductImage
                              imageUrl={item.imageUrl}
                              productName={item.productName}
                            />

                            <div className="my-review-card__info">
                              <h3>{item.productName}</h3>

                              <p>Sản phẩm đã được giao thành công.</p>

                              {item.completedAt && (
                                <time>
                                  Hoàn thành: {formatDate(item.completedAt)}
                                </time>
                              )}
                            </div>
                          </button>

                          <button
                            type="button"
                            className="my-review-card__button"
                            onClick={() => openReviewModal(item)}
                          >
                            Đánh giá
                          </button>
                        </div>
                      </article>
                    ))
                  )}
                </div>
              )}

              {activeTab === "reviewed" && (
                <div className="customer-reviews-list">
                  {myReviews.length === 0 ? (
                    <div className="customer-reviews-empty">
                      <span>✍️</span>

                      <h2>Bạn chưa có đánh giá nào</h2>

                      <p>Những đánh giá đã gửi sẽ xuất hiện tại đây.</p>
                    </div>
                  ) : (
                    myReviews.map((review) => (
                      <article key={review.id} className="my-review-card">
                        <div className="my-review-card__shop">
                          <strong>🏪 {review.shopName || "Hair"}</strong>

                          <span className="my-review-card__done">
                            ✓ Đã đánh giá
                          </span>
                        </div>

                        <div className="my-review-card__body">
                          <button
                            type="button"
                            className="my-review-card__product"
                            onClick={() =>
                              navigate(`/products/${review.productId}`)
                            }
                          >
                            <ProductImage
                              imageUrl={review.imageUrl}
                              productName={review.productName}
                            />

                            <div className="my-review-card__info">
                              <h3>{review.productName}</h3>

                              <StarRating rating={review.rating} />

                              <time>{formatDate(review.createdAt)}</time>
                            </div>
                          </button>
                        </div>

                        <div className="my-review-card__comment">
                          {review.comment?.trim() ? (
                            review.comment
                          ) : (
                            <em>Bạn không để lại bình luận.</em>
                          )}
                        </div>
                      </article>
                    ))
                  )}
                </div>
              )}
            </>
          )}
        </section>
      </div>

      <ReviewModal
        open={reviewModal.open}
        product={reviewModal.product}
        onClose={closeReviewModal}
        onSuccess={handleReviewSuccess}
      />
    </main>
  );
}

export default CustomerReviewsPage;
