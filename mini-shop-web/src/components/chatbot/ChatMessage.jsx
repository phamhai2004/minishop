import ChatProductCard from "./ChatProductCard";

export default function ChatMessage({ message }) {
  return (
    <div className={`chat-message ${message.role}`}>
      <div className="chat-message-content">{message.content}</div>

      {message.products?.length > 0 && (
        <div className="chat-product-list">
          {message.products.map((product) => (
            <ChatProductCard key={product.id} product={product} />
          ))}
        </div>
      )}
    </div>
  );
}
