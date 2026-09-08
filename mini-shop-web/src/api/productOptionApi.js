import axiosClient from "./axiosClient";

const productOptionApi = {

  getAllTypes() {
    return axiosClient.get("/product-options/types");
  },

  getTypeById(id) {
    return axiosClient.get(`/product-options/types/${id}`);
  },

  createType(data) {
    return axiosClient.post("/product-options/types", data);
  },

  updateType(id, data) {
    return axiosClient.put(`/product-options/types/${id}`, data);
  },

  deleteType(id) {
    return axiosClient.delete(`/product-options/types/${id}`);
  },

  getValuesByType(typeId) {
    return axiosClient.get(`/product-options/types/${typeId}/values`);
  },

  getValueById(id) {
    return axiosClient.get(`/product-options/values/${id}`);
  },

  createValue(data) {
    return axiosClient.post("/product-options/values", data);
  },

  updateValue(id, data) {
    return axiosClient.put(`/product-options/values/${id}`, data);
  },

  deleteValue(id) {
    return axiosClient.delete(`/product-options/values/${id}`);
  },
};

export default productOptionApi;
