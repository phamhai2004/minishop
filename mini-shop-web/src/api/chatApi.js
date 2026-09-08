import { authenticatedClient } from "./httpClients";

const chatApi = {
  sendMessage: (message) => {
    return authenticatedClient.post(
      "/chat",
      {
        message,
      },
      {
        timeout: 120000,
      },
    );
  },
};

export default chatApi;
