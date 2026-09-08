import axiosClient from "./axiosClient";

const cartApi = {
  getMyCart() {
    return axiosClient.get("/cart");
  },

  addToCart(data) {
    return axiosClient.post("/cart/items", data);
  },

  updateItem(productId, variantId, data) {
    return axiosClient.put(`/cart/items/${productId}`, data, {
      params: {
        variantId,
      },
    });
  },

  removeItem(productId, variantId) {
    return axiosClient.delete(`/cart/items/${productId}`, {
      params: {
        variantId,
      },
    });
  },
};

export default cartApi;
