import { useEffect, useState } from "react";

import flashSaleApi from "../../api/flashSaleApi";

import ProductCard from "../../components/product/ProductCard";
import HomePagination from "../../components/home/HomePagination";
import LoadingSpinner from "../../components/common/LoadingSpinner";

import "../home/HomePage.css";

function mapFlashSaleToProduct(flashSale) {
  return {
    id: flashSale.productId,
    name: flashSale.productName,
    price: flashSale.originalPrice,
    salePrice: flashSale.salePrice,
    images: flashSale.productImage
      ? [
          {
            imageUrl: flashSale.productImage,
          },
        ]
      : [],
    quantity: flashSale.remainingQuantity,
    flashSale: true,
    flashSaleQuantity: flashSale.quantity,
    flashSaleSold: flashSale.sold,
  };
}

function FlashSalePage() {
  const [items, setItems] = useState([]);

  const [page, setPage] = useState(0);

  const [totalPages, setTotalPages] = useState(0);

  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);

        const response = await flashSaleApi.getActive({
          page,
          size: 18,
        });

        const data = response.data?.data;

        setItems(Array.isArray(data?.content) ? data.content : []);

        setTotalPages(Number(data?.totalPages ?? 0));
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [page]);

  return (
    <main className="home-page">
      <section className="home-section">
        <div className="home-section__heading">
          <h2>⚡ FLASH SALE</h2>
        </div>

        {loading ? (
          <div className="home-section__state">
            <LoadingSpinner size="medium" />
          </div>
        ) : (
          <>
            <div className="home-product-grid">
              {items.map((item) => (
                <ProductCard
                  key={item.id}
                  product={mapFlashSaleToProduct(item)}
                />
              ))}
            </div>

            <HomePagination
              page={page}
              totalPages={totalPages}
              onChange={setPage}
            />
          </>
        )}
      </section>
    </main>
  );
}

export default FlashSalePage;
