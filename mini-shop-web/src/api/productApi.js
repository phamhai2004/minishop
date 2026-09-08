import axiosClient from "./axiosClient";

const productApi = {
  getAll(params = {}) {
    return axiosClient.get("/products", {
      params: {
        page: 0,
        size: 12,
        sort: "id",
        direction: "desc",
        ...params,
      },
    });
  },

  getByCategory(categoryId, params = {}) {
    return axiosClient.get(`/products/category/${categoryId}`, {
      params: {
        page: 0,
        size: 12,
        ...params,
      },
    });
  },

  search(params = {}) {
    return axiosClient.get("/products/search", {
      params: {
        page: 0,
        size: 12,
        sort: "id",
        direction: "desc",
        ...params,
      },
    });
  },

  getSuggestions(keyword, limit = 8) {
    return axiosClient.get("/products/suggestions", {
      params: {
        keyword,
        limit,
      },
    });
  },

  getById(id) {
    return axiosClient.get(`/products/${id}`);
  },
};

export const apiSearchByImage = async (file) => {
  const formData = new FormData();

  formData.append("file", file);

  const response = await axiosClient.post("/products/search/image", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });

  return response.data.data;
};

export const getSimilarProducts = async (productId) => {
  const response = await axiosClient.get(`/products/${productId}/similar`);

  return response.data.data;
};

export default productApi;
