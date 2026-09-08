import { useCallback, useEffect, useRef, useState } from "react";

import { useLocation, useNavigate } from "react-router-dom";

import shopChatApi from "../../api/shopChatApi";
import shopChatSocket from "../../services/shopChatSocket";

import { useAuth } from "../../contexts/AuthContext";

import ROLES from "../../components/constants/roles";
import LoadingSpinner from "../../components/common/LoadingSpinner";

import "./ShopChatPage.css";

function formatTime(value) {
  if (!value) {
    return "";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "";
  }

  const now = new Date();

  const sameDay = date.toDateString() === now.toDateString();

  if (sameDay) {
    return date.toLocaleTimeString("vi-VN", {
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  return date.toLocaleDateString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
  });
}

function ConversationAvatar({ conversation, isSeller }) {
  if (!isSeller && conversation.shopLogoUrl) {
    return (
      <img
        className="shop-chat__avatar"
        src={conversation.shopLogoUrl}
        alt={conversation.shopName}
      />
    );
  }

  const name = isSeller ? conversation.customerName : conversation.shopName;

  return (
    <div className="shop-chat__avatar shop-chat__avatar--empty">
      {name?.trim()?.charAt(0)?.toUpperCase() || "C"}
    </div>
  );
}

function ShopChatPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { currentUser } = useAuth();
  const currentUserId = Number(currentUser?.userId ?? currentUser?.id);
  const isSeller = currentUser?.role === ROLES.SELLER;
  const [conversations, setConversations] = useState([]);
  const [selectedConversationId, setSelectedConversationId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [messageText, setMessageText] = useState("");
  const [conversationsLoading, setConversationsLoading] = useState(true);
  const [messagesLoading, setMessagesLoading] = useState(false);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState("");
  const messagesEndRef = useRef(null);
  const selectedConversationIdRef = useRef(null);
  const currentUserIdRef = useRef(currentUserId);

  const selectedConversation =
    conversations.find(
      (conversation) => conversation.id === selectedConversationId,
    ) ?? null;

  const contextProduct = selectedConversation?.contextProduct ?? null;

  const loadConversations = useCallback(async () => {
    try {
      setConversationsLoading(true);

      const response = await shopChatApi.getConversations();

      setConversations(response.data?.data ?? []);
    } catch (requestError) {
      console.error("Không thể tải danh sách chat:", requestError);

      setError(
        requestError.response?.data?.message || "Không thể tải danh sách chat.",
      );
    } finally {
      setConversationsLoading(false);
    }
  }, []);

  const loadMessages = useCallback(async (conversationId, markRead = true) => {
    if (!conversationId) {
      return;
    }

    try {
      setMessagesLoading(true);

      const response = await shopChatApi.getMessages(conversationId);

      setMessages(response.data?.data ?? []);

      if (markRead) {
        await shopChatApi.markAsRead(conversationId);

        setConversations((current) =>
          current.map((conversation) =>
            conversation.id === conversationId
              ? {
                  ...conversation,
                  unreadCount: 0,
                }
              : conversation,
          ),
        );
      }
    } catch (requestError) {
      console.error("Không thể tải tin nhắn:", requestError);
    } finally {
      setMessagesLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadConversations();
  }, [loadConversations]);

  useEffect(() => {
    if (conversations.length === 0 || selectedConversationId) {
      return;
    }

    const requestedId = Number(location.state?.conversationId);

    if (!Number.isFinite(requestedId)) {
      return;
    }

    const exists = conversations.some(
      (conversation) => conversation.id === requestedId,
    );

    if (!exists) {
      return;
    }

    setSelectedConversationId(requestedId);

    void loadMessages(requestedId);
  }, [conversations, location.state, selectedConversationId, loadMessages]);

  useEffect(() => {
    const removeListener = shopChatSocket.addListener((event) => {
      const activeConversationId = selectedConversationIdRef.current;

      const activeUserId = currentUserIdRef.current;

      if (event.type === "MESSAGE_CREATED") {
        const incomingMessage = event.message;

        if (event.conversationId === activeConversationId && incomingMessage) {
          setMessages((current) => {
            const exists = current.some(
              (message) => message.id === incomingMessage.id,
            );

            if (exists) {
              return current;
            }

            return [...current, incomingMessage];
          });

          if (Number(incomingMessage.senderId) !== activeUserId) {
            void shopChatApi.markAsRead(event.conversationId);
          }
        }

        void loadConversations();
      }

      if (event.type === "MESSAGES_READ") {
        if (event.conversationId === activeConversationId) {
          void loadMessages(event.conversationId, false);
        }

        void loadConversations();
      }

      if (event.type === "PRODUCT_CONTEXT_CHANGED") {
        void loadConversations();
      }
    });

    return () => {
      removeListener();
    };
  }, [loadConversations, loadMessages]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({
      behavior: "smooth",
    });
  }, [messages]);

  useEffect(() => {
    selectedConversationIdRef.current = selectedConversationId;
  }, [selectedConversationId]);

  useEffect(() => {
    currentUserIdRef.current = currentUserId;
  }, [currentUserId]);

  const handleSelectConversation = async (conversationId) => {
    setSelectedConversationId(conversationId);

    setMessages([]);

    await loadMessages(conversationId);
  };

  const handleSendMessage = async (event) => {
    event.preventDefault();

    const content = messageText.trim();

    if (!content || !selectedConversationId || sending) {
      return;
    }

    try {
      setSending(true);

      const response = await shopChatApi.sendMessage(
        selectedConversationId,
        content,
      );

      const savedMessage = response.data?.data;

      if (savedMessage) {
        setMessages((current) => {
          const exists = current.some(
            (message) => message.id === savedMessage.id,
          );

          if (exists) {
            return current;
          }

          return [...current, savedMessage];
        });
      }

      setMessageText("");

      void loadConversations();
    } catch (requestError) {
      console.error("Không thể gửi tin nhắn:", requestError);

      alert(requestError.response?.data?.message || "Không thể gửi tin nhắn.");
    } finally {
      setSending(false);
    }
  };

  const handleBack = () => {
    const returnTo = location.state?.returnTo;

    const returnState = location.state?.returnState;

    if (returnTo) {
      navigate(returnTo, {
        state: returnState ?? null,
      });

      return;
    }

    navigate(-1);
  };

  const handleCloseProductContext = async () => {
    if (isSeller || !selectedConversationId) {
      return;
    }

    try {
      await shopChatApi.closeProductContext(selectedConversationId);

      await loadConversations();
    } catch (requestError) {
      console.error("Không thể đóng thông tin sản phẩm:", requestError);
    }
  };

  return (
    <main
      className={
        selectedConversationId
          ? "shop-chat shop-chat--conversation-open"
          : "shop-chat"
      }
    >
      {/* LIST */}
      <aside className="shop-chat__sidebar">
        <header className="shop-chat__sidebar-header">
          <button
            type="button"
            className="shop-chat__back"
            onClick={handleBack}
          >
            ←
          </button>

          <div>
            <h1>Chat</h1>

            <span>{conversations.length} cuộc trò chuyện</span>
          </div>

          {!selectedConversation && (
            <button
              type="button"
              className="shop-chat__sidebar-close"
              onClick={handleBack}
              aria-label="Đóng chat"
              title="Đóng"
            >
              ×
            </button>
          )}
        </header>

        {conversationsLoading ? (
          <div className="shop-chat__state">
            <LoadingSpinner size="medium" />
          </div>
        ) : error ? (
          <div className="shop-chat__state shop-chat__state--error">
            {error}
          </div>
        ) : conversations.length === 0 ? (
          <div className="shop-chat__state">Chưa có cuộc trò chuyện nào.</div>
        ) : (
          <div className="shop-chat__conversation-list">
            {conversations.map((conversation) => {
              const title = isSeller
                ? conversation.customerName
                : conversation.shopName;

              return (
                <button
                  type="button"
                  key={conversation.id}
                  className={
                    selectedConversationId === conversation.id
                      ? "shop-chat__conversation shop-chat__conversation--active"
                      : "shop-chat__conversation"
                  }
                  onClick={() => handleSelectConversation(conversation.id)}
                >
                  <ConversationAvatar
                    conversation={conversation}
                    isSeller={isSeller}
                  />

                  <div className="shop-chat__conversation-content">
                    <div className="shop-chat__conversation-top">
                      <strong>{title || "Người dùng"}</strong>

                      <time>{formatTime(conversation.lastMessageAt)}</time>
                    </div>

                    <div className="shop-chat__conversation-bottom">
                      <span>
                        {conversation.lastMessage || "Bắt đầu trò chuyện"}
                      </span>

                      {Number(conversation.unreadCount) > 0 && (
                        <b>{conversation.unreadCount}</b>
                      )}
                    </div>
                  </div>
                </button>
              );
            })}
          </div>
        )}
      </aside>

      {/* CHAT DETAIL */}
      <section className="shop-chat__main">
        {!selectedConversation ? (
          <div className="shop-chat__welcome">
            <p>Chọn một cuộc trò chuyện để bắt đầu.</p>
          </div>
        ) : (
          <>
            <header className="shop-chat__chat-header">
              <button
                type="button"
                className="shop-chat__mobile-back"
                onClick={handleBack}
              >
                ←
              </button>

              <ConversationAvatar
                conversation={selectedConversation}
                isSeller={isSeller}
              />

              <div>
                <strong>
                  {isSeller
                    ? selectedConversation.customerName
                    : selectedConversation.shopName}
                </strong>

                <span>{isSeller ? "Khách hàng" : "Shop"}</span>
              </div>

              <button
                type="button"
                className="shop-chat__close"
                onClick={() => navigate(-1)}
                aria-label="Đóng chat"
              >
                ×
              </button>
            </header>

            <div className="shop-chat__messages">
              {messagesLoading ? (
                <div className="shop-chat__state">
                  <LoadingSpinner size="medium" />
                </div>
              ) : messages.length === 0 ? (
                <div className="shop-chat__empty-chat">
                  <div>👋</div>

                  <p>Hãy gửi lời chào đầu tiên.</p>
                </div>
              ) : (
                messages.map((message) => {
                  if (message.type === "PRODUCT") {
                    return null;
                  }

                  const mine = Number(message.senderId) === currentUserId;

                  return (
                    <div
                      key={message.id}
                      className={
                        mine
                          ? "shop-chat__message-row shop-chat__message-row--mine"
                          : "shop-chat__message-row"
                      }
                    >
                      <div className="shop-chat__message">
                        <p>{message.content}</p>

                        <div className="shop-chat__message-meta">
                          <time>{formatTime(message.createdAt)}</time>

                          {mine && message.readStatus && <span>Đã xem</span>}
                        </div>
                      </div>
                    </div>
                  );
                })
              )}

              <div ref={messagesEndRef} />
            </div>

            {contextProduct && (
              <div className="shop-chat__product-context">
                <div className="shop-chat__product-context-header">
                  <div className="shop-chat__product-context-label">
                    {isSeller
                      ? "Khách hàng đang trao đổi với bạn về sản phẩm này"
                      : "Bạn đang trao đổi với Người bán về sản phẩm này"}
                  </div>

                  {!isSeller && (
                    <button
                      type="button"
                      className="shop-chat__product-context-close"
                      onClick={handleCloseProductContext}
                      aria-label="Đóng thông tin sản phẩm"
                      title="Đóng"
                    >
                      ×
                    </button>
                  )}
                </div>

                <div className="shop-chat__product-card">
                  <button
                    type="button"
                    className="shop-chat__product-main"
                    onClick={() => navigate(`/products/${contextProduct.id}`)}
                  >
                    {contextProduct.imageUrl ? (
                      <img
                        src={contextProduct.imageUrl}
                        alt={contextProduct.name}
                      />
                    ) : (
                      <div className="shop-chat__product-image-empty">SP</div>
                    )}

                    <div className="shop-chat__product-info">
                      <strong>{contextProduct.name}</strong>

                      <div className="shop-chat__product-price">
                        {Number(
                          contextProduct.salePrice ?? contextProduct.price ?? 0,
                        ).toLocaleString("vi-VN")}
                        ₫
                      </div>

                      {contextProduct.salePrice != null && (
                        <del>
                          {Number(contextProduct.price ?? 0).toLocaleString(
                            "vi-VN",
                          )}
                          ₫
                        </del>
                      )}
                    </div>
                  </button>

                  {!isSeller && (
                    <button
                      type="button"
                      className="shop-chat__product-change"
                      onClick={() => navigate(`/products/${contextProduct.id}`)}
                    >
                      Xem
                    </button>
                  )}
                </div>
              </div>
            )}

            <form className="shop-chat__composer" onSubmit={handleSendMessage}>
              <textarea
                value={messageText}
                onChange={(event) => setMessageText(event.target.value)}
                placeholder="Nhập tin nhắn..."
                maxLength={2000}
                rows={1}
                disabled={sending}
                onKeyDown={(event) => {
                  if (event.key === "Enter" && !event.shiftKey) {
                    event.preventDefault();

                    event.currentTarget.form?.requestSubmit();
                  }
                }}
              />

              <button type="submit" disabled={sending || !messageText.trim()}>
                {sending ? (
                  <LoadingSpinner size="small" inline variant="light" />
                ) : (
                  "Gửi"
                )}
              </button>
            </form>
          </>
        )}
      </section>
    </main>
  );
}

export default ShopChatPage;
