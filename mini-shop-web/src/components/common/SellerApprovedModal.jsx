import "./SellerApprovedModal.css";

function SellerApprovedModal({ open, onConfirm, confirming = false }) {
  if (!open) {
    return null;
  }

  return (
    <div className="seller-approved-modal__overlay">
      <div
        className="seller-approved-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="seller-approved-title"
      >
        <div className="seller-approved-modal__icon">✓</div>

        <h2 id="seller-approved-title">Đăng ký Người bán đã được duyệt</h2>

        <p>
          Tài khoản của bạn đã được duyệt lên
          <strong> Người bán</strong>.
          <br />
          Vui lòng đăng nhập lại để sử dụng quyền Seller.
        </p>

        <button
          type="button"
          className="seller-approved-modal__confirm"
          onClick={onConfirm}
          disabled={confirming}
        >
          {confirming ? "Đang xử lý..." : "Xác nhận"}
        </button>
      </div>
    </div>
  );
}

export default SellerApprovedModal;
