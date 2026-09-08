import { authenticatedClient, publicClient } from "./httpClients";

const shopApi = {
  getById(shopId) {
    return publicClient.get(`/shops/${shopId}`);
  },

  getCategories(shopId) {
    return publicClient.get(`/shops/${shopId}/categories`);
  },

  register(data) {
    return authenticatedClient.post("/shops", data);
  },

  getMyShop() {
    return authenticatedClient.get("/shops/me");
  },

  updateMyRegistration(data) {
    return authenticatedClient.put("/shops/me", data);
  },

  resubmitMyShop() {
    return authenticatedClient.patch("/shops/me/resubmit");
  },

  getByCategory(categoryId, params = {}) {
    return publicClient.get(`/shops/category/${categoryId}`, {
      params: {
        page: 0,
        size: 6,
        ...params,
      },
    });
  },
};

export default shopApi;
