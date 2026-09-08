import axiosClient from "./axiosClient";

const sellerProductApi = {
  getAll(params = {}) {
    return axiosClient.get("/seller/products", {
      params: {
        page: 0,
        size: 12,
        sort: "id",
        direction: "desc",
        ...params,
      },
    });
  },

  search(keyword, params = {}) {
    return axiosClient.get("/seller/products/search", {
      params: {
        keyword,
        page: 0,
        size: 12,
        sort: "id",
        direction: "desc",
        ...params,
      },
    });
  },

  getById(id) {
    return axiosClient.get(`/seller/products/${id}`);
  },

  create(data) {
    return axiosClient.post("/seller/products", data);
  },

  update(id, data) {
    return axiosClient.put(`/seller/products/${id}`, data);
  },

  updateStatus(id, status) {
    return axiosClient.patch(`/seller/products/${id}/status`, { status });
  },

  discontinue(id) {
    return axiosClient.patch(`/seller/products/${id}/discontinue`);
  },

  count() {
    return axiosClient.get("/seller/products/count");
  },

  uploadImages(productId, files) {
    const formData = new FormData();

    files.forEach((file) => {
      formData.append("files", file);
    });

    return axiosClient.post(`/seller/products/${productId}/images`, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
  },

  setPrimaryImage(imageId) {
    return axiosClient.patch(`/seller/products/images/${imageId}/primary`);
  },

  deleteImage(imageId) {
    return axiosClient.delete(`/seller/products/images/${imageId}`);
  },
};

export default sellerProductApi;
