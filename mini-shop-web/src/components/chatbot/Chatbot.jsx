import { useEffect, useRef, useState } from "react";

import useChatbot from "../../hooks/useChatbot";
import ChatMessage from "./ChatMessage";
import "./Chatbot.css";

export default function Chatbot({ open, onClose }) {
  const [input, setInput] = useState("");
  const messagesEndRef = useRef(null);

  const { messages, loading, error, sendMessage } = useChatbot();

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({
      behavior: "smooth",
    });
  }, [messages, loading]);

  const handleKeyDown = (event) => {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();

      if (loading || !input.trim()) {
        return;
      }

      event.currentTarget.form?.requestSubmit();
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    const message = input.trim();

    if (!message || loading) {
      return;
    }

    setInput("");

    await sendMessage(message);
  };

  return (
    <>
      {open && (
        <div className="chatbot-window">
          <div className="chatbot-header">
            <div>
              <strong>Trợ lý Hair</strong>
            </div>

            <button
              type="button"
              className="chatbot-close"
              onClick={onClose}
              aria-label="Đóng trợ lý AI"
            >
              ×
            </button>
          </div>

          <div className="chatbot-messages">
            {messages.map((message) => (
              <ChatMessage key={message.id} message={message} />
            ))}

            {loading && (
              <div className="chat-message assistant">
                <div className="chat-typing">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {error && <div className="chatbot-error">{error}</div>}

          <form className="chatbot-input-area" onSubmit={handleSubmit}>
            <textarea
              value={input}
              onChange={(event) => setInput(event.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Nhập tin nhắn..."
              disabled={loading}
              rows={1}
            />

            <button type="submit" disabled={loading || !input.trim()}>
              ➤
            </button>
          </form>
        </div>
      )}
    </>
  );
}
