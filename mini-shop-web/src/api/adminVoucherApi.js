import axiosClient from "./axiosClient";

const adminVoucherApi = {
  getAll() {
    return axiosClient.get("/vouchers");
  },

  create(data) {
    return axiosClient.post("/vouchers", data);
  },

  update(id, data) {
    return axiosClient.put(`/vouchers/${id}`, data);
  },

  deactivate(id) {
    return axiosClient.patch(`/vouchers/${id}/deactivate`);
  },
};

export default adminVoucherApi;
