import "./OrderSuccessModal.css";

function OrderSuccessModal({ open, onViewOrder, onContinueShopping }) {
  if (!open) {
    return null;
  }

  return (
    <div
      className="order-success-modal__overlay"
      role="dialog"
      aria-modal="true"
      aria-labelledby="order-success-title"
    >
      <div className="order-success-modal">
        <div className="order-success-modal__icon">✓</div>

        <h2 id="order-success-title" className="order-success-modal__title">
          Đặt hàng thành công!
        </h2>

        <p className="order-success-modal__message">
          Cảm ơn bạn đã mua hàng tại Hair.
        </p>

        <div className="order-success-modal__actions">
          <button
            type="button"
            className="order-success-modal__button order-success-modal__button--secondary"
            onClick={onContinueShopping}
          >
            Tiếp tục mua sắm
          </button>

          <button
            type="button"
            className="order-success-modal__button order-success-modal__button--primary"
            onClick={onViewOrder}
          >
            Xem đơn hàng
          </button>
        </div>
      </div>
    </div>
  );
}

export default OrderSuccessModal;
