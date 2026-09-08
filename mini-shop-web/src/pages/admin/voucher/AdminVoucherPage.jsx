import { useEffect, useState } from "react";

import adminVoucherApi from "../../../api/adminVoucherApi";
import LoadingSpinner from "../../../components/common/LoadingSpinner";

import "./AdminVoucherPage.css";

function formatCurrency(value) {
  if (value == null) {
    return "—";
  }

  return `${Number(value).toLocaleString("vi-VN")} ₫`;
}

function formatDate(value) {
  if (!value) {
    return "—";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function getDiscountText(voucher) {
  if (voucher.discountType === "PERCENTAGE") {
    return `Giảm ${Number(voucher.discountValue ?? 0)}%`;
  }

  return `Giảm ${formatCurrency(voucher.discountValue)}`;
}

function AdminVoucherPage() {
  const [vouchers, setVouchers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);
  const [formOpen, setFormOpen] = useState(false);
  const [editingVoucher, setEditingVoucher] = useState(null);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState("");
  const [voucherForm, setVoucherForm] = useState({
    code: "",
    discountType: "PERCENTAGE",
    discountValue: "",
    minOrderAmount: "",
    maxDiscountAmount: "",
    startDate: "",
    endDate: "",
    quantity: "",
  });
  const [deactivateVoucher, setDeactivateVoucher] = useState(null);
  const [deactivating, setDeactivating] = useState(false);
  const [deactivateError, setDeactivateError] = useState("");

  useEffect(() => {
    let cancelled = false;

    const loadVouchers = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await adminVoucherApi.getAll();

        const apiResponse = response.data;

        if (!apiResponse?.success) {
          throw new Error(
            apiResponse?.message ?? "Không thể tải danh sách Voucher.",
          );
        }

        const data = apiResponse.data;

        if (!cancelled) {
          setVouchers(Array.isArray(data) ? data : []);
        }
      } catch (err) {
        console.error("Unable to load admin vouchers:", err);

        if (!cancelled) {
          setVouchers([]);

          setError(
            err.response?.data?.message ??
              err.message ??
              "Không thể tải danh sách Voucher.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    loadVouchers();

    return () => {
      cancelled = true;
    };
  }, [reloadKey]);

  const resetVoucherForm = () => {
    setVoucherForm({
      code: "",
      discountType: "PERCENTAGE",
      discountValue: "",
      minOrderAmount: "",
      maxDiscountAmount: "",
      startDate: "",
      endDate: "",
      quantity: "",
    });

    setFormError("");
  };

  const toDateTimeLocalValue = (value) => {
    if (!value) {
      return "";
    }

    return String(value).slice(0, 16);
  };

  const normalizeDateTime = (value) => {
    if (!value) {
      return value;
    }

    return value.length === 16 ? `${value}:00` : value;
  };

  const handleOpenCreate = () => {
    setEditingVoucher(null);

    resetVoucherForm();

    setFormOpen(true);
  };

  const handleOpenEdit = (voucher) => {
    setEditingVoucher(voucher);

    setVoucherForm({
      code: voucher.code ?? "",
      discountType: voucher.discountType ?? "PERCENTAGE",
      discountValue: voucher.discountValue ?? "",
      minOrderAmount: voucher.minOrderAmount ?? "",
      maxDiscountAmount: voucher.maxDiscountAmount ?? "",
      startDate: toDateTimeLocalValue(voucher.startDate),
      endDate: toDateTimeLocalValue(voucher.endDate),
      quantity: voucher.quantity ?? "",
    });

    setFormError("");
    setFormOpen(true);
  };

  const handleCloseForm = () => {
    if (saving) {
      return;
    }

    setFormOpen(false);

    setEditingVoucher(null);

    resetVoucherForm();
  };

  const handleFormChange = (event) => {
    const { name, value } = event.target;

    setVoucherForm((current) => {
      const nextForm = {
        ...current,
        [name]: value,
      };

      if (name === "discountType" && value === "FIXED_AMOUNT") {
        nextForm.maxDiscountAmount = "";
      }

      return nextForm;
    });
  };

  const validateVoucherForm = () => {
    if (!voucherForm.code.trim()) {
      return "Vui lòng nhập mã Voucher.";
    }

    if (Number(voucherForm.discountValue) <= 0) {
      return "Giá trị giảm phải lớn hơn 0.";
    }

    if (
      voucherForm.discountType === "PERCENTAGE" &&
      Number(voucherForm.discountValue) > 100
    ) {
      return "Phần trăm giảm không được vượt quá 100%.";
    }

    if (
      voucherForm.minOrderAmount !== "" &&
      Number(voucherForm.minOrderAmount) < 0
    ) {
      return "Đơn tối thiểu không hợp lệ.";
    }

    if (
      voucherForm.maxDiscountAmount !== "" &&
      Number(voucherForm.maxDiscountAmount) < 0
    ) {
      return "Mức giảm tối đa không hợp lệ.";
    }

    if (!voucherForm.startDate) {
      return "Vui lòng chọn ngày bắt đầu.";
    }

    if (!voucherForm.endDate) {
      return "Vui lòng chọn ngày kết thúc.";
    }

    if (new Date(voucherForm.endDate) <= new Date(voucherForm.startDate)) {
      return "Ngày kết thúc phải sau ngày bắt đầu.";
    }

    if (
      !Number.isInteger(Number(voucherForm.quantity)) ||
      Number(voucherForm.quantity) <= 0
    ) {
      return "Số lượng phải lớn hơn 0.";
    }

    return "";
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    const validationMessage = validateVoucherForm();

    if (validationMessage) {
      setFormError(validationMessage);
      return;
    }

    try {
      setSaving(true);
      setFormError("");

      const payload = {
        code: voucherForm.code.trim(),

        discountType: voucherForm.discountType,

        discountValue: Number(voucherForm.discountValue),

        minOrderAmount:
          voucherForm.minOrderAmount === ""
            ? 0
            : Number(voucherForm.minOrderAmount),

        maxDiscountAmount:
          voucherForm.discountType === "PERCENTAGE" &&
          voucherForm.maxDiscountAmount !== ""
            ? Number(voucherForm.maxDiscountAmount)
            : null,

        startDate: normalizeDateTime(voucherForm.startDate),

        endDate: normalizeDateTime(voucherForm.endDate),

        quantity: Number(voucherForm.quantity),
      };

      const response = editingVoucher
        ? await adminVoucherApi.update(editingVoucher.id, payload)
        : await adminVoucherApi.create(payload);

      const apiResponse = response.data;

      if (!apiResponse?.success) {
        throw new Error(apiResponse?.message ?? "Không thể lưu Voucher.");
      }

      setFormOpen(false);
      setEditingVoucher(null);

      resetVoucherForm();

      setReloadKey((current) => current + 1);
    } catch (err) {
      console.error("Unable to save admin voucher:", err);

      setFormError(
        err.response?.data?.message ?? err.message ?? "Không thể lưu Voucher.",
      );
    } finally {
      setSaving(false);
    }
  };

  const handleOpenDeactivate = (voucher) => {
    if (!voucher?.active) {
      return;
    }

    setDeactivateVoucher(voucher);
    setDeactivateError("");
  };

  const handleCloseDeactivate = () => {
    if (deactivating) {
      return;
    }

    setDeactivateVoucher(null);
    setDeactivateError("");
  };

  const handleDeactivate = async () => {
    if (!deactivateVoucher?.id) {
      return;
    }

    try {
      setDeactivating(true);
      setDeactivateError("");

      const response = await adminVoucherApi.deactivate(deactivateVoucher.id);

      const apiResponse = response.data;

      if (!apiResponse?.success) {
        throw new Error(apiResponse?.message ?? "Không thể tắt Voucher.");
      }

      setDeactivateVoucher(null);

      setReloadKey((current) => current + 1);
    } catch (err) {
      console.error("Unable to deactivate admin voucher:", err);

      setDeactivateError(
        err.response?.data?.message ?? err.message ?? "Không thể tắt Voucher.",
      );
    } finally {
      setDeactivating(false);
    }
  };

  return (
    <main className="admin-voucher-page">
      <div className="admin-voucher__container">
        <header className="admin-voucher__header">
          <div className="admin-voucher__header-content">
            <h1>Quản lý Voucher</h1>

            <p>Quản lý các chương trình Voucher toàn sàn của Hair.</p>
          </div>

          <button
            type="button"
            className="admin-voucher__create-button"
            onClick={handleOpenCreate}
          >
            + Tạo Voucher
          </button>
        </header>

        {error && (
          <section className="admin-voucher__status-box admin-voucher__status-box--error">
            <h2>Không thể tải Voucher</h2>

            <p>{error}</p>

            <button
              type="button"
              className="admin-voucher__retry-button"
              onClick={() => setReloadKey((current) => current + 1)}
            >
              Thử lại
            </button>
          </section>
        )}

        {loading && (
          <section className="admin-voucher__status-box">
            <div className="admin-voucher__spinner" />

            <LoadingSpinner size="medium" />
          </section>
        )}

        {!loading && !error && vouchers.length === 0 && (
          <section className="admin-voucher__status-box">
            <div className="admin-voucher__empty-icon">🎟️</div>

            <h2>Chưa có Voucher</h2>

            <p>Hair chưa có chương trình Voucher toàn sàn nào.</p>
          </section>
        )}

        {!loading && !error && vouchers.length > 0 && (
          <>
            <div className="admin-voucher__summary">
              <span>Voucher toàn sàn</span>

              <strong>{vouchers.length} voucher</strong>
            </div>

            <section className="admin-voucher__list">
              {vouchers.map((voucher) => (
                <article key={voucher.id} className="admin-voucher-item">
                  <div className="admin-voucher-item__content">
                    <div className="admin-voucher-item__top">
                      <div className="admin-voucher-item__code">
                        <span>Mã Voucher</span>

                        <strong>{voucher.code}</strong>
                      </div>

                      <span
                        className={
                          voucher.active
                            ? "admin-voucher__badge admin-voucher__badge--active"
                            : "admin-voucher__badge admin-voucher__badge--inactive"
                        }
                      >
                        {voucher.active ? "Đang hoạt động" : "Đã tắt"}
                      </span>
                    </div>

                    <div className="admin-voucher-item__main">
                      <div className="admin-voucher-item__discount">
                        <strong>{getDiscountText(voucher)}</strong>

                        {voucher.discountType === "PERCENTAGE" &&
                          voucher.maxDiscountAmount != null && (
                            <span>
                              Giảm tối đa{" "}
                              {formatCurrency(voucher.maxDiscountAmount)}
                            </span>
                          )}
                      </div>

                      <div className="admin-voucher-item__details">
                        <div>
                          <span>Đơn tối thiểu:</span>

                          <strong>
                            {formatCurrency(voucher.minOrderAmount ?? 0)}
                          </strong>
                        </div>

                        <div>
                          <span>Còn có thể nhận:</span>

                          <strong>{voucher.quantity ?? 0}</strong>
                        </div>

                        <div>
                          <span>Phạm vi:</span>

                          <strong>{voucher.scopeName ?? "Toàn sàn"}</strong>
                        </div>

                        <div>
                          <span>Bắt đầu:</span>

                          <strong>{formatDate(voucher.startDate)}</strong>
                        </div>

                        <div>
                          <span>Kết thúc:</span>

                          <strong>{formatDate(voucher.endDate)}</strong>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="admin-voucher-item__actions">
                    <button
                      type="button"
                      className="admin-voucher-item__edit-button"
                      onClick={() => handleOpenEdit(voucher)}
                    >
                      Sửa
                    </button>

                    <button
                      type="button"
                      className="admin-voucher-item__deactivate-button"
                      disabled={!voucher.active}
                      onClick={() => handleOpenDeactivate(voucher)}
                    >
                      {voucher.active ? "Tắt Voucher" : "Đã tắt"}
                    </button>
                  </div>
                </article>
              ))}
            </section>
          </>
        )}
      </div>

      {formOpen && (
        <div
          className="admin-voucher-modal"
          role="presentation"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) {
              handleCloseForm();
            }
          }}
        >
          <div
            className="admin-voucher-modal__dialog"
            role="dialog"
            aria-modal="true"
            aria-labelledby="admin-voucher-form-title"
          >
            <div className="admin-voucher-modal__header">
              <div>
                <h2 id="admin-voucher-form-title">
                  {editingVoucher ? "Sửa Voucher" : "Tạo Voucher"}
                </h2>

                <p>
                  {editingVoucher
                    ? "Cập nhật chương trình Voucher toàn sàn."
                    : "Tạo chương trình Voucher toàn sàn mới cho Hair."}
                </p>
              </div>

              <button
                type="button"
                className="admin-voucher-modal__close"
                onClick={handleCloseForm}
                disabled={saving}
                aria-label="Đóng"
              >
                ×
              </button>
            </div>

            <form className="admin-voucher-form" onSubmit={handleSubmit}>
              <div className="admin-voucher-form__body">
                {formError && (
                  <div className="admin-voucher-form__error">{formError}</div>
                )}

                <label className="admin-voucher-form__field admin-voucher-form__field--full">
                  <span>Mã Voucher</span>

                  <input
                    type="text"
                    name="code"
                    value={voucherForm.code}
                    onChange={handleFormChange}
                    placeholder="Ví dụ: HAIR22"
                    disabled={saving}
                  />
                </label>

                <label className="admin-voucher-form__field">
                  <span>Loại giảm giá</span>

                  <select
                    name="discountType"
                    value={voucherForm.discountType}
                    onChange={handleFormChange}
                    disabled={saving}
                  >
                    <option value="PERCENTAGE">Phần trăm</option>

                    <option value="FIXED_AMOUNT">Số tiền cố định</option>
                  </select>
                </label>

                <label className="admin-voucher-form__field">
                  <span>
                    {voucherForm.discountType === "PERCENTAGE"
                      ? "Phần trăm giảm"
                      : "Số tiền giảm"}
                  </span>

                  <input
                    type="number"
                    name="discountValue"
                    min="0"
                    step={
                      voucherForm.discountType === "PERCENTAGE"
                        ? "0.01"
                        : "1000"
                    }
                    value={voucherForm.discountValue}
                    onChange={handleFormChange}
                    disabled={saving}
                  />
                </label>

                <label className="admin-voucher-form__field">
                  <span>Đơn tối thiểu</span>

                  <input
                    type="number"
                    name="minOrderAmount"
                    min="0"
                    step="1000"
                    value={voucherForm.minOrderAmount}
                    onChange={handleFormChange}
                    placeholder="0"
                    disabled={saving}
                  />
                </label>

                <label className="admin-voucher-form__field">
                  <span>Giảm tối đa</span>

                  <input
                    type="number"
                    name="maxDiscountAmount"
                    min="0"
                    step="1000"
                    value={voucherForm.maxDiscountAmount}
                    onChange={handleFormChange}
                    placeholder={
                      voucherForm.discountType === "PERCENTAGE"
                        ? "Ví dụ: 50000"
                        : "Không áp dụng"
                    }
                    disabled={
                      saving || voucherForm.discountType === "FIXED_AMOUNT"
                    }
                  />
                </label>

                <label className="admin-voucher-form__field">
                  <span>Ngày bắt đầu</span>

                  <input
                    type="datetime-local"
                    name="startDate"
                    value={voucherForm.startDate}
                    onChange={handleFormChange}
                    disabled={saving}
                  />
                </label>

                <label className="admin-voucher-form__field">
                  <span>Ngày kết thúc</span>

                  <input
                    type="datetime-local"
                    name="endDate"
                    value={voucherForm.endDate}
                    onChange={handleFormChange}
                    disabled={saving}
                  />
                </label>

                <label className="admin-voucher-form__field admin-voucher-form__field--full">
                  <span>Số lượng có thể nhận</span>

                  <input
                    type="number"
                    name="quantity"
                    min="1"
                    step="1"
                    value={voucherForm.quantity}
                    onChange={handleFormChange}
                    placeholder="Ví dụ: 1000"
                    disabled={saving}
                  />

                  <small>
                    Đây là số Voucher toàn sàn Customer còn có thể nhận.
                  </small>
                </label>

                <div className="admin-voucher-form__scope">
                  <span>Phạm vi</span>

                  <strong>Toàn sàn Hair</strong>

                  <small>Voucher Admin tạo luôn có scope PLATFORM.</small>
                </div>
              </div>

              <div className="admin-voucher-form__footer">
                <button
                  type="button"
                  className="admin-voucher-form__cancel"
                  onClick={handleCloseForm}
                  disabled={saving}
                >
                  Hủy
                </button>

                <button
                  type="submit"
                  className="admin-voucher-form__submit"
                  disabled={saving}
                >
                  {saving ? (
                    <LoadingSpinner size="small" inline variant="light" />
                  ) : editingVoucher ? (
                    "Lưu thay đổi"
                  ) : (
                    "Tạo Voucher"
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {deactivateVoucher && (
        <div
          className="admin-voucher-modal"
          role="presentation"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) {
              handleCloseDeactivate();
            }
          }}
        >
          <div
            className="admin-voucher-modal__dialog admin-voucher-deactivate-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="admin-voucher-deactivate-title"
          >
            <div className="admin-voucher-modal__header">
              <div>
                <h2 id="admin-voucher-deactivate-title">Tắt Voucher</h2>

                <p>Xác nhận tắt chương trình Voucher toàn sàn.</p>
              </div>

              <button
                type="button"
                className="admin-voucher-modal__close"
                onClick={handleCloseDeactivate}
                disabled={deactivating}
                aria-label="Đóng"
              >
                ×
              </button>
            </div>

            <div className="admin-voucher-deactivate__body">
              {deactivateError && (
                <div className="admin-voucher-form__error">
                  {deactivateError}
                </div>
              )}

              <p>
                Bạn có chắc muốn tắt Voucher{" "}
                <strong>{deactivateVoucher.code}</strong>?
              </p>

              <div className="admin-voucher-deactivate__warning">
                Sau khi tắt:
                <ul>
                  <li>Voucher sẽ biến mất khỏi Kho Voucher Customer.</li>

                  <li>
                    Customer đã nhận Voucher sẽ không thể sử dụng Voucher này.
                  </li>

                  <li>Số lượng còn lại vẫn được giữ nguyên.</li>
                </ul>
              </div>
            </div>

            <div className="admin-voucher-form__footer">
              <button
                type="button"
                className="admin-voucher-form__cancel"
                onClick={handleCloseDeactivate}
                disabled={deactivating}
              >
                Hủy
              </button>

              <button
                type="button"
                className="admin-voucher-deactivate__confirm"
                onClick={handleDeactivate}
                disabled={deactivating}
              >
                {deactivating ? (
                  <LoadingSpinner size="small" inline variant="light" />
                ) : (
                  "Tắt Voucher"
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  );
}

export default AdminVoucherPage;
