import { useEffect, useState } from "react";

import sellerVoucherApi from "../../../api/sellerVoucherApi";
import LoadingSpinner from "../../../components/common/LoadingSpinner";

import "./SellerVoucherPage.css";

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

function SellerVoucherPage() {
  const [vouchers, setVouchers] = useState([]);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);
  const [createOpen, setCreateOpen] = useState(false);
  const [editingVoucher, setEditingVoucher] = useState(null);
  const [deactivateVoucher, setDeactivateVoucher] = useState(null);
  const [deactivating, setDeactivating] = useState(false);
  const [deactivateError, setDeactivateError] = useState("");
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState("");
  const [createForm, setCreateForm] = useState({
    code: "",
    discountType: "PERCENTAGE",
    discountValue: "",
    minOrderAmount: "",
    maxDiscountAmount: "",
    startDate: "",
    endDate: "",
    quantity: "",
  });

  const PAGE_SIZE = 20;

  useEffect(() => {
    let cancelled = false;

    const loadVouchers = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await sellerVoucherApi.getAll({
          page,
          size: PAGE_SIZE,
        });

        const apiResponse = response.data;

        if (!apiResponse?.success) {
          throw new Error(
            apiResponse?.message ?? "Không thể tải danh sách voucher.",
          );
        }

        const pageData = apiResponse.data;

        if (!pageData) {
          throw new Error("Dữ liệu danh sách voucher không hợp lệ.");
        }

        if (!cancelled) {
          setVouchers(Array.isArray(pageData.content) ? pageData.content : []);

          setTotalPages(Number(pageData.totalPages ?? 0));

          setTotalElements(Number(pageData.totalElements ?? 0));
        }
      } catch (err) {
        console.error("Unable to load seller vouchers:", err);

        if (!cancelled) {
          setVouchers([]);
          setTotalPages(0);
          setTotalElements(0);

          setError(
            err.response?.data?.message ??
              err.message ??
              "Không thể tải danh sách voucher.",
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
  }, [page, reloadKey]);

  const handlePreviousPage = () => {
    if (page <= 0) {
      return;
    }

    setPage((current) => current - 1);
  };

  const handleNextPage = () => {
    if (page >= totalPages - 1) {
      return;
    }

    setPage((current) => current + 1);
  };

  const resetCreateForm = () => {
    setCreateForm({
      code: "",
      discountType: "PERCENTAGE",
      discountValue: "",
      minOrderAmount: "",
      maxDiscountAmount: "",
      startDate: "",
      endDate: "",
      quantity: "",
    });

    setCreateError("");
  };

  const handleOpenCreate = () => {
    setEditingVoucher(null);
    resetCreateForm();
    setCreateOpen(true);
  };

  const handleOpenEdit = (voucher) => {
    setEditingVoucher(voucher);
    setCreateForm({
      code: voucher.code ?? "",
      discountType: voucher.discountType ?? "PERCENTAGE",
      discountValue: voucher.discountValue ?? "",
      minOrderAmount: voucher.minOrderAmount ?? "",
      maxDiscountAmount: voucher.maxDiscountAmount ?? "",
      startDate: toDateTimeLocalValue(voucher.startDate),
      endDate: toDateTimeLocalValue(voucher.endDate),
      quantity: voucher.quantity ?? "",
    });

    setCreateError("");
    setCreateOpen(true);
  };

  const handleCloseCreate = () => {
    if (creating) {
      return;
    }

    setCreateOpen(false);
    setEditingVoucher(null);
    resetCreateForm();
  };

  const handleCreateChange = (event) => {
    const { name, value } = event.target;

    setCreateForm((current) => {
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

  const validateCreateForm = () => {
    if (!createForm.code.trim()) {
      return "Vui lòng nhập mã Voucher.";
    }

    if (Number(createForm.discountValue) <= 0) {
      return "Giá trị giảm phải lớn hơn 0.";
    }

    if (
      createForm.discountType === "PERCENTAGE" &&
      Number(createForm.discountValue) > 100
    ) {
      return "Phần trăm giảm không được vượt quá 100%.";
    }

    if (
      createForm.minOrderAmount !== "" &&
      Number(createForm.minOrderAmount) < 0
    ) {
      return "Đơn tối thiểu không hợp lệ.";
    }

    if (
      createForm.maxDiscountAmount !== "" &&
      Number(createForm.maxDiscountAmount) < 0
    ) {
      return "Mức giảm tối đa không hợp lệ.";
    }

    if (!createForm.startDate) {
      return "Vui lòng chọn ngày bắt đầu.";
    }

    if (!createForm.endDate) {
      return "Vui lòng chọn ngày kết thúc.";
    }

    if (new Date(createForm.endDate) <= new Date(createForm.startDate)) {
      return "Ngày kết thúc phải sau ngày bắt đầu.";
    }

    if (
      !Number.isInteger(Number(createForm.quantity)) ||
      Number(createForm.quantity) <= 0
    ) {
      return "Số lượng phải lớn hơn 0.";
    }

    return "";
  };

  const normalizeDateTime = (value) => {
    if (!value) {
      return value;
    }

    return value.length === 16 ? `${value}:00` : value;
  };

  const toDateTimeLocalValue = (value) => {
    if (!value) {
      return "";
    }

    return String(value).slice(0, 16);
  };

  const handleCreateSubmit = async (event) => {
    event.preventDefault();

    const validationMessage = validateCreateForm();

    if (validationMessage) {
      setCreateError(validationMessage);
      return;
    }

    try {
      setCreating(true);
      setCreateError("");

      const payload = {
        code: createForm.code.trim(),
        discountType: createForm.discountType,

        discountValue: Number(createForm.discountValue),

        minOrderAmount:
          createForm.minOrderAmount === ""
            ? 0
            : Number(createForm.minOrderAmount),

        maxDiscountAmount:
          createForm.discountType === "PERCENTAGE" &&
          createForm.maxDiscountAmount !== ""
            ? Number(createForm.maxDiscountAmount)
            : null,

        startDate: normalizeDateTime(createForm.startDate),

        endDate: normalizeDateTime(createForm.endDate),

        quantity: Number(createForm.quantity),
      };

      const response = editingVoucher
        ? await sellerVoucherApi.update(editingVoucher.id, payload)
        : await sellerVoucherApi.create(payload);

      const apiResponse = response.data;

      if (!apiResponse?.success) {
        throw new Error(apiResponse?.message ?? "Không thể tạo Voucher.");
      }

      setCreateOpen(false);
      setEditingVoucher(null);
      resetCreateForm();

      setPage(0);
      setReloadKey((current) => current + 1);
    } catch (err) {
      console.error("Unable to create seller voucher:", err);

      setCreateError(
        err.response?.data?.message ?? err.message ?? "Không thể tạo Voucher.",
      );
    } finally {
      setCreating(false);
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

      const response = await sellerVoucherApi.deactivate(deactivateVoucher.id);

      const apiResponse = response.data;

      if (!apiResponse?.success) {
        throw new Error(apiResponse?.message ?? "Không thể tắt Voucher.");
      }

      setDeactivateVoucher(null);

      setReloadKey((current) => current + 1);
    } catch (err) {
      console.error("Unable to deactivate seller voucher:", err);

      setDeactivateError(
        err.response?.data?.message ?? err.message ?? "Không thể tắt Voucher.",
      );
    } finally {
      setDeactivating(false);
    }
  };

  return (
    <main className="seller-voucher-page">
      <div className="seller-voucher__container">
        <header className="seller-voucher__header">
          <div className="seller-voucher__header-content">
            <h1>Quản lý Voucher</h1>

            <p>Quản lý các chương trình giảm giá thuộc shop của bạn.</p>
          </div>

          <button
            type="button"
            className="seller-voucher__create-button"
            onClick={handleOpenCreate}
          >
            + Tạo Voucher
          </button>
        </header>

        {error && (
          <section className="seller-voucher__status-box seller-voucher__status-box--error">
            <h2>Không thể tải Voucher</h2>

            <p>{error}</p>

            <button
              type="button"
              className="seller-voucher__retry-button"
              onClick={() => setReloadKey((current) => current + 1)}
            >
              Thử lại
            </button>
          </section>
        )}

        {loading && (
          <section className="seller-voucher__status-box">
            <LoadingSpinner size="large" />
          </section>
        )}

        {!loading && !error && vouchers.length === 0 && (
          <section className="seller-voucher__status-box">
            <div className="seller-voucher__empty-icon">🎟️</div>

            <h2>Chưa có Voucher</h2>

            <p>Shop của bạn chưa tạo chương trình Voucher nào.</p>
          </section>
        )}

        {!loading && !error && vouchers.length > 0 && (
          <>
            <div className="seller-voucher__summary">
              <span>Danh sách Voucher</span>

              <strong>{totalElements} voucher</strong>
            </div>

            <section className="seller-voucher__list">
              {vouchers.map((voucher) => (
                <article key={voucher.id} className="seller-voucher-item">
                  <div className="seller-voucher-item__content">
                    <div className="seller-voucher-item__top">
                      <div className="seller-voucher-item__code">
                        <span>Mã Voucher</span>

                        <strong>{voucher.code}</strong>
                      </div>

                      <span
                        className={
                          voucher.active
                            ? "seller-voucher__badge seller-voucher__badge--active"
                            : "seller-voucher__badge seller-voucher__badge--inactive"
                        }
                      >
                        {voucher.active ? "Đang hoạt động" : "Đã tắt"}
                      </span>
                    </div>

                    <div className="seller-voucher-item__main">
                      <div className="seller-voucher-item__discount">
                        <strong>{getDiscountText(voucher)}</strong>

                        {voucher.discountType === "PERCENTAGE" &&
                          voucher.maxDiscountAmount != null && (
                            <span>
                              Giảm tối đa{" "}
                              {formatCurrency(voucher.maxDiscountAmount)}
                            </span>
                          )}
                      </div>

                      <div className="seller-voucher-item__details">
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

                          <strong>{voucher.scopeName ?? "Theo shop"}</strong>
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

                  <div className="seller-voucher-item__actions">
                    <button
                      type="button"
                      className="seller-voucher-item__edit-button"
                      onClick={() => handleOpenEdit(voucher)}
                    >
                      Sửa
                    </button>

                    <button
                      type="button"
                      className="seller-voucher-item__deactivate-button"
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

        {!loading && !error && totalPages > 0 && (
          <nav
            className="seller-voucher__pagination"
            aria-label="Phân trang Voucher"
          >
            <button
              type="button"
              disabled={page <= 0}
              onClick={handlePreviousPage}
            >
              ← Trước
            </button>

            <span>
              Trang <strong>{page + 1}</strong>
              {" / "}
              <strong>{totalPages}</strong>
            </span>

            <button
              type="button"
              disabled={page >= totalPages - 1}
              onClick={handleNextPage}
            >
              Sau →
            </button>
          </nav>
        )}
      </div>

      {createOpen && (
        <div
          className="seller-voucher-modal"
          role="presentation"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) {
              handleCloseCreate();
            }
          }}
        >
          <div
            className="seller-voucher-modal__dialog"
            role="dialog"
            aria-modal="true"
            aria-labelledby="seller-voucher-create-title"
          >
            <div className="seller-voucher-modal__header">
              <div>
                <h2 id="seller-voucher-create-title">
                  {editingVoucher ? "Sửa Voucher" : "Tạo Voucher"}
                </h2>

                <p>
                  {editingVoucher
                    ? "Cập nhật thông tin Voucher của shop."
                    : "Tạo chương trình giảm giá mới cho shop của bạn."}
                </p>
              </div>

              <button
                type="button"
                className="seller-voucher-modal__close"
                onClick={handleCloseCreate}
                disabled={creating}
                aria-label="Đóng"
              >
                ×
              </button>
            </div>

            <form className="seller-voucher-form" onSubmit={handleCreateSubmit}>
              <div className="seller-voucher-form__body">
                {createError && (
                  <div className="seller-voucher-form__error">
                    {createError}
                  </div>
                )}

                <label className="seller-voucher-form__field seller-voucher-form__field--full">
                  <span>Mã Voucher</span>

                  <input
                    type="text"
                    name="code"
                    value={createForm.code}
                    onChange={handleCreateChange}
                    placeholder="Ví dụ: SALE20"
                    disabled={creating}
                  />
                </label>

                <label className="seller-voucher-form__field">
                  <span>Loại giảm giá</span>

                  <select
                    name="discountType"
                    value={createForm.discountType}
                    onChange={handleCreateChange}
                    disabled={creating}
                  >
                    <option value="PERCENTAGE">Phần trăm</option>

                    <option value="FIXED_AMOUNT">Số tiền cố định</option>
                  </select>
                </label>

                <label className="seller-voucher-form__field">
                  <span>
                    {createForm.discountType === "PERCENTAGE"
                      ? "Phần trăm giảm"
                      : "Số tiền giảm"}
                  </span>

                  <input
                    type="number"
                    name="discountValue"
                    min="0"
                    step={
                      createForm.discountType === "PERCENTAGE" ? "0.01" : "1000"
                    }
                    value={createForm.discountValue}
                    onChange={handleCreateChange}
                    disabled={creating}
                  />
                </label>

                <label className="seller-voucher-form__field">
                  <span>Đơn tối thiểu</span>

                  <input
                    type="number"
                    name="minOrderAmount"
                    min="0"
                    step="1000"
                    value={createForm.minOrderAmount}
                    onChange={handleCreateChange}
                    placeholder="0"
                    disabled={creating}
                  />
                </label>

                <label className="seller-voucher-form__field">
                  <span>Giảm tối đa</span>

                  <input
                    type="number"
                    name="maxDiscountAmount"
                    min="0"
                    step="1000"
                    value={createForm.maxDiscountAmount}
                    onChange={handleCreateChange}
                    placeholder={
                      createForm.discountType === "PERCENTAGE"
                        ? "Ví dụ: 50000"
                        : "Không áp dụng"
                    }
                    disabled={
                      creating || createForm.discountType === "FIXED_AMOUNT"
                    }
                  />
                </label>

                <label className="seller-voucher-form__field">
                  <span>Ngày bắt đầu</span>

                  <input
                    type="datetime-local"
                    name="startDate"
                    value={createForm.startDate}
                    onChange={handleCreateChange}
                    disabled={creating}
                  />
                </label>

                <label className="seller-voucher-form__field">
                  <span>Ngày kết thúc</span>

                  <input
                    type="datetime-local"
                    name="endDate"
                    value={createForm.endDate}
                    onChange={handleCreateChange}
                    disabled={creating}
                  />
                </label>

                <label className="seller-voucher-form__field seller-voucher-form__field--full">
                  <span>Số lượng có thể nhận</span>

                  <input
                    type="number"
                    name="quantity"
                    min="1"
                    step="1"
                    value={createForm.quantity}
                    onChange={handleCreateChange}
                    placeholder="Ví dụ: 100"
                    disabled={creating}
                  />

                  <small>
                    Đây là số lượt Voucher Customer có thể nhận từ kho chung.
                  </small>
                </label>
              </div>

              <div className="seller-voucher-form__footer">
                <button
                  type="button"
                  className="seller-voucher-form__cancel"
                  onClick={handleCloseCreate}
                  disabled={creating}
                >
                  Hủy
                </button>

                <button
                  type="submit"
                  className="seller-voucher-form__submit"
                  disabled={creating}
                >
                  {creating ? (
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
          className="seller-voucher-modal"
          role="presentation"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) {
              handleCloseDeactivate();
            }
          }}
        >
          <div
            className="seller-voucher-modal__dialog seller-voucher-deactivate-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="seller-voucher-deactivate-title"
          >
            <div className="seller-voucher-modal__header">
              <div>
                <h2 id="seller-voucher-deactivate-title">Tắt Voucher</h2>

                <p>Voucher sau khi tắt sẽ không còn được sử dụng.</p>
              </div>

              <button
                type="button"
                className="seller-voucher-modal__close"
                onClick={handleCloseDeactivate}
                disabled={deactivating}
                aria-label="Đóng"
              >
                ×
              </button>
            </div>

            <div className="seller-voucher-deactivate__body">
              {deactivateError && (
                <div className="seller-voucher-form__error">
                  {deactivateError}
                </div>
              )}

              <p>
                Bạn có chắc muốn tắt Voucher{" "}
                <strong>{deactivateVoucher.code}</strong>?
              </p>

              <div className="seller-voucher-deactivate__warning">
                Sau khi tắt:
                <ul>
                  <li>Customer mới sẽ không thể nhận Voucher này.</li>

                  <li>
                    Voucher đã nhận sẽ chuyển sang trạng thái không thể sử dụng.
                  </li>

                  <li>Số lượng còn lại không bị xóa.</li>
                </ul>
              </div>
            </div>

            <div className="seller-voucher-form__footer">
              <button
                type="button"
                className="seller-voucher-form__cancel"
                onClick={handleCloseDeactivate}
                disabled={deactivating}
              >
                Hủy
              </button>

              <button
                type="button"
                className="seller-voucher-deactivate__confirm"
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

export default SellerVoucherPage;
