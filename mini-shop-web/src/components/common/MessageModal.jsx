import "./MessageModal.css";

function MessageModal({
  open,
  title = "Thông báo",
  message,
  type = "error",
  onClose,
}) {
  if (!open) {
    return null;
  }

  return (
    <div
      className="message-modal__overlay"
      role="presentation"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose();
        }
      }}
    >
      <div
        className="message-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="message-modal-title"
      >
        <div className={`message-modal__icon message-modal__icon--${type}`}>
          {type === "success" ? "✓" : type === "warning" ? "!" : "×"}
        </div>

        <h2 id="message-modal-title">{title}</h2>

        <p>{message}</p>

        <button
          type="button"
          className="message-modal__button"
          onClick={onClose}
        >
          Đóng
        </button>
      </div>
    </div>
  );
}

export default MessageModal;
