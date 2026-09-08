import { useEffect, useRef, useState } from "react";

import notificationApi from "../../api/notificationApi";

import FilledBellIcon from "../icons/FilledBellIcon";
import LoadingSpinner from "../common/LoadingSpinner";

import "./NotificationDropdown.css";

function NotificationDropdown() {
  const [isOpen, setIsOpen] = useState(false);

  const [notifications, setNotifications] = useState([]);

  const [unreadCount, setUnreadCount] = useState(0);

  const [loading, setLoading] = useState(false);

  const dropdownRef = useRef(null);
  const bellIconRef = useRef(null);

  useEffect(() => {
    loadUnreadCount();
  }, []);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  const loadUnreadCount = async () => {
    try {
      const response = await notificationApi.getUnreadCount();

      setUnreadCount(response?.data?.data ?? 0);
    } catch (error) {
      console.error("Không thể lấy số lượng thông báo chưa đọc:", error);
    }
  };

  const loadNotifications = async () => {
    try {
      setLoading(true);

      const response = await notificationApi.getMyNotifications();

      setNotifications(response?.data?.data ?? []);
    } catch (error) {
      console.error("Không thể lấy danh sách thông báo:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleToggle = async () => {
    bellIconRef.current?.startAnimation();

    const nextOpen = !isOpen;

    setIsOpen(nextOpen);

    if (nextOpen) {
      await loadNotifications();
    }
  };

  const handleNotificationClick = async (notification) => {
    try {
      // ==============================
      // MARK AS READ
      // ==============================

      if (!notification.readStatus) {
        const response = await notificationApi.markAsRead(notification.id);

        const updatedNotification = response?.data?.data;

        setNotifications((current) =>
          current.map((item) =>
            item.id === notification.id
              ? (updatedNotification ?? {
                  ...item,
                  readStatus: true,
                })
              : item,
          ),
        );

        setUnreadCount((current) => Math.max(0, current - 1));
      }

      // ==============================
      // SELLER NOTIFICATION
      // ==============================

      const eventKey = notification.eventKey ?? "";

      const sellerOrderMatch = eventKey.match(
        /^(ORDER_CREATED_SELLER|PAYMENT_PAID_SELLER|ORDER_RECEIVED_SELLER|ORDER_CANCELLED_SELLER):(\d+)$/,
      );

      if (sellerOrderMatch) {
        const shopOrderId = sellerOrderMatch[2];

        window.location.href = `/seller/orders/${shopOrderId}`;

        return;
      }

      // Customer notification:
      // Chưa điều hướng ở bước này.
    } catch (error) {
      console.error("Không thể xử lý thông báo:", error);
    }
  };

  const handleMarkAllAsRead = async () => {
    if (unreadCount === 0) {
      return;
    }

    try {
      await notificationApi.markAllAsRead();

      setNotifications((current) =>
        current.map((notification) => ({
          ...notification,
          readStatus: true,
        })),
      );

      setUnreadCount(0);
    } catch (error) {
      console.error("Không thể đánh dấu tất cả thông báo đã đọc:", error);
    }
  };

  return (
    <div className="notification" ref={dropdownRef}>
      <button
        type="button"
        className="notification__button"
        onClick={handleToggle}
        aria-label="Thông báo"
        aria-expanded={isOpen}
      >
        <FilledBellIcon
          ref={bellIconRef}
          size={24}
          className="notification__icon"
        />

        {unreadCount > 0 && (
          <span className="notification__badge">
            {unreadCount > 99 ? "99+" : unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="notification__dropdown">
          <div className="notification__header">
            <h3>Thông báo</h3>

            {unreadCount > 0 && (
              <button
                type="button"
                className="notification__mark-all"
                onClick={handleMarkAllAsRead}
              >
                Đọc tất cả
              </button>
            )}
          </div>

          <div className="notification__list">
            {loading ? (
              <div className="notification__empty">
                <LoadingSpinner size="medium" />
              </div>
            ) : notifications.length === 0 ? (
              <div className="notification__empty">Chưa có thông báo</div>
            ) : (
              notifications.map((notification) => (
                <button
                  type="button"
                  key={notification.id}
                  className={`notification__item ${
                    !notification.readStatus ? "notification__item--unread" : ""
                  }`}
                  onClick={() => handleNotificationClick(notification)}
                >
                  <div className="notification__item-title">
                    {notification.title}
                  </div>

                  <div className="notification__item-content">
                    {notification.content}
                  </div>

                  <div className="notification__item-time">
                    {formatNotificationTime(notification.createdAt)}
                  </div>
                </button>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}

function formatNotificationTime(createdAt) {
  if (!createdAt) {
    return "";
  }

  const date = new Date(createdAt);

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

export default NotificationDropdown;
