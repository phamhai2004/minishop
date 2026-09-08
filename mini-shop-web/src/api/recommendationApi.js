import { authenticatedClient } from "./httpClients";

const recommendationApi = {
  getMine() {
    return authenticatedClient.get("/users/me/recommendations");
  },
};

export default recommendationApi;
