import { useCallback, useEffect, useState } from "react";

import shopApi from "../../../api/shopApi";
import LoadingSpinner from "../../../components/common/LoadingSpinner";

import "./ShopRegistrationPage.css";

const EMPTY_FORM = {
  name: "",
  description: "",
  phone: "",
  email: "",
  pickupAddress: "",
};

function ShopRegistrationPage() {
  const [shop, setShop] = useState(null);

  const [form, setForm] = useState(EMPTY_FORM);

  const [loading, setLoading] = useState(true);

  const [submitting, setSubmitting] = useState(false);

  const [error, setError] = useState("");

  const [success, setSuccess] = useState("");

  const loadMyShop = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const response = await shopApi.getMyShop();

      const apiResponse = response.data;

      if (!apiResponse?.success || !apiResponse?.data) {
        throw new Error(
          apiResponse?.message ?? "Không thể tải thông tin shop.",
        );
      }

      const shopData = apiResponse.data;

      setShop(shopData);

      setForm({
        name: shopData.name ?? "",
        description: shopData.description ?? "",
        phone: shopData.phone ?? "",
        email: shopData.email ?? "",
        pickupAddress: shopData.pickupAddress ?? "",
      });
    } catch (err) {
      /*
       * 404 = Customer chưa từng
       * đăng ký shop.
       */
      if (err.response?.status === 404) {
        setShop(null);
        setForm(EMPTY_FORM);
        setError("");

        return;
      }

      console.error("Unable to load shop registration:", err);

      setError(
        err.response?.data?.message ??
          err.message ??
          "Không thể tải thông tin shop.",
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadMyShop();
  }, [loadMyShop]);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((current) => ({
      ...current,
      [name]: value,
    }));

    setError("");
    setSuccess("");
  };

  const validateForm = () => {
    const name = form.name.trim();

    const phone = form.phone.trim();

    const pickupAddress = form.pickupAddress.trim();

    if (name.length < 3) {
      return "Tên shop phải có ít nhất 3 ký tự.";
    }

    if (!/^[0-9]{9,11}$/.test(phone)) {
      return "Số điện thoại phải có từ 9 đến 11 chữ số.";
    }

    if (!pickupAddress) {
      return "Địa chỉ lấy hàng không được bỏ trống.";
    }

    if (
      form.email.trim() &&
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())
    ) {
      return "Email không hợp lệ.";
    }

    return "";
  };

  const handleRegister = async (event) => {
    event.preventDefault();

    const validationError = validateForm();

    if (validationError) {
      setError(validationError);
      return;
    }

    try {
      setSubmitting(true);
      setError("");
      setSuccess("");

      const payload = {
        name: form.name.trim(),

        description: form.description.trim(),

        phone: form.phone.trim(),

        email: form.email.trim() || null,

        pickupAddress: form.pickupAddress.trim(),

        /*
         * Logo/cover để Seller
         * upload sau khi shop
         * được duyệt.
         */
        logoUrl: null,
        coverUrl: null,
      };

      const response = await shopApi.register(payload);

      const apiResponse = response.data;

      if (!apiResponse?.success || !apiResponse?.data) {
        throw new Error(apiResponse?.message ?? "Đăng ký shop thất bại.");
      }

      setShop(apiResponse.data);

      setSuccess(
        apiResponse.message ??
          "Đăng ký shop thành công. Vui lòng chờ Admin xét duyệt.",
      );
    } catch (err) {
      console.error("Unable to register shop:", err);

      setError(
        err.response?.data?.message ?? err.message ?? "Đăng ký shop thất bại.",
      );
    } finally {
      setSubmitting(false);
    }
  };

  const handleUpdate = async (event) => {
    event.preventDefault();

    const validationError = validateForm();

    if (validationError) {
      setError(validationError);
      return;
    }

    try {
      setSubmitting(true);
      setError("");
      setSuccess("");

      /*
       * Backend
       * UpdateShopRegistrationRequest
       * hiện dùng field address.
       */
      const payload = {
        name: form.name.trim(),

        description: form.description.trim(),

        phone: form.phone.trim(),

        address: form.pickupAddress.trim(),
      };

      const response = await shopApi.updateMyRegistration(payload);

      const apiResponse = response.data;

      if (!apiResponse?.success || !apiResponse?.data) {
        throw new Error(
          apiResponse?.message ?? "Không thể cập nhật hồ sơ shop.",
        );
      }

      setShop(apiResponse.data);

      setSuccess("Đã cập nhật thông tin shop.");
    } catch (err) {
      console.error("Unable to update shop registration:", err);

      setError(
        err.response?.data?.message ??
          err.message ??
          "Không thể cập nhật hồ sơ shop.",
      );
    } finally {
      setSubmitting(false);
    }
  };

  const handleResubmit = async () => {
    try {
      setSubmitting(true);
      setError("");
      setSuccess("");

      const response = await shopApi.resubmitMyShop();

      const apiResponse = response.data;

      if (!apiResponse?.success || !apiResponse?.data) {
        throw new Error(
          apiResponse?.message ?? "Không thể gửi lại yêu cầu xét duyệt.",
        );
      }

      setShop(apiResponse.data);

      setSuccess("Đã gửi lại yêu cầu mở shop. Vui lòng chờ Admin xét duyệt.");
    } catch (err) {
      console.error("Unable to resubmit shop:", err);

      setError(
        err.response?.data?.message ??
          err.message ??
          "Không thể gửi lại yêu cầu xét duyệt.",
      );
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <main className="shop-registration-page">
        <div className="shop-registration-page__state">
          <LoadingSpinner size="large" />
        </div>
      </main>
    );
  }

  return (
    <main className="shop-registration-page">
      <div className="shop-registration-page__heading">
        <h1>Đăng ký mở shop</h1>

        <p>
          Tạo gian hàng của bạn trên Hair và chờ Admin xét duyệt trước khi bắt
          đầu bán hàng.
        </p>
      </div>

      {error && (
        <div className="shop-registration-page__alert shop-registration-page__alert--error">
          {error}
        </div>
      )}

      {success && (
        <div className="shop-registration-page__alert shop-registration-page__alert--success">
          {success}
        </div>
      )}

      {!shop && (
        <ShopForm
          form={form}
          onChange={handleChange}
          onSubmit={handleRegister}
          submitting={submitting}
          submitLabel="Đăng ký"
        />
      )}

      {shop?.status === "PENDING" && <PendingShop shop={shop} />}

      {shop?.status === "REJECTED" && (
        <>
          <RejectedShop shop={shop} />

          <ShopForm
            form={form}
            onChange={handleChange}
            onSubmit={handleUpdate}
            submitting={submitting}
            submitLabel="Lưu thông tin chỉnh sửa"
          />

          <div className="shop-registration-page__resubmit">
            <p>
              Sau khi đã chỉnh sửa đầy đủ thông tin, bạn có thể gửi lại yêu cầu
              cho Admin.
            </p>

            <button
              type="button"
              onClick={handleResubmit}
              disabled={submitting}
            >
              {submitting ? (
                <LoadingSpinner size="small" inline variant="light" />
              ) : (
                "Gửi lại xét duyệt"
              )}
            </button>
          </div>
        </>
      )}

      {shop?.status === "ACTIVE" && <ActiveShop shop={shop} />}

      {shop?.status === "SUSPENDED" && <SuspendedShop shop={shop} />}
    </main>
  );
}

