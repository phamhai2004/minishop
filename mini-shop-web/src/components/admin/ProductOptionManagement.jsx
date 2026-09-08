import { useEffect, useState } from "react";

import productOptionApi from "../../api/productOptionApi";

import LoadingSpinner from "../common/LoadingSpinner";

import "././category/CategoryManagement.css";

function ProductOptionManagement() {
  const [optionTypes, setOptionTypes] = useState([]);
  const [valuesByType, setValuesByType] = useState({});

  const [showValueForm, setShowValueForm] = useState(false);
  const [valueType, setValueType] = useState(null);
  const [editingValueId, setEditingValueId] = useState(null);

  const [valueFormData, setValueFormData] = useState({
    name: "",
  });

  const [valueSaving, setValueSaving] = useState(false);
  const [valueFormError, setValueFormError] = useState("");

  const [deleteValueTarget, setDeleteValueTarget] = useState(null);
  const [valueDeleting, setValueDeleting] = useState(false);
  const [deleteValueError, setDeleteValueError] = useState("");

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);

  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState("");

  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState("");

  const [formData, setFormData] = useState({
    name: "",
    description: "",
  });

  const loadOptionTypes = async () => {
    try {
      setLoading(true);
      setError("");
      const response = await productOptionApi.getAllTypes();

      const types = response.data?.data ?? [];

      setOptionTypes(types);

      const valueEntries = await Promise.all(
        types.map(async (type) => {
          const valueResponse = await productOptionApi.getValuesByType(type.id);

          const values = valueResponse.data?.data ?? [];

          return [type.id, values];
        }),
      );
      const valueMap = Object.fromEntries(valueEntries);

      setValuesByType(valueMap);

      console.log("Option types:", types);

      console.log("Values by type:", valueMap);
    } catch (err) {
      console.error("Không thể tải phân loại:", err);

      setError(
        err.response?.data?.message || "Không thể tải danh sách phân loại.",
      );
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setFormData({
      name: "",
      description: "",
    });

    setEditingId(null);
    setFormError("");
  };

  const handleOpenCreate = () => {
    resetForm();
    setShowForm(true);
  };

  const handleCloseForm = () => {
    if (saving) {
      return;
    }

    setShowForm(false);
    resetForm();
  };

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const handleEdit = (optionType) => {
    setEditingId(optionType.id);

    setFormData({
      name: optionType.name ?? "",
      description: optionType.description ?? "",
    });

    setFormError("");
    setShowForm(true);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    const name = formData.name.trim();

    if (!name) {
      setFormError("Tên phân loại không được để trống.");

      return;
    }

    const payload = {
      name,
      description: formData.description.trim(),
    };

    try {
      setSaving(true);
      setFormError("");

      if (editingId != null) {
        await productOptionApi.updateType(editingId, payload);
      } else {
        await productOptionApi.createType(payload);
      }

      setShowForm(false);
      resetForm();

      await loadOptionTypes();
    } catch (err) {
      console.error("Không thể lưu phân loại:", err);

      setFormError(err.response?.data?.message || "Không thể lưu phân loại.");
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteClick = (optionType) => {
    setDeleteTarget(optionType);
    setDeleteError("");
  };

  const handleCloseDelete = () => {
    if (deleting) {
      return;
    }

    setDeleteTarget(null);
    setDeleteError("");
  };

  const resetValueForm = () => {
    setValueFormData({
      name: "",
    });

    setValueType(null);
    setEditingValueId(null);
    setValueFormError("");
  };

  const handleOpenCreateValue = (optionType) => {
    setValueType(optionType);
    setEditingValueId(null);

    setValueFormData({
      name: "",
    });

    setValueFormError("");
    setShowValueForm(true);
  };

  const handleEditValue = (optionType, value) => {
    setValueType(optionType);
    setEditingValueId(value.id);

    setValueFormData({
      name: value.name ?? "",
    });

    setValueFormError("");
    setShowValueForm(true);
  };

  const handleCloseValueForm = () => {
    if (valueSaving) {
      return;
    }

    setShowValueForm(false);
    resetValueForm();
  };

  const handleValueChange = (event) => {
    const { name, value } = event.target;

    setValueFormData((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const handleValueSubmit = async (event) => {
    event.preventDefault();

    if (!valueType) {
      return;
    }

    const name = valueFormData.name.trim();

    if (!name) {
      setValueFormError("Tên giá trị phân loại không được để trống.");

      return;
    }

    const payload = {
      name,
      optionTypeId: valueType.id,
    };

    try {
      setValueSaving(true);
      setValueFormError("");

      if (editingValueId != null) {
        await productOptionApi.updateValue(editingValueId, payload);
      } else {
        await productOptionApi.createValue(payload);
      }

      setShowValueForm(false);
      resetValueForm();

      await loadOptionTypes();
    } catch (err) {
      console.error("Không thể lưu giá trị phân loại:", err);

      setValueFormError(
        err.response?.data?.message || "Không thể lưu giá trị phân loại.",
      );
    } finally {
      setValueSaving(false);
    }
  };

  const handleDeleteValueClick = (optionType, value) => {
    setDeleteValueTarget({
      optionType,
      value,
    });

    setDeleteValueError("");
  };

  const handleCloseDeleteValue = () => {
    if (valueDeleting) {
      return;
    }

    setDeleteValueTarget(null);
    setDeleteValueError("");
  };

  const handleDeleteValueConfirm = async () => {
    if (!deleteValueTarget) {
      return;
    }

    try {
      setValueDeleting(true);
      setDeleteValueError("");

      await productOptionApi.deleteValue(deleteValueTarget.value.id);

      setDeleteValueTarget(null);

      await loadOptionTypes();
    } catch (err) {
      console.error("Không thể xóa giá trị phân loại:", err);

      setDeleteValueError(
        err.response?.data?.message || "Không thể xóa giá trị phân loại.",
      );
    } finally {
      setValueDeleting(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deleteTarget) {
      return;
    }

    try {
      setDeleting(true);
      setDeleteError("");

      await productOptionApi.deleteType(deleteTarget.id);

      setDeleteTarget(null);

      await loadOptionTypes();
    } catch (err) {
      console.error("Không thể xóa phân loại:", err);

      setDeleteError(err.response?.data?.message || "Không thể xóa phân loại.");
    } finally {
      setDeleting(false);
    }
  };

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void loadOptionTypes();
  }, []);

  if (loading) {
    return (
      <section className="category-management">
        <h2>Quản lý phân loại</h2>

        <LoadingSpinner size="large" />
      </section>
    );
  }

  return (
    <section className="category-management">
      <div className="category-management__header">
        <div className="category-management__toolbar">
          <h2>Quản lý phân loại</h2>

          <button
            type="button"
            className="category-management__add-button"
            onClick={handleOpenCreate}
          >
            + Thêm phân loại
          </button>
        </div>

        <p>Quản lý các loại phân loại dùng cho biến thể sản phẩm.</p>
      </div>

      {error && <div className="category-management__error">{error}</div>}

      <div className="category-management__list">
        {optionTypes.length === 0 ? (
          <p>Chưa có phân loại nào.</p>
        ) : (
          optionTypes.map((optionType) => (
            <article
              key={optionType.id}
              className="category-management__item product-option-management__item"
            >
              <div className="product-option-management__type-header">
                <div>
                  <h3>{optionType.name}</h3>

                  <p>{optionType.description || "Chưa có mô tả."}</p>
                </div>

                <div className="category-management__actions">
                  <button
                    type="button"
                    className="category-management__edit-button"
                    onClick={() => handleEdit(optionType)}
                  >
                    Sửa
                  </button>

                  <button
                    type="button"
                    className="category-management__delete-button"
                    onClick={() => handleDeleteClick(optionType)}
                  >
                    Xóa
                  </button>
                </div>
              </div>

              <div className="product-option-management__values">
                <div className="product-option-management__values-header">
                  <strong>Giá trị phân loại</strong>

                  <button
                    type="button"
                    className="product-option-management__add-value"
                    onClick={() => handleOpenCreateValue(optionType)}
                  >
                    + Thêm giá trị
                  </button>
                </div>

                {(valuesByType[optionType.id] ?? []).length === 0 ? (
                  <p className="product-option-management__empty-values">
                    Chưa có giá trị phân loại.
                  </p>
                ) : (
                  <div className="product-option-management__value-list">
                    {(valuesByType[optionType.id] ?? []).map((value) => (
                      <div
                        key={value.id}
                        className="product-option-management__value"
                      >
                        <span>{value.name}</span>

                        <button
                          type="button"
                          className="product-option-management__value-edit"
                          onClick={() => handleEditValue(optionType, value)}
                          title="Sửa giá trị"
                          aria-label={`Sửa ${value.name}`}
                        >
                          ✎
                        </button>

                        <button
                          type="button"
                          className="product-option-management__value-delete"
                          onClick={() =>
                            handleDeleteValueClick(optionType, value)
                          }
                          title="Xóa giá trị"
                          aria-label={`Xóa ${value.name}`}
                        >
                          ×
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </article>
          ))
        )}
      </div>

      {showForm && (
        <div
          className="category-modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="product-option-modal-title"
        >
          <div
            className="category-modal__backdrop"
            onMouseDown={(event) => {
              if (event.target === event.currentTarget && !saving) {
                handleCloseForm();
              }
            }}
          >
            <form className="category-modal__content" onSubmit={handleSubmit}>
              <div className="category-modal__header">
                <div>
                  <h3 id="product-option-modal-title">
                    {editingId != null
                      ? "Cập nhật phân loại"
                      : "Thêm phân loại"}
                  </h3>

                  <p>
                    {editingId != null
                      ? "Chỉnh sửa thông tin phân loại."
                      : "Nhập thông tin phân loại mới."}
                  </p>
                </div>

                <button
                  type="button"
                  className="category-modal__close"
                  onClick={handleCloseForm}
                  disabled={saving}
                  aria-label="Đóng"
                >
                  ×
                </button>
              </div>

              {formError && (
                <div className="category-management__error">{formError}</div>
              )}

              <div className="category-management__field">
                <label htmlFor="option-type-name">Tên phân loại</label>

                <input
                  id="option-type-name"
                  name="name"
                  type="text"
                  value={formData.name}
                  onChange={handleChange}
                  placeholder="Ví dụ: Màu sắc"
                  maxLength={100}
                  disabled={saving}
                  required
                  autoFocus
                />
              </div>

              <div className="category-management__field">
                <label htmlFor="option-type-description">Mô tả</label>

                <textarea
                  id="option-type-description"
                  name="description"
                  value={formData.description}
                  onChange={handleChange}
                  placeholder="Ví dụ: Phân loại sản phẩm theo màu sắc..."
                  rows={4}
                  maxLength={255}
                  disabled={saving}
                />

                <span className="category-modal__counter">
                  {formData.description.length}/255
                </span>
              </div>

              <div className="category-modal__actions">
                <button
                  type="button"
                  className="category-modal__cancel"
                  onClick={handleCloseForm}
                  disabled={saving}
                >
                  Hủy
                </button>

                <button
                  type="submit"
                  className="category-modal__submit"
                  disabled={saving}
                >
                  {saving ? (
                    <LoadingSpinner size="small" inline variant="light" />
                  ) : editingId != null ? (
                    "Cập nhật"
                  ) : (
                    "Thêm phân loại"
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showValueForm && valueType && (
        <div
          className="category-modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="value-modal-title"
        >
          <div
            className="category-modal__backdrop"
            onMouseDown={(event) => {
              if (event.target === event.currentTarget && !valueSaving) {
                handleCloseValueForm();
              }
            }}
          >
            <form
              className="category-modal__content"
              onSubmit={handleValueSubmit}
            >
              <div className="category-modal__header">
                <div>
                  <h3 id="value-modal-title">
                    {editingValueId != null
                      ? "Cập nhật giá trị"
                      : "Thêm giá trị phân loại"}
                  </h3>

                  <p>
                    Phân loại: <strong>{valueType.name}</strong>
                  </p>
                </div>

                <button
                  type="button"
                  className="category-modal__close"
                  onClick={handleCloseValueForm}
                  disabled={valueSaving}
                  aria-label="Đóng"
                >
                  ×
                </button>
              </div>

              {valueFormError && (
                <div className="category-management__error">
                  {valueFormError}
                </div>
              )}

              <div className="category-management__field">
                <label htmlFor="option-value-name">Tên giá trị</label>

                <input
                  id="option-value-name"
                  name="name"
                  type="text"
                  value={valueFormData.name}
                  onChange={handleValueChange}
                  placeholder={
                    valueType.name.toLowerCase().includes("màu")
                      ? "Ví dụ: Đỏ"
                      : "Ví dụ: 38"
                  }
                  maxLength={100}
                  disabled={valueSaving}
                  required
                  autoFocus
                />
              </div>

              <div className="category-modal__actions">
                <button
                  type="button"
                  className="category-modal__cancel"
                  onClick={handleCloseValueForm}
                  disabled={valueSaving}
                >
                  Hủy
                </button>

                <button
                  type="submit"
                  className="category-modal__submit"
                  disabled={valueSaving}
                >
                  {valueSaving ? (
                    <LoadingSpinner size="small" inline variant="light" />
                  ) : editingValueId != null ? (
                    "Cập nhật"
                  ) : (
                    "Thêm giá trị"
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {deleteTarget && (
        <div
          className="category-delete-modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="option-delete-modal-title"
        >
          <div
            className="category-delete-modal__backdrop"
            onMouseDown={(event) => {
              if (event.target === event.currentTarget && !deleting) {
                handleCloseDelete();
              }
            }}
          >
            <div className="category-delete-modal__content">
              <div className="category-delete-modal__header">
                <h3 id="option-delete-modal-title">Xóa phân loại</h3>

                <button
                  type="button"
                  className="category-delete-modal__close"
                  onClick={handleCloseDelete}
                  disabled={deleting}
                  aria-label="Đóng"
                >
                  ×
                </button>
              </div>

              <p>
                Bạn có chắc muốn xóa phân loại{" "}
                <strong>{deleteTarget.name}</strong> không?
              </p>

              <p className="category-delete-modal__warning">
                Phân loại đang có giá trị sẽ không thể xóa.
              </p>

              {deleteError && (
                <div className="category-management__error">{deleteError}</div>
              )}

              <div className="category-delete-modal__actions">
                <button
                  type="button"
                  className="category-delete-modal__cancel"
                  onClick={handleCloseDelete}
                  disabled={deleting}
                >
                  Hủy
                </button>

                <button
                  type="button"
                  className="category-delete-modal__confirm"
                  onClick={handleDeleteConfirm}
                  disabled={deleting}
                >
                  {deleting ? (
                    <LoadingSpinner size="small" inline variant="light" />
                  ) : (
                    "Xóa phân loại"
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {deleteValueTarget && (
        <div
          className="category-delete-modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="delete-value-title"
        >
          <div
            className="category-delete-modal__backdrop"
            onMouseDown={(event) => {
              if (event.target === event.currentTarget && !valueDeleting) {
                handleCloseDeleteValue();
              }
            }}
          >
            <div className="category-delete-modal__content">
              <div className="category-delete-modal__header">
                <h3 id="delete-value-title">Xóa giá trị phân loại</h3>

                <button
                  type="button"
                  className="category-delete-modal__close"
                  onClick={handleCloseDeleteValue}
                  disabled={valueDeleting}
                  aria-label="Đóng"
                >
                  ×
                </button>
              </div>

              <p>
                Bạn có chắc muốn xóa{" "}
                <strong>{deleteValueTarget.value.name}</strong> khỏi phân loại{" "}
                <strong>{deleteValueTarget.optionType.name}</strong> không?
              </p>

              {deleteValueError && (
                <div className="category-management__error">
                  {deleteValueError}
                </div>
              )}

              <div className="category-delete-modal__actions">
                <button
                  type="button"
                  className="category-delete-modal__cancel"
                  onClick={handleCloseDeleteValue}
                  disabled={valueDeleting}
                >
                  Hủy
                </button>

                <button
                  type="button"
                  className="category-delete-modal__confirm"
                  onClick={handleDeleteValueConfirm}
                  disabled={valueDeleting}
                >
                  {valueDeleting ? (
                    <LoadingSpinner size="small" inline variant="light" />
                  ) : (
                    "Xóa giá trị"
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}

export default ProductOptionManagement;
