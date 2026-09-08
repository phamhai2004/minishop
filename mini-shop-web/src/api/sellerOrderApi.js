import axiosClient from "./axiosClient";

const sellerOrderApi = {
  getAll(params = {}) {
    return axiosClient.get("/seller/orders", {
      params: {
        page: params.page ?? 0,
        size: params.size ?? 20,
        direction: params.direction ?? "desc",
        timeRange: params.timeRange ?? "ALL",
      },
    });
  },

  getByStatus(status, params = {}) {
    return axiosClient.get(`/seller/orders/status/${status}`, {
      params: {
        page: params.page ?? 0,
        size: params.size ?? 20,
        timeRange: params.timeRange ?? "ALL",
      },
    });
  },

  getById(id) {
    return axiosClient.get(`/seller/orders/${id}`);
  },

  confirm(id) {
    return axiosClient.patch(`/seller/orders/${id}/confirm`);
  },

  startPacking(id) {
    return axiosClient.patch(`/seller/orders/${id}/packing`);
  },

  startShipping(id) {
    return axiosClient.patch(`/seller/orders/${id}/shipping`);
  },

  delivered(id) {
    return axiosClient.patch(`/seller/orders/${id}/delivered`);
  },

  cancel(id, reason) {
    return axiosClient.patch(`/seller/orders/${id}/cancel`, {
      reason,
    });
  },
};

export default sellerOrderApi;
