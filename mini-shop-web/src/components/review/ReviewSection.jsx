import { useCallback, useEffect, useMemo, useState } from "react";

import reviewApi from "../../api/reviewApi";

import LoadingSpinner from "../common/LoadingSpinner";

import "./ReviewSection.css";

function formatDate(value) {
  if (!value) {
    return "—";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleDateString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
}

function getInitials(fullName) {
  if (!fullName) {
    return "?";
  }

  const words = fullName.trim().split(/\s+/);

  if (words.length === 1) {
    return words[0].charAt(0).toUpperCase();
  }

  return (words[0].charAt(0) + words[words.length - 1].charAt(0)).toUpperCase();
}

function getPaginationItems(currentPage, totalPages) {
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, index) => index + 1);
  }

  const items = [1];

  if (currentPage > 4) {
    items.push("...");
  }

  const startPage = Math.max(2, currentPage - 1);
  const endPage = Math.min(totalPages - 1, currentPage + 1);

  for (let page = startPage; page <= endPage; page++) {
    items.push(page);
  }

  if (currentPage < totalPages - 3) {
    items.push("...");
  }

  items.push(totalPages);

  return items;
}

function StarRating({ rating, size = "normal" }) {
  const value = Number(rating ?? 0);

  return (
    <div
      className={`review-stars review-stars--${size}`}
      aria-label={`${value} trên 5 sao`}
    >
      {[1, 2, 3, 4, 5].map((star) => (
        <span
          key={star}
          className={
            star <= value ? "review-star review-star--active" : "review-star"
          }
        >
          ★
        </span>
      ))}
    </div>
  );
}

