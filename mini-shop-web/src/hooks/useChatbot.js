import { useState } from "react";
import chatApi from "../api/chatApi";

export default function useChatbot() {
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const getChatErrorMessage = (error) => {
    if (error.code === "ECONNABORTED") {
      return "Chatbot đang xử lý hơi lâu. Bạn vui lòng thử lại nhé.";
    }

    if (error.code === "ERR_NETWORK") {
      return "Không thể kết nối đến Hair. Bạn kiểm tra kết nối mạng rồi thử lại nhé.";
    }

    if (error.response?.status >= 500) {
      return "Hệ thống Hair đang gặp sự cố. Bạn vui lòng thử lại sau nhé.";
    }

    if (error.response?.status === 401) {
      return "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
    }

    return "Đã xảy ra lỗi khi xử lý yêu cầu. Bạn vui lòng thử lại nhé.";
  };

  const sendMessage = async (message) => {
    const text = message.trim();

    if (!text || loading) {
      return;
    }

    setError(null);

    setMessages((prev) => [
      ...prev,
      {
        id: Date.now(),
        role: "user",
        content: text,
      },
    ]);

    setLoading(true);

    try {
      const response = await chatApi.sendMessage(text);

      setMessages((prev) => [
        ...prev,
        {
          id: Date.now() + 1,
          role: "assistant",
          content: response.data.message,
          products: response.data.products ?? [],
        },
      ]);
    } catch (error) {
      console.error("========== CHAT ERROR ==========");
      console.error("message:", error.message);
      console.error("code:", error.code);
      console.error("response:", error.response);
      console.error("status:", error.response?.status);
      console.error("================================");

      setError(getChatErrorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  return {
    messages,
    loading,
    error,
    sendMessage,
  };
}