function ShopForm({ form, onChange, onSubmit, submitting, submitLabel }) {
  return (
    <form className="shop-registration-form" onSubmit={onSubmit}>
      <div className="shop-registration-form__grid">
        <label>
          <span>Tên shop *</span>

          <input
            type="text"
            name="name"
            value={form.name}
            onChange={onChange}
            maxLength={150}
            placeholder="Ví dụ: Hai Store"
            required
          />
        </label>

        <label>
          <span>Số điện thoại *</span>

          <input
            type="tel"
            name="phone"
            value={form.phone}
            onChange={onChange}
            maxLength={11}
            placeholder="0987654321"
            required
          />
        </label>

        <label>
          <span>Email</span>

          <input
            type="email"
            name="email"
            value={form.email}
            onChange={onChange}
            placeholder="shop@example.com"
          />
        </label>

        <label className="shop-registration-form__full">
          <span>Địa chỉ lấy hàng *</span>

          <input
            type="text"
            name="pickupAddress"
            value={form.pickupAddress}
            onChange={onChange}
            placeholder="Nhập địa chỉ lấy hàng"
            required
          />
        </label>

        <label className="shop-registration-form__full">
          <span>Mô tả shop</span>

          <textarea
            name="description"
            value={form.description}
            onChange={onChange}
            maxLength={1000}
            rows={5}
            placeholder="Giới thiệu ngắn về shop của bạn..."
          />
        </label>
      </div>

      <button
        className="shop-registration-form__submit"
        type="submit"
        disabled={submitting}
      >
        {submitting ? (
          <LoadingSpinner size="small" inline variant="light" />
        ) : (
          submitLabel
        )}
      </button>
    </form>
  );
}

