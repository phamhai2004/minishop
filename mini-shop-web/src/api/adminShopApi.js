import { authenticatedClient } from "./httpClients";

const adminShopApi = {
  getShops(params = {}) {
    return authenticatedClient.get("/admin/shops", {
      params,
    });
  },

  getShopById(shopId) {
    return authenticatedClient.get(`/admin/shops/${shopId}`);
  },

  approve(shopId) {
    return authenticatedClient.patch(`/admin/shops/${shopId}/approve`);
  },

  reject(shopId, reason) {
    return authenticatedClient.patch(`/admin/shops/${shopId}/reject`, {
      reason,
    });
  },
};

export default adminShopApi;
