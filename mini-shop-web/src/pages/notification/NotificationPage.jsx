import { useEffect, useState } from "react";

import notificationApi from "../../api/notificationApi";
import LoadingSpinner from "../../components/common/LoadingSpinner";

import "./NotificationPage.css";

function NotificationPage() {
  const [notifications, setNotifications] = useState([]);

  const [unreadCount, setUnreadCount] = useState(0);

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      try {
        setLoading(true);
        setError("");

        const [listResponse, countResponse] = await Promise.all([
          notificationApi.getMyNotifications(),

          notificationApi.getUnreadCount(),
        ]);

        if (cancelled) {
          return;
        }

        setNotifications(
          Array.isArray(listResponse.data?.data) ? listResponse.data.data : [],
        );

        setUnreadCount(Number(countResponse.data?.data ?? 0));
      } catch (err) {
        console.error("Không thể tải thông báo:", err);

        if (!cancelled) {
          setError(err.response?.data?.message ?? "Không thể tải thông báo.");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    void load();

    return () => {
      cancelled = true;
    };
  }, []);

  const handleNotificationClick = async (notification) => {
    if (notification.readStatus) {
      return;
    }

    try {
      const response = await notificationApi.markAsRead(notification.id);

      const updated = response.data?.data;

      setNotifications((current) =>
        current.map((item) =>
          item.id === notification.id
            ? (updated ?? {
                ...item,
                readStatus: true,
              })
            : item,
        ),
      );

      setUnreadCount((current) => Math.max(0, current - 1));
    } catch (err) {
      console.error("Không thể đánh dấu đã đọc:", err);
    }
  };

  const handleMarkAll = async () => {
    if (unreadCount === 0) {
      return;
    }

    try {
      await notificationApi.markAllAsRead();

      setNotifications((current) =>
        current.map((item) => ({
          ...item,
          readStatus: true,
        })),
      );

      setUnreadCount(0);
    } catch (err) {
      console.error("Không thể đọc tất cả:", err);
    }
  };

  return (
    <main className="notification-page">
      <header className="notification-page__header">
        <h1>Thông báo</h1>

        {unreadCount > 0 && (
          <button type="button" onClick={handleMarkAll}>
            Đọc tất cả
          </button>
        )}
      </header>

      {loading ? (
        <div className="notification-page__state">
          <LoadingSpinner size="medium" />
        </div>
      ) : error ? (
        <div className="notification-page__error">{error}</div>
      ) : notifications.length === 0 ? (
        <div className="notification-page__state">Chưa có thông báo</div>
      ) : (
        <div className="notification-page__list">
          {notifications.map((notification) => (
            <button
              type="button"
              key={notification.id}
              className={`notification-page__item ${
                !notification.readStatus
                  ? "notification-page__item--unread"
                  : ""
              }`}
              onClick={() => handleNotificationClick(notification)}
            >
              <span className="notification-page__indicator" />

              <div className="notification-page__body">
                <strong>{notification.title}</strong>

                <p>{notification.content}</p>

                <time>{formatTime(notification.createdAt)}</time>
              </div>
            </button>
          ))}
        </div>
      )}
    </main>
  );
}

function formatTime(value) {
  if (!value) {
    return "";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "";
  }

  return date.toLocaleString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export default NotificationPage;
