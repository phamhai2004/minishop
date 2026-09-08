import axiosClient from "./axiosClient";

const reviewApi = {
  createReview(data) {
    return axiosClient.post("/reviews", data);
  },

  getByProduct(productId) {
    return axiosClient.get(`/reviews/product/${productId}`);
  },

  getMyReviews() {
    return axiosClient.get("/reviews/my");
  },

  getMyPendingReviews() {
    return axiosClient.get("/reviews/my/pending");
  },

  deleteReview(id) {
    return axiosClient.delete(`/reviews/${id}`);
  },
};

export default reviewApi;
