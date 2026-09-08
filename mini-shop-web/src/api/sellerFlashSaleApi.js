import axiosClient from "./axiosClient";

const sellerFlashSaleApi = {
  getAll(params = {}) {
    return axiosClient.get("/seller/flash-sales", {
      params: {
        page: params.page ?? 0,
        size: params.size ?? 20,
      },
    });
  },

  create(data) {
    return axiosClient.post("/seller/flash-sales", data);
  },

  update(id, data) {
    return axiosClient.put(`/seller/flash-sales/${id}`, data);
  },

  deactivate(id) {
    return axiosClient.patch(`/seller/flash-sales/${id}/deactivate`);
  },
};

export default sellerFlashSaleApi;
