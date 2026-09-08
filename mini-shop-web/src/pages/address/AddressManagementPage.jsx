import { useCallback, useEffect, useState } from "react";

import addressApi from "../../api/addressApi";

import AddressFormModal from "../../components/address/AddressFormModal";
import LoadingSpinner from "../../components/common/LoadingSpinner";

import "./AddressManagementPage.css";

function formatAddress(address) {
  return [address?.detail, address?.ward, address?.province]
    .filter(Boolean)
    .join(", ");
}

function AddressManagementPage() {
  const [addresses, setAddresses] = useState([]);

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState("");

  const [formOpen, setFormOpen] = useState(false);

  const [editingAddress, setEditingAddress] = useState(null);

  const [actionId, setActionId] = useState(null);

  const loadAddresses = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const response = await addressApi.getMyAddresses();

      const apiResponse = response.data;

      if (!apiResponse?.success) {
        throw new Error(apiResponse?.message ?? "Không thể tải địa chỉ.");
      }

      setAddresses(Array.isArray(apiResponse.data) ? apiResponse.data : []);
    } catch (requestError) {
      console.error("Unable to load addresses:", requestError);

      setError(
        requestError.response?.data?.message ??
          requestError.message ??
          "Không thể tải danh sách địa chỉ.",
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAddresses();
  }, [loadAddresses]);

  const handleAdd = () => {
    setEditingAddress(null);
    setFormOpen(true);
  };

  const handleEdit = (address) => {
    setEditingAddress(address);
    setFormOpen(true);
  };

  const handleCloseForm = () => {
    setFormOpen(false);
    setEditingAddress(null);
  };

  const handleSaved = async () => {
    handleCloseForm();
    await loadAddresses();
  };

  const handleSetDefault = async (address) => {
    if (address.defaultAddress) {
      return;
    }

    try {
      setActionId(address.id);

      await addressApi.setDefault(address.id);

      await loadAddresses();
    } catch (requestError) {
      console.error("Unable to set default address:", requestError);

      setError(
        requestError.response?.data?.message ??
          "Không thể đặt địa chỉ mặc định.",
      );
    } finally {
      setActionId(null);
    }
  };

  const handleDelete = async (address) => {
    const confirmed = window.confirm(
      `Bạn có chắc muốn xóa địa chỉ "${formatAddress(address)}" không?`,
    );

    if (!confirmed) {
      return;
    }

    try {
      setActionId(address.id);

      await addressApi.remove(address.id);

      await loadAddresses();
    } catch (requestError) {
      console.error("Unable to delete address:", requestError);

      setError(
        requestError.response?.data?.message ?? "Không thể xóa địa chỉ.",
      );
    } finally {
      setActionId(null);
    }
  };

  return (
    <section className="address-management">
      <div className="address-management__header">
        <div>
          <h1>Địa chỉ của tôi</h1>

          <p>Quản lý địa chỉ nhận hàng của bạn.</p>
        </div>

        <button
          type="button"
          className="address-management__add"
          onClick={handleAdd}
        >
          <span>＋</span>
          Thêm địa chỉ mới
        </button>
      </div>

      {error && (
        <div className="address-management__error" role="alert">
          {error}
        </div>
      )}

      {loading ? (
        <div className="address-management__state">
          <LoadingSpinner size="medium" />
        </div>
      ) : addresses.length === 0 ? (
        <div className="address-management__empty">
          <div className="address-management__empty-icon">⌂</div>

          <h2>Bạn chưa có địa chỉ</h2>

          <p>Hãy thêm địa chỉ để sử dụng khi thanh toán.</p>

          <button type="button" onClick={handleAdd}>
            ＋ Thêm địa chỉ mới
          </button>
        </div>
      ) : (
        <div className="address-management__list">
          {addresses.map((address) => {
            const busy = Number(actionId) === Number(address.id);

            return (
              <article key={address.id} className="address-card">
                <div className="address-card__main">
                  <div className="address-card__top">
                    <strong>{address.receiverName}</strong>

                    <span className="address-card__divider" />

                    <span>{address.phone}</span>
                  </div>

                  <p className="address-card__address">
                    {formatAddress(address)}
                  </p>

                  {address.latitude != null && address.longitude != null && (
                    <p className="address-card__location">
                      Đã xác định vị trí chính xác
                    </p>
                  )}

                  {address.defaultAddress && (
                    <span className="address-card__default">Mặc định</span>
                  )}
                </div>

                <div className="address-card__actions">
                  <button
                    type="button"
                    onClick={() => handleEdit(address)}
                    disabled={busy}
                  >
                    Sửa
                  </button>

                  <button
                    type="button"
                    className="address-card__delete"
                    onClick={() => handleDelete(address)}
                    disabled={busy}
                  >
                    Xóa
                  </button>

                  {!address.defaultAddress && (
                    <button
                      type="button"
                      className="address-card__set-default"
                      onClick={() => handleSetDefault(address)}
                      disabled={busy}
                    >
                      Đặt làm mặc định
                    </button>
                  )}
                </div>
              </article>
            );
          })}
        </div>
      )}

      <AddressFormModal
        open={formOpen}
        address={editingAddress}
        onClose={handleCloseForm}
        onSaved={handleSaved}
      />
    </section>
  );
}

export default AddressManagementPage;
