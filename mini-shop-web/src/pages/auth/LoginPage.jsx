import { useState } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";

import authApi from "../../api/authApi";
import { API_BASE_URL } from "../../api/httpClients";
import { useAuth } from "../../contexts/AuthContext";
import LoadingSpinner from "../../components/common/LoadingSpinner";

import "./LoginPage.css";

import {
  PENDING_ACTION_TYPES,
  getPendingProductAction,
  clearPendingProductAction,
} from "../../utils/pendingProductAction";

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();

  const { isAuthenticated, login } = useAuth();

  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((currentFormData) => ({
      ...currentFormData,
      [name]: value,
    }));

    if (error) {
      setError("");
    }
  };

  const validateForm = () => {
    const email = formData.email.trim();

    if (!email) {
      return "Email không được bỏ trống.";
    }

    if (!email.includes("@")) {
      return "Email không hợp lệ.";
    }

    if (!formData.password) {
      return "Mật khẩu không được bỏ trống.";
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
      setLoading(true);
      setError("");

      const loginRequest = {
        email: formData.email.trim(),
        password: formData.password,
      };

      const response = await authApi.login(loginRequest);

      const apiResponse = response.data;
      const loginData = apiResponse?.data;

      if (!apiResponse?.success) {
        setError(apiResponse?.message ?? "Đăng nhập không thành công.");
        return;
      }

      if (!loginData?.accessToken) {
        setError("Backend không trả về access token.");
        return;
      }

      if (!loginData?.refreshToken) {
        setError("Backend không trả về refresh token.");
        return;
      }

      login(loginData);

      const pendingAction = getPendingProductAction();

      if (pendingAction) {
        clearPendingProductAction();

        if (pendingAction.type === PENDING_ACTION_TYPES.BUY_NOW) {
          navigate("/checkout", {
            replace: true,
            state: {
              type: "BUY_NOW",
              items: [
                {
                  productId: pendingAction.productId,
                  variantId: pendingAction.variantId ?? null,
                  quantity: pendingAction.quantity ?? 1,
                },
              ],
            },
          });

          return;
        }

        if (pendingAction.type === PENDING_ACTION_TYPES.ADD_TO_CART) {
          console.log("ADD_TO_CART", {
            productId: pendingAction.productId,
          });

          navigate("/products", {
            replace: true,
          });

          return;
        }
      }

      const redirectPath = location.state?.from?.pathname ?? "/";

      navigate(redirectPath, {
        replace: true,
      });
    } catch (err) {
      console.error("Login failed:", err);

      const status = err.response?.status;
      const backendMessage = err.response?.data?.message;

      if (status === 400) {
        setError(backendMessage ?? "Dữ liệu đăng nhập không hợp lệ.");
        return;
      }

      if (status === 401 || status === 403) {
        setError(backendMessage ?? "Email hoặc mật khẩu không đúng.");
        return;
      }

      setError(backendMessage ?? "Không thể đăng nhập. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = () => {
    window.location.href = `${API_BASE_URL}/oauth2/authorization/google`;
  };

  const handleFacebookLogin = () => {
    window.location.href = `${API_BASE_URL}/oauth2/authorization/facebook`;
  };

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  return (
    <main className="login-page">
      <section className="login-page__card">
        <h1>Đăng nhập</h1>

        <form onSubmit={handleSubmit}>
          <div>
            <label htmlFor="email">Email</label>

            <input
              id="email"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              autoComplete="email"
              placeholder="customer@example.com"
              disabled={loading}
            />
          </div>

          <div>
            <label htmlFor="password">Mật khẩu</label>

            <input
              id="password"
              name="password"
              type="password"
              value={formData.password}
              onChange={handleChange}
              autoComplete="current-password"
              placeholder="Nhập mật khẩu"
              disabled={loading}
            />
          </div>

          {error && (
            <p className="login-page__error" role="alert">
              {error}
            </p>
          )}

          <button type="submit" disabled={loading}>
            {loading ? (
              <LoadingSpinner size="small" inline variant="light" />
            ) : (
              "Đăng nhập"
            )}
          </button>
        </form>

        <div className="oauth-divider">
          <span>OR</span>
        </div>

        <div className="login-page__oauth-buttons">
          <button
            type="button"
            className="login-page__oauth-button login-page__oauth-button--facebook"
            onClick={handleFacebookLogin}
            disabled={loading}
          >
            <span className="oauth-icon oauth-icon--facebook">f</span>
            <span>Facebook</span>
          </button>

          <button
            type="button"
            className="login-page__oauth-button login-page__oauth-button--google"
            onClick={handleGoogleLogin}
            disabled={loading}
          >
            <span className="oauth-icon oauth-icon--google">G</span>
            <span>Google</span>
          </button>
        </div>

        <p>
          Chưa có tài khoản? <Link to="/register">Đăng ký</Link>
        </p>
      </section>
    </main>
  );
}

export default LoginPage;
