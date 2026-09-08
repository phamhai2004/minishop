import { useEffect, useRef, useState } from "react";

import sellerProductApi from "../../../api/sellerProductApi";
import categoryApi from "../../../api/categoryApi";
import productOptionApi from "../../../api/productOptionApi";

import LoadingSpinner from "../../common/LoadingSpinner";

import "./SellerProductForm.css";

const INITIAL_FORM = {
  name: "",
  price: "",
  quantity: "",
  description: "",
  categoryId: "",
  optionSelections: {},
};

const normalizeText = (value) =>
  String(value ?? "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/gi, "d")
    .replace(/[^a-zA-Z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .toUpperCase();

const createSku = (productName, variantName) => {
  const productPart = normalizeText(productName).slice(0, 24) || "SP";
  const variantPart = normalizeText(variantName).slice(0, 30) || "VARIANT";
  const randomPart = Math.random().toString(36).slice(2, 7).toUpperCase();

  return `${productPart}-${variantPart}-${randomPart}`;
};

function SellerProductForm({ product, onClose, onSuccess }) {
  const isEditMode = Boolean(product?.id);

  const [form, setForm] = useState(INITIAL_FORM);

  const [variants, setVariants] = useState([]);

  const [images, setImages] = useState([]);

  const [pendingImageFiles, setPendingImageFiles] = useState([]);

  const [uploadingImages, setUploadingImages] = useState(false);

  const [imageError, setImageError] = useState("");

  const [categories, setCategories] = useState([]);

  const [optionTypes, setOptionTypes] = useState([]);

  const [optionValues, setOptionValues] = useState({});

  const [loadingCategories, setLoadingCategories] = useState(true);

  const [loadingOptionTypes, setLoadingOptionTypes] = useState(true);

  const [submitting, setSubmitting] = useState(false);

  const [error, setError] = useState("");

  const [fieldErrors, setFieldErrors] = useState({});

  const skipNextVariantGeneration = useRef(false);

  const hasVariants = variants.length > 0;

  useEffect(() => {
    if (!product) {
      skipNextVariantGeneration.current = true;
      setForm(INITIAL_FORM);
      setVariants([]);
      setImages([]);
      setPendingImageFiles([]);
      setImageError("");
      setError("");
      setFieldErrors({});
      return;
    }

    const productVariants = Array.isArray(product.variants)
      ? product.variants
      : [];

    skipNextVariantGeneration.current = true;

    const optionSelections = {};

    productVariants.forEach((variant) => {
      if (!Array.isArray(variant.options)) {
        return;
      }

      variant.options.forEach((option) => {
        const typeId = Number(option.optionTypeId);
        const valueId = Number(option.optionValueId);

        if (!optionSelections[typeId]) {
          optionSelections[typeId] = [];
        }

        if (!optionSelections[typeId].includes(valueId)) {
          optionSelections[typeId].push(valueId);
        }
      });
    });

    setForm({
      name: product.name ?? "",
      price: productVariants.length > 0 ? "" : (product.price ?? ""),
      quantity: productVariants.length > 0 ? "" : (product.quantity ?? ""),
      description: product.description ?? "",
      categoryId: product.categoryId ?? "",
      optionSelections,
    });

    setVariants(
      productVariants.map((variant) => ({
        id: variant.id,
        price: Number(variant.price ?? 0),
        quantity: Number(variant.quantity ?? 0),
        sku: variant.sku ?? "",
        optionValueIds: Array.isArray(variant.options)
          ? variant.options.map((option) => Number(option.optionValueId))
          : [],
      })),
    );

    setImages(Array.isArray(product.images) ? product.images : []);

    Object.keys(optionSelections).forEach((typeId) => {
      const numericTypeId = Number(typeId);

      if (!optionValues[numericTypeId]) {
        loadOptionValues(numericTypeId);
      }
    });

    setError("");
    setFieldErrors({});
  }, [product]);

  useEffect(() => {
    const loadCategories = async () => {
      try {
        setLoadingCategories(true);

        const response = await categoryApi.getAll();

        const responseData = response.data;

        const categoryData = responseData?.data ?? responseData;

        setCategories(
          Array.isArray(categoryData)
            ? categoryData
            : (categoryData?.content ?? []),
        );
      } catch (err) {
        console.error("Không thể tải danh mục:", err);

        setError(
          err.response?.data?.message ?? "Không thể tải danh mục sản phẩm.",
        );
      } finally {
        setLoadingCategories(false);
      }
    };

    loadCategories();
  }, []);

  useEffect(() => {
    const loadOptionTypes = async () => {
      try {
        setLoadingOptionTypes(true);

        const response = await productOptionApi.getAllTypes();

        const responseData = response.data;
        const data = responseData?.data ?? responseData;

        setOptionTypes(Array.isArray(data) ? data : []);
      } catch (err) {
        console.error("Không thể tải loại phân loại:", err);

        setError(
          err.response?.data?.message ??
            "Không thể tải danh sách phân loại sản phẩm.",
        );
      } finally {
        setLoadingOptionTypes(false);
      }
    };

    loadOptionTypes();
  }, []);

  useEffect(() => {
    if (skipNextVariantGeneration.current) {
      skipNextVariantGeneration.current = false;
      return;
    }

    const generatedVariants = generateVariants();

    setVariants((previousVariants) => {
      return generatedVariants.map((generated) => {
        const existing = previousVariants.find(
          (variant) =>
            variant.optionValueIds.length === generated.optionValueIds.length &&
            variant.optionValueIds.every((id) =>
              generated.optionValueIds.includes(id),
            ),
        );

        if (existing) {
          return existing;
        }

        return {
          ...generated,
          sku: createSku(form.name, getVariantName(generated.optionValueIds)),
        };
      });
    });
  }, [form.optionSelections]);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));

    setFieldErrors((previous) => ({
      ...previous,
      [name]: "",
    }));

    setError("");
  };

  const validate = () => {
    const errors = {};

    if (!form.name.trim()) {
      errors.name = "Vui lòng nhập tên sản phẩm.";
    }

    if (!hasVariants) {
      const price = Number(form.price);

      if (!form.price) {
        errors.price = "Vui lòng nhập giá sản phẩm.";
      } else if (!Number.isFinite(price) || price <= 0) {
        errors.price = "Giá sản phẩm phải lớn hơn 0.";
      }

      const quantity = Number(form.quantity);

      if (!form.quantity) {
        errors.quantity = "Vui lòng nhập số lượng.";
      } else if (!Number.isInteger(quantity) || quantity < 1) {
        errors.quantity = "Số lượng phải là số nguyên lớn hơn 0.";
      }
    }

    if (!form.categoryId) {
      errors.categoryId = "Vui lòng chọn danh mục.";
    }

    if (!form.description.trim()) {
      errors.description = "Vui lòng nhập mô tả sản phẩm.";
    } else if (form.description.length > 500) {
      errors.description = "Mô tả không được quá 500 ký tự.";
    }

    if (hasVariants) {
      if (variants.length === 0) {
        errors.variants = "Vui lòng chọn ít nhất một giá trị phân loại.";
      } else {
        const invalidVariant = variants.some(
          (variant) =>
            !Number.isFinite(Number(variant.price)) ||
            Number(variant.price) <= 0 ||
            !Number.isInteger(Number(variant.quantity)) ||
            Number(variant.quantity) < 1 ||
            !variant.sku?.trim(),
        );

        if (invalidVariant) {
          errors.variants =
            "Vui lòng kiểm tra giá, số lượng và SKU của các sản phẩm.";
        }

        const skus = variants.map((variant) => variant.sku.trim());
        const hasDuplicateSku = new Set(skus).size !== skus.length;

        if (hasDuplicateSku) {
          errors.variants = "SKU của các sản phẩm không được trùng nhau.";
        }
      }
    }

    setFieldErrors(errors);

    return Object.keys(errors).length === 0;
  };

  const loadOptionValues = async (typeId) => {
    try {
      const response = await productOptionApi.getValuesByType(typeId);

      const responseData = response.data;
      const data = responseData?.data ?? responseData;

      setOptionValues((previous) => ({
        ...previous,
        [typeId]: Array.isArray(data) ? data : [],
      }));
    } catch (err) {
      console.error("Không thể tải giá trị phân loại:", err);

      setError(
        err.response?.data?.message ?? "Không thể tải giá trị phân loại.",
      );
    }
  };

  const handleOptionTypeChange = async (typeId) => {
    const numericTypeId = Number(typeId);

    setForm((previous) => {
      const currentSelections = previous.optionSelections ?? {};

      if (currentSelections[numericTypeId]) {
        const nextSelections = { ...currentSelections };
        delete nextSelections[numericTypeId];

        return {
          ...previous,
          optionSelections: nextSelections,
        };
      }

      return {
        ...previous,
        optionSelections: {
          ...currentSelections,
          [numericTypeId]: [],
        },
      };
    });

    if (!optionValues[numericTypeId]) {
      await loadOptionValues(numericTypeId);
    }
  };

  const handleOptionValueChange = (typeId, valueId) => {
    setForm((previous) => {
      const currentValues = previous.optionSelections?.[typeId] ?? [];

      const exists = currentValues.includes(valueId);

      const nextValues = exists
        ? currentValues.filter((id) => id !== valueId)
        : [...currentValues, valueId];

      return {
        ...previous,
        optionSelections: {
          ...previous.optionSelections,
          [typeId]: nextValues,
        },
      };
    });
  };

  const handleVariantChange = (index, field, value) => {
    setVariants((previous) =>
      previous.map((variant, variantIndex) => {
        if (variantIndex !== index) {
          return variant;
        }

        return {
          ...variant,
          [field]:
            field === "price" || field === "quantity" ? Number(value) : value,
        };
      }),
    );
  };

  const getMinVariantPrice = () => {
    if (variants.length === 0) {
      return 0;
    }

    return Math.min(...variants.map((variant) => Number(variant.price)));
  };

  const getTotalVariantQuantity = () => {
    return variants.reduce(
      (total, variant) => total + Number(variant.quantity || 0),
      0,
    );
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!validate()) {
      return;
    }

    try {
      setSubmitting(true);
      setError("");
      const request = {
        name: form.name.trim(),
        price: hasVariants ? getMinVariantPrice() : Number(form.price),
        quantity: hasVariants
          ? getTotalVariantQuantity()
          : Number(form.quantity),
        description: form.description.trim(),
        categoryId: Number(form.categoryId),
        variants,
      };

      const response = isEditMode
        ? await sellerProductApi.update(product.id, request)
        : await sellerProductApi.create(request);

      const responseData = response.data;

      if (responseData?.success === false) {
        throw new Error(
          responseData.message ??
            (isEditMode
              ? "Không thể cập nhật sản phẩm."
              : "Không thể tạo sản phẩm."),
        );
      }

      const savedProduct = responseData?.data ?? responseData;
      const productId = isEditMode ? product.id : savedProduct?.id;

      if (!productId) {
        throw new Error("Không xác định được ID sản phẩm.");
      }

      if (!isEditMode && pendingImageFiles.length > 0) {
        setUploadingImages(true);

        await sellerProductApi.uploadImages(productId, pendingImageFiles);
      }

      onSuccess?.(savedProduct);
      onClose();
    } catch (err) {
      console.error(
        isEditMode ? "Không thể cập nhật sản phẩm:" : "Không thể tạo sản phẩm:",
        err,
      );

      setError(
        err.response?.data?.message ??
          err.message ??
          (isEditMode
            ? "Không thể cập nhật sản phẩm."
            : "Không thể tạo sản phẩm."),
      );
    } finally {
      setSubmitting(false);
    }
  };

  const MAX_IMAGES = 8;
  const ALLOWED_IMAGE_TYPES = ["image/jpeg", "image/png", "image/webp"];
  const MAX_IMAGE_SIZE = 5 * 1024 * 1024;

  const getImageUrl = (image) => {
    if (typeof image === "string") {
      return image;
    }

    return image?.imageUrl ?? image?.url ?? image?.secureUrl ?? null;
  };

  const isPrimaryImage = (image) =>
    image?.primaryImage === true ||
    image?.isPrimary === true ||
    image?.primary === true;

  const handleImageSelect = async (event) => {
    const files = Array.from(event.target.files ?? []);

    if (files.length === 0) {
      return;
    }

    setImageError("");

    const currentImageCount = isEditMode
      ? images.length
      : pendingImageFiles.length;

    const remainingSlots = MAX_IMAGES - currentImageCount;

    if (files.length > remainingSlots) {
      setImageError(
        `Sản phẩm chỉ được có tối đa ${MAX_IMAGES} ảnh. Bạn còn ${remainingSlots} vị trí.`,
      );
      event.target.value = "";
      return;
    }

    const invalidFile = files.find(
      (file) =>
        !ALLOWED_IMAGE_TYPES.includes(file.type) || file.size > MAX_IMAGE_SIZE,
    );

    if (invalidFile) {
      setImageError(
        "Chỉ chấp nhận ảnh JPG, PNG hoặc WEBP và kích thước tối đa 5MB/ảnh.",
      );
      event.target.value = "";
      return;
    }

    if (!isEditMode) {
      setPendingImageFiles((previous) => [...previous, ...files]);
      event.target.value = "";
      return;
    }

    try {
      setUploadingImages(true);

      const response = await sellerProductApi.uploadImages(product.id, files);

      const responseData = response.data;

      const uploadedImages = Array.isArray(responseData?.data)
        ? responseData.data
        : Array.isArray(responseData)
          ? responseData
          : [];

      setImages((previous) => [...previous, ...uploadedImages]);
    } catch (err) {
      console.error("Không thể tải ảnh sản phẩm:", err);

      setImageError(
        err.response?.data?.message ??
          "Không thể tải ảnh lên. Vui lòng thử lại.",
      );
    } finally {
      setUploadingImages(false);
      event.target.value = "";
    }
  };

  const handleSetPrimaryImage = async (imageId) => {
    try {
      setImageError("");

      const response = await sellerProductApi.setPrimaryImage(imageId);

      const updatedImage = response.data?.data ?? response.data;

      setImages((previous) =>
        previous.map((image) => ({
          ...image,
          primaryImage: image.id === (updatedImage?.id ?? imageId),
        })),
      );
    } catch (err) {
      console.error("Không thể đặt ảnh đại diện:", err);

      setImageError(
        err.response?.data?.message ?? "Không thể đặt ảnh đại diện.",
      );
    }
  };

  const handleDeleteImage = async (imageId) => {
    try {
      setImageError("");

      await sellerProductApi.deleteImage(imageId);

      setImages((previous) => {
        const deletedImage = previous.find((image) => image.id === imageId);

        const remaining = previous.filter((image) => image.id !== imageId);

        if (
          deletedImage &&
          isPrimaryImage(deletedImage) &&
          remaining.length > 0
        ) {
          return remaining.map((image, index) => ({
            ...image,
            primaryImage: index === 0,
          }));
        }

        return remaining;
      });
    } catch (err) {
      console.error("Không thể xóa ảnh:", err);

      setImageError(err.response?.data?.message ?? "Không thể xóa ảnh.");
    }
  };

  const getOptionValueName = (valueId) => {
    for (const values of Object.values(optionValues)) {
      const value = values.find((item) => Number(item.id) === Number(valueId));

      if (value) {
        return value.name;
      }
    }

    return `Giá trị #${valueId}`;
  };

  const getVariantName = (optionValueIds) => {
    return optionValueIds
      .map((valueId) => getOptionValueName(valueId))
      .join(" - ");
  };

  const generateVariants = () => {
    const selectedTypeIds = Object.keys(form.optionSelections ?? {}).filter(
      (typeId) => form.optionSelections[typeId]?.length > 0,
    );

    if (selectedTypeIds.length === 0) {
      return [];
    }

    let combinations = [[]];

    selectedTypeIds.forEach((typeId) => {
      const valueIds = form.optionSelections[typeId];

      const nextCombinations = [];

      combinations.forEach((combination) => {
        valueIds.forEach((valueId) => {
          nextCombinations.push([...combination, valueId]);
        });
      });

      combinations = nextCombinations;
    });

    return combinations.map((optionValueIds) => ({
      price: 0,
      quantity: 0,
      sku: "",
      optionValueIds,
    }));
  };

  return (
    <div className="seller-product-form__overlay" role="presentation">
      <div
        className="seller-product-form"
        role="dialog"
        aria-modal="true"
        aria-labelledby="seller-product-form-title"
      >
        <div className="seller-product-form__header">
          <div>
            <h2 id="seller-product-form-title">
              {isEditMode ? "Sửa sản phẩm" : "Thêm sản phẩm"}
            </h2>

            <p>
              {isEditMode
                ? "Cập nhật thông tin sản phẩm của shop."
                : "Nhập thông tin sản phẩm của shop."}
            </p>
          </div>

          <button
            type="button"
            className="seller-product-form__close"
            onClick={onClose}
            disabled={submitting}
            aria-label="Đóng"
          >
            ×
          </button>
        </div>

        {error && (
          <div className="seller-product-form__error" role="alert">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="seller-product-form__field">
            <label htmlFor="seller-product-name">Tên sản phẩm</label>

            <input
              id="seller-product-name"
              name="name"
              type="text"
              value={form.name}
              onChange={handleChange}
              placeholder="Nhập tên sản phẩm"
              disabled={submitting}
            />

            {fieldErrors.name && <small>{fieldErrors.name}</small>}
          </div>

          {!hasVariants && (
            <div className="seller-product-form__row">
              <div className="seller-product-form__field">
                <label htmlFor="seller-product-price">Giá</label>

                <input
                  id="seller-product-price"
                  name="price"
                  type="number"
                  min="0.01"
                  step="0.01"
                  value={form.price}
                  onChange={handleChange}
                  placeholder="Nhập giá"
                  disabled={submitting}
                />

                {fieldErrors.price && <small>{fieldErrors.price}</small>}
              </div>

              <div className="seller-product-form__field">
                <label htmlFor="seller-product-quantity">Số lượng</label>

                <input
                  id="seller-product-quantity"
                  name="quantity"
                  type="number"
                  min="1"
                  step="1"
                  value={form.quantity}
                  onChange={handleChange}
                  placeholder="Nhập số lượng"
                  disabled={submitting}
                />

                {fieldErrors.quantity && <small>{fieldErrors.quantity}</small>}
              </div>
            </div>
          )}

          <div className="seller-product-form__field">
            <label htmlFor="seller-product-category">Danh mục</label>

            {loadingCategories ? (
              <LoadingSpinner size="small" />
            ) : (
              <select
                id="seller-product-category"
                name="categoryId"
                value={form.categoryId}
                onChange={handleChange}
                disabled={submitting}
              >
                <option value="">Chọn danh mục</option>

                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
            )}

            {fieldErrors.categoryId && <small>{fieldErrors.categoryId}</small>}
          </div>

          <div className="seller-product-form__field">
            <label>Phân loại sản phẩm</label>

            {loadingOptionTypes ? (
              <LoadingSpinner size="small" />
            ) : optionTypes.length === 0 ? (
              <div>Chưa có loại phân loại nào.</div>
            ) : (
              <div className="seller-product-form__option-types">
                {optionTypes.map((type) => {
                  const typeId = Number(type.id);

                  const selectedValues = form.optionSelections?.[typeId] ?? [];

                  const isSelected = Object.prototype.hasOwnProperty.call(
                    form.optionSelections ?? {},
                    typeId,
                  );

                  return (
                    <div
                      key={type.id}
                      className="seller-product-form__option-group"
                    >
                      <label className="seller-product-form__checkbox">
                        <input
                          type="checkbox"
                          checked={isSelected}
                          onChange={() => handleOptionTypeChange(type.id)}
                          disabled={submitting}
                        />

                        <span>{type.name}</span>
                      </label>

                      {isSelected && (
                        <div className="seller-product-form__option-values">
                          {!optionValues[typeId] ? (
                            <LoadingSpinner size="small" inline />
                          ) : optionValues[typeId].length === 0 ? (
                            <span>Chưa có giá trị cho {type.name}.</span>
                          ) : (
                            optionValues[typeId].map((value) => {
                              const valueId = Number(value.id);

                              return (
                                <label
                                  key={value.id}
                                  className="seller-product-form__value"
                                >
                                  <input
                                    type="checkbox"
                                    checked={selectedValues.includes(valueId)}
                                    onChange={() =>
                                      handleOptionValueChange(typeId, valueId)
                                    }
                                    disabled={submitting}
                                  />

                                  <span>{value.name}</span>
                                </label>
                              );
                            })
                          )}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          <div className="seller-product-form__field">
            <div className="seller-product-form__images-header">
              <div>
                <label>Ảnh sản phẩm</label>
                <span>
                  {isEditMode
                    ? `${images.length}/${MAX_IMAGES} ảnh`
                    : `${pendingImageFiles.length}/${MAX_IMAGES} ảnh`}
                </span>
              </div>

              <label
                htmlFor="seller-product-images"
                className="seller-product-form__upload-button"
              >
                {uploadingImages ? (
                  <LoadingSpinner size="small" inline />
                ) : (
                  "+ Thêm ảnh"
                )}
              </label>

              <input
                id="seller-product-images"
                type="file"
                accept="image/jpeg,image/png,image/webp"
                multiple
                onChange={handleImageSelect}
                disabled={
                  submitting ||
                  uploadingImages ||
                  (isEditMode
                    ? images.length >= MAX_IMAGES
                    : pendingImageFiles.length >= MAX_IMAGES)
                }
                hidden
              />
            </div>

            <div className="seller-product-form__image-hint">
              JPG, PNG hoặc WEBP · tối đa 5MB/ảnh · tối đa 8 ảnh
            </div>

            {imageError && (
              <small className="seller-product-form__image-error">
                {imageError}
              </small>
            )}

            {isEditMode ? (
              images.length === 0 ? (
                <div className="seller-product-form__images-empty">
                  Chưa có ảnh sản phẩm.
                </div>
              ) : (
                <div className="seller-product-form__images-grid">
                  {images.map((image) => {
                    const imageUrl = getImageUrl(image);
                    const primary = isPrimaryImage(image);

                    return (
                      <div
                        key={image.id}
                        className={`seller-product-form__image-card ${
                          primary
                            ? "seller-product-form__image-card--primary"
                            : ""
                        }`}
                      >
                        {imageUrl && (
                          <img
                            src={imageUrl}
                            alt={`${form.name} - ảnh sản phẩm`}
                            className="seller-product-form__image-preview"
                          />
                        )}

                        {primary && (
                          <span className="seller-product-form__primary-badge">
                            Ảnh đại diện
                          </span>
                        )}

                        <div className="seller-product-form__image-actions">
                          {!primary && (
                            <button
                              type="button"
                              onClick={() => handleSetPrimaryImage(image.id)}
                              disabled={uploadingImages || submitting}
                            >
                              Đặt đại diện
                            </button>
                          )}

                          <button
                            type="button"
                            onClick={() => handleDeleteImage(image.id)}
                            disabled={uploadingImages || submitting}
                          >
                            Xóa
                          </button>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )
            ) : pendingImageFiles.length === 0 ? (
              <div className="seller-product-form__images-empty">
                Chưa chọn ảnh sản phẩm.
              </div>
            ) : (
              <div className="seller-product-form__images-grid">
                {pendingImageFiles.map((file, index) => {
                  const previewUrl = URL.createObjectURL(file);

                  return (
                    <div
                      key={`${file.name}-${file.lastModified}-${index}`}
                      className="seller-product-form__image-card"
                    >
                      <img
                        src={previewUrl}
                        alt={`${form.name || "Sản phẩm"} - ảnh ${index + 1}`}
                        className="seller-product-form__image-preview"
                      />

                      <div className="seller-product-form__image-actions">
                        <button
                          type="button"
                          onClick={() =>
                            setPendingImageFiles((previous) =>
                              previous.filter(
                                (_, fileIndex) => fileIndex !== index,
                              ),
                            )
                          }
                          disabled={submitting || uploadingImages}
                        >
                          Xóa
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          {variants.length > 0 && (
            <div className="seller-product-form__field">
              <label>Danh sách biến thể</label>

              <div className="seller-product-form__variants">
                <div className="seller-product-form__variants-header">
                  <strong>Có {variants.length} biến thể</strong>

                  <span>Nhập giá, số lượng và SKU cho từng biến thể.</span>
                </div>

                <div className="seller-product-form__variant-list">
                  {variants.map((variant, index) => (
                    <div
                      key={variant.optionValueIds.join("-")}
                      className="seller-product-form__variant"
                    >
                      <div className="seller-product-form__variant-name">
                        {getVariantName(variant.optionValueIds)}
                      </div>

                      <div className="seller-product-form__variant-fields">
                        <div>
                          <label htmlFor={`variant-price-${index}`}>Giá</label>

                          <input
                            id={`variant-price-${index}`}
                            type="number"
                            min="0.01"
                            step="0.01"
                            value={variant.price}
                            onChange={(event) =>
                              handleVariantChange(
                                index,
                                "price",
                                event.target.value,
                              )
                            }
                            disabled={submitting}
                          />
                        </div>

                        <div>
                          <label htmlFor={`variant-quantity-${index}`}>
                            Số lượng
                          </label>

                          <input
                            id={`variant-quantity-${index}`}
                            type="number"
                            min="1"
                            step="1"
                            value={variant.quantity}
                            onChange={(event) =>
                              handleVariantChange(
                                index,
                                "quantity",
                                event.target.value,
                              )
                            }
                            disabled={submitting}
                          />
                        </div>

                        <div>
                          <label htmlFor={`variant-sku-${index}`}>SKU</label>

                          <input
                            id={`variant-sku-${index}`}
                            type="text"
                            value={variant.sku}
                            onChange={(event) =>
                              handleVariantChange(
                                index,
                                "sku",
                                event.target.value,
                              )
                            }
                            placeholder="SKU"
                            disabled={submitting}
                          />
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {fieldErrors.variants && <small>{fieldErrors.variants}</small>}
            </div>
          )}

          <div className="seller-product-form__field">
            <label htmlFor="seller-product-description">Mô tả</label>

            <textarea
              id="seller-product-description"
              name="description"
              rows="5"
              maxLength="500"
              value={form.description}
              onChange={handleChange}
              placeholder="Nhập mô tả sản phẩm..."
              disabled={submitting}
            />

            <div className="seller-product-form__counter">
              {form.description.length}/500
            </div>

            {fieldErrors.description && (
              <small>{fieldErrors.description}</small>
            )}
          </div>

          <div className="seller-product-form__actions">
            <button
              type="button"
              className="seller-product-form__cancel"
              onClick={onClose}
              disabled={submitting}
            >
              Hủy
            </button>

            <button
              type="submit"
              className="seller-product-form__submit"
              disabled={submitting}
            >
              {submitting ? (
                <LoadingSpinner size="small" inline variant="light" />
              ) : isEditMode ? (
                "Lưu thay đổi"
              ) : (
                "Thêm sản phẩm"
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default SellerProductForm;
