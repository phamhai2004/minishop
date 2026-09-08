import { useEffect } from "react";

const HISTORY_STATUSES = new Set(["USED", "EXPIRED", "INACTIVE", "SOLD_OUT"]);

const STATUS_LABELS = {
  USED: "Đã sử dụng",
  EXPIRED: "Đã hết hạn",
  INACTIVE: "Ngừng hoạt động",
  SOLD_OUT: "Đã hết lượt",
};

function formatCurrency(value) {
  const amount = Number(value ?? 0);

  if (!Number.isFinite(amount)) {
    return "0₫";
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

function VoucherHistoryModal({ vouchers = [], onClose }) {
  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.key === "Escape") {
        onClose?.();
      }
    };

    window.addEventListener("keydown", handleKeyDown);

    return () => {
      window.removeEventListener("keydown", handleKeyDown);
    };
  }, [onClose]);

  const historyVouchers = vouchers.filter((voucher) =>
    HISTORY_STATUSES.has(voucher.status),
  );

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
        aria-labelledby="voucher-history-title"
      >
        <div className="voucher-modal__header">
          <h2 id="voucher-history-title">Lịch sử Voucher</h2>

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
          {historyVouchers.length === 0 ? (
            <div className="voucher-history__empty">
              Bạn chưa có lịch sử voucher.
            </div>
          ) : (
            <div className="voucher-history">
              {historyVouchers.map((voucher) => {
                const platform = voucher.scope === "PLATFORM";

                return (
                  <article
                    key={voucher.voucherId}
                    className="voucher-history__item"
                  >
                    <div
                      className={`voucher-history__icon ${
                        platform
                          ? "voucher-history__icon--platform"
                          : "voucher-history__icon--shop"
                      }`}
                    >
                      {platform ? "M" : "S"}
                    </div>

                    <div className="voucher-history__content">
                      <div className="voucher-history__source">
                        {platform ? "Hair" : voucher.shopName || "Shop"}
                      </div>

                      <strong>{getDiscountText(voucher)}</strong>

                      <div className="voucher-history__code">
                        Mã: {voucher.code}
                      </div>

                      <div className="voucher-history__date">
                        {voucher.status === "USED"
                          ? `Đã dùng: ${formatDate(voucher.usedAt)}`
                          : `HSD: ${formatDate(voucher.endDate)}`}
                      </div>
                    </div>

                    <span
                      className={`voucher-history__status voucher-history__status--${String(
                        voucher.status,
                      ).toLowerCase()}`}
                    >
                      {STATUS_LABELS[voucher.status] ?? voucher.status}
                    </span>
                  </article>
                );
              })}
            </div>
          )}
        </div>

        <div className="voucher-modal__footer">
          <button type="button" onClick={onClose}>
            Đóng
          </button>
        </div>
      </div>
    </div>
  );
}

export default VoucherHistoryModal;
