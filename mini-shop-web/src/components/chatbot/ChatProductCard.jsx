import { useNavigate } from "react-router-dom";
import "./ChatProductCard.css";

export default function ChatProductCard({ product }) {
  const navigate = useNavigate();

  const imageUrl =
    product.images?.find((image) => image.primaryImage)?.imageUrl ||
    product.images?.[0]?.imageUrl;

  const handleViewDetail = () => {
    navigate(`/products/${product.id}`);
  };

  return (
    <div className="chat-product-card">
      {imageUrl && (
        <img src={imageUrl} alt={product.name} className="chat-product-image" />
      )}

      <div className="chat-product-info">
        <div className="chat-product-category">{product.categoryName}</div>

        <div className="chat-product-name">{product.name}</div>

        <div className="chat-product-price">
          {Number(product.price).toLocaleString("vi-VN")} đ
        </div>

        <div className="chat-product-quantity">
          Còn {product.quantity} sản phẩm
        </div>

        <button
          type="button"
          className="chat-product-detail-button"
          onClick={handleViewDetail}
        >
          Xem chi tiết
        </button>
      </div>
    </div>
  );
}
