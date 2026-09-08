import { useNavigate } from "react-router-dom";

import { clearPendingProductAction } from "../../utils/pendingProductAction";

import "./AuthRequiredModal.css";

function AuthRequiredModal({ isOpen, onClose }) {
  const navigate = useNavigate();

  if (!isOpen) {
    return null;
  }

  const handleLogin = () => {
    navigate("/login");
  };

  const handleRegister = () => {
    navigate("/register");
  };

  const handleClose = () => {
    clearPendingProductAction();
    onClose();
  };

  return (
    <div
      className="auth-required-modal__overlay"
      role="presentation"
      onMouseDown={handleClose}
    >
      <div
        className="auth-required-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="auth-required-modal-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <div className="auth-required-modal__icon">🔐</div>

        <h2 id="auth-required-modal-title">Yêu cầu đăng nhập</h2>

        <p>Bạn cần đăng nhập hoặc đăng ký để tiếp tục.</p>

        <div className="auth-required-modal__actions">
          <button
            type="button"
            className="auth-required-modal__login"
            onClick={handleLogin}
          >
            Đăng nhập
          </button>

          <button
            type="button"
            className="auth-required-modal__register"
            onClick={handleRegister}
          >
            Đăng ký
          </button>
        </div>

        <button
          type="button"
          className="auth-required-modal__cancel"
          onClick={handleClose}
        >
          Hủy
        </button>
      </div>
    </div>
  );
}

export default AuthRequiredModal;
