import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import paymentApi from "../../api/paymentApi";
import LoadingSpinner from "../../components/common/LoadingSpinner";

import "./PaymentResultPage.css";

function formatCurrency(value) {
  return `${Number(value ?? 0).toLocaleString("vi-VN")} ₫`;
}

function formatDateTime(value) {
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

function getPaymentStatus(payment) {
  return String(
    payment?.status ?? payment?.transactionStatus ?? "",
  ).toUpperCase();
}

function getStatusInfo(status) {
  switch (status) {
    case "SUCCESS":
      return {
        type: "success",
        icon: "✓",
        title: "Thanh toán thành công",
        description: "Đơn hàng của bạn đã được thanh toán thành công.",
      };

    case "EXPIRED":
      return {
        type: "expired",
        icon: "!",
        title: "Thanh toán đã hết hạn",
        description: "Giao dịch đã hết thời gian thanh toán.",
      };

    case "FAILED":
      return {
        type: "failed",
        icon: "×",
        title: "Thanh toán thất bại",
        description: "Giao dịch VNPay chưa được thanh toán thành công.",
      };

    case "CANCELLED":
      return {
        type: "failed",
        icon: "×",
        title: "Giao dịch đã bị hủy",
        description: "Giao dịch thanh toán đã bị hủy.",
      };

    case "PENDING":
    case "CREATED":
      return {
        type: "pending",
        icon: "…",
        title: "Đang chờ thanh toán",
        description: "Giao dịch chưa được xác nhận hoàn tất.",
      };

    default:
      return {
        type: "failed",
        icon: "?",
        title: "Không xác định được trạng thái",
        description: "Không thể xác định trạng thái giao dịch.",
      };
  }
}

function getPaymentMethodText(method) {
  switch (String(method ?? "").toUpperCase()) {
    case "VNPAY":
      return "VNPay";

    case "COD":
      return "Thanh toán khi nhận hàng";

    case "MOMO":
      return "MoMo";

    default:
      return method || "—";
  }
}

function PaymentResultPage() {
  const location = useLocation();
  const navigate = useNavigate();

  const [payment, setPayment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const transactionRef = useMemo(() => {
    const params = new URLSearchParams(location.search);

    return params.get("transactionRef");
  }, [location.search]);

  useEffect(() => {
    let cancelled = false;

    const loadPayment = async () => {
      if (!transactionRef) {
        setError("Không tìm thấy mã giao dịch thanh toán.");
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError("");

        const response = await paymentApi.getPaymentResult(transactionRef);

        const paymentResponse = response.data;

        if (!paymentResponse) {
          throw new Error("Không thể lấy kết quả thanh toán.");
        }

        if (!cancelled) {
          setPayment(paymentResponse);
        }
      } catch (requestError) {
        console.error("Unable to load payment result:", requestError);

        if (!cancelled) {
          setError(
            requestError.response?.data?.message ??
              requestError.message ??
              "Không thể lấy kết quả thanh toán.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    loadPayment();

    return () => {
      cancelled = true;
    };
  }, [transactionRef]);

  if (loading) {
    return (
      <main className="payment-result-page">
        <section className="payment-result-card" aria-live="polite">
          <div className="payment-result-loading">
            <LoadingSpinner size="large" />
          </div>
        </section>
      </main>
    );
  }

  if (error) {
    return (
      <main className="payment-result-page">
        <section className="payment-result-card">
          <div className="payment-result-status payment-result-status--error">
            <div className="payment-result-icon" aria-hidden="true">
              !
            </div>

            <h1>Không thể kiểm tra thanh toán</h1>

            <p>{error}</p>
          </div>

          <div className="payment-result-actions">
            <button
              type="button"
              className="payment-result-button payment-result-button--secondary"
              onClick={() => navigate("/customer/orders")}
            >
              Đơn hàng của tôi
            </button>

            <button
              type="button"
              className="payment-result-button payment-result-button--primary"
              onClick={() => navigate("/")}
            >
              Về trang chủ
            </button>
          </div>
        </section>
      </main>
    );
  }

  const status = getPaymentStatus(payment);
  const statusInfo = getStatusInfo(status);

  return (
    <main className="payment-result-page">
      <section className="payment-result-card">
        <div
          className={`payment-result-status payment-result-status--${statusInfo.type}`}
        >
          <div className="payment-result-icon" aria-hidden="true">
            {statusInfo.icon}
          </div>

          <h1>{statusInfo.title}</h1>

          <p>{statusInfo.description}</p>
        </div>

        <div className="payment-result-info">
          <div className="payment-result-info-row">
            <span>Mã giao dịch</span>

            <strong>{payment?.transactionRef ?? transactionRef ?? "—"}</strong>
          </div>

          <div className="payment-result-info-row">
            <span>Phương thức thanh toán</span>

            <strong>{getPaymentMethodText(payment?.paymentMethod)}</strong>
          </div>

          <div className="payment-result-info-row">
            <span>Số tiền</span>

            <strong>{formatCurrency(payment?.amount)}</strong>
          </div>

          <div className="payment-result-info-row">
            <span>Trạng thái</span>

            <strong>{status || "—"}</strong>
          </div>

          <div className="payment-result-info-row">
            <span>
              {status === "SUCCESS"
                ? "Thời gian thanh toán"
                : status === "EXPIRED"
                  ? "Thời gian hết hạn"
                  : "Thời gian tạo giao dịch"}
            </span>

            <strong>
              {formatDateTime(
                status === "SUCCESS"
                  ? payment?.paidAt
                  : status === "EXPIRED"
                    ? payment?.expiredAt
                    : payment?.createdAt,
              )}
            </strong>
          </div>

          {payment?.responseCode && (
            <div className="payment-result-info-row">
              <span>Mã phản hồi</span>

              <strong>{payment.responseCode}</strong>
            </div>
          )}

          {payment?.responseMessage && (
            <div className="payment-result-message">
              {payment.responseMessage}
            </div>
          )}
        </div>

        <div className="payment-result-actions">
          <button
            type="button"
            className="payment-result-button payment-result-button--primary"
            onClick={() => navigate("/customer/orders")}
          >
            Xem đơn hàng
          </button>

          <button
            type="button"
            className="payment-result-button payment-result-button--secondary"
            onClick={() => navigate("/")}
          >
            Về trang chủ
          </button>
        </div>
      </section>
    </main>
  );
}

export default PaymentResultPage;
