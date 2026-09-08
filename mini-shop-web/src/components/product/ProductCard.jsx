import { Link } from "react-router-dom";

import { formatCurrency } from "../../utils/formatCurrency";

import "./ProductCard.css";

function ProductCard({ product }) {
  const {
    id,
    name,
    price,
    salePrice,
    quantity,
    categoryName,
    shopName,
    images,
    flashSale,
    flashSaleQuantity,
    flashSaleSold,
  } = product;

  const hasSalePrice =
    salePrice !== null &&
    salePrice !== undefined &&
    Number(salePrice) < Number(price);

  const displayPrice = hasSalePrice ? salePrice : price;

  const firstImage =
    Array.isArray(images) && images.length > 0 ? images[0] : null;

  const imageUrl =
    typeof firstImage === "string"
      ? firstImage
      : (firstImage?.url ??
        firstImage?.imageUrl ??
        firstImage?.secureUrl ??
        null);

  const remainingFlashSaleQuantity =
    flashSale && flashSaleQuantity !== null && flashSaleQuantity !== undefined
      ? Math.max(Number(flashSaleQuantity) - Number(flashSaleSold ?? 0), 0)
      : null;

  return (
    <Link
      to={`/products/${id}`}
      className="product-card"
      aria-label={`Xem sản phẩm ${name}`}
    >
      <div className="product-card__image-wrapper">
        {imageUrl ? (
          <img
            src={imageUrl}
            alt={name}
            className="product-card__image"
            loading="lazy"
          />
        ) : (
          <div className="product-card__image-placeholder">
            <span aria-hidden="true">📦</span>
            <span>Chưa có ảnh</span>
          </div>
        )}

        <div className="product-card__badges">
          {flashSale && (
            <span className="product-card__badge product-card__badge--flash">
              Flash sale
            </span>
          )}

          {Number(quantity ?? 0) <= 0 && (
            <span className="product-card__badge product-card__badge--out-of-stock">
              Hết hàng
            </span>
          )}
        </div>
      </div>

      <div className="product-card__body">
        <div className="product-card__meta">
          {categoryName && (
            <span className="product-card__category">{categoryName}</span>
          )}

          {shopName && (
            <span className="product-card__shop" title={shopName}>
              {shopName}
            </span>
          )}
        </div>

        <h2 className="product-card__name">{name}</h2>

        <div className="product-card__pricing">
          <strong className="product-card__current-price">
            {formatCurrency(displayPrice)}
          </strong>

          {hasSalePrice && (
            <span className="product-card__original-price">
              {formatCurrency(price)}
            </span>
          )}
        </div>

        {flashSale && remainingFlashSaleQuantity !== null && (
          <p className="product-card__flash-stock">
            Còn {remainingFlashSaleQuantity} sản phẩm giá ưu đãi
          </p>
        )}

        {!flashSale && (
          <p
            className={
              Number(quantity ?? 0) <= 0
                ? "product-card__stock product-card__stock--empty"
                : "product-card__stock"
            }
          >
            {Number(quantity ?? 0) <= 0
              ? "Sản phẩm hiện đã hết hàng"
              : `Còn ${quantity ?? 0} sản phẩm`}
          </p>
        )}
      </div>
    </Link>
  );
}

export default ProductCard;
