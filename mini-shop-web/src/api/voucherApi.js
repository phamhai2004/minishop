import axiosClient from "./axiosClient";

const voucherApi = {
  getCatalog({ scope, shopId } = {}) {
    const params = {};

    if (scope) {
      params.scope = scope;
    }

    if (shopId) {
      params.shopId = shopId;
    }

    return axiosClient.get("/voucher-catalog", {
      params,
    });
  },

  collect(code) {
    return axiosClient.post(`/my-vouchers/${encodeURIComponent(code)}/collect`);
  },

  getMyVouchers() {
    return axiosClient.get("/my-vouchers");
  },

  getAvailable() {
    return axiosClient.get("/my-vouchers/available");
  },

  getUsed() {
    return axiosClient.get("/my-vouchers/used");
  },
};

export default voucherApi;
