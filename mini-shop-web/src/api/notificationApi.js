import axiosClient from "./axiosClient";

const notificationApi = {
  getMyNotifications() {
    return axiosClient.get("/notifications");
  },

  getUnreadCount() {
    return axiosClient.get("/notifications/unread-count");
  },

  markAsRead(id) {
    return axiosClient.patch(`/notifications/${id}/read`);
  },

  markAllAsRead() {
    return axiosClient.patch("/notifications/read-all");
  },
};

export default notificationApi;
