import { useEffect, useState } from "react";

import categoryApi from "../../../api/categoryApi";

import LoadingSpinner from "../../common/LoadingSpinner";

import "./CategoryManagement.css";

function CategoryManagement() {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState("");

  const [formData, setFormData] = useState({
    name: "",
    description: "",
  });

  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState("");

  const loadCategories = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await categoryApi.getAll();

      setCategories(response.data?.data ?? []);
    } catch (err) {
      console.error("Không thể tải danh mục:", err);

      setError(
        err.response?.data?.message || "Không thể tải danh sách danh mục.",
      );
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleEdit = (category) => {
    setEditingId(category.id);

    setFormData({
      name: category.name ?? "",
      description: category.description ?? "",
    });

    setFormError("");
    setShowForm(true);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    const payload = {
      name: formData.name.trim(),
      description: formData.description.trim(),
    };

    try {
      setSaving(true);
      setFormError("");

      if (editingId) {
        await categoryApi.update(editingId, payload);
      } else {
        await categoryApi.create(payload);
      }

      setFormData({
        name: "",
        description: "",
      });

      setEditingId(null);
      setShowForm(false);

      await loadCategories();
    } catch (err) {
      console.error("Không thể lưu danh mục:", err);

      setFormError(err.response?.data?.message || "Không thể lưu danh mục.");
    } finally {
      setSaving(false);
    }
  };

  const handleCloseForm = () => {
    setShowForm(false);
    setEditingId(null);
    setFormError("");

    setFormData({
      name: "",
      description: "",
    });
  };

  const handleDeleteClick = (category) => {
    setDeleteTarget(category);
    setDeleteError("");
  };

  const handleDeleteConfirm = async () => {
    if (!deleteTarget) {
      return;
    }

    try {
      setDeleting(true);
      setDeleteError("");

      await categoryApi.delete(deleteTarget.id);

      setDeleteTarget(null);

      await loadCategories();
    } catch (err) {
      console.error("Không thể xóa danh mục:", err);

      setDeleteError(err.response?.data?.message || "Không thể xóa danh mục.");
    } finally {
      setDeleting(false);
    }
  };

  useEffect(() => {
    loadCategories();
  }, []);

  if (loading) {
    return (
      <section className="category-management">
        <h2>Quản lý danh mục</h2>

        <LoadingSpinner size="large" />
      </section>
    );
  }

  return (
    <section className="category-management">
      <div className="category-management__header">
        <div className="category-management__toolbar">
          <h2>Quản lý danh mục</h2>
          <button
            type="button"
            className="category-management__add-button"
            onClick={() => {
              setShowForm(true);
              setFormError("");
            }}
          >
            + Thêm danh mục
          </button>
        </div>

        {showForm && (
          <div
            className="category-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="category-modal-title"
          >
            <div className="category-modal__backdrop">
              <form className="category-modal__content" onSubmit={handleSubmit}>
                <div className="category-modal__header">
                  <div>
                    <h3 id="category-modal-title">
                      {editingId ? "Cập nhật danh mục" : "Thêm danh mục"}
                    </h3>

                    <p>
                      {editingId
                        ? "Chỉnh sửa thông tin danh mục."
                        : "Nhập thông tin danh mục mới."}
                    </p>
                  </div>

                  <button
                    type="button"
                    className="category-modal__close"
                    onClick={handleCloseForm}
                    aria-label="Đóng"
                  >
                    ×
                  </button>
                </div>

                {formError && (
                  <div className="category-management__error">{formError}</div>
                )}

                <div className="category-management__field">
                  <label htmlFor="category-name">Tên danh mục</label>

                  <input
                    id="category-name"
                    name="name"
                    type="text"
                    value={formData.name}
                    onChange={handleChange}
                    maxLength={100}
                    required
                    autoFocus
                  />
                </div>

                <div className="category-management__field">
                  <label htmlFor="category-description">Mô tả</label>

                  <textarea
                    id="category-description"
                    name="description"
                    value={formData.description}
                    onChange={handleChange}
                    placeholder="Nhập mô tả danh mục..."
                    rows={4}
                    maxLength={255}
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
                    ) : editingId ? (
                      "Cập nhật"
                    ) : (
                      "Thêm danh mục"
                    )}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        <p>Quản lý các danh mục sản phẩm của MiniShop.</p>
      </div>

      {error && <div className="category-management__error">{error}</div>}

      <div className="category-management__list">
        {categories.length === 0 ? (
          <p>Chưa có danh mục nào.</p>
        ) : (
          categories.map((category) => (
            <article key={category.id} className="category-management__item">
              <div>
                <h3>{category.name}</h3>

                <p>{category.description || "Chưa có mô tả."}</p>
              </div>

              <div className="category-management__actions">
                <button
                  type="button"
                  className="category-management__edit-button"
                  onClick={() => handleEdit(category)}
                >
                  Sửa
                </button>

                <button
                  type="button"
                  className="category-management__delete-button"
                  onClick={() => handleDeleteClick(category)}
                >
                  Xóa
                </button>
              </div>
            </article>
          ))
        )}
      </div>

      {deleteTarget && (
        <div
          className="category-delete-modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="category-delete-modal-title"
        >
          <div className="category-delete-modal__backdrop">
            <div className="category-delete-modal__content">
              <div className="category-delete-modal__header">
                <h3 id="category-delete-modal-title">Xóa danh mục</h3>

                <button
                  type="button"
                  className="category-delete-modal__close"
                  onClick={() => {
                    setDeleteTarget(null);
                    setDeleteError("");
                  }}
                  disabled={deleting}
                  aria-label="Đóng"
                >
                  ×
                </button>
              </div>

              <p>
                Bạn có chắc muốn xóa danh mục{" "}
                <strong>{deleteTarget.name}</strong> không?
              </p>

              <p className="category-delete-modal__warning"></p>

              {deleteError && (
                <div className="category-management__error">{deleteError}</div>
              )}

              <div className="category-delete-modal__actions">
                <button
                  type="button"
                  className="category-delete-modal__cancel"
                  onClick={() => {
                    setDeleteTarget(null);
                    setDeleteError("");
                  }}
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
                    "Xóa danh mục"
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

export default CategoryManagement;
