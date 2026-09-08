import axiosClient from "./axiosClient";

const sellerShopApi = {
  getMyShop() {
    return axiosClient.get("/seller/shop");
  },

  updateMyShop(data) {
    return axiosClient.put("/seller/shop", data);
  },

  uploadLogo(file) {
    const formData = new FormData();

    formData.append("file", file);

    return axiosClient.post("/seller/shop/logo", formData);
  },

  uploadCover(file) {
    const formData = new FormData();

    formData.append("file", file);

    return axiosClient.post("/seller/shop/cover", formData);
  },
};

export default sellerShopApi;
