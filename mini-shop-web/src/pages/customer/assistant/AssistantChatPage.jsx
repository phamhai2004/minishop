import { useEffect, useRef, useState } from "react";

import { useNavigate } from "react-router-dom";

import useChatbot from "../../../hooks/useChatbot";

import ChatMessage from "../../../components/chatbot/ChatMessage";

import "../../../components/chatbot/Chatbot.css";
import "./AssistantChatPage.css";

function AssistantChatPage() {
  const navigate = useNavigate();

  const [input, setInput] = useState("");

  const messagesEndRef = useRef(null);

  const { messages, loading, error, sendMessage } = useChatbot();

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({
      behavior: "smooth",
    });
  }, [messages, loading]);

  const handleSubmit = async (event) => {
    event.preventDefault();

    const message = input.trim();

    if (!message || loading) {
      return;
    }

    setInput("");

    await sendMessage(message);
  };

  const handleKeyDown = (event) => {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();

      if (loading || !input.trim()) {
        return;
      }

      event.currentTarget.form?.requestSubmit();
    }
  };

  return (
    <main className="assistant-chat-page">
      <header className="assistant-chat-page__header">
        <button
          type="button"
          className="assistant-chat-page__back"
          onClick={() => navigate(-1)}
          aria-label="Quay lại"
        >
          ←
        </button>

        <div className="assistant-chat-page__avatar">AI</div>

        <div className="assistant-chat-page__identity">
          <strong>Hair Assistant</strong>
        </div>
      </header>

      <section className="assistant-chat-page__messages">
        {messages.length === 0 && (
          <div className="assistant-chat-page__welcome">
            <div className="assistant-chat-page__welcome-avatar">AI</div>

            <div>
              <strong>Trợ lý Hair</strong>
            </div>
          </div>
        )}

        {messages.map((message) => (
          <ChatMessage key={message.id} message={message} />
        ))}

        {loading && (
          <div className="chat-message assistant">
            <div className="chat-typing">
              <span />
              <span />
              <span />
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </section>

      {error && <div className="assistant-chat-page__error">{error}</div>}

      <form className="assistant-chat-page__composer" onSubmit={handleSubmit}>
        <textarea
          value={input}
          rows={1}
          placeholder="Nhập tin nhắn..."
          disabled={loading}
          onChange={(event) => setInput(event.target.value)}
          onKeyDown={handleKeyDown}
        />

        <button
          type="submit"
          disabled={loading || !input.trim()}
          aria-label="Gửi"
        >
          ➤
        </button>
      </form>
    </main>
  );
}

export default AssistantChatPage;