function PendingShop({ shop }) {
  return (
    <section className="shop-registration-status shop-registration-status--pending">
      <span className="shop-registration-status__badge">Chờ xét duyệt</span>

      <h2>{shop.name}</h2>

      <p>
        Yêu cầu mở shop của bạn đã được gửi thành công. Admin đang xem xét hồ
        sơ.
      </p>

      <ShopInformation shop={shop} />
    </section>
  );
}

function RejectedShop({ shop }) {
  return (
    <section className="shop-registration-status shop-registration-status--rejected">
      <span className="shop-registration-status__badge">Bị từ chối</span>

      <h2>{shop.name}</h2>

      <p>Yêu cầu mở shop chưa được chấp nhận.</p>

      <div className="shop-registration-status__reason">
        <strong>Lý do từ chối</strong>

        <p>{shop.rejectionReason ?? "Admin chưa cung cấp lý do."}</p>
      </div>

      <ShopInformation shop={shop} />
    </section>
  );
}

function ActiveShop({ shop }) {
  return (
    <section className="shop-registration-status shop-registration-status--active">
      <span className="shop-registration-status__badge">Đã được duyệt</span>

      <h2>{shop.name}</h2>

      <p>Shop của bạn đã được Admin duyệt và có thể bắt đầu hoạt động.</p>

      <div className="shop-registration-status__notice">
        Nếu menu Seller chưa xuất hiện, hãy đăng xuất và đăng nhập lại để nhận
        quyền SELLER mới.
      </div>

      <ShopInformation shop={shop} />
    </section>
  );
}

function SuspendedShop({ shop }) {
  return (
    <section className="shop-registration-status shop-registration-status--suspended">
      <span className="shop-registration-status__badge">Tạm ngưng</span>

      <h2>{shop.name}</h2>

      <p>
        Shop hiện đang bị tạm ngưng hoạt động. Vui lòng liên hệ quản trị viên để
        biết thêm thông tin.
      </p>

      <ShopInformation shop={shop} />
    </section>
  );
}

function ShopInformation({ shop }) {
  return (
    <dl className="shop-registration-info">
      <div>
        <dt>Số điện thoại</dt>

        <dd>{shop.phone || "—"}</dd>
      </div>

      <div>
        <dt>Email</dt>

        <dd>{shop.email || "—"}</dd>
      </div>

      <div>
        <dt>Địa chỉ lấy hàng</dt>

        <dd>{shop.pickupAddress || "—"}</dd>
      </div>

      <div>
        <dt>Trạng thái</dt>

        <dd>{shop.statusName ?? shop.status}</dd>
      </div>
    </dl>
  );
}

export default ShopRegistrationPage;
