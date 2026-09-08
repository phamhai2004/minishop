import { useEffect, useState } from "react";

import addressApi from "../../api/addressApi";
import { searchPhotonAddresses } from "../../utils/photonAddress";

import AddressLocationModal from "./AddressLocationModal";
import AddressMapModal from "./AddressMapModal";
import LoadingSpinner from "../common/LoadingSpinner";

import "./AddAddressModal.css";

const EMPTY_FORM = {
  receiverName: "",
  phone: "",

  province: "",
  provinceCode: null,

  ward: "",
  wardCode: null,

  detail: "",

  latitude: null,
  longitude: null,

  defaultAddress: false,
};

function createFormFromAddress(address) {
  if (!address) {
    return { ...EMPTY_FORM };
  }

  return {
    receiverName: address.receiverName ?? "",

    phone: address.phone ?? "",

    province: address.province ?? "",

    provinceCode: null,

    ward: address.ward ?? "",

    wardCode: null,

    detail: address.detail ?? "",

    latitude: address.latitude ?? null,

    longitude: address.longitude ?? null,

    defaultAddress: Boolean(address.defaultAddress),
  };
}

function AddressFormModal({ open, address = null, onClose, onSaved }) {
  const isEditing = Boolean(address?.id);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [showLocationModal, setShowLocationModal] = useState(false);
  const [showMapModal, setShowMapModal] = useState(false);
  const [detailSuggestions, setDetailSuggestions] = useState([]);
  const [detailSearching, setDetailSearching] = useState(false);
  const [detailSearchError, setDetailSearchError] = useState("");

  useEffect(() => {
    if (!open) {
      return;
    }

    setForm(createFormFromAddress(address));

    setError("");
    setShowLocationModal(false);
    setShowMapModal(false);
  }, [open, address]);

  useEffect(() => {
    if (!open || !form.province || !form.ward || !form.detail.trim()) {
      setDetailSuggestions([]);
      setDetailSearching(false);
      setDetailSearchError("");

      return;
    }

    if (form.latitude != null && form.longitude != null) {
      setDetailSuggestions([]);
      setDetailSearching(false);

      return;
    }

    const controller = new AbortController();

    const timeoutId = setTimeout(async () => {
      try {
        setDetailSearching(true);
        setDetailSearchError("");

        const results = await searchPhotonAddresses({
          keyword: form.detail,

          province: form.province,

          ward: form.ward,

          limit: 5,

          signal: controller.signal,
        });

        setDetailSuggestions(results);
      } catch (searchError) {
        if (searchError.name === "AbortError") {
          return;
        }

        console.error("Unable to search address suggestions:", searchError);

        setDetailSuggestions([]);

        setDetailSearchError("Không thể tải gợi ý địa chỉ.");
      } finally {
        if (!controller.signal.aborted) {
          setDetailSearching(false);
        }
      }
    }, 500);

    return () => {
      clearTimeout(timeoutId);
      controller.abort();
    };
  }, [
    open,

    form.detail,
    form.province,
    form.ward,

    form.latitude,
    form.longitude,
  ]);

  if (!open) {
    return null;
  }

  const handleChange = (event) => {
    const { name, value, checked, type } = event.target;

    setForm((previous) => {
      const next = {
        ...previous,
        [name]: type === "checkbox" ? checked : value,
      };

      if (name === "detail") {
        next.latitude = null;
        next.longitude = null;
      }

      return next;
    });
  };

  const handleLocationComplete = (location) => {
    setForm((previous) => ({
      ...previous,

      province: location.province ?? "",

      provinceCode: location.provinceCode ?? null,

      ward: location.ward ?? "",

      wardCode: location.wardCode ?? null,

      latitude: null,
      longitude: null,
    }));

    setShowLocationModal(false);
    setError("");
  };

  const handleMapConfirm = ({ latitude, longitude }) => {
    setForm((previous) => ({
      ...previous,
      latitude,
      longitude,
    }));

    setShowMapModal(false);
    setError("");
  };

  const validateForm = () => {
    if (!form.receiverName.trim()) {
      return "Vui lòng nhập tên người nhận.";
    }

    if (!form.phone.trim()) {
      return "Vui lòng nhập số điện thoại.";
    }

    if (!/^0\d{9}$/.test(form.phone.trim())) {
      return "Số điện thoại phải gồm " + "10 chữ số và bắt đầu bằng 0.";
    }

    if (!form.province.trim()) {
      return "Vui lòng chọn Tỉnh/Thành phố.";
    }

    if (!form.ward.trim()) {
      return "Vui lòng chọn Phường/Xã.";
    }

    if (!form.detail.trim()) {
      return "Vui lòng nhập địa chỉ chi tiết.";
    }

    return "";
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    const validationError = validateForm();

    if (validationError) {
      setError(validationError);
      return;
    }

    try {
      setSaving(true);
      setError("");

      const payload = {
        receiverName: form.receiverName.trim(),

        phone: form.phone.trim(),

        province: form.province.trim(),

        ward: form.ward.trim(),

        detail: form.detail.trim(),

        latitude: form.latitude,

        longitude: form.longitude,

        defaultAddress: form.defaultAddress,
      };

      const response = isEditing
        ? await addressApi.update(address.id, payload)
        : await addressApi.create(payload);

      const apiResponse = response.data;

      if (!apiResponse?.success || !apiResponse?.data) {
        throw new Error(
          apiResponse?.message ?? "Lưu địa chỉ không thành công.",
        );
      }

      onSaved?.(apiResponse.data);

      setError("");
    } catch (requestError) {
      console.error("Unable to save address:", requestError);

      setError(
        requestError.response?.data?.message ??
          requestError.message ??
          "Không thể lưu địa chỉ. Vui lòng thử lại.",
      );
    } finally {
      setSaving(false);
    }
  };

  const handleOverlayClick = (event) => {
    if (
      event.target === event.currentTarget &&
      !saving &&
      !showLocationModal &&
      !showMapModal
    ) {
      onClose?.();
    }
  };

  const handleSelectDetailSuggestion = (suggestion) => {
    setForm((previous) => ({
      ...previous,

      detail: suggestion.detail || previous.detail,

      latitude: suggestion.latitude,

      longitude: suggestion.longitude,
    }));

    setDetailSuggestions([]);
    setDetailSearchError("");
  };

  return (
    <>
      <div
        className="add-address-modal"
        role="presentation"
        onMouseDown={handleOverlayClick}
      >
        <div
          className="add-address-modal__dialog"
          role="dialog"
          aria-modal="true"
          aria-labelledby="address-form-title"
        >
          <header className="add-address-modal__header">
            <button
              type="button"
              className="add-address-modal__back"
              onClick={onClose}
              disabled={saving}
              aria-label="Quay lại"
            >
              ←
            </button>

            <h2 id="address-form-title">
              {isEditing ? "Cập nhật địa chỉ" : "Thêm địa chỉ mới"}
            </h2>

            <button
              type="button"
              className="add-address-modal__close"
              onClick={onClose}
              disabled={saving}
              aria-label="Đóng"
            >
              ×
            </button>
          </header>

          <form className="add-address-modal__form" onSubmit={handleSubmit}>
            <div className="add-address-modal__body">
              <div className="add-address-modal__row">
                <label htmlFor="receiverName">Tên người nhận</label>

                <input
                  id="receiverName"
                  name="receiverName"
                  type="text"
                  value={form.receiverName}
                  onChange={handleChange}
                  placeholder="Nhập tên người nhận"
                  disabled={saving}
                  autoComplete="name"
                />
              </div>

              <div className="add-address-modal__row">
                <label htmlFor="phone">Số điện thoại</label>

                <input
                  id="phone"
                  name="phone"
                  type="tel"
                  value={form.phone}
                  onChange={handleChange}
                  placeholder="Nhập số điện thoại"
                  maxLength={10}
                  disabled={saving}
                  autoComplete="tel"
                />
              </div>

              <div className="add-address-modal__row">
                <label>Tỉnh/Thành phố, Phường/Xã</label>

                <button
                  type="button"
                  className={`add-address-modal__location ${
                    form.province && form.ward
                      ? "add-address-modal__location--selected"
                      : ""
                  }`}
                  onClick={() => setShowLocationModal(true)}
                  disabled={saving}
                >
                  <span className="add-address-modal__location-text">
                    {form.province && form.ward ? (
                      <>
                        <strong>{form.province}</strong>

                        <span>{form.ward}</span>
                      </>
                    ) : (
                      <span className="add-address-modal__location-placeholder">
                        Chọn Tỉnh/Thành phố, Phường/Xã
                      </span>
                    )}
                  </span>

                  <span className="add-address-modal__location-arrow">›</span>
                </button>
              </div>

              <div className="add-address-modal__row">
                <label htmlFor="detail">Địa chỉ chi tiết</label>

                <textarea
                  id="detail"
                  name="detail"
                  value={form.detail}
                  onChange={handleChange}
                  placeholder="Số nhà, tên đường, tòa nhà..."
                  rows={3}
                  disabled={saving}
                  autoComplete="street-address"
                />

                {form.detail.trim() &&
                  form.province &&
                  form.ward &&
                  form.latitude == null &&
                  form.longitude == null && (
                    <div className="add-address-modal__suggestions">
                      <div className="add-address-modal__suggestions-title">
                        Các địa điểm được đề xuất dựa trên khu vực đã chọn
                      </div>

                      {detailSearching ? (
                        <div className="add-address-modal__suggestions-status">
                          <LoadingSpinner size="small" inline />
                        </div>
                      ) : detailSearchError ? (
                        <div className="add-address-modal__suggestions-error">
                          {detailSearchError}
                        </div>
                      ) : detailSuggestions.length > 0 ? (
                        <div className="add-address-modal__suggestions-list">
                          {detailSuggestions.map((suggestion) => (
                            <button
                              type="button"
                              key={`${suggestion.latitude}-${suggestion.longitude}`}
                              className="add-address-modal__suggestion"
                              onClick={() =>
                                handleSelectDetailSuggestion(suggestion)
                              }
                            >
                              <span className="add-address-modal__suggestion-content">
                                <strong>{suggestion.detail}</strong>

                                <small>{suggestion.label}</small>
                              </span>
                            </button>
                          ))}
                        </div>
                      ) : (
                        <div className="add-address-modal__suggestions-status">
                          Chưa tìm thấy địa chỉ phù hợp trong khu vực này.
                        </div>
                      )}
                    </div>
                  )}

                <button
                  type="button"
                  className="add-address-modal__map-button"
                  onClick={() => setShowMapModal(true)}
                  disabled={
                    saving ||
                    !form.province ||
                    !form.ward ||
                    !form.detail.trim()
                  }
                >
                  <span className="add-address-modal__map-content">
                    <strong>
                      {form.latitude != null && form.longitude != null
                        ? "Thay đổi vị trí trên bản đồ"
                        : "Chọn vị trí chính xác trên bản đồ"}
                    </strong>

                    <small>
                      {form.latitude != null && form.longitude != null
                        ? "Vị trí đã được xác định"
                        : "Chọn tỉnh/phường và nhập địa chỉ trước"}
                    </small>
                  </span>

                  <span className="add-address-modal__map-arrow">›</span>
                </button>
              </div>

              {/* Chuẩn bị cho bước Map */}
              {form.latitude != null && form.longitude != null && (
                <div className="add-address-modal__coordinates">
                  Đã lưu vị trí: {Number(form.latitude).toFixed(6)},{" "}
                  {Number(form.longitude).toFixed(6)}
                </div>
              )}

              <label className="add-address-modal__default">
                <input
                  type="checkbox"
                  name="defaultAddress"
                  checked={form.defaultAddress}
                  onChange={handleChange}
                  disabled={saving}
                />

                <span>Đặt làm địa chỉ mặc định</span>
              </label>

              {error && (
                <div className="add-address-modal__error" role="alert">
                  {error}
                </div>
              )}
            </div>

            <footer className="add-address-modal__footer">
              <button
                type="button"
                className="add-address-modal__cancel"
                onClick={onClose}
                disabled={saving}
              >
                Hủy
              </button>

              <button
                type="submit"
                className="add-address-modal__submit"
                disabled={saving}
              >
                {saving ? (
                  <LoadingSpinner size="small" inline />
                ) : isEditing ? (
                  "Cập nhật"
                ) : (
                  "Lưu địa chỉ"
                )}
              </button>
            </footer>
          </form>
        </div>
      </div>

      <AddressLocationModal
        open={showLocationModal}
        initialProvince={form.province}
        initialWard={form.ward}
        onComplete={handleLocationComplete}
        onClose={() => setShowLocationModal(false)}
      />

      <AddressMapModal
        open={showMapModal}
        detail={form.detail}
        ward={form.ward}
        province={form.province}
        latitude={form.latitude}
        longitude={form.longitude}
        onConfirm={handleMapConfirm}
        onClose={() => setShowMapModal(false)}
      />
    </>
  );
}

export default AddressFormModal;
