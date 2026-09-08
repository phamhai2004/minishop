import { useCallback, useEffect, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import { useAuth } from "../../contexts/AuthContext";
import ROLES from "../constants/roles";
import MessageCircleIcon from "../icons/MessageCircleIcon";
import Chatbot from "../chatbot/Chatbot";
import shopChatApi from "../../api/shopChatApi";
import shopChatSocket from "../../services/shopChatSocket";

import "./ChatHub.css";

export default function ChatHub() {
  const navigate = useNavigate();
  const location = useLocation();
  const iconRef = useRef(null);
  const [menuOpen, setMenuOpen] = useState(false);
  const [chatbotOpen, setChatbotOpen] = useState(false);
  const { currentUser } = useAuth();
  const isCustomer = currentUser?.role === ROLES.CUSTOMER;
  const isSeller = currentUser?.role === ROLES.SELLER;
  const chatPath = isSeller ? "/seller/chat" : "/customer/chat";
  const isShopChatPage = location.pathname.startsWith(chatPath);
  const [unreadCount, setUnreadCount] = useState(0);

  const loadUnreadCount = useCallback(async () => {
    try {
      const response = await shopChatApi.getConversations();
      const conversations = response.data?.data ?? [];
      const total = conversations.reduce(
        (sum, conversation) => sum + Number(conversation.unreadCount ?? 0),
        0,
      );

      setUnreadCount(total);
    } catch (error) {
      console.error("Không thể tải số tin nhắn chưa đọc:", error);
    }
  }, []);

  useEffect(() => {
    setMenuOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    void loadUnreadCount();
  }, [loadUnreadCount, location.pathname]);

  const handleOpenMessages = () => {
    setMenuOpen(false);
    setChatbotOpen(false);

    navigate(chatPath);
  };

  const handleOpenAssistant = () => {
    if (!isCustomer) {
      return;
    }

    setMenuOpen(false);

    const isMobile = window.matchMedia("(max-width: 820px)").matches;

    if (isMobile) {
      setChatbotOpen(false);

      navigate("/customer/assistant");

      return;
    }

    setChatbotOpen(true);
  };

  const handleLauncherClick = () => {
    if (isSeller) {
      handleOpenMessages();
      return;
    }

    setMenuOpen((current) => !current);
  };

  useEffect(() => {
    const handleExternalToggle = () => {
      if (isSeller) {
        handleOpenMessages();
        return;
      }

      if (!isCustomer) {
        return;
      }

      setMenuOpen((current) => !current);
    };

    window.addEventListener("chat-hub:toggle", handleExternalToggle);

    return () => {
      window.removeEventListener("chat-hub:toggle", handleExternalToggle);
    };
  }, [isCustomer, isSeller]);

  useEffect(() => {
    const removeListener = shopChatSocket.addListener((event) => {
      if (event.type === "MESSAGE_CREATED" || event.type === "MESSAGES_READ") {
        void loadUnreadCount();
      }
    });

    shopChatSocket.connect();

    return () => {
      removeListener();

      shopChatSocket.disconnect();
    };
  }, [loadUnreadCount]);

  const showLauncher = !chatbotOpen && !isShopChatPage;

  return (
    <>
      {showLauncher && (
        <>
          {isCustomer && menuOpen && (
            <div className="chat-hub__menu">
              <button
                type="button"
                className="chat-hub__menu-item"
                onClick={handleOpenMessages}
              >
                <strong>Tin nhắn</strong>

                <span>Trò chuyện với Shop</span>
              </button>

              <button
                type="button"
                className="chat-hub__menu-item"
                onClick={handleOpenAssistant}
              >
                <strong>Trợ lý AI</strong>
              </button>
            </div>
          )}

          <button
            type="button"
            className="chat-hub__launcher"
            onClick={handleLauncherClick}
            onMouseEnter={() => iconRef.current?.startAnimation?.()}
            onMouseLeave={() => iconRef.current?.stopAnimation?.()}
            aria-label={
              unreadCount > 0
                ? `Chat, ${unreadCount} tin nhắn chưa đọc`
                : "Mở Chat"
            }
          >
            <MessageCircleIcon ref={iconRef} size={30} strokeWidth={2} />

            {unreadCount > 0 && (
              <span className="chat-hub__badge">
                {unreadCount > 99 ? "99+" : unreadCount}
              </span>
            )}
          </button>
        </>
      )}

      {isCustomer && (
        <Chatbot open={chatbotOpen} onClose={() => setChatbotOpen(false)} />
      )}
    </>
  );
}
