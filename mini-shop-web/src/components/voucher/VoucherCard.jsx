import LoadingSpinner from "../common/LoadingSpinner";

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
  }).format(date);
}

function getDiscountTitle(voucher) {
  if (voucher.discountType === "PERCENTAGE") {
    return `Giảm ${Number(voucher.discountValue ?? 0)}%`;
  }

  return `Giảm ${formatCurrency(voucher.discountValue)}`;
}

function isUpcoming(voucher) {
  if (!voucher.startDate) {
    return false;
  }

  const startDate = new Date(voucher.startDate);

  if (Number.isNaN(startDate.getTime())) {
    return false;
  }

  return startDate.getTime() > Date.now();
}

function getSavedStatusLabel(status) {
  switch (status) {
    case "USED":
      return "Đã dùng";

    case "UPCOMING":
      return "Dùng sau";

    case "EXPIRED":
      return "Hết hạn";

    case "SOLD_OUT":
      return "Hết lượt";

    case "INACTIVE":
      return "Ngừng dùng";

    default:
      return "Đã lưu";
  }
}

function VoucherCard({
  voucher,
  userVoucher = null,
  collecting = false,
  showQuantity = true,
  onCollect,
  onUse,
  onShowCondition,
}) {
  const platform = voucher.scope === "PLATFORM";
  const upcoming = isUpcoming(voucher);
  const sourceName = platform ? "HAIR" : voucher.shopName || "SHOP";
  const discountTitle = getDiscountTitle(voucher);
  const remainingQuantity = Number(voucher.quantity ?? 0);
  const saved = userVoucher != null;
  const usableNow = userVoucher?.usableNow === true;
  const savedStatus = userVoucher?.status ?? null;

  return (
    <article
      className={`voucher-card ${
        platform ? "voucher-card--platform" : "voucher-card--shop"
      }`}
    >
      <div className="voucher-card__brand">
        {showQuantity && remainingQuantity > 0 && (
          <span className="voucher-card__quantity">
            Còn {remainingQuantity}
          </span>
        )}

        <div className="voucher-card__brand-icon">{platform ? "M" : "S"}</div>

        <span className="voucher-card__brand-name">{sourceName}</span>
      </div>

      <div className="voucher-card__content">
        <div className="voucher-card__main">
          <div className="voucher-card__title">
            {discountTitle}

            {voucher.maxDiscountAmount != null &&
              voucher.discountType === "PERCENTAGE" && (
                <> Giảm tối đa {formatCurrency(voucher.maxDiscountAmount)}</>
              )}
          </div>

          {Number(voucher.minOrderAmount ?? 0) > 0 && (
            <div className="voucher-card__condition">
              Đơn tối thiểu {formatCurrency(voucher.minOrderAmount)}
            </div>
          )}

          <div className="voucher-card__meta">
            {upcoming ? (
              <span>Có hiệu lực từ {formatDate(voucher.startDate)}</span>
            ) : (
              <span>HSD: {formatDate(voucher.endDate)}</span>
            )}

            <button
              type="button"
              className="voucher-card__condition-button"
              onClick={() => onShowCondition?.(voucher)}
            >
              Điều kiện
            </button>
          </div>
        </div>

        <div className="voucher-card__action">
          {saved && usableNow ? (
            <button
              type="button"
              className="voucher-card__button voucher-card__button--use"
              onClick={() => onUse?.(voucher)}
            >
              Dùng ngay
            </button>
          ) : saved ? (
            <button
              type="button"
              className="voucher-card__button voucher-card__button--saved"
              disabled
            >
              {getSavedStatusLabel(savedStatus)}
            </button>
          ) : upcoming ? (
            <button
              type="button"
              className="voucher-card__button voucher-card__button--upcoming"
              onClick={() => onCollect?.(voucher.code)}
              disabled={collecting}
            >
              {collecting ? (
                <LoadingSpinner size="small" inline variant="light" />
              ) : (
                "Dùng sau"
              )}
            </button>
          ) : (
            <button
              type="button"
              className="voucher-card__button"
              onClick={() => onCollect?.(voucher.code)}
              disabled={collecting}
            >
              {collecting ? (
                <LoadingSpinner size="small" inline variant="light" />
              ) : (
                "Lưu"
              )}
            </button>
          )}
        </div>
      </div>
    </article>
  );
}

export default VoucherCard;
