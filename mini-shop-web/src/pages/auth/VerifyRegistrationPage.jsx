import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";

import authApi from "../../api/authApi";

function VerifyRegistrationPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const [status, setStatus] = useState("loading");
  const [message, setMessage] = useState("");

  useEffect(() => {
    const verify = async () => {
      const token = searchParams.get("token");

      if (!token) {
        setStatus("error");
        setMessage("Liên kết xác minh không hợp lệ.");
        return;
      }

      try {
        const response = await authApi.verifyRegistrationEmail(token);
        const data = response.data;

        if (!data?.success) {
          setStatus("error");
          setMessage(data?.message ?? "Xác minh email không thành công.");
          return;
        }

        const verifiedEmail = data?.data;

        if (!verifiedEmail) {
          setStatus("error");
          setMessage("Xác minh thành công nhưng không nhận được email.");
          return;
        }

        sessionStorage.setItem("registrationVerifiedEmail", verifiedEmail);

        setStatus("success");
        setMessage("Xác minh email thành công.");

        setTimeout(() => {
          navigate("/register?verified=true", {
            replace: true,
          });
        }, 1000);
      } catch (err) {
        console.error("Verify registration email failed:", err);

        setStatus("error");

        setMessage(
          err.response?.data?.message ??
            "Không thể xác minh email. Liên kết có thể đã hết hạn.",
        );
      }
    };

    verify();
  }, [navigate, searchParams]);

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

            <p>Đang chuyển sang bước hoàn tất đăng ký...</p>
          </>
        )}

        {status === "error" && (
          <>
            <h1>Xác minh thất bại</h1>

            <p className="register-page__error" role="alert">
              {message}
            </p>

            <Link to="/register">Quay lại đăng ký</Link>
          </>
        )}
      </section>
    </main>
  );
}

export default VerifyRegistrationPage;
