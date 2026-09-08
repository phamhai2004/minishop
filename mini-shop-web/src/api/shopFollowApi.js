import axiosClient from "./axiosClient";

const shopFollowApi = {
  getStatus(shopId) {
    return axiosClient.get(`/shop-follows/${shopId}/status`);
  },

  getMyFollowing() {
    return axiosClient.get("/shop-follows/my");
  },

  follow(shopId) {
    return axiosClient.post(`/shop-follows/${shopId}`);
  },

  unfollow(shopId) {
    return axiosClient.delete(`/shop-follows/${shopId}`);
  },
};

export default shopFollowApi;
