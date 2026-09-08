import { useCallback, useEffect, useMemo, useState } from "react";

import { useLocation, useNavigate, useParams } from "react-router-dom";

import shopApi from "../../api/shopApi";
import productApi from "../../api/productApi";
import shopFollowApi from "../../api/shopFollowApi";
import shopChatApi from "../../api/shopChatApi";

import ProductCard from "../../components/product/ProductCard";
import MessageModal from "../../components/common/MessageModal";
import LoadingSpinner from "../../components/common/LoadingSpinner";

import { useAuth } from "../../contexts/AuthContext";
import AuthRequiredModal from "../../components/auth/AuthRequiredModal";
import ROLES from "../../components/constants/roles";

import "./ShopPublicPage.css";

function formatJoinedTime(value) {
  if (!value) {
    return "—";
  }

  const createdAt = new Date(value);

  if (Number.isNaN(createdAt.getTime())) {
    return "—";
  }

  const now = new Date();

  let months =
    (now.getFullYear() - createdAt.getFullYear()) * 12 +
    (now.getMonth() - createdAt.getMonth());

  if (now.getDate() < createdAt.getDate()) {
    months -= 1;
  }

  if (months >= 12) {
    const years = Math.floor(months / 12);

    return `${years} năm`;
  }

  if (months > 0) {
    return `${months} tháng`;
  }

  const diff = now.getTime() - createdAt.getTime();
  const days = Math.max(0, Math.floor(diff / (1000 * 60 * 60 * 24)));

  if (days > 0) {
    return `${days} ngày`;
  }

  return "Hôm nay";
}

function ShopLogo({ shop }) {
  if (shop?.logoUrl) {
    return (
      <img className="shop-public__logo" src={shop.logoUrl} alt={shop.name} />
    );
  }

  return (
    <div className="shop-public__logo shop-public__logo--empty">
      {shop?.name?.trim()?.charAt(0)?.toUpperCase() || "S"}
    </div>
  );
}

