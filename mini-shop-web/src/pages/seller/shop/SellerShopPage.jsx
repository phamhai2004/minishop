import { useEffect, useRef, useState } from "react";

import sellerShopApi from "../../../api/sellerShopApi";
import LoadingSpinner from "../../../components/common/LoadingSpinner";

import "./SellerShopPage.css";

const EMPTY_FORM = {
  name: "",
  description: "",
  phone: "",
  email: "",
  pickupAddress: "",
};

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

function SellerShopPage() {
  const [shop, setShop] = useState(null);

  const [form, setForm] = useState(EMPTY_FORM);

  const [loading, setLoading] = useState(true);

  const [saving, setSaving] = useState(false);

  const [uploadingLogo, setUploadingLogo] = useState(false);

  const [uploadingCover, setUploadingCover] = useState(false);

  const [error, setError] = useState("");

  const [success, setSuccess] = useState("");

  const logoInputRef = useRef(null);
  const coverInputRef = useRef(null);

  const applyShop = (shopData) => {
    setShop(shopData);

    setForm({
      name: shopData?.name ?? "",
      description: shopData?.description ?? "",
      phone: shopData?.phone ?? "",
      email: shopData?.email ?? "",
      pickupAddress: shopData?.pickupAddress ?? "",
    });
  };

  const loadShop = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await sellerShopApi.getMyShop();

      const apiResponse = response.data;

      if (!apiResponse?.success || !apiResponse?.data) {
        throw new Error(
          apiResponse?.message || "Không thể tải thông tin shop.",
        );
      }

      applyShop(apiResponse.data);
    } catch (requestError) {
      console.error("Không thể tải shop:", requestError);

      setError(
        requestError.response?.data?.message ||
          requestError.message ||
          "Không thể tải thông tin shop.",
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadShop();
  }, []);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));

    setSuccess("");
  };

  const validateForm = () => {
    const name = form.name.trim();

    if (!name) {
      return "Tên shop không được để trống.";
    }

    if (name.length < 3 || name.length > 150) {
      return "Tên shop phải từ 3 đến 150 ký tự.";
    }

    if (form.description.length > 1000) {
      return "Mô tả shop không được vượt quá 1000 ký tự.";
    }

    if (form.phone && !/^[0-9]{9,11}$/.test(form.phone.trim())) {
      return "Số điện thoại không hợp lệ.";
    }

    if (form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
      return "Email không hợp lệ.";
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
      setSuccess("");

      const payload = {
        name: form.name.trim(),

        description: form.description.trim(),

        logoUrl: shop?.logoUrl ?? null,

        coverUrl: shop?.coverUrl ?? null,

        phone: form.phone.trim(),

        email: form.email.trim(),

        pickupAddress: form.pickupAddress.trim(),
      };

      const response = await sellerShopApi.updateMyShop(payload);

      const apiResponse = response.data;

      if (!apiResponse?.success || !apiResponse?.data) {
        throw new Error(
          apiResponse?.message || "Cập nhật shop không thành công.",
        );
      }

      applyShop(apiResponse.data);

      setSuccess(apiResponse.message || "Cập nhật thông tin shop thành công.");
    } catch (requestError) {
      console.error("Không thể cập nhật shop:", requestError);

      setError(
        requestError.response?.data?.message ||
          requestError.message ||
          "Không thể cập nhật shop.",
      );
    } finally {
      setSaving(false);
    }
  };

  const handleLogoChange = async (event) => {
    const file = event.target.files?.[0];

    event.target.value = "";

    if (!file) {
      return;
    }

    if (!file.type.startsWith("image/")) {
      setError("Vui lòng chọn file hình ảnh.");

      return;
    }

    try {
      setUploadingLogo(true);
      setError("");
      setSuccess("");

      const response = await sellerShopApi.uploadLogo(file);

      const apiResponse = response.data;

      if (!apiResponse?.success || !apiResponse?.data) {
        throw new Error(
          apiResponse?.message || "Cập nhật logo không thành công.",
        );
      }

      applyShop(apiResponse.data);

      setSuccess(apiResponse.message || "Cập nhật logo shop thành công.");
    } catch (requestError) {
      console.error("Không thể upload logo:", requestError);

      setError(
        requestError.response?.data?.message ||
          requestError.message ||
          "Không thể cập nhật logo shop.",
      );
    } finally {
      setUploadingLogo(false);
    }
  };

  const handleCoverChange = async (event) => {
    const file = event.target.files?.[0];

    event.target.value = "";

    if (!file) {
      return;
    }

    if (!file.type.startsWith("image/")) {
      setError("Vui lòng chọn file hình ảnh.");

      return;
    }

    try {
      setUploadingCover(true);
      setError("");
      setSuccess("");

      const response = await sellerShopApi.uploadCover(file);

      const apiResponse = response.data;

      if (!apiResponse?.success || !apiResponse?.data) {
        throw new Error(
          apiResponse?.message || "Cập nhật ảnh bìa không thành công.",
        );
      }

      applyShop(apiResponse.data);

      setSuccess(apiResponse.message || "Cập nhật ảnh bìa shop thành công.");
    } catch (requestError) {
      console.error("Không thể upload cover:", requestError);

      setError(
        requestError.response?.data?.message ||
          requestError.message ||
          "Không thể cập nhật ảnh bìa.",
      );
    } finally {
      setUploadingCover(false);
    }
  };

  if (loading) {
    return (
      <main className="seller-shop-page">
        <div className="seller-shop-page__loading">
          <LoadingSpinner size="large" />
        </div>
      </main>
    );
  }

  if (!shop) {
    return (
      <main className="seller-shop-page">
        <div className="seller-shop-page__error">
          {error || "Không tìm thấy thông tin shop."}
        </div>
      </main>
    );
  }

  return (
    <main className="seller-shop-page">
      <section className="seller-shop-profile">
        {/* COVER */}
        <div className="seller-shop-cover">
          {shop.coverUrl ? (
            <img src={shop.coverUrl} alt={`Ảnh bìa ${shop.name}`} />
          ) : (
            <div className="seller-shop-cover__placeholder">Hair</div>
          )}

          <button
            type="button"
            className="seller-shop-cover__button"
            onClick={() => coverInputRef.current?.click()}
            disabled={uploadingCover}
          >
            {uploadingCover ? (
              <LoadingSpinner size="small" inline variant="light" />
            ) : (
              "Đổi ảnh bìa"
            )}
          </button>

          <input
            ref={coverInputRef}
            type="file"
            accept="image/*"
            hidden
            onChange={handleCoverChange}
          />
        </div>

        {/* HEADER SHOP */}
        <div className="seller-shop-profile__header">
          <div className="seller-shop-logo">
            {shop.logoUrl ? (
              <img src={shop.logoUrl} alt={`Logo ${shop.name}`} />
            ) : (
              <span>{shop.name?.charAt(0)?.toUpperCase() || "S"}</span>
            )}

            <button
              type="button"
              className="seller-shop-logo__edit"
              onClick={() => logoInputRef.current?.click()}
              disabled={uploadingLogo}
              title="Đổi logo"
            >
              {uploadingLogo ? (
                <LoadingSpinner size="small" inline variant="light" />
              ) : (
                "✎"
              )}
            </button>

            <input
              ref={logoInputRef}
              type="file"
              accept="image/*"
              hidden
              onChange={handleLogoChange}
            />
          </div>

          <div className="seller-shop-profile__identity">
            <div className="seller-shop-profile__name-row">
              <h1>{shop.name}</h1>

              {shop.verified && (
                <span
                  className="seller-shop-profile__verified"
                  title="Shop đã được xác minh"
                >
                  ✓ Đã xác minh
                </span>
              )}
            </div>

            <p>{shop.statusName || shop.status || "Chưa xác định"}</p>
          </div>
        </div>

        {/* STATS */}
        <div className="seller-shop-stats">
          <div>
            <strong>{shop.totalProducts ?? 0}</strong>
            <span>Sản phẩm</span>
          </div>

          <div>
            <strong>{shop.totalOrders ?? 0}</strong>
            <span>Đơn hàng</span>
          </div>

          <div>
            <strong>{shop.totalReviews ?? 0}</strong>
            <span>Đánh giá</span>
          </div>

          <div>
            <strong>{Number(shop.rating ?? 0).toFixed(1)}</strong>
            <span>Điểm đánh giá</span>
          </div>

          <div>
            <strong>{shop.totalFollowers ?? 0}</strong>
            <span>Người theo dõi</span>
          </div>
        </div>

        {error && (
          <div
            className="seller-shop-message seller-shop-message--error"
            role="alert"
          >
            {error}
          </div>
        )}

        {success && (
          <div
            className="seller-shop-message seller-shop-message--success"
            role="status"
          >
            {success}
          </div>
        )}

        {/* FORM */}
        <form className="seller-shop-form" onSubmit={handleSubmit}>
          <div className="seller-shop-form__title">
            <div>
              <h2>Thông tin shop</h2>

              <p>Cập nhật thông tin hiển thị và liên hệ của shop.</p>
            </div>
          </div>

          <div className="seller-shop-form__grid">
            <label className="seller-shop-field">
              <span>Tên shop</span>

              <input
                type="text"
                name="name"
                value={form.name}
                onChange={handleChange}
                maxLength={150}
                disabled={saving}
                required
              />
            </label>

            <label className="seller-shop-field">
              <span>Số điện thoại</span>

              <input
                type="tel"
                name="phone"
                value={form.phone}
                onChange={handleChange}
                maxLength={11}
                disabled={saving}
              />
            </label>

            <label className="seller-shop-field">
              <span>Email</span>

              <input
                type="email"
                name="email"
                value={form.email}
                onChange={handleChange}
                disabled={saving}
              />
            </label>

            <label className="seller-shop-field">
              <span>Địa chỉ cửa hàng</span>

              <input
                type="text"
                name="pickupAddress"
                value={form.pickupAddress}
                onChange={handleChange}
                disabled={saving}
              />
            </label>

            <label className="seller-shop-field seller-shop-field--full">
              <span>Mô tả shop</span>

              <textarea
                name="description"
                value={form.description}
                onChange={handleChange}
                maxLength={1000}
                rows={5}
                disabled={saving}
              />

              <small>{form.description.length}/1000</small>
            </label>
          </div>

          <div className="seller-shop-form__actions">
            <button type="submit" disabled={saving}>
              {saving ? (
                <LoadingSpinner size="small" inline variant="light" />
              ) : (
                "Lưu thay đổi"
              )}
            </button>
          </div>
        </form>

        {/* META */}
        <div className="seller-shop-meta">
          <h2>Thông tin hệ thống</h2>

          <div className="seller-shop-meta__grid">
            <div>
              <span>Chủ shop</span>
              <strong>{shop.ownerName || "—"}</strong>
            </div>

            <div>
              <span>Ngày tạo</span>
              <strong>{formatDate(shop.createdAt)}</strong>
            </div>

            <div>
              <span>Ngày được duyệt</span>
              <strong>{formatDate(shop.approvedAt)}</strong>
            </div>

            <div>
              <span>Trạng thái xác minh</span>
              <strong>{shop.verified ? "Đã xác minh" : "Chưa xác minh"}</strong>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}

export default SellerShopPage;
