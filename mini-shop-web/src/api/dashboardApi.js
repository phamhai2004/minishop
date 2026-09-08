import axiosClient from "./axiosClient";

const dashboardApi = {
  getAdminDashboard() {
    return axiosClient.get("/admin/dashboard");
  },

  getAdminProducts(params = {}) {
    return axiosClient.get("/admin/dashboard/products", {
      params,
    });
  },

  getAdminOrders(params = {}) {
    return axiosClient.get("/admin/dashboard/orders", {
      params,
    });
  },

  getAdminUsers(params = {}) {
    return axiosClient.get("/admin/dashboard/users", {
      params,
    });
  },

  getAdminCategories(params = {}) {
    return axiosClient.get("/admin/dashboard/categories", {
      params,
    });
  },

  getAdminRevenue(period = "MONTH") {
    return axiosClient.get("/admin/dashboard/revenue", {
      params: {
        period,
      },
    });
  },

  getSellerDashboard() {
    return axiosClient.get("/seller/dashboard");
  },

  getSellerProducts(params = {}) {
    return axiosClient.get("/seller/dashboard/products", {
      params,
    });
  },

  getSellerOrders(params = {}) {
    return axiosClient.get("/seller/orders", {
      params,
    });
  },

  getSellerCustomers(params = {}) {
    return axiosClient.get("/seller/dashboard/customers", {
      params,
    });
  },

  getSellerPerformance() {
    return axiosClient.get("/seller/dashboard/performance");
  },

  getSellerPerformanceProducts() {
    return axiosClient.get("/seller/dashboard/performance/products");
  },

  getSellerRevenue(period = "MONTH") {
    return axiosClient.get("/seller/dashboard/revenue", {
      params: {
        period,
      },
    });
  },
};

export default dashboardApi;
