import { useEffect, useState } from "react";

import { Link, useParams } from "react-router-dom";

import productApi from "../../api/productApi";
import shopApi from "../../api/shopApi";

import SimilarProducts from "../../components/product/SimilarProducts";
import ProductGallery from "../../components/product/ProductGallery";
import ProductInfo from "../../components/product/ProductInfo";
import ReviewSection from "../../components/review/ReviewSection";
import ShopSummaryCard from "../../components/shop/ShopSummaryCard";
import WishlistButton from "../../components/wishlist/WishlistButton";
import LoadingSpinner from "../../components/common/LoadingSpinner";

import "./ProductDetailPage.css";

function ProductDetailPage() {
  const { id } = useParams();

  const [product, setProduct] = useState(null);

  const [shop, setShop] = useState(null);

  const [shopLoading, setShopLoading] = useState(false);

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    const fetchProduct = async () => {
      try {
        setLoading(true);
        setError("");
        setProduct(null);
        setShop(null);

        const response = await productApi.getById(id);

        console.log("Product detail response:", response.data);

        const productData = response.data?.data;

        if (!productData) {
          setError("Product not found.");

          return;
        }

        if (cancelled) {
          return;
        }

        setProduct(productData);

        if (productData.shopId) {
          try {
            setShopLoading(true);

            const shopResponse = await shopApi.getById(productData.shopId);

            if (!cancelled) {
              setShop(shopResponse.data?.data ?? null);
            }
          } catch (shopError) {
            console.error("Unable to load shop:", shopError);

            if (!cancelled) {
              setShop(null);
            }
          } finally {
            if (!cancelled) {
              setShopLoading(false);
            }
          }
        }
      } catch (err) {
        console.error("Unable to load product:", err);

        if (cancelled) {
          return;
        }

        if (err.response?.status === 404) {
          setError("Product not found.");

          return;
        }

        setError(
          err.response?.data?.message ??
            "Unable to load product. Please try again.",
        );
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    void fetchProduct();

    return () => {
      cancelled = true;
    };
  }, [id]);

  if (loading) {
    return (
      <main>
        <LoadingSpinner size="large" />
      </main>
    );
  }

  if (error) {
    return (
      <main>
        <h1>Product detail</h1>

        <p role="alert">{error}</p>

        <Link to="/products">Back to products</Link>
      </main>
    );
  }

  if (!product) {
    return null;
  }

  return (
    <main>
      <Link to="/products">← Back to products</Link>

      <article className="product-detail">
        <div className="product-detail__media">
          <ProductGallery images={product.images} productName={product.name} />

          <WishlistButton productId={product.id} />
        </div>

        <ProductInfo product={product} />
      </article>

      <ShopSummaryCard shop={shop} product={product} loading={shopLoading} />

      <ReviewSection productId={product.id} />

      <SimilarProducts productId={product.id} />
    </main>
  );
}

export default ProductDetailPage;