function ReviewSection({ productId }) {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedRating, setSelectedRating] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);

  const REVIEWS_PER_PAGE = 5;

  const loadReviews = useCallback(async () => {
    if (!productId) {
      setReviews([]);
      setCurrentPage(1);
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError("");
      setCurrentPage(1);

      const response = await reviewApi.getByProduct(productId);

      const apiResponse = response.data;

      if (!apiResponse?.success) {
        throw new Error(
          apiResponse?.message ?? "Không thể tải đánh giá sản phẩm.",
        );
      }

      const reviewList = Array.isArray(apiResponse.data)
        ? apiResponse.data
        : [];

      setReviews(reviewList);
    } catch (requestError) {
      console.error("Unable to load product reviews:", requestError);

      setReviews([]);

      setError(
        requestError.response?.data?.message ??
          requestError.message ??
          "Không thể tải đánh giá sản phẩm.",
      );
    } finally {
      setLoading(false);
    }
  }, [productId]);

  useEffect(() => {
    loadReviews();
  }, [loadReviews]);

  const reviewStats = useMemo(() => {
    const total = reviews.length;

    const distribution = {
      5: 0,
      4: 0,
      3: 0,
      2: 0,
      1: 0,
    };

    if (total === 0) {
      return {
        total: 0,
        average: 0,
        distribution,
      };
    }

    let totalRating = 0;

    reviews.forEach((review) => {
      const rating = Math.min(Math.max(Number(review.rating ?? 0), 1), 5);

      totalRating += rating;

      const roundedRating = Math.round(rating);

      if (distribution[roundedRating] !== undefined) {
        distribution[roundedRating] += 1;
      }
    });

    return {
      total,
      average: totalRating / total,
      distribution,
    };
  }, [reviews]);

  const filteredReviews = useMemo(() => {
    if (selectedRating === 0) {
      return reviews;
    }

    return reviews.filter((review) => Number(review.rating) === selectedRating);
  }, [reviews, selectedRating]);

  const totalPages = Math.ceil(filteredReviews.length / REVIEWS_PER_PAGE);

  const paginatedReviews = useMemo(() => {
    const startIndex = (currentPage - 1) * REVIEWS_PER_PAGE;

    const endIndex = startIndex + REVIEWS_PER_PAGE;

    return filteredReviews.slice(startIndex, endIndex);
  }, [filteredReviews, currentPage]);

  useEffect(() => {
    if (totalPages > 0 && currentPage > totalPages) {
      setCurrentPage(totalPages);
    }
  }, [currentPage, totalPages]);

  return (
    <section className="review-section">
      <div className="review-section__header">
        <div>
          <h2 className="review-section__title">Đánh giá sản phẩm</h2>
        </div>

        {reviewStats.total > 0 && (
          <span className="review-section__count">
            {reviewStats.total} đánh giá
          </span>
        )}
      </div>

      {loading && (
        <div className="review-section__status" role="status">
          <LoadingSpinner size="medium" />
        </div>
      )}

      {!loading && error && (
        <div
          className="review-section__status review-section__status--error"
          role="alert"
        >
          <span className="review-section__status-icon">⚠️</span>

          <p>{error}</p>

          <button
            type="button"
            className="review-section__retry"
            onClick={loadReviews}
          >
            Thử lại
          </button>
        </div>
      )}

      {!loading && !error && reviews.length === 0 && (
        <div className="review-section__empty">
          <div className="review-section__empty-icon">⭐</div>

          <h3>Chưa có đánh giá</h3>

          <p>Sản phẩm này chưa có đánh giá nào.</p>
        </div>
      )}

      {!loading && !error && reviews.length > 0 && (
        <>
          <div className="review-summary">
            <div className="review-summary__score">
              <strong>{reviewStats.average.toFixed(1)}</strong>

              <StarRating
                rating={Math.round(reviewStats.average)}
                size="large"
              />

              <span>trên 5</span>
            </div>

            <div className="review-summary__distribution">
              {[5, 4, 3, 2, 1].map((rating) => {
                const count = reviewStats.distribution[rating];

                const percentage =
                  reviewStats.total > 0 ? (count / reviewStats.total) * 100 : 0;

                return (
                  <div key={rating} className="review-distribution">
                    <span className="review-distribution__label">
                      {rating} ★
                    </span>

                    <div className="review-distribution__bar">
                      <div
                        className="review-distribution__bar-fill"
                        style={{
                          width: `${percentage}%`,
                        }}
                      />
                    </div>

                    <span className="review-distribution__count">{count}</span>
                  </div>
                );
              })}
            </div>
          </div>

          <div className="review-filters">
            <button
              type="button"
              className={`review-filter ${
                selectedRating === 0 ? "review-filter--active" : ""
              }`}
              onClick={() => {
                setSelectedRating(0);
                setCurrentPage(1);
              }}
            >
              Tất cả ({reviewStats.total})
            </button>

            {[5, 4, 3, 2, 1].map((rating) => (
              <button
                key={rating}
                type="button"
                className={`review-filter ${
                  selectedRating === rating ? "review-filter--active" : ""
                }`}
                onClick={() => {
                  setSelectedRating(rating);
                  setCurrentPage(1);
                }}
              >
                {rating} Sao ({reviewStats.distribution[rating]})
              </button>
            ))}
          </div>

          <div className="review-list">
            {filteredReviews.length === 0 ? (
              <div className="review-filter-empty">
                <div className="review-filter-empty__icon">⭐</div>

                <h3>Chưa có đánh giá {selectedRating} sao</h3>

                <p>
                  Chưa có khách hàng nào đánh giá sản phẩm này
                  {selectedRating > 0 ? ` ${selectedRating} sao` : ""}.
                </p>
              </div>
            ) : (
              paginatedReviews.map((review) => (
                <article key={review.id} className="review-item">
                  <div className="review-item__avatar">
                    {getInitials(review.fullName)}
                  </div>

                  <div className="review-item__content">
                    <h3 className="review-item__name">
                      {review.fullName ?? "Khách hàng"}
                    </h3>

                    <div className="review-item__rating">
                      <StarRating rating={review.rating} />

                      <span className="review-item__date">
                        {formatDate(review.createdAt)}
                      </span>
                    </div>

                    {review.comment?.trim() && (
                      <p className="review-item__comment">{review.comment}</p>
                    )}
                  </div>
                </article>
              ))
            )}
          </div>

          {totalPages > 1 && (
            <div className="review-pagination">
              <button
                type="button"
                className="review-pagination__button"
                disabled={currentPage === 1}
                onClick={() => setCurrentPage((page) => page - 1)}
              >
                ←
              </button>

              {getPaginationItems(currentPage, totalPages).map(
                (item, index) => {
                  if (item === "...") {
                    return (
                      <span
                        key={`ellipsis-${index}`}
                        className="review-pagination__ellipsis"
                      >
                        ...
                      </span>
                    );
                  }

                  return (
                    <button
                      key={item}
                      type="button"
                      className={`review-pagination__button ${
                        currentPage === item
                          ? "review-pagination__button--active"
                          : ""
                      }`}
                      onClick={() => setCurrentPage(item)}
                    >
                      {item}
                    </button>
                  );
                },
              )}

              <button
                type="button"
                className="review-pagination__button"
                disabled={currentPage === totalPages}
                onClick={() => setCurrentPage((page) => page + 1)}
              >
                →
              </button>
            </div>
          )}
        </>
      )}
    </section>
  );
}

export default ReviewSection;
