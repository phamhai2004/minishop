import { useCallback, useEffect, useState } from "react";

import adminShopApi from "../../../api/adminShopApi";
import LoadingSpinner from "../../../components/common/LoadingSpinner";

import "./AdminShopManagementPage.css";

const STATUS_OPTIONS = [
  {
    value: "",
    label: "Tất cả",
  },
  {
    value: "PENDING",
    label: "Chờ duyệt",
  },
  {
    value: "ACTIVE",
    label: "Đang hoạt động",
  },
  {
    value: "REJECTED",
    label: "Bị từ chối",
  },
  {
    value: "SUSPENDED",
    label: "Tạm ngưng",
  },
];

const STATUS_LABELS = {
  PENDING: "Chờ duyệt",
  ACTIVE: "Đang hoạt động",
  REJECTED: "Bị từ chối",
  SUSPENDED: "Tạm ngưng",
};

function formatDateTime(value) {
  if (!value) {
    return "—";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString("vi-VN");
}

function AdminShopManagementPage() {
  const [shops, setShops] = useState([]);

  const [status, setStatus] = useState("PENDING");

  const [page, setPage] = useState(0);

  const [totalPages, setTotalPages] = useState(0);

  const [totalElements, setTotalElements] = useState(0);

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState("");

  const [success, setSuccess] = useState("");

  const [selectedShop, setSelectedShop] = useState(null);

  const [detailLoading, setDetailLoading] = useState(false);

  const [rejectingShop, setRejectingShop] = useState(null);

  const [rejectReason, setRejectReason] = useState("");

  const [actionLoading, setActionLoading] = useState(false);

  const loadShops = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const params = {
        page,
        size: 10,
      };

      if (status) {
        params.status = status;
      }

      const response = await adminShopApi.getShops(params);

      const apiResponse = response.data;
      const pageData = apiResponse?.data;

      if (!apiResponse?.success || !pageData) {
        throw new Error(
          apiResponse?.message ?? "Không thể tải danh sách shop.",
        );
      }

      setShops(Array.isArray(pageData.content) ? pageData.content : []);

      setTotalPages(Number(pageData.totalPages ?? 0));

      setTotalElements(Number(pageData.totalElements ?? 0));
    } catch (err) {
      console.error("Unable to load admin shops:", err);

      setError(
        err.response?.data?.message ??
          err.message ??
          "Không thể tải danh sách shop.",
      );
    } finally {
      setLoading(false);
    }
  }, [page, status]);

  useEffect(() => {
    void loadShops();
  }, [loadShops]);

  const handleStatusChange = (nextStatus) => {
    setStatus(nextStatus);
    setPage(0);

    setSelectedShop(null);
    setRejectingShop(null);
    setSuccess("");
    setError("");
  };

  const handleViewDetail = async (shopId) => {
    try {
      setDetailLoading(true);
      setError("");

      const response = await adminShopApi.getShopById(shopId);

      const apiResponse = response.data;

      if (!apiResponse?.success || !apiResponse?.data) {
        throw new Error(apiResponse?.message ?? "Không thể tải chi tiết shop.");
      }

      setSelectedShop(apiResponse.data);
    } catch (err) {
      console.error("Unable to load shop detail:", err);

      setError(
        err.response?.data?.message ??
          err.message ??
          "Không thể tải chi tiết shop.",
      );
    } finally {
      setDetailLoading(false);
    }
  };

  const handleApprove = async (shop) => {
    const confirmed = window.confirm(
      `Bạn có chắc muốn duyệt shop "${shop.name}" không?`,
    );

    if (!confirmed) {
      return;
    }

    try {
      setActionLoading(true);
      setError("");
      setSuccess("");

      const response = await adminShopApi.approve(shop.id);

      const apiResponse = response.data;

      if (!apiResponse?.success) {
        throw new Error(apiResponse?.message ?? "Duyệt shop thất bại.");
      }

      setSuccess(apiResponse.message ?? `Đã duyệt shop "${shop.name}".`);

      setSelectedShop(null);

      await loadShops();
    } catch (err) {
      console.error("Unable to approve shop:", err);

      setError(
        err.response?.data?.message ?? err.message ?? "Duyệt shop thất bại.",
      );
    } finally {
      setActionLoading(false);
    }
  };

  const openRejectDialog = (shop) => {
    setRejectingShop(shop);
    setRejectReason("");
    setError("");
    setSuccess("");
  };

  const closeRejectDialog = () => {
    if (actionLoading) {
      return;
    }

    setRejectingShop(null);
    setRejectReason("");
  };

  const handleReject = async (event) => {
    event.preventDefault();

    const reason = rejectReason.trim();

    if (!reason) {
      setError("Vui lòng nhập lý do từ chối.");
      return;
    }

    if (reason.length > 500) {
      setError("Lý do từ chối không được vượt quá 500 ký tự.");
      return;
    }

    try {
      setActionLoading(true);
      setError("");
      setSuccess("");

      const response = await adminShopApi.reject(rejectingShop.id, reason);

      const apiResponse = response.data;

      if (!apiResponse?.success) {
        throw new Error(apiResponse?.message ?? "Từ chối shop thất bại.");
      }

      setSuccess(`Đã từ chối shop "${rejectingShop.name}".`);

      setRejectingShop(null);
      setRejectReason("");
      setSelectedShop(null);

      await loadShops();
    } catch (err) {
      console.error("Unable to reject shop:", err);

      setError(
        err.response?.data?.message ?? err.message ?? "Từ chối shop thất bại.",
      );
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <main className="admin-shop-management-page">
      <div className="admin-shop-management-page__header">
        <div>
          <h1>Quản lý shop</h1>

          <p>Xem và xét duyệt các yêu cầu mở shop trên Hair.</p>
        </div>

        <div className="admin-shop-management-page__total">
          Tổng: <strong>{totalElements}</strong>
        </div>
      </div>

      <div className="admin-shop-management-page__filters">
        {STATUS_OPTIONS.map((option) => (
          <button
            key={option.value || "ALL"}
            type="button"
            className={
              status === option.value
                ? "admin-shop-management-page__filter active"
                : "admin-shop-management-page__filter"
            }
            onClick={() => handleStatusChange(option.value)}
          >
            {option.label}
          </button>
        ))}
      </div>

      {error && (
        <div className="admin-shop-management-page__alert admin-shop-management-page__alert--error">
          {error}
        </div>
      )}

      {success && (
        <div className="admin-shop-management-page__alert admin-shop-management-page__alert--success">
          {success}
        </div>
      )}

      {loading ? (
        <div className="admin-shop-management-page__state">
          <LoadingSpinner size="medium" />
        </div>
      ) : shops.length === 0 ? (
        <div className="admin-shop-management-page__empty">
          Không có shop phù hợp với bộ lọc hiện tại.
        </div>
      ) : (
        <>
          <div className="admin-shop-management-page__table-wrapper">
            <table className="admin-shop-management-page__table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Tên shop</th>
                  <th>Chủ shop</th>
                  <th>Số điện thoại</th>
                  <th>Ngày đăng ký</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>

              <tbody>
                {shops.map((shop) => (
                  <tr key={shop.id}>
                    <td>#{shop.id}</td>

                    <td>
                      <strong>{shop.name}</strong>
                    </td>

                    <td>{shop.ownerName || "—"}</td>

                    <td>{shop.phone || "—"}</td>

                    <td>{formatDateTime(shop.createdAt)}</td>

                    <td>
                      <span
                        className={`admin-shop-management-page__status admin-shop-management-page__status--${String(
                          shop.status,
                        ).toLowerCase()}`}
                      >
                        {shop.statusName ??
                          STATUS_LABELS[shop.status] ??
                          shop.status}
                      </span>
                    </td>

                    <td>
                      <div className="admin-shop-management-page__actions">
                        <button
                          type="button"
                          className="admin-shop-management-page__button admin-shop-management-page__button--view"
                          onClick={() => handleViewDetail(shop.id)}
                        >
                          Chi tiết
                        </button>

                        {shop.status === "PENDING" && (
                          <>
                            <button
                              type="button"
                              className="admin-shop-management-page__button admin-shop-management-page__button--approve"
                              disabled={actionLoading}
                              onClick={() => handleApprove(shop)}
                            >
                              Duyệt
                            </button>

                            <button
                              type="button"
                              className="admin-shop-management-page__button admin-shop-management-page__button--reject"
                              disabled={actionLoading}
                              onClick={() => openRejectDialog(shop)}
                            >
                              Từ chối
                            </button>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {totalPages > 1 && (
            <div className="admin-shop-management-page__pagination">
              <button
                type="button"
                disabled={page <= 0}
                onClick={() => setPage((current) => current - 1)}
              >
                Trước
              </button>

              <span>
                Trang {page + 1} / {totalPages}
              </span>

              <button
                type="button"
                disabled={page + 1 >= totalPages}
                onClick={() => setPage((current) => current + 1)}
              >
                Sau
              </button>
            </div>
          )}
        </>
      )}

      {selectedShop && (
        <ShopDetailModal
          shop={selectedShop}
          loading={detailLoading}
          actionLoading={actionLoading}
          onClose={() => setSelectedShop(null)}
          onApprove={handleApprove}
          onReject={openRejectDialog}
        />
      )}

      {rejectingShop && (
        <RejectShopModal
          shop={rejectingShop}
          reason={rejectReason}
          submitting={actionLoading}
          onReasonChange={setRejectReason}
          onSubmit={handleReject}
          onClose={closeRejectDialog}
        />
      )}
    </main>
  );
}

function ShopDetailModal({
  shop,
  actionLoading,
  onClose,
  onApprove,
  onReject,
}) {
  return (
    <div
      className="admin-shop-modal"
      role="presentation"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose();
        }
      }}
    >
      <section className="admin-shop-modal__content">
        <div className="admin-shop-modal__header">
          <div>
            <h2>Chi tiết shop</h2>

            <p>Hồ sơ đăng ký #{shop.id}</p>
          </div>

          <button
            type="button"
            className="admin-shop-modal__close"
            onClick={onClose}
          >
            ×
          </button>
        </div>

        <div className="admin-shop-modal__status-row">
          <span
            className={`admin-shop-management-page__status admin-shop-management-page__status--${String(
              shop.status,
            ).toLowerCase()}`}
          >
            {shop.statusName ?? STATUS_LABELS[shop.status] ?? shop.status}
          </span>
        </div>

        <dl className="admin-shop-modal__info">
          <div>
            <dt>Tên shop</dt>
            <dd>{shop.name}</dd>
          </div>

          <div>
            <dt>Chủ shop</dt>
            <dd>{shop.ownerName || "—"}</dd>
          </div>

          <div>
            <dt>Email</dt>
            <dd>{shop.email || "—"}</dd>
          </div>

          <div>
            <dt>Số điện thoại</dt>
            <dd>{shop.phone || "—"}</dd>
          </div>

          <div className="admin-shop-modal__info-full">
            <dt>Địa chỉ lấy hàng</dt>
            <dd>{shop.pickupAddress || "—"}</dd>
          </div>

          <div className="admin-shop-modal__info-full">
            <dt>Mô tả</dt>
            <dd>{shop.description || "Chưa có mô tả"}</dd>
          </div>

          <div>
            <dt>Ngày đăng ký</dt>
            <dd>{formatDateTime(shop.createdAt)}</dd>
          </div>

          <div>
            <dt>Ngày được duyệt</dt>
            <dd>{formatDateTime(shop.approvedAt)}</dd>
          </div>

          {shop.rejectionReason && (
            <div className="admin-shop-modal__info-full admin-shop-modal__reason">
              <dt>Lý do từ chối</dt>

              <dd>{shop.rejectionReason}</dd>
            </div>
          )}
        </dl>

        {shop.status === "PENDING" && (
          <div className="admin-shop-modal__actions">
            <button
              type="button"
              className="admin-shop-management-page__button admin-shop-management-page__button--approve"
              disabled={actionLoading}
              onClick={() => onApprove(shop)}
            >
              Duyệt shop
            </button>

            <button
              type="button"
              className="admin-shop-management-page__button admin-shop-management-page__button--reject"
              disabled={actionLoading}
              onClick={() => onReject(shop)}
            >
              Từ chối
            </button>
          </div>
        )}
      </section>
    </div>
  );
}

function RejectShopModal({
  shop,
  reason,
  submitting,
  onReasonChange,
  onSubmit,
  onClose,
}) {
  return (
    <div className="admin-shop-modal" role="presentation">
      <form
        className="admin-shop-modal__content admin-shop-modal__content--small"
        onSubmit={onSubmit}
      >
        <div className="admin-shop-modal__header">
          <div>
            <h2>Từ chối shop</h2>

            <p>{shop.name}</p>
          </div>

          <button
            type="button"
            className="admin-shop-modal__close"
            onClick={onClose}
            disabled={submitting}
          >
            ×
          </button>
        </div>

        <label className="admin-shop-modal__field">
          <span>Lý do từ chối *</span>

          <textarea
            value={reason}
            onChange={(event) => onReasonChange(event.target.value)}
            rows={5}
            maxLength={500}
            placeholder="Ví dụ: Địa chỉ lấy hàng chưa đầy đủ..."
            required
          />

          <small>{reason.length}/500</small>
        </label>

        <div className="admin-shop-modal__actions">
          <button
            type="button"
            className="admin-shop-management-page__button admin-shop-management-page__button--view"
            onClick={onClose}
            disabled={submitting}
          >
            Hủy
          </button>

          <button
            type="submit"
            className="admin-shop-management-page__button admin-shop-management-page__button--reject"
            disabled={submitting}
          >
            {submitting ? (
              <LoadingSpinner size="small" inline variant="light" />
            ) : (
              "Xác nhận từ chối"
            )}
          </button>
        </div>
      </form>
    </div>
  );
}

export default AdminShopManagementPage;
