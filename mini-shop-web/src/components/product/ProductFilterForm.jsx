import { useEffect, useState } from "react";

import ProductSearchBox from "./ProductSearchBox";

import "./ProductFilterForm.css";

function ProductFilterForm({
  initialFilters,
  loading,
  onSubmit,
  onReset,
  onImageSearch,
}) {
  const [formData, setFormData] = useState({
    keyword: initialFilters.keyword ?? "",

    minPrice: initialFilters.minPrice ?? "",

    maxPrice: initialFilters.maxPrice ?? "",
  });

  const [validationError, setValidationError] = useState("");

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setFormData({
      keyword: initialFilters.keyword ?? "",
      minPrice: initialFilters.minPrice ?? "",
      maxPrice: initialFilters.maxPrice ?? "",
    });
  }, [
    initialFilters.keyword,
    initialFilters.minPrice,
    initialFilters.maxPrice,
  ]);

  const validatePrices = () => {
    const minPrice =
      formData.minPrice === "" ? null : Number(formData.minPrice);

    const maxPrice =
      formData.maxPrice === "" ? null : Number(formData.maxPrice);

    if (minPrice !== null && (!Number.isFinite(minPrice) || minPrice < 0)) {
      setValidationError("Giá tối thiểu phải là số không âm.");

      return null;
    }

    if (maxPrice !== null && (!Number.isFinite(maxPrice) || maxPrice < 0)) {
      setValidationError("Giá tối đa phải là số không âm.");

      return null;
    }

    if (minPrice !== null && maxPrice !== null && minPrice > maxPrice) {
      setValidationError("Giá tối thiểu không được lớn hơn giá tối đa.");

      return null;
    }

    return {
      minPrice,
      maxPrice,
    };
  };

  const submitFilters = (keywordOverride) => {
    const prices = validatePrices();

    if (!prices) {
      return;
    }

    const submittedKeyword = (keywordOverride ?? formData.keyword).trim();

    onSubmit({
      keyword: submittedKeyword,

      minPrice: prices.minPrice,

      maxPrice: prices.maxPrice,
    });

    setValidationError("");
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    submitFilters();
  };

  const handlePriceChange = (event) => {
    const { name, value } = event.target;

    setFormData((current) => ({
      ...current,
      [name]: value,
    }));

    if (validationError) {
      setValidationError("");
    }
  };

  const handleSearch = (keyword) => {
    setFormData((current) => ({
      ...current,
      keyword,
    }));

    submitFilters(keyword);
  };

  const handleReset = () => {
    setFormData({
      keyword: "",
      minPrice: "",
      maxPrice: "",
    });

    setValidationError("");

    onReset();
  };

  return (
    <form className="product-filter" onSubmit={handleSubmit}>
      <div className="product-filter__main">
        <div className="product-filter__field product-filter__field--keyword">
          <span>Tìm sản phẩm</span>

          <ProductSearchBox
            value={formData.keyword}
            loading={loading}
            placeholder="Nhập tên sản phẩm..."
            onSearch={handleSearch}
            onImageSearch={onImageSearch}
          />
        </div>

        <label className="product-filter__field">
          <span>Giá từ</span>

          <input
            type="number"
            name="minPrice"
            min="0"
            value={formData.minPrice}
            onChange={handlePriceChange}
            placeholder="0"
            disabled={loading}
          />
        </label>

        <label className="product-filter__field">
          <span>Giá đến</span>

          <input
            type="number"
            name="maxPrice"
            min="0"
            value={formData.maxPrice}
            onChange={handlePriceChange}
            placeholder="Không giới hạn"
            disabled={loading}
          />
        </label>
      </div>

      {validationError && (
        <p className="product-filter__error">{validationError}</p>
      )}

      <div className="product-filter__actions">
        <button
          type="submit"
          className="product-filter__submit"
          disabled={loading}
        >
          Tìm kiếm
        </button>

        <button
          type="button"
          className="product-filter__reset"
          onClick={handleReset}
          disabled={loading}
        >
          Xóa bộ lọc
        </button>
      </div>
    </form>
  );
}

export default ProductFilterForm;
