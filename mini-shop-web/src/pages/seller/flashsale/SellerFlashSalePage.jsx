import { useEffect, useState } from "react";

import sellerFlashSaleApi from "../../../api/sellerFlashSaleApi";
import sellerProductApi from "../../../api/sellerProductApi";
import LoadingSpinner from "../../../components/common/LoadingSpinner";

import "./SellerFlashSalePage.css";

function formatCurrency(value) {
  if (value == null) {
    return "—";
  }

  return `${Number(value).toLocaleString("vi-VN")} ₫`;
}

function formatDate(value) {
  if (!value) {
    return "—";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function getStatusText(status) {
  switch (status) {
    case "UPCOMING":
      return "Sắp diễn ra";

    case "ONGOING":
      return "Đang diễn ra";

    case "SOLD_OUT":
      return "Hết suất";

    case "ENDED":
      return "Đã kết thúc";

    case "INACTIVE":
      return "Đã tắt";

    default:
      return status ?? "Không xác định";
  }
}

function getStatusClassName(status) {
  return `seller-flash-sale__badge seller-flash-sale__badge--${String(
    status ?? "unknown",
  ).toLowerCase()}`;
}

function SellerFlashSalePage() {
  const [flashSales, setFlashSales] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);
  const [createOpen, setCreateOpen] = useState(false);
  const [editingFlashSale, setEditingFlashSale] = useState(null);
  const [deactivateFlashSale, setDeactivateFlashSale] = useState(null);
  const [deactivating, setDeactivating] = useState(false);
  const [deactivateError, setDeactivateError] = useState("");
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState("");
  const [products, setProducts] = useState([]);
  const [loadingProducts, setLoadingProducts] = useState(false);
  const [createForm, setCreateForm] = useState({
    productId: "",
    salePrice: "",
    quantity: "",
    startTime: "",
    endTime: "",
  });
  const PAGE_SIZE = 20;

  useEffect(() => {
    let cancelled = false;

    const loadFlashSales = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await sellerFlashSaleApi.getAll({
          page,
          size: PAGE_SIZE,
        });

        const apiResponse = response.data;

        if (!apiResponse?.success) {
          throw new Error(
            apiResponse?.message ?? "Không thể tải danh sách Flash Sale.",
          );
        }

        const pageData = apiResponse.data;

        if (!pageData) {
          throw new Error("Dữ liệu danh sách Flash Sale không hợp lệ.");
        }

        if (!cancelled) {
          setFlashSales(
            Array.isArray(pageData.content) ? pageData.content : [],
          );

          setTotalPages(Number(pageData.totalPages ?? 0));

          setTotalElements(Number(pageData.totalElements ?? 0));
        }
      } catch (err) {
        console.error("Unable to load seller flash sales:", err);

        if (!cancelled) {
          setFlashSales([]);
          setTotalPages(0);
          setTotalElements(0);

          setError(
            err.response?.data?.message ??
              err.message ??
              "Không thể tải danh sách Flash Sale.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    loadFlashSales();

    return () => {
      cancelled = true;
    };
  }, [page, reloadKey]);

  const handlePreviousPage = () => {
    if (page <= 0) {
      return;
    }

    setPage((current) => current - 1);
  };

  const handleNextPage = () => {
    if (page >= totalPages - 1) {
      return;
    }

    setPage((current) => current + 1);
  };

  const selectedProduct =
    products.find(
      (product) => Number(product.id) === Number(createForm.productId),
    ) ?? null;

  const resetCreateForm = () => {
    setCreateForm({
      productId: "",
      salePrice: "",
      quantity: "",
      startTime: "",
      endTime: "",
    });

    setCreateError("");
  };

  const loadSellerProducts = async () => {
    try {
      setLoadingProducts(true);
      setCreateError("");

      const response = await sellerProductApi.getAll({
        page: 0,
        size: 100,
        sort: "id",
        direction: "desc",
      });

      const apiResponse = response.data;
      const productPage = apiResponse?.data;

      if (!apiResponse?.success || !productPage) {
        throw new Error(
          apiResponse?.message ?? "Không thể tải danh sách sản phẩm.",
        );
      }

      const productList = Array.isArray(productPage.content)
        ? productPage.content
        : [];

      setProducts(
        productList.filter(
          (product) => String(product.status ?? "").toUpperCase() === "ACTIVE",
        ),
      );
    } catch (err) {
      console.error("Unable to load seller products for flash sale:", err);

      setProducts([]);

      setCreateError(
        err.response?.data?.message ??
          err.message ??
          "Không thể tải danh sách sản phẩm.",
      );
    } finally {
      setLoadingProducts(false);
    }
  };

  const handleOpenCreate = async () => {
    setEditingFlashSale(null);

    resetCreateForm();
    setCreateOpen(true);

    await loadSellerProducts();
  };

  const handleCloseCreate = () => {
    if (creating) {
      return;
    }

    setCreateOpen(false);
    setEditingFlashSale(null);
    resetCreateForm();
  };

  const handleCreateChange = (event) => {
    const { name, value } = event.target;

    setCreateForm((current) => ({
      ...current,
      [name]: value,
    }));

    setCreateError("");
  };

  const validateCreateForm = () => {
    if (!createForm.productId) {
      return "Vui lòng chọn sản phẩm.";
    }

    if (
      !Number.isFinite(Number(createForm.salePrice)) ||
      Number(createForm.salePrice) <= 0
    ) {
      return "Giá Flash Sale phải lớn hơn 0.";
    }

    if (
      selectedProduct &&
      Number(createForm.salePrice) >= Number(selectedProduct.price)
    ) {
      return "Giá Flash Sale phải nhỏ hơn giá bán của sản phẩm.";
    }

    if (
      !Number.isInteger(Number(createForm.quantity)) ||
      Number(createForm.quantity) <= 0
    ) {
      return "Số lượng Flash Sale phải lớn hơn 0.";
    }

    const soldQuantity = editingFlashSale
      ? Number(editingFlashSale.sold ?? 0)
      : 0;

    if (editingFlashSale && Number(createForm.quantity) < soldQuantity) {
      return "Số lượng Flash Sale không được nhỏ hơn số lượng đã bán.";
    }

    const remainingQuota = Number(createForm.quantity) - soldQuantity;

    if (
      selectedProduct &&
      remainingQuota > Number(selectedProduct.quantity ?? 0)
    ) {
      return "Số lượng Flash Sale còn lại không được vượt quá tồn kho hiện tại.";
    }

    if (!createForm.startTime) {
      return "Vui lòng chọn thời gian bắt đầu.";
    }

    if (!createForm.endTime) {
      return "Vui lòng chọn thời gian kết thúc.";
    }

    if (new Date(createForm.endTime) <= new Date(createForm.startTime)) {
      return "Thời gian kết thúc phải sau thời gian bắt đầu.";
    }

    return "";
  };

  const normalizeDateTime = (value) => {
    if (!value) {
      return value;
    }

    return value.length === 16 ? `${value}:00` : value;
  };

  const toDateTimeLocalValue = (value) => {
    if (!value) {
      return "";
    }

    return String(value).slice(0, 16);
  };

  const handleOpenEdit = async (flashSale) => {
    setEditingFlashSale(flashSale);

    setCreateForm({
      productId: flashSale.productId ?? "",
      salePrice: flashSale.salePrice ?? "",
      quantity: flashSale.quantity ?? "",
      startTime: toDateTimeLocalValue(flashSale.startTime),
      endTime: toDateTimeLocalValue(flashSale.endTime),
    });

    setCreateError("");
    setCreateOpen(true);

    await loadSellerProducts();
  };

  const handleCreateSubmit = async (event) => {
    event.preventDefault();

    const validationMessage = validateCreateForm();

    if (validationMessage) {
      setCreateError(validationMessage);
      return;
    }

    try {
      setCreating(true);
      setCreateError("");

      const payload = {
        productId: Number(createForm.productId),

        salePrice: Number(createForm.salePrice),

        quantity: Number(createForm.quantity),

        startTime: normalizeDateTime(createForm.startTime),

        endTime: normalizeDateTime(createForm.endTime),
      };

      const response = editingFlashSale
        ? await sellerFlashSaleApi.update(editingFlashSale.id, payload)
        : await sellerFlashSaleApi.create(payload);

      const apiResponse = response.data;

      if (!apiResponse?.success) {
        throw new Error(
          apiResponse?.message ??
            (editingFlashSale
              ? "Không thể cập nhật Flash Sale."
              : "Không thể tạo Flash Sale."),
        );
      }

      setCreateOpen(false);
      setEditingFlashSale(null);
      resetCreateForm();

      setPage(0);
      setReloadKey((current) => current + 1);
    } catch (err) {
      console.error("Unable to create flash sale:", err);

      setCreateError(
        err.response?.data?.message ??
          err.message ??
          "Không thể tạo Flash Sale.",
      );
    } finally {
      setCreating(false);
    }
  };

  const hasSoldItems = Number(editingFlashSale?.sold ?? 0) > 0;

  const handleOpenDeactivate = (flashSale) => {
    if (!flashSale?.active) {
      return;
    }

    setDeactivateFlashSale(flashSale);
    setDeactivateError("");
  };

  const handleCloseDeactivate = () => {
    if (deactivating) {
      return;
    }

    setDeactivateFlashSale(null);
    setDeactivateError("");
  };

  const handleDeactivate = async () => {
    if (!deactivateFlashSale?.id) {
      return;
    }

    try {
      setDeactivating(true);
      setDeactivateError("");

      const response = await sellerFlashSaleApi.deactivate(
        deactivateFlashSale.id,
      );

      const apiResponse = response.data;

      if (!apiResponse?.success) {
        throw new Error(apiResponse?.message ?? "Không thể tắt Flash Sale.");
      }

      setDeactivateFlashSale(null);

      setReloadKey((current) => current + 1);
    } catch (err) {
      console.error("Unable to deactivate flash sale:", err);

      setDeactivateError(
        err.response?.data?.message ??
          err.message ??
          "Không thể tắt Flash Sale.",
      );
    } finally {
      setDeactivating(false);
    }
  };

  return (
    <main className="seller-flash-sale-page">
      <div className="seller-flash-sale__container">
        <header className="seller-flash-sale__header">
          <div>
            <h1>Quản lý Flash Sale</h1>

            <p>Quản lý các chương trình Flash Sale của shop.</p>
          </div>

          <button
            type="button"
            className="seller-flash-sale__create-button"
            onClick={handleOpenCreate}
          >
            + Tạo Flash Sale
          </button>
        </header>

        {error && (
          <section className="seller-flash-sale__status-box seller-flash-sale__status-box--error">
            <h2>Không thể tải Flash Sale</h2>

            <p>{error}</p>

            <button
              type="button"
              onClick={() => setReloadKey((current) => current + 1)}
            >
              Thử lại
            </button>
          </section>
        )}

        {loading && (
          <section className="seller-flash-sale__status-box">
            <LoadingSpinner size="large" />
          </section>
        )}

        {!loading && !error && flashSales.length === 0 && (
          <section className="seller-flash-sale__status-box">
            <div className="seller-flash-sale__empty-icon">⚡</div>

            <h2>Chưa có Flash Sale</h2>

            <p>Shop của bạn chưa tạo chương trình Flash Sale nào.</p>
          </section>
        )}

        {!loading && !error && flashSales.length > 0 && (
          <>
            <div className="seller-flash-sale__summary">
              <span>Danh sách Flash Sale</span>

              <strong>{totalElements} chương trình</strong>
            </div>

            <section className="seller-flash-sale__list">
              {flashSales.map((flashSale) => (
                <article key={flashSale.id} className="seller-flash-sale-item">
                  <div className="seller-flash-sale-item__product">
                    {flashSale.productImage ? (
                      <img
                        src={flashSale.productImage}
                        alt={flashSale.productName ?? "Sản phẩm"}
                      />
                    ) : (
                      <div className="seller-flash-sale-item__image-placeholder">
                        📦
                      </div>
                    )}

                    <div className="seller-flash-sale-item__product-info">
                      <strong>{flashSale.productName ?? "Sản phẩm"}</strong>

                      <span>ID sản phẩm: {flashSale.productId}</span>
                    </div>
                  </div>

                  <div className="seller-flash-sale-item__content">
                    <div className="seller-flash-sale-item__top">
                      <div className="seller-flash-sale-item__price">
                        <span>{formatCurrency(flashSale.originalPrice)}</span>

                        <strong>{formatCurrency(flashSale.salePrice)}</strong>
                      </div>

                      <span className={getStatusClassName(flashSale.status)}>
                        {getStatusText(flashSale.status)}
                      </span>
                    </div>

                    <div className="seller-flash-sale-item__details">
                      <div>
                        <span>Tổng suất:</span>

                        <strong>{flashSale.quantity ?? 0}</strong>
                      </div>

                      <div>
                        <span>Đã bán:</span>

                        <strong>{flashSale.sold ?? 0}</strong>
                      </div>

                      <div>
                        <span>Còn lại:</span>

                        <strong>{flashSale.remainingQuantity ?? 0}</strong>
                      </div>

                      <div>
                        <span>Bắt đầu:</span>

                        <strong>{formatDate(flashSale.startTime)}</strong>
                      </div>

                      <div>
                        <span>Kết thúc:</span>

                        <strong>{formatDate(flashSale.endTime)}</strong>
                      </div>
                    </div>
                  </div>

                  <div className="seller-flash-sale-item__actions">
                    <button
                      type="button"
                      onClick={() => handleOpenEdit(flashSale)}
                    >
                      Sửa
                    </button>

                    <button
                      type="button"
                      disabled={!flashSale.active}
                      onClick={() => handleOpenDeactivate(flashSale)}
                    >
                      {flashSale.active ? "Tắt" : "Đã tắt"}
                    </button>
                  </div>
                </article>
              ))}
            </section>
          </>
        )}

        {!loading && !error && totalPages > 0 && (
          <nav
            className="seller-flash-sale__pagination"
            aria-label="Phân trang Flash Sale"
          >
            <button
              type="button"
              disabled={page <= 0}
              onClick={handlePreviousPage}
            >
              ← Trước
            </button>

            <span>
              Trang <strong>{page + 1}</strong>
              {" / "}
              <strong>{totalPages}</strong>
            </span>

            <button
              type="button"
              disabled={page >= totalPages - 1}
              onClick={handleNextPage}
            >
              Sau →
            </button>
          </nav>
        )}
      </div>

      {createOpen && (
        <div
          className="seller-flash-sale-modal"
          role="presentation"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) {
              handleCloseCreate();
            }
          }}
        >
          <div
            className="seller-flash-sale-modal__dialog"
            role="dialog"
            aria-modal="true"
            aria-labelledby="seller-flash-sale-create-title"
          >
            <div className="seller-flash-sale-modal__header">
              <div>
                <h2 id="seller-flash-sale-create-title">
                  {editingFlashSale ? "Sửa Flash Sale" : "Tạo Flash Sale"}
                </h2>

                <p>
                  {editingFlashSale
                    ? "Cập nhật chương trình Flash Sale."
                    : "Tạo chương trình giảm giá theo thời gian cho sản phẩm."}
                </p>
              </div>

              <button
                type="button"
                className="seller-flash-sale-modal__close"
                onClick={handleCloseCreate}
                disabled={creating}
                aria-label="Đóng"
              >
                ×
              </button>
            </div>

            <form
              className="seller-flash-sale-form"
              onSubmit={handleCreateSubmit}
            >
              <div className="seller-flash-sale-form__body">
                {createError && (
                  <div className="seller-flash-sale-form__error">
                    {createError}
                  </div>
                )}

                <label className="seller-flash-sale-form__field seller-flash-sale-form__field--full">
                  <span>Sản phẩm</span>

                  {loadingProducts ? (
                    <LoadingSpinner size="small" />
                  ) : (
                    <select
                      name="productId"
                      value={createForm.productId}
                      onChange={handleCreateChange}
                      disabled={creating || Boolean(editingFlashSale)}
                    >
                      <option value="">Chọn sản phẩm</option>

                      {products.map((product) => (
                        <option key={product.id} value={product.id}>
                          {product.name} — {formatCurrency(product.price)} —
                          Tồn: {product.quantity ?? 0}
                        </option>
                      ))}
                    </select>
                  )}
                </label>

                {selectedProduct && (
                  <div className="seller-flash-sale-form__product-preview seller-flash-sale-form__field--full">
                    <div>
                      <span>Giá hiện tại</span>

                      <strong>{formatCurrency(selectedProduct.price)}</strong>
                    </div>

                    <div>
                      <span>Tồn kho</span>

                      <strong>{selectedProduct.quantity ?? 0}</strong>
                    </div>
                  </div>
                )}

                <label className="seller-flash-sale-form__field">
                  <span>Giá Flash Sale</span>

                  <input
                    type="number"
                    name="salePrice"
                    min="0.01"
                    step="0.01"
                    value={createForm.salePrice}
                    onChange={handleCreateChange}
                    disabled={creating || hasSoldItems}
                    placeholder="Ví dụ: 350000"
                  />
                </label>

                <label className="seller-flash-sale-form__field">
                  <span>Số lượng Flash Sale</span>

                  <input
                    type="number"
                    name="quantity"
                    min="1"
                    step="1"
                    value={createForm.quantity}
                    onChange={handleCreateChange}
                    disabled={creating}
                    placeholder="Ví dụ: 50"
                  />
                </label>

                <label className="seller-flash-sale-form__field">
                  <span>Thời gian bắt đầu</span>

                  <input
                    type="datetime-local"
                    name="startTime"
                    value={createForm.startTime}
                    onChange={handleCreateChange}
                    disabled={creating || hasSoldItems}
                  />
                </label>

                <label className="seller-flash-sale-form__field">
                  <span>Thời gian kết thúc</span>

                  <input
                    type="datetime-local"
                    name="endTime"
                    value={createForm.endTime}
                    onChange={handleCreateChange}
                    disabled={creating || hasSoldItems}
                  />
                </label>
              </div>

              <div className="seller-flash-sale-form__footer">
                <button
                  type="button"
                  className="seller-flash-sale-form__cancel"
                  onClick={handleCloseCreate}
                  disabled={creating}
                >
                  Hủy
                </button>

                <button
                  type="submit"
                  className="seller-flash-sale-form__submit"
                  disabled={creating || loadingProducts}
                >
                  {creating ? (
                    <LoadingSpinner size="small" inline variant="light" />
                  ) : editingFlashSale ? (
                    "Lưu thay đổi"
                  ) : (
                    "Tạo Flash Sale"
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {deactivateFlashSale && (
        <div
          className="seller-flash-sale-modal"
          role="presentation"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) {
              handleCloseDeactivate();
            }
          }}
        >
          <div
            className="seller-flash-sale-modal__dialog seller-flash-sale-deactivate-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="seller-flash-sale-deactivate-title"
          >
            <div className="seller-flash-sale-modal__header">
              <div>
                <h2 id="seller-flash-sale-deactivate-title">Tắt Flash Sale</h2>

                <p>Flash Sale sau khi tắt sẽ không còn áp dụng giá giảm.</p>
              </div>

              <button
                type="button"
                className="seller-flash-sale-modal__close"
                onClick={handleCloseDeactivate}
                disabled={deactivating}
                aria-label="Đóng"
              >
                ×
              </button>
            </div>

            <div className="seller-flash-sale-deactivate__body">
              {deactivateError && (
                <div className="seller-flash-sale-form__error">
                  {deactivateError}
                </div>
              )}

              <p>
                Bạn có chắc muốn tắt Flash Sale của sản phẩm{" "}
                <strong>{deactivateFlashSale.productName ?? "này"}</strong>?
              </p>

              <div className="seller-flash-sale-deactivate__info">
                <div>
                  <span>Giá Flash Sale</span>

                  <strong>
                    {formatCurrency(deactivateFlashSale.salePrice)}
                  </strong>
                </div>

                <div>
                  <span>Đã bán</span>

                  <strong>{deactivateFlashSale.sold ?? 0}</strong>
                </div>

                <div>
                  <span>Còn lại</span>

                  <strong>{deactivateFlashSale.remainingQuantity ?? 0}</strong>
                </div>
              </div>

              <div className="seller-flash-sale-deactivate__warning">
                Sau khi tắt:
                <ul>
                  <li>Sản phẩm sẽ không còn được áp dụng giá Flash Sale.</li>

                  <li>
                    Các đơn hàng đã mua bằng Flash Sale trước đó không bị thay
                    đổi.
                  </li>

                  <li>Số lượng đã bán vẫn được giữ nguyên.</li>
                </ul>
              </div>
            </div>

            <div className="seller-flash-sale-form__footer">
              <button
                type="button"
                className="seller-flash-sale-form__cancel"
                onClick={handleCloseDeactivate}
                disabled={deactivating}
              >
                Hủy
              </button>

              <button
                type="button"
                className="seller-flash-sale-deactivate__confirm"
                onClick={handleDeactivate}
                disabled={deactivating}
              >
                {deactivating ? (
                  <LoadingSpinner size="small" inline variant="light" />
                ) : (
                  "Tắt Flash Sale"
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  );
}

export default SellerFlashSalePage;
