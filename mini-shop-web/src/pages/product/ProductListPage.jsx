import { useCallback, useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";

import productApi from "../../api/productApi";

import ROLES from "../../components/constants/roles";
import { useAuth } from "../../contexts/AuthContext";

import Pagination from "../../components/pagination/Pagination";
import ProductFilterForm from "../../components/product/ProductFilterForm";
import ProductGrid from "../../components/product/ProductGrid";
import SellerProductManagement from "../../components/seller/product/SellerProductManagement";
import LoadingSpinner from "../../components/common/LoadingSpinner";

import useInfiniteScroll from "../../hooks/useInfiniteScroll";
import useMediaQuery from "../../hooks/useMediaQuery";
import useImageSearch from "../../hooks/useImageSearch";

import "./ProductListPage.css";

const DEFAULT_PAGE = 0;
const DEFAULT_SIZE = 12;
const DEFAULT_SORT = "id";
const DEFAULT_DIRECTION = "desc";

const ALLOWED_PAGE_SIZES = [8, 12, 20, 40];

const SORT_OPTIONS = {
  newest: {
    sort: "id",
    direction: "desc",
  },

  oldest: {
    sort: "id",
    direction: "asc",
  },

  priceAsc: {
    sort: "price",
    direction: "asc",
  },

  priceDesc: {
    sort: "price",
    direction: "desc",
  },

  nameAsc: {
    sort: "name",
    direction: "asc",
  },

  nameDesc: {
    sort: "name",
    direction: "desc",
  },
};

function parseNonNegativeInteger(value, fallbackValue) {
  const parsedValue = Number.parseInt(value, 10);

  if (!Number.isInteger(parsedValue) || parsedValue < 0) {
    return fallbackValue;
  }

  return parsedValue;
}

function getCurrentSortOption(sort, direction) {
  const matchingEntry = Object.entries(SORT_OPTIONS).find(([, option]) => {
    return option.sort === sort && option.direction === direction;
  });

  return matchingEntry?.[0] ?? "newest";
}

function mergeUniqueProducts(currentProducts, newProducts) {
  const productMap = new Map();

  currentProducts.forEach((product) => {
    productMap.set(product.id, product);
  });

  newProducts.forEach((product) => {
    productMap.set(product.id, product);
  });

  return Array.from(productMap.values());
}

function ProductListPage() {
  const { searchByImage } = useImageSearch();

  const { currentUser, isAuthenticated } = useAuth();

  const userRole = currentUser?.role ?? null;

  // eslint-disable-next-line no-unused-vars
  const isGuest = !isAuthenticated;
  // eslint-disable-next-line no-unused-vars
  const isCustomer = userRole === ROLES.CUSTOMER;
  const isSeller = userRole === ROLES.SELLER;
  // eslint-disable-next-line no-unused-vars
  const isAdmin = userRole === ROLES.ADMIN;

  const [searchParams, setSearchParams] = useSearchParams();

  const isMobile = useMediaQuery("(max-width: 767px)");

  const requestedPage = parseNonNegativeInteger(
    searchParams.get("page"),
    DEFAULT_PAGE,
  );

  const requestedSize = parseNonNegativeInteger(
    searchParams.get("size"),
    DEFAULT_SIZE,
  );

  const size = ALLOWED_PAGE_SIZES.includes(requestedSize)
    ? requestedSize
    : DEFAULT_SIZE;

  const sort = searchParams.get("sort") ?? DEFAULT_SORT;

  const direction =
    searchParams.get("direction") === "asc" ? "asc" : DEFAULT_DIRECTION;

  const keyword = searchParams.get("keyword")?.trim() ?? "";

  const minPrice = searchParams.get("minPrice") ?? "";

  const maxPrice = searchParams.get("maxPrice") ?? "";

  const hasActiveFilters =
    Boolean(keyword) || minPrice !== "" || maxPrice !== "";

  const currentFilters = {
    keyword,
    minPrice,
    maxPrice,
  };

  const currentSortOption = getCurrentSortOption(sort, direction);

  const desktopPage = requestedPage;

  const [mobilePage, setMobilePage] = useState(DEFAULT_PAGE);

  const [products, setProducts] = useState([]);

  const [pagination, setPagination] = useState({
    currentPage: DEFAULT_PAGE,
    pageSize: size,
    totalPages: 0,
    totalElements: 0,
    first: true,
    last: true,
  });

  const [initialLoading, setInitialLoading] = useState(true);

  const [loadingMore, setLoadingMore] = useState(false);

  const [error, setError] = useState("");

  const requestInProgressRef = useRef(false);

  const updateSearchParams = useCallback(
    (nextValues) => {
      const nextParams = new URLSearchParams(searchParams);

      Object.entries(nextValues).forEach(([key, value]) => {
        if (value === null || value === undefined || value === "") {
          nextParams.delete(key);
          return;
        }

        nextParams.set(key, String(value));
      });

      setSearchParams(nextParams);
    },
    [searchParams, setSearchParams],
  );

  const loadProducts = useCallback(
    async ({ pageToLoad, append }) => {
      if (isSeller) {
        return;
      }
      if (requestInProgressRef.current) {
        return;
      }

      requestInProgressRef.current = true;

      if (append) {
        setLoadingMore(true);
      } else {
        setInitialLoading(true);
      }

      setError("");

      try {
        const requestParams = {
          page: pageToLoad,
          size,
          sort,
          direction,
        };

        if (keyword && !isSeller) {
          requestParams.keyword = keyword;
        }

        if (minPrice !== "") {
          requestParams.minPrice = minPrice;
        }

        if (maxPrice !== "") {
          requestParams.maxPrice = maxPrice;
        }

        const response = hasActiveFilters
          ? await productApi.search(requestParams)
          : await productApi.getAll(requestParams);

        const apiResponse = response.data;

        const productPage = apiResponse?.data;

        if (!apiResponse?.success || !productPage) {
          throw new Error(
            apiResponse?.message ?? "Không thể tải danh sách sản phẩm.",
          );
        }

        const newProducts = Array.isArray(productPage.content)
          ? productPage.content
          : [];

        if (append) {
          setProducts((currentProducts) =>
            mergeUniqueProducts(currentProducts, newProducts),
          );
        } else {
          setProducts(newProducts);
        }

        setPagination({
          currentPage: productPage.number ?? pageToLoad,

          pageSize: productPage.size ?? size,

          totalPages: productPage.totalPages ?? 0,

          totalElements: productPage.totalElements ?? 0,

          first: productPage.first ?? pageToLoad === 0,

          last:
            productPage.last ?? pageToLoad >= (productPage.totalPages ?? 1) - 1,
        });

        if (isMobile) {
          setMobilePage(productPage.number ?? pageToLoad);
        }
      } catch (requestError) {
        console.error("Unable to load products:", requestError);

        if (!append) {
          setProducts([]);
        }

        setError(
          requestError.response?.data?.message ??
            requestError.message ??
            "Không thể tải danh sách sản phẩm. Vui lòng thử lại.",
        );
      } finally {
        requestInProgressRef.current = false;

        setInitialLoading(false);
        setLoadingMore(false);
      }
    },
    [
      direction,
      hasActiveFilters,
      isMobile,
      isSeller,
      keyword,
      maxPrice,
      minPrice,
      size,
      sort,
    ],
  );

  useEffect(() => {
    if (!isMobile || !searchParams.has("page")) {
      return;
    }

    const nextParams = new URLSearchParams(searchParams);

    nextParams.delete("page");

    setSearchParams(nextParams, {
      replace: true,
    });
  }, [isMobile, searchParams, setSearchParams]);

  useEffect(() => {
    if (isMobile) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setMobilePage(DEFAULT_PAGE);

      loadProducts({
        pageToLoad: DEFAULT_PAGE,
        append: false,
      });

      return;
    }

    loadProducts({
      pageToLoad: desktopPage,
      append: false,
    });
  }, [desktopPage, isMobile, loadProducts]);

  const hasNextPage =
    pagination.totalPages > 0 &&
    pagination.currentPage < pagination.totalPages - 1;

  const handleLoadMore = useCallback(() => {
    if (
      !isMobile ||
      !hasNextPage ||
      initialLoading ||
      loadingMore ||
      requestInProgressRef.current
    ) {
      return;
    }

    loadProducts({
      pageToLoad: mobilePage + 1,
      append: true,
    });
  }, [
    hasNextPage,
    initialLoading,
    isMobile,
    loadProducts,
    loadingMore,
    mobilePage,
  ]);

  const sentinelRef = useInfiniteScroll({
    enabled: isMobile,
    hasNextPage,
    loading: initialLoading || loadingMore,
    onLoadMore: handleLoadMore,
  });

  const handlePageChange = (nextPage) => {
    if (
      isMobile ||
      nextPage < 0 ||
      nextPage >= pagination.totalPages ||
      nextPage === desktopPage
    ) {
      return;
    }

    updateSearchParams({
      page: nextPage,
    });

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  };

  const handlePageSizeChange = (event) => {
    const nextSize = Number(event.target.value);

    updateSearchParams({
      page: isMobile ? null : 0,
      size: nextSize,
    });

    if (isMobile) {
      setMobilePage(DEFAULT_PAGE);
    }
  };

  const handleSortChange = (event) => {
    const selectedOption =
      SORT_OPTIONS[event.target.value] ?? SORT_OPTIONS.newest;

    updateSearchParams({
      page: isMobile ? null : 0,
      sort: selectedOption.sort,
      direction: selectedOption.direction,
    });

    if (isMobile) {
      setMobilePage(DEFAULT_PAGE);
    }
  };

  const handleFilterSubmit = (filters) => {
    updateSearchParams({
      page: isMobile ? null : 0,

      keyword: filters.keyword || null,

      minPrice: filters.minPrice === null ? null : filters.minPrice,

      maxPrice: filters.maxPrice === null ? null : filters.maxPrice,
    });

    if (isMobile) {
      setMobilePage(DEFAULT_PAGE);
    }

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  };

  const handleFilterReset = () => {
    updateSearchParams({
      page: isMobile ? null : 0,
      keyword: null,
      minPrice: null,
      maxPrice: null,
    });

    if (isMobile) {
      setMobilePage(DEFAULT_PAGE);
    }

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  };

  const handleImageSearch = async (file) => {
    try {
      setInitialLoading(true);
      setError("");

      const result = await searchByImage(file);

      setProducts(result);

      setPagination({
        currentPage: 0,
        pageSize: result.length,
        totalPages: 1,
        totalElements: result.length,
        first: true,
        last: true,
      });
      // eslint-disable-next-line no-unused-vars
    } catch (e) {
      setProducts([]);
      setError("Không thể tìm kiếm bằng hình ảnh.");
    } finally {
      setInitialLoading(false);
    }
  };

  const retryPage = isMobile
    ? products.length > 0
      ? mobilePage + 1
      : DEFAULT_PAGE
    : desktopPage;

  return (
    <section className="product-list-page">
      <header className="product-list-page__header">
        <div>
          <h1 className="product-list-page__title">
            {isSeller ? "" : "Sản phẩm"}
          </h1>

          <p className="product-list-page__description">
            {isSeller ? "" : "Khám phá sản phẩm từ các gian hàng trên Hair."}
          </p>
        </div>

        {!initialLoading && !error && (
          <span className="product-list-page__count">
            {pagination.totalElements} sản phẩm
          </span>
        )}
      </header>
      {!isSeller && (
        <>
          <ProductFilterForm
            initialFilters={currentFilters}
            loading={initialLoading || loadingMore}
            onSubmit={handleFilterSubmit}
            onReset={handleFilterReset}
            onImageSearch={handleImageSearch}
          />

          {hasActiveFilters && (
            <div className="product-list-page__active-filters">
              <span>Đang lọc:</span>

              {keyword && (
                <span className="product-list-page__filter-tag">
                  Từ khóa: {keyword}
                </span>
              )}

              {minPrice !== "" && (
                <span className="product-list-page__filter-tag">
                  Giá từ: {Number(minPrice).toLocaleString("vi-VN")} ₫
                </span>
              )}

              {maxPrice !== "" && (
                <span className="product-list-page__filter-tag">
                  Giá đến: {Number(maxPrice).toLocaleString("vi-VN")} ₫
                </span>
              )}
            </div>
          )}

          <div className="product-list-page__toolbar">
            <label className="product-list-page__control">
              <span>Sắp xếp</span>

              <select
                value={currentSortOption}
                onChange={handleSortChange}
                disabled={initialLoading || loadingMore}
              >
                <option value="newest">Mới nhất</option>

                <option value="oldest">Cũ nhất</option>

                <option value="priceAsc">Giá thấp đến cao</option>

                <option value="priceDesc">Giá cao đến thấp</option>

                <option value="nameAsc">Tên A → Z</option>

                <option value="nameDesc">Tên Z → A</option>
              </select>
            </label>

            <label className="product-list-page__control">
              <span>Sản phẩm mỗi lần tải</span>

              <select
                value={size}
                onChange={handlePageSizeChange}
                disabled={initialLoading || loadingMore}
              >
                {ALLOWED_PAGE_SIZES.map((pageSize) => (
                  <option key={pageSize} value={pageSize}>
                    {pageSize}
                  </option>
                ))}
              </select>
            </label>
          </div>
        </>
      )}
      {!isSeller && initialLoading && (
        <div className="product-list-page__status" role="status">
          <LoadingSpinner size="large" />
        </div>
      )}
      {!initialLoading && error && products.length === 0 && (
        <div
          className="product-list-page__status product-list-page__status--error"
          role="alert"
        >
          <h2>Không tải được sản phẩm</h2>

          <p>{error}</p>

          <button
            type="button"
            className="product-list-page__retry-button"
            onClick={() =>
              loadProducts({
                pageToLoad: retryPage,
                append: false,
              })
            }
          >
            Thử lại
          </button>
        </div>
      )}
      {!initialLoading && !error && products.length === 0 && (
        <div className="product-list-page__status">
          <span className="product-list-page__empty-icon" aria-hidden="true">
            📭
          </span>

          <h2>Không tìm thấy sản phẩm</h2>

          <p>Hãy thử thay đổi từ khóa hoặc khoảng giá.</p>
        </div>
      )}
      {isSeller ? (
        <SellerProductManagement />
      ) : (
        <>
          {!initialLoading && products.length > 0 && (
            <>
              <ProductGrid products={products} />

              {!isMobile && (
                <Pagination
                  currentPage={pagination.currentPage}
                  totalPages={pagination.totalPages}
                  onPageChange={handlePageChange}
                />
              )}

              {isMobile && (
                <div className="product-list-page__mobile-pagination">
                  {error && (
                    <p
                      className="product-list-page__load-more-error"
                      role="alert"
                    >
                      {error}
                    </p>
                  )}

                  {hasNextPage && (
                    <>
                      <div
                        ref={sentinelRef}
                        className="product-list-page__sentinel"
                        aria-hidden="true"
                      />

                      <button
                        type="button"
                        className="product-list-page__load-more-button"
                        onClick={handleLoadMore}
                        disabled={loadingMore}
                      >
                        {loadingMore ? (
                          <LoadingSpinner size="small" inline variant="light" />
                        ) : (
                          "Tải thêm sản phẩm"
                        )}
                      </button>
                    </>
                  )}

                  {!hasNextPage && (
                    <p className="product-list-page__end-message">
                      Bạn đã xem hết sản phẩm.
                    </p>
                  )}
                </div>
              )}
            </>
          )}
        </>
      )}
    </section>
  );
}

export default ProductListPage;
