import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";

import authApi from "../../api/authApi";
import { API_BASE_URL } from "../../api/httpClients";
import LoadingSpinner from "../../components/common/LoadingSpinner";

import "./RegisterPage.css";

function RegisterPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const [step, setStep] = useState(1);

  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    password: "",
    phone: "",
  });

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const verified = searchParams.get("verified");
    const verifiedEmail = sessionStorage.getItem("registrationVerifiedEmail");

    if (verified === "true" && verifiedEmail) {
      setFormData((previous) => ({
        ...previous,
        email: verifiedEmail,
      }));

      setStep(2);
      setSuccess(
        "Email đã được xác minh. Vui lòng hoàn tất thông tin đăng ký.",
      );
    }
  }, [searchParams]);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const handleRequestVerification = async (event) => {
    event.preventDefault();

    setError("");
    setSuccess("");

    const email = formData.email.trim();

    if (!email) {
      setError("Vui lòng nhập email.");
      return;
    }

    try {
      setLoading(true);

      const response = await authApi.requestRegistrationEmail(email);
      const data = response.data;

      if (!data?.success) {
        setError(data?.message ?? "Không thể gửi email xác minh.");
        return;
      }

      setSuccess(
        "Đã gửi email xác minh. Vui lòng kiểm tra hộp thư và nhấn vào liên kết xác minh.",
      );

      sessionStorage.setItem("registrationPendingEmail", email);
    } catch (err) {
      console.error("Request registration email failed:", err);

      const backendMessage = err.response?.data?.message;

      setError(
        backendMessage ?? "Không thể gửi email xác minh. Vui lòng thử lại.",
      );
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (event) => {
    event.preventDefault();

    setError("");
    setSuccess("");

    if (!formData.fullName.trim()) {
      setError("Vui lòng nhập họ tên.");
      return;
    }

    if (!formData.password) {
      setError("Vui lòng nhập mật khẩu.");
      return;
    }

    if (formData.password.length < 3) {
      setError("Mật khẩu phải có ít nhất 3 ký tự.");
      return;
    }

    try {
      setLoading(true);

      const response = await authApi.register({
        fullName: formData.fullName.trim(),
        email: formData.email.trim(),
        password: formData.password,
        phone: formData.phone.trim(),
      });

      const data = response.data;

      if (!data?.success) {
        setError(data?.message ?? "Đăng ký không thành công.");
        return;
      }

      sessionStorage.removeItem("registrationPendingEmail");

      sessionStorage.removeItem("registrationVerifiedEmail");

      setSuccess("Đăng ký thành công. Vui lòng đăng nhập để tiếp tục.");

      setTimeout(() => {
        navigate("/login", {
          replace: true,
        });
      }, 800);
    } catch (err) {
      console.error("Register failed:", err);

      const status = err.response?.status;
      const backendMessage = err.response?.data?.message;

      if (status === 400) {
        setError(backendMessage ?? "Thông tin đăng ký không hợp lệ.");
        return;
      }

      setError(backendMessage ?? "Không thể đăng ký. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleRegister = () => {
    window.location.href = `${API_BASE_URL}/oauth2/authorization/google`;
  };

  const handleFacebookRegister = () => {
    window.location.href = `${API_BASE_URL}/oauth2/authorization/facebook`;
  };

  return (
    <main className="register-page">
      <section className="register-page__card">
        <h1>Đăng ký</h1>
        {step === 1 && (
          <form onSubmit={handleRequestVerification}>
            <div>
              <label htmlFor="email">Email</label>

              <input
                id="email"
                name="email"
                type="email"
                value={formData.email}
                onChange={handleChange}
                autoComplete="email"
                placeholder="Nhập email của bạn"
                disabled={loading}
                required
              />
            </div>

            {error && (
              <p className="register-page__error" role="alert">
                {error}
              </p>
            )}

            {success && (
              <p className="register-page__success" role="status">
                {success}
              </p>
            )}

            <button type="submit" disabled={loading}>
              {loading ? (
                <LoadingSpinner size="small" inline variant="light" />
              ) : (
                "Tiếp theo"
              )}
            </button>
          </form>
        )}

        {step === 2 && (
          <form onSubmit={handleRegister}>
            <div>
              <label htmlFor="email">Email</label>

              <input
                id="email"
                name="email"
                type="email"
                value={formData.email}
                readOnly
              />
            </div>

            <div>
              <label htmlFor="fullName">Họ và tên</label>

              <input
                id="fullName"
                name="fullName"
                type="text"
                value={formData.fullName}
                onChange={handleChange}
                autoComplete="name"
                disabled={loading}
                required
              />
            </div>

            <div>
              <label htmlFor="phone">Số điện thoại</label>

              <input
                id="phone"
                name="phone"
                type="tel"
                value={formData.phone}
                onChange={handleChange}
                autoComplete="tel"
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
                autoComplete="new-password"
                disabled={loading}
                required
              />
            </div>

            {error && (
              <p className="register-page__error" role="alert">
                {error}
              </p>
            )}

            {success && (
              <p className="register-page__success" role="status">
                {success}
              </p>
            )}

            <button type="submit" disabled={loading}>
              {loading ? (
                <LoadingSpinner size="small" inline variant="light" />
              ) : (
                "Đăng ký"
              )}
            </button>
          </form>
        )}

        {step === 1 && (
          <>
            <div className="oauth-divider">
              <span>Hoặc</span>
            </div>

            <div className="register-page__oauth-buttons">
              <button
                type="button"
                className="register-page__oauth-button register-page__oauth-button--facebook"
                onClick={handleFacebookRegister}
                disabled={loading}
              >
                <span className="oauth-icon oauth-icon--facebook">f</span>
                <span>Facebook</span>
              </button>

              <button
                type="button"
                className="register-page__oauth-button register-page__oauth-button--google"
                onClick={handleGoogleRegister}
                disabled={loading}
              >
                <span className="oauth-icon oauth-icon--google">G</span>
                <span>Google</span>
              </button>
            </div>
          </>
        )}

        <p>
          Đã có tài khoản? <Link to="/login">Đăng nhập</Link>
        </p>
      </section>
    </main>
  );
}

export default RegisterPage;
