import { authenticatedClient } from "./httpClients";

const userApi = {
  getCurrentUser() {
    return authenticatedClient.get("/users/me");
  },

  updateCurrentUser(updateRequest) {
    return authenticatedClient.patch("/users/me", updateRequest);
  },

  updateAvatar(file) {
    const formData = new FormData();

    formData.append("file", file);

    return authenticatedClient.post("/users/me/avatar", formData);
  },
};

export default userApi;
