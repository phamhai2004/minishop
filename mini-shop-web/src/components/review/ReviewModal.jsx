import { useEffect, useState } from "react";
import reviewApi from "../../api/reviewApi";
import LoadingSpinner from "../common/LoadingSpinner";
import "./ReviewModal.css";

function ReviewModal({ open, product, onClose, onSuccess }) {
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!open) {
      return;
    }

    setRating(5);
    setComment("");
    setError("");
    setSubmitting(false);
  }, [open, product]);

  if (!open || !product) {
    return null;
  }

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (rating < 1 || rating > 5) {
      setError("Vui lòng chọn số sao từ 1 đến 5");
      return;
    }

    if (comment.trim().length > 1000) {
      setError("Nội dung đánh giá không được vượt quá 1000 ký tự");
      return;
    }

    try {
      setSubmitting(true);
      setError("");

      const response = await reviewApi.createReview({
        productId: product.productId,
        rating,
        comment: comment.trim(),
      });

      if (onSuccess) {
        onSuccess(response.data);
      }

      onClose();
    } catch (err) {
      console.error("Create review error:", err);

      const message =
        err?.response?.data?.message ||
        "Không thể gửi đánh giá. Vui lòng thử lại.";

      setError(message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div
      className="review-modal-overlay"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose();
        }
      }}
    >
      <div
        className="review-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="review-modal-title"
      >
        <div className="review-modal-header">
          <div>
            <h2 id="review-modal-title">Đánh giá sản phẩm</h2>

            <p>Chia sẻ trải nghiệm của bạn với sản phẩm</p>
          </div>

          <button
            type="button"
            className="review-modal-close"
            onClick={onClose}
            disabled={submitting}
            aria-label="Đóng"
          >
            ×
          </button>
        </div>

        <div className="review-product">
          {product.imageUrl ? (
            <img
              src={product.imageUrl}
              alt={product.productName}
              className="review-product-image"
            />
          ) : (
            <div className="review-product-image review-product-image-empty">
              Không có ảnh
            </div>
          )}

          <div className="review-product-info">
            <h3>{product.productName}</h3>

            <p>Bạn đã nhận sản phẩm này. Hãy chia sẻ cảm nhận của bạn nhé!</p>
          </div>
        </div>

        <form className="review-form" onSubmit={handleSubmit}>
          <div className="review-rating-section">
            <label>Đánh giá của bạn</label>

            <div
              className="review-stars"
              role="radiogroup"
              aria-label="Đánh giá từ 1 đến 5 sao"
            >
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  key={star}
                  type="button"
                  className={
                    star <= rating ? "review-star active" : "review-star"
                  }
                  onClick={() => setRating(star)}
                  disabled={submitting}
                  aria-label={`${star} sao`}
                  aria-checked={star === rating}
                  role="radio"
                >
                  ★
                </button>
              ))}
            </div>

            <span className="review-rating-text">
              {rating === 1 && "Rất không hài lòng"}
              {rating === 2 && "Không hài lòng"}
              {rating === 3 && "Bình thường"}
              {rating === 4 && "Hài lòng"}
              {rating === 5 && "Rất hài lòng"}
            </span>
          </div>

          <div className="review-comment-section">
            <label htmlFor="review-comment">Nhận xét</label>

            <textarea
              id="review-comment"
              value={comment}
              onChange={(event) => setComment(event.target.value)}
              placeholder="Hãy chia sẻ cảm nhận của bạn về sản phẩm..."
              maxLength={1000}
              disabled={submitting}
              rows={5}
            />

            <div className="review-character-count">{comment.length}/1000</div>
          </div>

          {error && <div className="review-error">{error}</div>}

          <div className="review-modal-footer">
            <button
              type="button"
              className="review-btn review-btn-cancel"
              onClick={onClose}
              disabled={submitting}
            >
              Hủy
            </button>

            <button
              type="submit"
              className="review-btn review-btn-submit"
              disabled={submitting}
            >
              {submitting ? (
                <LoadingSpinner size="small" inline variant="light" />
              ) : (
                "Gửi đánh giá"
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default ReviewModal;
