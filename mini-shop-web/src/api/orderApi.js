import axiosClient from "./axiosClient";

const orderApi = {
  createOrder(data) {
    return axiosClient.post("/orders", data);
  },

  getMyOrders() {
    return axiosClient.get("/orders/my");
  },

  getByOrderCode(orderCode) {
    return axiosClient.get(`/orders/${encodeURIComponent(orderCode)}`);
  },

  cancelOrder(orderCode) {
    return axiosClient.patch(`/orders/${encodeURIComponent(orderCode)}/cancel`);
  },

  confirmReceived(orderCode) {
    return axiosClient.patch(
      `/orders/${encodeURIComponent(orderCode)}/confirm-received`,
    );
  },
};

export default orderApi;
