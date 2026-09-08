import { useState, useEffect } from "react";

import "./ProductGallery.css";

export default function ProductGallery({ images = [], productName }) {
  const [selectedImage, setSelectedImage] = useState(null);

  useEffect(() => {
    if (images.length > 0) {
      setSelectedImage(images[0].imageUrl ?? images[0].url);
    }
  }, [images]);

  if (images.length === 0) {
    return (
      <div className="product-gallery">
        <div className="product-gallery-empty">No image</div>
      </div>
    );
  }

  return (
    <div className="product-gallery">
      <div className="product-gallery-main">
        <img src={selectedImage} alt={productName} />
      </div>

      <div className="product-gallery-thumbnails">
        {images.map((image) => {
          const url = image.imageUrl ?? image.url;

          return (
            <img
              key={image.id}
              src={url}
              alt={productName}
              className={selectedImage === url ? "active" : ""}
              onClick={() => setSelectedImage(url)}
            />
          );
        })}
      </div>
    </div>
  );
}
