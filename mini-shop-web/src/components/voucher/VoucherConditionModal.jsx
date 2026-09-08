import { useEffect } from "react";

function formatCurrency(value) {
  const amount = Number(value ?? 0);

  if (!Number.isFinite(amount)) {
    return "0 ₫";
  }

  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(amount);
}

function formatDate(value) {
  if (!value) {
    return "—";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "—";
  }

  return new Intl.DateTimeFormat("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

function getDiscountText(voucher) {
  if (voucher.discountType === "PERCENTAGE") {
    return `Giảm ${Number(voucher.discountValue ?? 0)}%`;
  }

  return `Giảm ${formatCurrency(voucher.discountValue)}`;
}

function VoucherConditionModal({ voucher, onClose }) {
  useEffect(() => {
    if (!voucher) {
      return undefined;
    }

    const handleKeyDown = (event) => {
      if (event.key === "Escape") {
        onClose?.();
      }
    };

    window.addEventListener("keydown", handleKeyDown);

    return () => {
      window.removeEventListener("keydown", handleKeyDown);
    };
  }, [voucher, onClose]);

  if (!voucher) {
    return null;
  }

  const platform = voucher.scope === "PLATFORM";

  return (
    <div
      className="voucher-modal"
      role="presentation"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose?.();
        }
      }}
    >
      <div
        className="voucher-modal__dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="voucher-condition-title"
      >
        <div className="voucher-modal__header">
          <h2 id="voucher-condition-title">Điều kiện sử dụng Voucher</h2>

          <button
            type="button"
            className="voucher-modal__close"
            onClick={onClose}
            aria-label="Đóng"
          >
            ×
          </button>
        </div>

        <div className="voucher-modal__body">
          <div className="voucher-modal__source">
            <div
              className={`voucher-modal__source-icon ${
                platform
                  ? "voucher-modal__source-icon--platform"
                  : "voucher-modal__source-icon--shop"
              }`}
            >
              {platform ? "M" : "S"}
            </div>

            <div>
              <strong>{platform ? "Hair" : voucher.shopName || "Shop"}</strong>

              <div className="voucher-modal__code">Mã: {voucher.code}</div>
            </div>
          </div>

          <div className="voucher-modal__rows">
            <div className="voucher-modal__row">
              <span>Ưu đãi</span>
              <strong>{getDiscountText(voucher)}</strong>
            </div>

            {voucher.maxDiscountAmount != null && (
              <div className="voucher-modal__row">
                <span>Giảm tối đa</span>
                <strong>{formatCurrency(voucher.maxDiscountAmount)}</strong>
              </div>
            )}

            <div className="voucher-modal__row">
              <span>Đơn tối thiểu</span>
              <strong>{formatCurrency(voucher.minOrderAmount ?? 0)}</strong>
            </div>

            <div className="voucher-modal__row">
              <span>Phạm vi</span>
              <strong>
                {platform
                  ? "Toàn sàn Hair"
                  : `Sản phẩm của ${voucher.shopName || "Shop"}`}
              </strong>
            </div>

            <div className="voucher-modal__row">
              <span>Bắt đầu</span>
              <strong>{formatDate(voucher.startDate)}</strong>
            </div>

            <div className="voucher-modal__row">
              <span>Hết hạn</span>
              <strong>{formatDate(voucher.endDate)}</strong>
            </div>

            <div className="voucher-modal__row">
              <span>Số lượng còn lại</span>
              <strong>{voucher.quantity ?? 0}</strong>
            </div>
          </div>

          <div className="voucher-modal__note">
            Voucher chỉ được áp dụng khi đơn hàng thỏa đầy đủ điều kiện. Hệ
            thống sẽ kiểm tra lại voucher khi đặt hàng.
          </div>
        </div>

        <div className="voucher-modal__footer">
          <button type="button" onClick={onClose}>
            Đã hiểu
          </button>
        </div>
      </div>
    </div>
  );
}

export default VoucherConditionModal;