function ShopPublicPage() {
  const { shopId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const sourceProductId = Number(location.state?.sourceProductId);
  const hasSourceProduct =
    Number.isFinite(sourceProductId) && sourceProductId > 0;
  const { currentUser, isAuthenticated } = useAuth();
  const [shop, setShop] = useState(null);
  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState(null);
  const [mobileTab, setMobileTab] = useState("products");
  const [page, setPage] = useState(0);
  const [pageInfo, setPageInfo] = useState({
    totalPages: 0,
    totalElements: 0,
    first: true,
    last: true,
  });
  const [shopLoading, setShopLoading] = useState(true);
  const [productsLoading, setProductsLoading] = useState(true);
  const [error, setError] = useState("");
  const [isFollowing, setIsFollowing] = useState(false);
  const [followLoading, setFollowLoading] = useState(false);
  const [chatLoading, setChatLoading] = useState(false);
  const [chatNotice, setChatNotice] = useState({
    open: false,
    title: "",
    message: "",
    type: "error",
  });
  const [showAuthModal, setShowAuthModal] = useState(false);

  useEffect(() => {
    let cancelled = false;

    const loadShop = async () => {
      try {
        setShopLoading(true);
        setError("");

        const [shopResponse, categoryResponse] = await Promise.all([
          shopApi.getById(shopId),
          shopApi.getCategories(shopId),
        ]);

        if (cancelled) {
          return;
        }

        setShop(shopResponse.data?.data ?? null);

        setCategories(categoryResponse.data?.data ?? []);
      } catch (requestError) {
        console.error("Không thể tải thông tin shop:", requestError);

        if (!cancelled) {
          setError(
            requestError.response?.data?.message ||
              "Không thể tải thông tin shop.",
          );

          setShop(null);
          setCategories([]);
        }
      } finally {
        if (!cancelled) {
          setShopLoading(false);
        }
      }
    };

    void loadShop();

    return () => {
      cancelled = true;
    };
  }, [shopId]);

  useEffect(() => {
    let cancelled = false;

    const loadFollowStatus = async () => {
      if (!isAuthenticated) {
        setIsFollowing(false);
        return;
      }

      if (currentUser?.role !== ROLES.CUSTOMER) {
        setIsFollowing(false);
        return;
      }

      try {
        const response = await shopFollowApi.getStatus(shopId);

        if (cancelled) {
          return;
        }

        const data = response.data?.data;

        setIsFollowing(Boolean(data?.following));

        if (data?.totalFollowers != null) {
          setShop((current) => {
            if (!current) {
              return current;
            }

            return {
              ...current,
              totalFollowers: Number(data.totalFollowers),
            };
          });
        }
      } catch (requestError) {
        console.error("Không thể lấy trạng thái theo dõi shop:", requestError);
      }
    };

    void loadFollowStatus();

    return () => {
      cancelled = true;
    };
  }, [shopId, isAuthenticated, currentUser?.role]);

  const loadProducts = useCallback(async () => {
    try {
      setProductsLoading(true);

      const params = {
        shopId,
        page,
        size: 20,
        sort: "id",
        direction: "desc",
      };

      if (selectedCategoryId) {
        params.categoryId = selectedCategoryId;
      }

      const response = await productApi.search(params);

      const pageData = response.data?.data;

      setProducts(pageData?.content ?? []);

      setPageInfo({
        totalPages: pageData?.totalPages ?? 0,

        totalElements: pageData?.totalElements ?? 0,

        first: pageData?.first ?? true,

        last: pageData?.last ?? true,
      });
    } catch (requestError) {
      console.error("Không thể tải sản phẩm shop:", requestError);

      setProducts([]);
    } finally {
      setProductsLoading(false);
    }
  }, [shopId, selectedCategoryId, page]);

  useEffect(() => {
    void loadProducts();
  }, [loadProducts]);

  const selectedCategory = useMemo(
    () =>
      categories.find(
        (category) => category.categoryId === selectedCategoryId,
      ) ?? null,
    [categories, selectedCategoryId],
  );

  const handleCategorySelect = (categoryId) => {
    setSelectedCategoryId(categoryId);

    setPage(0);
    setMobileTab("products");
  };

  const handleFollowToggle = async () => {
    if (!isAuthenticated) {
      navigate("/login");
      return;
    }

    if (currentUser?.role !== ROLES.CUSTOMER) {
      return;
    }

    if (followLoading) {
      return;
    }

    try {
      setFollowLoading(true);

      const response = isFollowing
        ? await shopFollowApi.unfollow(shopId)
        : await shopFollowApi.follow(shopId);

      const data = response.data?.data;

      if (!data) {
        return;
      }

      setIsFollowing(Boolean(data.following));

      setShop((current) => {
        if (!current) {
          return current;
        }

        return {
          ...current,
          totalFollowers: Number(data.totalFollowers ?? 0),
        };
      });
    } catch (requestError) {
      console.error("Không thể cập nhật theo dõi shop:", requestError);

      showChatNotice(
        "Không thể cập nhật theo dõi",
        requestError.response?.data?.message ||
          "Không thể cập nhật theo dõi shop.",
        "error",
      );
    } finally {
      setFollowLoading(false);
    }
  };

  const handleChat = async () => {
    if (!isAuthenticated) {
      setShowAuthModal(true);
      return;
    }

    if (currentUser?.role !== ROLES.CUSTOMER) {
      return;
    }

    if (chatLoading) {
      return;
    }

    try {
      setChatLoading(true);

      const response = hasSourceProduct
        ? await shopChatApi.openConversationWithProduct(shopId, sourceProductId)
        : await shopChatApi.openConversation(shopId);

      const conversation = response.data?.data;

      if (!conversation?.id) {
        throw new Error("Không lấy được conversationId");
      }

      navigate("/customer/chat", {
        state: {
          conversationId: conversation.id,

          returnTo: `/shops/${shopId}`,

          returnState: location.state ?? null,
        },
      });
    } catch (error) {
      console.error("Không thể mở chat:", error);

      setChatNotice({
        open: true,

        title: "Không thể mở cuộc trò chuyện",

        message:
          error.response?.data?.message ||
          "Không thể mở cuộc trò chuyện. Vui lòng thử lại.",

        type: "error",
      });
    } finally {
      setChatLoading(false);
    }
  };
  const showChatNotice = (title, message, type = "error") => {
    setChatNotice({
      open: true,
      title,
      message,
      type,
    });
  };

  if (shopLoading) {
    return (
      <main className="shop-public-page">
        <div className="shop-public__state">
          <LoadingSpinner size="large" />
        </div>
      </main>
    );
  }

  if (error || !shop) {
    return (
      <main className="shop-public-page">
        <div className="shop-public__state shop-public__state--error">
          {error || "Không tìm thấy shop."}
        </div>
      </main>
    );
  }

  const rating = Number(shop.rating ?? 0);
  const totalReviews = Number(shop.totalReviews ?? 0);
  const totalProducts = Number(shop.totalProducts ?? 0);
  const totalFollowers = Number(shop.totalFollowers ?? 0);
  const heroStyle = shop.coverUrl
    ? {
        backgroundImage: `
            linear-gradient(
              rgba(15, 23, 42, 0.72),
              rgba(15, 23, 42, 0.82)
            ),
            url("${shop.coverUrl}")
          `,
      }
    : undefined;

  return (
    <main className="shop-public-page">
      <div className="shop-public__container">
        <section className="shop-public__desktop-header">
          <div className="shop-public__desktop-profile" style={heroStyle}>
            <div className="shop-public__profile-main">
              <ShopLogo shop={shop} />

              <div>
                <div className="shop-public__name-row">
                  <h1>{shop.name}</h1>

                  {shop.verified && (
                    <span className="shop-public__verified">✓</span>
                  )}
                </div>

                <p className="shop-public__status">
                  <span />
                  {shop.statusName || "Đang hoạt động"}
                </p>
              </div>
            </div>

            <div className="shop-public__profile-actions">
              <button
                type="button"
                className={
                  isFollowing
                    ? "shop-public__follow-button shop-public__follow-button--active"
                    : "shop-public__follow-button"
                }
                onClick={handleFollowToggle}
                disabled={
                  followLoading ||
                  (isAuthenticated && currentUser?.role !== ROLES.CUSTOMER)
                }
                title={
                  isAuthenticated && currentUser?.role !== ROLES.CUSTOMER
                    ? "Chỉ khách hàng có thể theo dõi shop"
                    : undefined
                }
              >
                {followLoading ? (
                  <LoadingSpinner size="small" inline variant="light" />
                ) : isFollowing ? (
                  "✓ Đang theo dõi"
                ) : (
                  "＋ Theo dõi"
                )}
              </button>

              <button
                type="button"
                onClick={handleChat}
                disabled={
                  chatLoading ||
                  (isAuthenticated && currentUser?.role !== ROLES.CUSTOMER)
                }
              >
                {chatLoading ? (
                  <LoadingSpinner size="small" inline variant="light" />
                ) : (
                  "◌ Chat"
                )}
              </button>
            </div>
          </div>

          <div className="shop-public__desktop-info">
            <div className="shop-public__info-item">
              <span>Sản phẩm</span>
              <strong>{totalProducts}</strong>
            </div>

            <div className="shop-public__info-item">
              <span>Người theo dõi</span>

              <strong>{totalFollowers}</strong>
            </div>

            <div className="shop-public__info-item">
              <span>Đánh giá</span>

              <strong>
                {rating.toFixed(1)}

                <small> ({totalReviews} đánh giá)</small>
              </strong>
            </div>

            <div className="shop-public__info-item">
              <span>Tham gia</span>

              <strong>{formatJoinedTime(shop.createdAt)}</strong>
            </div>

            <div className="shop-public__info-item shop-public__info-item--wide">
              <span>Địa chỉ</span>

              <strong>{shop.pickupAddress || "Chưa cập nhật"}</strong>
            </div>
          </div>
        </section>

        <section className="shop-public__mobile-header" style={heroStyle}>
          <div className="shop-public__mobile-profile">
            <ShopLogo shop={shop} />

            <div className="shop-public__mobile-name">
              <div className="shop-public__name-row">
                <h1>{shop.name}</h1>

                {shop.verified && (
                  <span className="shop-public__verified">✓</span>
                )}
              </div>

              <p>
                ⭐ {rating.toFixed(1)}
                {"  |  "}
                {totalFollowers} người theo dõi
              </p>
            </div>
          </div>

          <div className="shop-public__mobile-actions">
            <button
              type="button"
              className={
                isFollowing
                  ? "shop-public__follow-button shop-public__follow-button--active"
                  : "shop-public__follow-button"
              }
              onClick={handleFollowToggle}
              disabled={
                followLoading ||
                (isAuthenticated && currentUser?.role !== ROLES.CUSTOMER)
              }
            >
              {followLoading ? (
                <LoadingSpinner size="small" inline variant="light" />
              ) : isFollowing ? (
                "✓ Đang theo dõi"
              ) : (
                "＋ Theo dõi"
              )}
            </button>

            <button
              type="button"
              onClick={handleChat}
              disabled={
                chatLoading ||
                (isAuthenticated && currentUser?.role !== ROLES.CUSTOMER)
              }
            >
              {chatLoading ? (
                <LoadingSpinner size="small" inline variant="light" />
              ) : (
                "◌ Chat"
              )}
            </button>
          </div>
        </section>

        {/* =========================
            DESKTOP CATEGORY BAR
        ========================== */}
        <nav
          className="shop-public__desktop-categories"
          aria-label="Danh mục shop"
        >
          <button
            type="button"
            className={
              selectedCategoryId === null
                ? "shop-public__category-button shop-public__category-button--active"
                : "shop-public__category-button"
            }
            onClick={() => handleCategorySelect(null)}
          >
            Tất cả
          </button>

          {categories.map((category) => (
            <button
              type="button"
              key={category.categoryId}
              className={
                selectedCategoryId === category.categoryId
                  ? "shop-public__category-button shop-public__category-button--active"
                  : "shop-public__category-button"
              }
              onClick={() => handleCategorySelect(category.categoryId)}
            >
              {category.categoryName}
            </button>
          ))}
        </nav>

        {/* =========================
            MOBILE TAB
        ========================== */}
        <nav className="shop-public__mobile-tabs">
          <button
            type="button"
            className={
              mobileTab === "products"
                ? "shop-public__mobile-tab shop-public__mobile-tab--active"
                : "shop-public__mobile-tab"
            }
            onClick={() => setMobileTab("products")}
          >
            Sản phẩm
          </button>

          <button
            type="button"
            className={
              mobileTab === "categories"
                ? "shop-public__mobile-tab shop-public__mobile-tab--active"
                : "shop-public__mobile-tab"
            }
            onClick={() => setMobileTab("categories")}
          >
            Danh mục
          </button>

          <button
            type="button"
            className={
              mobileTab === "detail"
                ? "shop-public__mobile-tab shop-public__mobile-tab--active"
                : "shop-public__mobile-tab"
            }
            onClick={() => setMobileTab("detail")}
          >
            Chi tiết shop
          </button>
        </nav>

        {/* =========================
            PRODUCTS
        ========================== */}
        <section
          className={
            mobileTab === "products"
              ? "shop-public__products"
              : "shop-public__products shop-public__mobile-hidden"
          }
        >
          <div className="shop-public__section-header">
            <div>
              <h2>
                {selectedCategory
                  ? selectedCategory.categoryName
                  : "Tất cả sản phẩm"}
              </h2>

              <span>{pageInfo.totalElements} sản phẩm</span>
            </div>
          </div>

          {productsLoading ? (
            <div className="shop-public__state">
              <LoadingSpinner size="medium" />
            </div>
          ) : products.length === 0 ? (
            <div className="shop-public__state">
              Chưa có sản phẩm trong danh mục này.
            </div>
          ) : (
            <div className="shop-public__product-grid">
              {products.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          )}

          {pageInfo.totalPages > 1 && (
            <div className="shop-public__pagination">
              <button
                type="button"
                disabled={pageInfo.first}
                onClick={() => setPage((current) => Math.max(current - 1, 0))}
              >
                ← Trước
              </button>

              <span>
                Trang {page + 1} / {pageInfo.totalPages}
              </span>

              <button
                type="button"
                disabled={pageInfo.last}
                onClick={() => setPage((current) => current + 1)}
              >
                Sau →
              </button>
            </div>
          )}
        </section>

        {/* =========================
            MOBILE CATEGORIES
        ========================== */}
        <section
          className={
            mobileTab === "categories"
              ? "shop-public__mobile-categories"
              : "shop-public__mobile-categories shop-public__mobile-hidden"
          }
        >
          <button
            type="button"
            className="shop-public__mobile-category"
            onClick={() => handleCategorySelect(null)}
          >
            <div>
              <strong>Tất cả sản phẩm</strong>

              <span>{totalProducts} sản phẩm</span>
            </div>

            <b>›</b>
          </button>

          {categories.map((category) => (
            <button
              type="button"
              key={category.categoryId}
              className="shop-public__mobile-category"
              onClick={() => handleCategorySelect(category.categoryId)}
            >
              <div>
                <strong>{category.categoryName}</strong>

                <span>{category.productCount} sản phẩm</span>
              </div>

              <b>›</b>
            </button>
          ))}
        </section>

        {/* =========================
            MOBILE SHOP DETAIL
        ========================== */}
        <section
          className={
            mobileTab === "detail"
              ? "shop-public__detail"
              : "shop-public__detail shop-public__mobile-hidden"
          }
        >
          <div className="shop-public__detail-title">
            <ShopLogo shop={shop} />

            <div>
              <h2>{shop.name}</h2>

              <p>{totalFollowers} người theo dõi</p>
            </div>
          </div>

          <div className="shop-public__detail-list">
            <div className="shop-public__detail-row">
              <span>☆</span>

              <strong>Đánh giá</strong>

              <p>
                {rating.toFixed(1)} / 5 <small>({totalReviews} đánh giá)</small>
              </p>
            </div>

            <div className="shop-public__detail-row">
              <span>▣</span>

              <strong>Sản phẩm</strong>

              <p>{totalProducts}</p>
            </div>

            <div className="shop-public__detail-row">
              <span>♙</span>

              <strong>Đã tham gia</strong>

              <p>{formatJoinedTime(shop.createdAt)}</p>
            </div>

            <div className="shop-public__detail-row">
              <span>⌖</span>

              <strong>Địa chỉ</strong>

              <p>{shop.pickupAddress || "Chưa cập nhật"}</p>
            </div>

            <div className="shop-public__detail-row">
              <span>▤</span>

              <strong>Mô tả shop</strong>

              <p>{shop.description || "Shop chưa cập nhật mô tả."}</p>
            </div>

            <div className="shop-public__detail-row">
              <span>✓</span>

              <strong>Xác minh</strong>

              <p>
                {shop.verified
                  ? "Shop đã được xác minh"
                  : "Shop chưa được xác minh"}
              </p>
            </div>
          </div>

          <button
            type="button"
            className="shop-public__view-all"
            onClick={() => {
              setSelectedCategoryId(null);

              setPage(0);

              setMobileTab("products");
            }}
          >
            Xem tất cả sản phẩm
          </button>
        </section>
      </div>

      <MessageModal
        open={chatNotice.open}
        title={chatNotice.title}
        message={chatNotice.message}
        type={chatNotice.type}
        onClose={() =>
          setChatNotice((current) => ({
            ...current,
            open: false,
          }))
        }
      />

      <AuthRequiredModal
        isOpen={showAuthModal}
        onClose={() => setShowAuthModal(false)}
      />
    </main>
  );
}

export default ShopPublicPage;
