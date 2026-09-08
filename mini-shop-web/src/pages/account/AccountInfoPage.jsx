import { useEffect, useState } from "react";

import userApi from "../../api/userApi";
import { useAuth } from "../../contexts/AuthContext";
import LoadingSpinner from "../../components/common/LoadingSpinner";

import "./AccountInfoPage.css";

function AccountInfoPage() {
  const { updateCurrentUser } = useAuth();
  const [user, setUser] = useState(null);
  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    phone: "",
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [avatarFile, setAvatarFile] = useState(null);
  const [avatarPreview, setAvatarPreview] = useState("");
  const [avatarSaving, setAvatarSaving] = useState(false);

  useEffect(() => {
    const loadCurrentUser = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await userApi.getCurrentUser();
        const currentUser = response.data.data;
        setUser(currentUser);
        setAvatarPreview(currentUser.avatarUrl ?? "");
        updateCurrentUser(currentUser);
        setFormData({
          fullName: currentUser.fullName ?? "",
          email: currentUser.email ?? "",
          phone: currentUser.phone ?? "",
        });
      } catch (err) {
        console.error("Lỗi lấy thông tin tài khoản:", err);

        setError(
          err?.response?.data?.message || "Không thể tải thông tin tài khoản.",
        );
      } finally {
        setLoading(false);
      }
    };

    loadCurrentUser();
  }, [updateCurrentUser]);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((previous) => ({
      ...previous,
      [name]: value,
    }));

    setError("");
    setSuccessMessage("");
  };

  const validateForm = () => {
    const fullName = formData.fullName.trim();
    const email = formData.email.trim();
    const phone = formData.phone.trim();

    if (!fullName) {
      return "Họ và tên không được để trống.";
    }

    if (email) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

      if (!emailRegex.test(email)) {
        return "Email không hợp lệ.";
      }
    }

    if (phone) {
      const phoneRegex = /^[0-9]{9,11}$/;

      if (!phoneRegex.test(phone)) {
        return "Số điện thoại phải gồm 9-11 chữ số.";
      }
    }

    return "";
  };

  const handleAvatarChange = (event) => {
    const file = event.target.files?.[0];

    if (!file) {
      return;
    }

    const allowedTypes = ["image/jpeg", "image/png", "image/webp"];

    if (!allowedTypes.includes(file.type)) {
      setError("Chỉ chấp nhận ảnh JPEG, PNG hoặc WEBP.");
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      setError("Dung lượng ảnh không được vượt quá 5MB.");
      return;
    }

    setError("");
    setSuccessMessage("");

    setAvatarFile(file);

    const previewUrl = URL.createObjectURL(file);

    setAvatarPreview(previewUrl);
  };

  const handleAvatarUpload = async () => {
    if (!avatarFile) {
      return;
    }

    try {
      setAvatarSaving(true);
      setError("");
      setSuccessMessage("");

      const response = await userApi.updateAvatar(avatarFile);

      const updatedUser = response.data?.data;

      if (!updatedUser) {
        throw new Error("Không nhận được dữ liệu người dùng.");
      }

      setUser(updatedUser);

      updateCurrentUser(updatedUser);

      setAvatarPreview(updatedUser.avatarUrl ?? "");

      setAvatarFile(null);

      setSuccessMessage("Cập nhật ảnh đại diện thành công.");
    } catch (err) {
      console.error("Lỗi cập nhật ảnh đại diện:", err);

      setError(
        err?.response?.data?.message || "Không thể cập nhật ảnh đại diện.",
      );
    } finally {
      setAvatarSaving(false);
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    setError("");
    setSuccessMessage("");

    const validationError = validateForm();

    if (validationError) {
      setError(validationError);
      return;
    }

    try {
      setSaving(true);

      const updateRequest = {
        fullName: formData.fullName.trim(),
        email: formData.email.trim() || null,
        phone: formData.phone.trim() || null,
      };

      const response = await userApi.updateCurrentUser(updateRequest);

      const updatedUser = response.data.data;

      setUser(updatedUser);
      updateCurrentUser(updatedUser);

      setFormData({
        fullName: updatedUser.fullName ?? "",
        email: updatedUser.email ?? "",
        phone: updatedUser.phone ?? "",
      });

      setSuccessMessage("Cập nhật thông tin thành công.");
    } catch (err) {
      console.error("Lỗi cập nhật thông tin tài khoản:", err);

      setError(
        err?.response?.data?.message ||
          "Không thể cập nhật thông tin tài khoản.",
      );
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <section className="account-info-page">
        <div className="account-info-page__loading">
          <LoadingSpinner size="large" />
        </div>
      </section>
    );
  }

  if (error && !user) {
    return (
      <section className="account-info-page">
        <div className="account-info-page__error">{error}</div>
      </section>
    );
  }

  if (!user) {
    return null;
  }

  return (
    <section className="account-info-page">
      <div className="account-info-page__header">
        <div>
          <span className="account-info-page__eyebrow">Tài khoản của tôi</span>

          <h1 className="account-info-page__title">Thông tin tài khoản</h1>

          <p className="account-info-page__description">
            Quản lý thông tin cá nhân của bạn tại Hair.
          </p>
        </div>

        <div className="account-info-page__avatar-section">
          <div className="account-info-page__avatar">
            {avatarPreview ? (
              <img src={avatarPreview} alt="Ảnh đại diện" />
            ) : (
              <span>👤</span>
            )}
          </div>

          <label
            htmlFor="avatarFile"
            className="account-info-page__avatar-button"
          >
            Chọn ảnh
          </label>

          <input
            id="avatarFile"
            type="file"
            accept="image/jpeg,image/png,image/webp"
            onChange={handleAvatarChange}
            hidden
          />

          <p className="account-info-page__avatar-hint">
            Dung lượng file tối đa 5MB
            <br />
            Định dạng: JPEG, PNG, WEBP
          </p>

          {avatarFile && (
            <button
              type="button"
              className="account-info-page__avatar-save"
              onClick={handleAvatarUpload}
              disabled={avatarSaving}
            >
              {avatarSaving ? (
                <LoadingSpinner size="small" inline variant="light" />
              ) : (
                "Lưu ảnh"
              )}
            </button>
          )}
        </div>
      </div>

      <div className="account-info-page__card">
        <div className="account-info-page__section-header">
          <div>
            <h2>Thông tin cá nhân</h2>

            <p>Bạn có thể cập nhật các thông tin cá nhân bên dưới.</p>
          </div>
        </div>

        <form className="account-info-page__form" onSubmit={handleSubmit}>
          <div className="account-info-page__field">
            <label htmlFor="fullName">Họ và tên</label>

            <input
              id="fullName"
              name="fullName"
              type="text"
              value={formData.fullName}
              onChange={handleChange}
              placeholder="Nhập họ và tên"
              disabled={saving}
            />
          </div>

          <div className="account-info-page__field">
            <label htmlFor="email">Email</label>

            <input
              id="email"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="Nhập email"
              disabled={saving}
            />

            {!formData.email && (
              <span className="account-info-page__hint account-info-page__hint--warning">
                Bạn chưa cập nhật email. Hãy nhập email để có thể nhận thông báo
                và email đơn hàng.
              </span>
            )}
          </div>

          <div className="account-info-page__field">
            <label htmlFor="phone">Số điện thoại</label>

            <input
              id="phone"
              name="phone"
              type="tel"
              value={formData.phone}
              onChange={handleChange}
              placeholder="Nhập số điện thoại"
              disabled={saving}
            />
          </div>

          <div className="account-info-page__field">
            <label htmlFor="role">Vai trò</label>

            <input
              id="role"
              type="text"
              value={user.roleName ?? user.role ?? ""}
              readOnly
            />
          </div>

          {error && (
            <div className="account-info-page__form-error">{error}</div>
          )}

          {successMessage && (
            <div className="account-info-page__form-success">
              {successMessage}
            </div>
          )}

          <div className="account-info-page__actions">
            <button
              type="submit"
              className="account-info-page__primary-button"
              disabled={saving}
            >
              {saving ? (
                <LoadingSpinner size="small" inline variant="light" />
              ) : (
                "Lưu thay đổi"
              )}
            </button>
          </div>
        </form>
      </div>
    </section>
  );
}

export default AccountInfoPage;
