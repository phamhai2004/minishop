import { useEffect, useState } from "react";

import sellerProductApi from "../../../api/sellerProductApi";
import SellerProductForm from "./SellerProductForm";
import LoadingSpinner from "../../common/LoadingSpinner";

import "./SellerProductManagement.css";

function SellerProductManagement() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [discontinueProduct, setDiscontinueProduct] = useState(null);
  const [discontinuing, setDiscontinuing] = useState(false);

  const loadProducts = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await sellerProductApi.getAll();

      const apiResponse = response.data;
      const productPage = apiResponse?.data;

      if (!apiResponse?.success || !productPage) {
        throw new Error(
          apiResponse?.message ?? "Không thể tải danh sách sản phẩm.",
        );
      }

      setProducts(
        Array.isArray(productPage.content) ? productPage.content : [],
      );
    } catch (err) {
      console.error("Không thể tải sản phẩm của shop:", err);

      setProducts([]);

      setError(
        err.response?.data?.message ??
          err.message ??
          "Không thể tải danh sách sản phẩm.",
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProducts();
  }, []);

  const getProductImage = (product) => {
    const images = product.images;

    if (!Array.isArray(images) || images.length === 0) {
      return null;
    }

    const primaryImage =
      images.find(
        (image) => image.isPrimary === true || image.primary === true,
      ) ?? images[0];

    if (typeof primaryImage === "string") {
      return primaryImage;
    }

    return (
      primaryImage?.url ??
      primaryImage?.imageUrl ??
      primaryImage?.secureUrl ??
      null
    );
  };

  const handleDiscontinue = (product) => {
    setDiscontinueProduct(product);
  };

  const confirmDiscontinue = async () => {
    if (!discontinueProduct) {
      return;
    }

    try {
      setDiscontinuing(true);
      setError("");

      await sellerProductApi.discontinue(discontinueProduct.id);

      setDiscontinueProduct(null);

      await loadProducts();
    } catch (err) {
      console.error("Không thể ngừng kinh doanh sản phẩm:", err);

      setError(
        err.response?.data?.message ??
          err.message ??
          "Không thể ngừng kinh doanh sản phẩm.",
      );
    } finally {
      setDiscontinuing(false);
    }
  };

  const handleEdit = async (product) => {
    try {
      setError("");

      const response = await sellerProductApi.getById(product.id);
      const apiResponse = response.data;

      if (!apiResponse?.success) {
        throw new Error(
          apiResponse?.message ?? "Không thể tải thông tin sản phẩm.",
        );
      }

      setEditingProduct(apiResponse.data);
    } catch (err) {
      console.error("Không thể tải sản phẩm để sửa:", err);

      setError(
        err.response?.data?.message ??
          err.message ??
          "Không thể tải thông tin sản phẩm.",
      );
    }
  };

  if (loading) {
    return (
      <section className="seller-product-management">
        <div className="seller-product-management__loading">
          <LoadingSpinner size="large" />
        </div>
      </section>
    );
  }

  return (
    <>
      <section className="seller-product-management">
        <div className="seller-product-management__header">
          <div>
            <h1>Quản lý sản phẩm</h1>

            <p>Quản lý các sản phẩm thuộc shop của bạn.</p>
          </div>

          <button
            type="button"
            className="seller-product-management__add-button"
            onClick={() => setShowCreateForm(true)}
          >
            + Thêm sản phẩm
          </button>
        </div>

        {error && (
          <div className="seller-product-management__error">{error}</div>
        )}

        {!error && products.length === 0 && (
          <div className="seller-product-management__empty">
            Shop chưa có sản phẩm nào.
          </div>
        )}

        {products.length > 0 && (
          <div className="seller-product-management__list">
            {products.map((product) => {
              const imageUrl = getProductImage(product);

              return (
                <article
                  key={product.id}
                  className="seller-product-management__card"
                >
                  <div className="seller-product-management__image">
                    {imageUrl ? (
                      <img src={imageUrl} alt={product.name} />
                    ) : (
                      <div className="seller-product-management__image-placeholder">
                        📦
                      </div>
                    )}
                  </div>

                  <div className="seller-product-management__info">
                    <h2>{product.name}</h2>

                    <div className="seller-product-management__details">
                      <span>
                        Giá:{" "}
                        {Number(product.price ?? 0).toLocaleString("vi-VN")} ₫
                      </span>

                      <span>Số lượng: {product.quantity ?? 0}</span>

                      <span>Danh mục: {product.categoryName ?? "Chưa có"}</span>

                      <span>
                        Trạng thái:{" "}
                        {product.statusName ??
                          product.status ??
                          "Không xác định"}
                      </span>
                    </div>
                  </div>

                  <div className="seller-product-management__actions">
                    <button
                      type="button"
                      className="seller-product-management__edit-button"
                      onClick={() => handleEdit(product)}
                    >
                      Sửa
                    </button>

                    <button
                      type="button"
                      className="seller-product-management__discontinue-button"
                      onClick={() => handleDiscontinue(product)}
                    >
                      Ngừng kinh doanh
                    </button>
                  </div>
                </article>
              );
            })}
          </div>
        )}
      </section>
      {showCreateForm && (
        <SellerProductForm
          onClose={() => setShowCreateForm(false)}
          onSuccess={() => {
            setShowCreateForm(false);
            loadProducts();
          }}
        />
      )}

      {editingProduct && (
        <SellerProductForm
          product={editingProduct}
          onClose={() => setEditingProduct(null)}
          onSuccess={() => {
            setEditingProduct(null);
            loadProducts();
          }}
        />
      )}
      {discontinueProduct && (
        <div
          className="seller-product-management__modal-overlay"
          role="presentation"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget && !discontinuing) {
              setDiscontinueProduct(null);
            }
          }}
        >
          <div
            className="seller-product-management__confirm-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="discontinue-product-title"
          >
            <div className="seller-product-management__confirm-icon">⚠️</div>

            <h2 id="discontinue-product-title">Ngừng kinh doanh sản phẩm?</h2>

            <p>
              Bạn có chắc muốn ngừng kinh doanh sản phẩm{" "}
              <strong>"{discontinueProduct.name}"</strong> không?
            </p>

            <div className="seller-product-management__confirm-actions">
              <button
                type="button"
                className="seller-product-management__confirm-cancel"
                onClick={() => setDiscontinueProduct(null)}
                disabled={discontinuing}
              >
                Hủy
              </button>

              <button
                type="button"
                className="seller-product-management__confirm-submit"
                onClick={confirmDiscontinue}
                disabled={discontinuing}
              >
                {discontinuing ? (
                  <LoadingSpinner size="small" inline variant="light" />
                ) : (
                  "Ngừng kinh doanh"
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default SellerProductManagement;
