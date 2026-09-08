import { authenticatedClient, publicClient } from "./httpClients";

const wishlistApi = {
  getMyWishlist() {
    return authenticatedClient.get("/wishlist");
  },

  add(productId) {
    return authenticatedClient.post("/wishlist", {
      productId,
    });
  },

  remove(productId) {
    return authenticatedClient.delete(`/wishlist/${productId}`);
  },

  getCount(productId) {
    return publicClient.get(`/wishlist/count/${productId}`);
  },
};

export default wishlistApi;
