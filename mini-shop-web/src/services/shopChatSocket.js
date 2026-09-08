import { Client } from "@stomp/stompjs";

const WS_URL = import.meta.env.VITE_WS_URL || "ws://localhost:8080/ws";

let stompClient = null;
let chatSubscription = null;

const listeners = new Set();

function getAccessToken() {
  return localStorage.getItem("minishop_access_token");
}

function emit(event) {
  listeners.forEach((listener) => {
    try {
      listener(event);
    } catch (error) {
      console.error("Chat socket listener error:", error);
    }
  });
}

const shopChatSocket = {
  connect() {
    const token = getAccessToken();

    if (!token) {
      console.warn("Không có access token để kết nối WebSocket.");
      return;
    }

    if (stompClient?.active) {
      return;
    }

    stompClient = new Client({
      brokerURL: WS_URL,

      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },

      reconnectDelay: 3000,

      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      debug: import.meta.env.DEV
        ? (message) => {
            console.log("[STOMP]", message);
          }
        : undefined,
    });

    stompClient.onConnect = () => {
      console.log("Shop Chat WebSocket connected");

      chatSubscription = stompClient.subscribe(
        "/user/queue/shop-chat",
        (message) => {
          try {
            const event = JSON.parse(message.body);

            emit(event);
          } catch (error) {
            console.error("Không thể parse realtime chat event:", error);
          }
        },
      );
    };

    stompClient.onStompError = (frame) => {
      console.error("STOMP error:", frame.headers?.message, frame.body);
    };

    stompClient.onWebSocketError = (error) => {
      console.error("WebSocket error:", error);
    };

    stompClient.activate();
  },

  disconnect() {
    const client = stompClient;

    stompClient = null;

    if (chatSubscription) {
      try {
        chatSubscription.unsubscribe();
      } catch {
        // ignore
      }

      chatSubscription = null;
    }

    if (client?.active) {
      void client.deactivate();
    }
  },

  addListener(listener) {
    listeners.add(listener);

    return () => {
      listeners.delete(listener);
    };
  },

  isConnected() {
    return Boolean(stompClient?.connected);
  },
};

export default shopChatSocket;
