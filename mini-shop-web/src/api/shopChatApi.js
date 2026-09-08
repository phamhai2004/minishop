import axiosClient from "./axiosClient";

const shopChatApi = {
  openConversation(shopId) {
    return axiosClient.post(`/shop-chat/conversations/shops/${shopId}`);
  },

  openConversationWithProduct(shopId, productId) {
    return axiosClient.post(
      `/shop-chat/conversations/shops/${shopId}/products/${productId}`,
    );
  },

  getConversations() {
    return axiosClient.get("/shop-chat/conversations");
  },

  getMessages(conversationId) {
    return axiosClient.get(
      `/shop-chat/conversations/${conversationId}/messages`,
    );
  },

  sendMessage(conversationId, content) {
    return axiosClient.post(
      `/shop-chat/conversations/${conversationId}/messages`,
      {
        content,
      },
    );
  },

  closeProductContext(conversationId) {
    return axiosClient.patch(
      `/shop-chat/conversations/${conversationId}/product-context/close`,
    );
  },

  markAsRead(conversationId) {
    return axiosClient.patch(`/shop-chat/conversations/${conversationId}/read`);
  },
};

export default shopChatApi;
