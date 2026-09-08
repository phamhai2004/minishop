import { publicClient } from "./httpClients";

const flashSaleApi = {
  getActive(params = {}) {
    return publicClient.get("/flash-sales", {
      params: {
        page: 0,
        size: 6,
        ...params,
      },
    });
  },
};

export default flashSaleApi;
