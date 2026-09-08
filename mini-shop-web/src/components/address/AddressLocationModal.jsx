import { useEffect, useMemo, useState } from "react";

import LoadingSpinner from "../common/LoadingSpinner";

import "./AddressLocationModal.css";

const PROVINCE_API = "https://provinces.open-api.vn/api/v2/p/";

function AddressLocationModal({
  open,
  initialProvince = "",
  initialWard = "",
  onComplete,
  onClose,
}) {
  const [step, setStep] = useState("province");

  const [provinces, setProvinces] = useState([]);
  const [wards, setWards] = useState([]);

  const [selectedProvince, setSelectedProvince] = useState(null);
  const [selectedWard, setSelectedWard] = useState(null);

  const [searchText, setSearchText] = useState("");

  const [loadingProvinces, setLoadingProvinces] = useState(false);
  const [loadingWards, setLoadingWards] = useState(false);

  const [error, setError] = useState("");

  /*
   * Khi mở modal:
   * - reset search
   * - reset lỗi
   * - nếu chưa có province thì bắt đầu từ province
   */
  useEffect(() => {
    if (!open) {
      return;
    }

    setSearchText("");
    setError("");
    setStep("province");

    setSelectedProvince(null);
    setSelectedWard(null);
  }, [open]);

  /*
   * Load danh sách tỉnh/thành phố
   */
  useEffect(() => {
    if (!open) {
      return;
    }

    let cancelled = false;

    const loadProvinces = async () => {
      try {
        setLoadingProvinces(true);
        setError("");

        const response = await fetch(PROVINCE_API);

        if (!response.ok) {
          throw new Error("Không thể tải danh sách tỉnh/thành phố.");
        }

        const data = await response.json();

        if (!cancelled) {
          setProvinces(Array.isArray(data) ? data : []);
        }
      } catch (requestError) {
        console.error("Unable to load provinces:", requestError);

        if (!cancelled) {
          setError(
            requestError.message ?? "Không thể tải danh sách tỉnh/thành phố.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoadingProvinces(false);
        }
      }
    };

    loadProvinces();

    return () => {
      cancelled = true;
    };
  }, [open]);

  /*
   * Tìm kiếm tỉnh/thành phố
   */
  const filteredProvinces = useMemo(() => {
    const keyword = searchText.trim().toLowerCase();

    if (!keyword) {
      return provinces;
    }

    return provinces.filter((province) =>
      province.name.toLowerCase().includes(keyword),
    );
  }, [provinces, searchText]);

  /*
   * Tìm kiếm phường/xã
   */
  const filteredWards = useMemo(() => {
    const keyword = searchText.trim().toLowerCase();

    if (!keyword) {
      return wards;
    }

    return wards.filter((ward) => ward.name.toLowerCase().includes(keyword));
  }, [wards, searchText]);

  /*
   * Chọn tỉnh/thành phố
   */
  const handleSelectProvince = async (province) => {
    try {
      setSelectedProvince(province);
      setSelectedWard(null);

      setSearchText("");
      setError("");
      setLoadingWards(true);

      const response = await fetch(`${PROVINCE_API}${province.code}?depth=2`);

      if (!response.ok) {
        throw new Error("Không thể tải danh sách phường/xã.");
      }

      const data = await response.json();

      const wardList = Array.isArray(data?.wards) ? data.wards : [];

      setWards(wardList);

      setStep("ward");
    } catch (requestError) {
      console.error("Unable to load wards:", requestError);

      setError(requestError.message ?? "Không thể tải danh sách phường/xã.");
    } finally {
      setLoadingWards(false);
    }
  };

  /*
   * Chọn phường/xã
   */
  const handleSelectWard = (ward) => {
    setSelectedWard(ward);
  };

  /*
   * Quay lại bước tỉnh
   */
  const handleBack = () => {
    if (step === "ward") {
      setStep("province");
      setSearchText("");
      setSelectedWard(null);
      setError("");
      return;
    }

    onClose?.();
  };

  /*
   * Hoàn thành lựa chọn
   */
  const handleComplete = () => {
    if (!selectedProvince) {
      setError("Vui lòng chọn tỉnh/thành phố.");
      return;
    }

    if (!selectedWard) {
      setError("Vui lòng chọn phường/xã.");
      return;
    }

    onComplete?.({
      province: selectedProvince.name,
      provinceCode: selectedProvince.code,
      ward: selectedWard.name,
      wardCode: selectedWard.code,
    });
  };

  /*
   * Reset lựa chọn
   */
  const handleReset = () => {
    setStep("province");

    setSelectedProvince(null);
    setSelectedWard(null);

    setSearchText("");
    setError("");
  };

  if (!open) {
    return null;
  }

  const isProvinceStep = step === "province";

  return (
    <div className="address-location-modal">
      <div className="address-location-modal__overlay" onClick={onClose} />

      <div
        className="address-location-modal__content"
        role="dialog"
        aria-modal="true"
        aria-labelledby="address-location-title"
      >
        {/* HEADER */}
        <header className="address-location-modal__header">
          <button
            type="button"
            className="address-location-modal__back"
            onClick={handleBack}
            aria-label="Quay lại"
          >
            ←
          </button>

          <h2 id="address-location-title">
            {isProvinceStep ? "Tỉnh/Thành phố" : "Phường/Xã"}
          </h2>

          <button
            type="button"
            className="address-location-modal__close"
            onClick={onClose}
            aria-label="Đóng"
          >
            ×
          </button>
        </header>

        {/* SEARCH */}
        <div className="address-location-modal__search">
          <span className="address-location-modal__search-icon">🔍</span>

          <input
            type="text"
            value={searchText}
            onChange={(event) => setSearchText(event.target.value)}
            placeholder={
              isProvinceStep ? "Tìm tỉnh/thành phố" : "Tìm phường/xã"
            }
            autoComplete="off"
          />

          {searchText && (
            <button
              type="button"
              className="address-location-modal__search-clear"
              onClick={() => setSearchText("")}
              aria-label="Xóa tìm kiếm"
            >
              ×
            </button>
          )}
        </div>

        {/* SELECTED LOCATION */}
        {(selectedProvince || selectedWard) && (
          <div className="address-location-modal__selected">
            {selectedProvince && (
              <div>
                <span>Tỉnh/Thành phố</span>
                <strong>{selectedProvince.name}</strong>
              </div>
            )}

            {selectedWard && (
              <div>
                <span>Phường/Xã</span>
                <strong>{selectedWard.name}</strong>
              </div>
            )}
          </div>
        )}

        {/* ERROR */}
        {error && (
          <div className="address-location-modal__error" role="alert">
            {error}
          </div>
        )}

        {/* LIST */}
        <div className="address-location-modal__body">
          {isProvinceStep ? (
            <>
              {loadingProvinces ? (
                <div className="address-location-modal__loading">
                  <LoadingSpinner size="medium" />
                </div>
              ) : filteredProvinces.length === 0 ? (
                <div className="address-location-modal__empty">
                  Không tìm thấy tỉnh/thành phố.
                </div>
              ) : (
                <div className="address-location-modal__list">
                  {filteredProvinces.map((province) => {
                    const selected = selectedProvince?.code === province.code;

                    return (
                      <button
                        type="button"
                        key={province.code}
                        className={`address-location-item ${
                          selected ? "address-location-item--selected" : ""
                        }`}
                        onClick={() => handleSelectProvince(province)}
                      >
                        <span>{province.name}</span>

                        {selected && (
                          <span className="address-location-item__check">
                            ✓
                          </span>
                        )}
                      </button>
                    );
                  })}
                </div>
              )}
            </>
          ) : (
            <>
              {loadingWards ? (
                <div className="address-location-modal__loading">
                  <LoadingSpinner size="medium" />
                </div>
              ) : filteredWards.length === 0 ? (
                <div className="address-location-modal__empty">
                  Không tìm thấy phường/xã.
                </div>
              ) : (
                <div className="address-location-modal__list">
                  {filteredWards.map((ward) => {
                    const selected = selectedWard?.code === ward.code;

                    return (
                      <button
                        type="button"
                        key={ward.code}
                        className={`address-location-item ${
                          selected ? "address-location-item--selected" : ""
                        }`}
                        onClick={() => handleSelectWard(ward)}
                      >
                        <span>{ward.name}</span>

                        {selected && (
                          <span className="address-location-item__check">
                            ✓
                          </span>
                        )}
                      </button>
                    );
                  })}
                </div>
              )}
            </>
          )}
        </div>

        {/* FOOTER */}
        <footer className="address-location-modal__footer">
          <button
            type="button"
            className="address-location-modal__reset"
            onClick={handleReset}
          >
            Thiết lập lại
          </button>

          <button
            type="button"
            className="address-location-modal__complete"
            onClick={handleComplete}
            disabled={!selectedProvince || !selectedWard}
          >
            Hoàn thành
          </button>
        </footer>
      </div>
    </div>
  );
}

export default AddressLocationModal;
