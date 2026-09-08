import { useEffect, useState } from "react";

import dashboardApi from "../../../../api/dashboardApi";

import LoadingSpinner from "../../../common/LoadingSpinner";

import "./AdminUsers.css";

function formatDateTime(value) {
  if (!value) {
    return "-";
  }

  return new Date(value).toLocaleString("vi-VN");
}

function getRoleLabel(role) {
  const labels = {
    ADMIN: "Quản trị viên",
    SELLER: "Người bán",
    CUSTOMER: "Khách hàng",
  };

  return labels[role] ?? role ?? "-";
}

function getProviderLabel(provider) {
  const labels = {
    LOCAL: "Tài khoản thường",
    GOOGLE: "Google",
    FACEBOOK: "Facebook",
  };

  return labels[provider] ?? provider ?? "-";
}

const EMPTY_FILTERS = {
  keyword: "",
  role: "",
  active: "",
  emailVerified: "",
  authProvider: "",
};

function AdminUsers() {
  const [users, setUsers] = useState([]);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [appliedFilters, setAppliedFilters] = useState(EMPTY_FILTERS);

  useEffect(() => {
    let cancelled = false;

    const loadUsers = async () => {
      try {
        setLoading(true);
        setError("");

        const params = {
          page,
          size: 10,
        };

        const keyword = appliedFilters.keyword.trim();

        if (keyword) {
          params.keyword = keyword;
        }

        if (appliedFilters.role) {
          params.role = appliedFilters.role;
        }

        if (appliedFilters.active !== "") {
          params.active = appliedFilters.active === "true";
        }

        if (appliedFilters.emailVerified !== "") {
          params.emailVerified = appliedFilters.emailVerified === "true";
        }

        if (appliedFilters.authProvider) {
          params.authProvider = appliedFilters.authProvider;
        }

        const response = await dashboardApi.getAdminUsers(params);

        const apiResponse = response.data;

        if (!apiResponse?.success || !apiResponse?.data) {
          throw new Error(
            apiResponse?.message ?? "Không thể tải danh sách người dùng.",
          );
        }

        if (!cancelled) {
          setUsers(apiResponse.data.content ?? []);
          setTotalPages(apiResponse.data.totalPages ?? 0);
          setTotalElements(apiResponse.data.totalElements ?? 0);
        }
      } catch (err) {
        console.error("Unable to load admin users:", err);

        if (!cancelled) {
          setError(
            err.response?.data?.message ??
              err.message ??
              "Không thể tải danh sách người dùng.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    loadUsers();

    return () => {
      cancelled = true;
    };
  }, [page, appliedFilters]);

  const handleFilterChange = (event) => {
    const { name, value } = event.target;

    setFilters((current) => ({
      ...current,
      [name]: value,
    }));
  };

  const handleFilterSubmit = (event) => {
    event.preventDefault();

    setPage(0);

    setAppliedFilters({
      ...filters,
    });
  };

  const handleFilterReset = () => {
    const resetFilters = {
      ...EMPTY_FILTERS,
    };

    setFilters(resetFilters);
    setAppliedFilters(resetFilters);
    setPage(0);
  };

  return (
    <section className="admin-users">
      <div className="admin-users__heading">
        <div>
          <h1>Người dùng</h1>

          <p>Theo dõi và tìm kiếm tài khoản người dùng trên toàn hệ thống.</p>
        </div>
      </div>

      <section className="admin-users__panel">
        <div className="admin-users__panel-heading">
          <div>
            <h2>Bộ lọc người dùng</h2>

            <p>
              Tìm theo tên, email, vai trò, trạng thái và phương thức đăng nhập.
            </p>
          </div>
        </div>

        <form className="admin-users__filters" onSubmit={handleFilterSubmit}>
          <div className="admin-users__filter-field">
            <label htmlFor="user-keyword">Tìm kiếm</label>

            <input
              id="user-keyword"
              type="text"
              name="keyword"
              value={filters.keyword}
              onChange={handleFilterChange}
              placeholder="Tên hoặc email..."
            />
          </div>

          <div className="admin-users__filter-field">
            <label htmlFor="user-role">Vai trò</label>

            <select
              id="user-role"
              name="role"
              value={filters.role}
              onChange={handleFilterChange}
            >
              <option value="">Tất cả</option>
              <option value="CUSTOMER">Khách hàng</option>
              <option value="SELLER">Người bán</option>
              <option value="ADMIN">Quản trị viên</option>
            </select>
          </div>

          <div className="admin-users__filter-field">
            <label htmlFor="user-active">Trạng thái tài khoản</label>

            <select
              id="user-active"
              name="active"
              value={filters.active}
              onChange={handleFilterChange}
            >
              <option value="">Tất cả</option>
              <option value="true">Đang hoạt động</option>
              <option value="false">Đã vô hiệu hóa</option>
            </select>
          </div>

          <div className="admin-users__filter-field">
            <label htmlFor="user-verified">Xác minh email</label>

            <select
              id="user-verified"
              name="emailVerified"
              value={filters.emailVerified}
              onChange={handleFilterChange}
            >
              <option value="">Tất cả</option>
              <option value="true">Đã xác minh</option>
              <option value="false">Chưa xác minh</option>
            </select>
          </div>

          <div className="admin-users__filter-field">
            <label htmlFor="auth-provider">Phương thức đăng nhập</label>

            <select
              id="auth-provider"
              name="authProvider"
              value={filters.authProvider}
              onChange={handleFilterChange}
            >
              <option value="">Tất cả</option>
              <option value="LOCAL">Tài khoản thường</option>
              <option value="GOOGLE">Google</option>
              <option value="FACEBOOK">Facebook</option>
            </select>
          </div>

          <div className="admin-users__filter-actions">
            <button type="submit" className="admin-users__filter-submit">
              Áp dụng
            </button>

            <button
              type="button"
              className="admin-users__filter-reset"
              onClick={handleFilterReset}
            >
              Đặt lại
            </button>
          </div>
        </form>
      </section>

      <section className="admin-users__panel admin-users__list-panel">
        <div className="admin-users__panel-heading">
          <div>
            <h2>Danh sách người dùng</h2>

            <p>Tổng cộng {totalElements} tài khoản phù hợp.</p>
          </div>
        </div>

        {loading && (
          <div className="admin-users__state">
            <LoadingSpinner size="medium" />
          </div>
        )}

        {!loading && error && <div className="admin-users__error">{error}</div>}

        {!loading && !error && (
          <div className="admin-users__table-wrapper">
            <table className="admin-users__table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Người dùng</th>
                  <th>Số điện thoại</th>
                  <th>Vai trò</th>
                  <th>Trạng thái</th>
                  <th>Email</th>
                  <th>Đăng nhập</th>
                  <th>Ngày tạo</th>
                </tr>
              </thead>

              <tbody>
                {users.length > 0 ? (
                  users.map((user) => (
                    <tr key={user.id}>
                      <td>
                        <strong>#{user.id}</strong>
                      </td>

                      <td>
                        <div className="admin-users__identity">
                          <strong>{user.fullName ?? "-"}</strong>

                          <small>{user.email ?? "-"}</small>
                        </div>
                      </td>

                      <td>{user.phone || "-"}</td>

                      <td>
                        <span
                          className={`admin-users__role admin-users__role--${String(
                            user.role ?? "",
                          ).toLowerCase()}`}
                        >
                          {getRoleLabel(user.role)}
                        </span>
                      </td>

                      <td>
                        <span
                          className={
                            user.active
                              ? "admin-users__status admin-users__status--active"
                              : "admin-users__status admin-users__status--inactive"
                          }
                        >
                          {user.active ? "Hoạt động" : "Vô hiệu hóa"}
                        </span>
                      </td>

                      <td>
                        <span
                          className={
                            user.emailVerified
                              ? "admin-users__verified admin-users__verified--yes"
                              : "admin-users__verified admin-users__verified--no"
                          }
                        >
                          {user.emailVerified ? "Đã xác minh" : "Chưa xác minh"}
                        </span>
                      </td>

                      <td>{getProviderLabel(user.authProvider)}</td>

                      <td>{formatDateTime(user.createdAt)}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="8">
                      <div className="admin-users__empty">
                        Không tìm thấy người dùng phù hợp.
                      </div>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}

        {!loading && !error && totalPages > 1 && (
          <div className="admin-users__pagination">
            <button
              type="button"
              disabled={page === 0}
              onClick={() => setPage((current) => Math.max(current - 1, 0))}
            >
              Trang trước
            </button>

            <span>
              Trang {page + 1} / {totalPages}
            </span>

            <button
              type="button"
              disabled={page + 1 >= totalPages}
              onClick={() => setPage((current) => current + 1)}
            >
              Trang sau
            </button>
          </div>
        )}
      </section>
    </section>
  );
}

export default AdminUsers;
