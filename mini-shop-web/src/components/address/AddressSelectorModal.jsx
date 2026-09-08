import "./AddressSelectorModal.css";

function formatAddress(address) {
  if (!address) {
    return "";
  }

  return [address.detail, address.ward, address.province]
    .filter(Boolean)
    .join(", ");
}

function AddressSelectorModal({
  open,
  addresses = [],
  selectedAddressId = null,
  onSelect,
  onClose,
  onAddAddress,
}) {
  if (!open) {
    return null;
  }

  return (
    <div
      className="address-selector-modal"
      role="dialog"
      aria-modal="true"
      aria-labelledby="address-selector-title"
    >
      <div className="address-selector-modal__overlay" onClick={onClose} />

      <div className="address-selector-modal__content">
        {/* HEADER */}
        <header className="address-selector-modal__header">
          <button
            type="button"
            className="address-selector-modal__back"
            onClick={onClose}
            aria-label="Đóng"
          >
            ←
          </button>

          <h2 id="address-selector-title">Chọn địa chỉ nhận hàng</h2>

          <button
            type="button"
            className="address-selector-modal__close"
            onClick={onClose}
            aria-label="Đóng"
          >
            ×
          </button>
        </header>

        {/* ADDRESS LIST */}
        <div className="address-selector-modal__body">
          {addresses.length === 0 ? (
            <div className="address-selector-modal__empty">
              <h3>Chưa có địa chỉ nhận hàng</h3>

              <p>
                Bạn chưa có địa chỉ nào. Hãy thêm địa chỉ để tiếp tục đặt hàng.
              </p>
            </div>
          ) : (
            <div className="address-selector-modal__list">
              {addresses.map((address) => {
                const selected =
                  Number(address.id) === Number(selectedAddressId);

                return (
                  <button
                    type="button"
                    key={address.id}
                    className={`address-selector-item ${
                      selected ? "address-selector-item--selected" : ""
                    }`}
                    onClick={() => onSelect(address)}
                  >
                    <span
                      className={`address-selector-item__radio ${
                        selected ? "address-selector-item__radio--selected" : ""
                      }`}
                      aria-hidden="true"
                    >
                      {selected && "✓"}
                    </span>

                    <span className="address-selector-item__content">
                      <span className="address-selector-item__top">
                        <strong>{address.receiverName}</strong>

                        <span>{address.phone}</span>
                      </span>

                      <span className="address-selector-item__address">
                        {formatAddress(address)}
                      </span>

                      {address.defaultAddress && (
                        <span className="address-selector-item__default">
                          Mặc định
                        </span>
                      )}
                    </span>
                  </button>
                );
              })}
            </div>
          )}
        </div>

        {/* FOOTER */}
        <footer className="address-selector-modal__footer">
          <button
            type="button"
            className="address-selector-modal__add"
            onClick={onAddAddress}
          >
            <span>＋</span>
            Thêm địa chỉ mới
          </button>
        </footer>
      </div>
    </div>
  );
}

export default AddressSelectorModal;
