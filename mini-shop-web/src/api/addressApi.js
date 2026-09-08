import axiosClient from "./axiosClient";

const addressApi = {
  getMyAddresses() {
    return axiosClient.get("/addresses");
  },

  getDefault() {
    return axiosClient.get("/addresses/default");
  },

  create(data) {
    return axiosClient.post("/addresses", data);
  },

  update(id, data) {
    return axiosClient.put(`/addresses/${id}`, data);
  },

  setDefault(id) {
    return axiosClient.patch(`/addresses/${id}/default`);
  },

  remove(id) {
    return axiosClient.delete(`/addresses/${id}`);
  },
};

export default addressApi;
