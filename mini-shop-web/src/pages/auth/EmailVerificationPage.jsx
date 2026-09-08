import { useEffect, useRef, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";

import authApi from "../../api/authApi";

function EmailVerificationPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const [status, setStatus] = useState("loading");
  const [message, setMessage] = useState("");
  const [token, setToken] = useState("");
  const [resending, setResending] = useState(false);
  const [resendSuccess, setResendSuccess] = useState(false);
  const hasVerifiedRef = useRef(false);

  useEffect(() => {
    if (hasVerifiedRef.current) {
      return;
    }

    hasVerifiedRef.current = true;

    const verify = async () => {
      const currentToken = searchParams.get("token");

      if (!currentToken) {
        setStatus("error");
        setMessage("Liên kết xác minh không hợp lệ.");
        return;
      }

      setToken(currentToken);

      try {
        const response = await authApi.verifyCurrentUserEmail(currentToken);

        const data = response.data;

        if (!data?.success) {
          setStatus("error");
          setMessage(data?.message ?? "Xác minh email không thành công.");
          return;
        }

        setStatus("success");
        setMessage(data?.message ?? "Xác minh email thành công.");

        setTimeout(() => {
          navigate("/account", {
            replace: true,
          });
        }, 1500);
      } catch (err) {
        console.error("Verify current user email failed:", err);

        setStatus("error");

        setMessage(
          err.response?.data?.message ??
            "Không thể xác minh email. Liên kết có thể đã hết hạn.",
        );
      }
    };

    verify();
  }, [navigate, searchParams]);

  const handleResend = async () => {
    if (!token || resending) {
      return;
    }

    try {
      setResending(true);

      const response = await authApi.resendCurrentUserEmailVerification(token);

      const data = response.data;

      if (!data?.success) {
        setMessage(data?.message ?? "Không thể gửi lại email xác minh.");
        return;
      }

      setResendSuccess(true);
      setMessage(data?.message ?? "Đã gửi lại email xác minh.");
    } catch (err) {
      console.error("Resend email verification failed:", err);

      setMessage(
        err.response?.data?.message ?? "Không thể gửi lại email xác minh.",
      );
    } finally {
      setResending(false);
    }
  };

  return (
    <main className="register-page">
      <section className="register-page__card">
        {status === "loading" && (
          <>
            <h1>Đang xác minh email</h1>

            <p className="register-page__description">
              Vui lòng chờ trong giây lát...
            </p>
          </>
        )}

        {status === "success" && (
          <>
            <h1>Xác minh thành công</h1>

            <p className="register-page__success" role="status">
              {message}
            </p>

            <p>Đang chuyển về trang thông tin tài khoản...</p>
          </>
        )}

        {status === "error" && (
          <>
            <h1>{resendSuccess ? "Đã gửi lại email" : "Xác minh thất bại"}</h1>

            <p
              className={
                resendSuccess
                  ? "register-page__success"
                  : "register-page__error"
              }
              role="alert"
            >
              {message}
            </p>

            {!resendSuccess && message === "Link xác minh email đã hết hạn" && (
              <button type="button" onClick={handleResend} disabled={resending}>
                {resending ? "Đang gửi..." : "Gửi lại email xác minh"}
              </button>
            )}

            {resendSuccess && (
              <p>Vui lòng kiểm tra hộp thư và sử dụng liên kết xác minh mới.</p>
            )}

            <Link to="/account">Quay lại thông tin tài khoản</Link>
          </>
        )}
      </section>
    </main>
  );
}

export default EmailVerificationPage;
