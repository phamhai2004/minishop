import axiosClient from "./axiosClient";

const sellerVoucherApi = {
  getAll(params = {}) {
    return axiosClient.get("/seller/vouchers", {
      params: {
        page: params.page ?? 0,
        size: params.size ?? 20,
      },
    });
  },

  create(data) {
    return axiosClient.post("/seller/vouchers", data);
  },

  update(id, data) {
    return axiosClient.put(`/seller/vouchers/${id}`, data);
  },

  deactivate(id) {
    return axiosClient.patch(`/seller/vouchers/${id}/deactivate`);
  },
};

export default sellerVoucherApi;
