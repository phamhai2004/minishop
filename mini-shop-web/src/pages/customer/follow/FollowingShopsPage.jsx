import { useCallback, useEffect, useState } from "react";

import { useNavigate } from "react-router-dom";

import shopFollowApi from "../../../api/shopFollowApi";
import LoadingSpinner from "../../../components/common/LoadingSpinner";

import "./FollowingShopsPage.css";

function ShopAvatar({ shop }) {
  if (shop.logoUrl) {
    return (
      <img
        className="following-shop__logo"
        src={shop.logoUrl}
        alt={shop.shopName}
      />
    );
  }

  return (
    <div className="following-shop__logo following-shop__logo--empty">
      {shop.shopName?.trim()?.charAt(0)?.toUpperCase() || "S"}
    </div>
  );
}

function FollowingShopsPage() {
  const navigate = useNavigate();
  const [shops, setShops] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [processingId, setProcessingId] = useState(null);
  const loadFollowing = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const response = await shopFollowApi.getMyFollowing();

      setShops(response.data?.data ?? []);
    } catch (requestError) {
      console.error("Không thể tải shop đang theo dõi:", requestError);

      setError(
        requestError.response?.data?.message || "Không thể tải danh sách shop.",
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadFollowing();
  }, [loadFollowing]);

  const handleUnfollow = async (shopId) => {
    if (processingId != null) {
      return;
    }

    try {
      setProcessingId(shopId);

      const response = await shopFollowApi.unfollow(shopId);

      if (response.data?.data?.following === false) {
        setShops((current) => current.filter((shop) => shop.shopId !== shopId));
      }
    } catch (requestError) {
      console.error("Không thể bỏ theo dõi:", requestError);

      alert(
        requestError.response?.data?.message || "Không thể bỏ theo dõi shop.",
      );
    } finally {
      setProcessingId(null);
    }
  };

  return (
    <main className="following-page">
      <div className="following-page__container">
        <header className="following-page__header">
          <h1>Đang theo dõi</h1>

          <span>{shops.length} shop</span>
        </header>

        {error && <div className="following-page__error">{error}</div>}

        {loading ? (
          <div className="following-page__state">
            <LoadingSpinner size="medium" />
          </div>
        ) : shops.length === 0 ? (
          <div className="following-page__empty">
            <div>🏪</div>

            <h2>Bạn chưa theo dõi shop nào</h2>

            <p>Khi theo dõi một shop, shop đó sẽ xuất hiện tại đây.</p>
          </div>
        ) : (
          <section className="following-page__list">
            {shops.map((shop) => (
              <article key={shop.shopId} className="following-shop">
                <button
                  type="button"
                  className="following-shop__main"
                  onClick={() => navigate(`/shops/${shop.shopId}`)}
                >
                  <ShopAvatar shop={shop} />

                  <div className="following-shop__info">
                    <div className="following-shop__name-row">
                      <h2>{shop.shopName}</h2>

                      {shop.verified && (
                        <span className="following-shop__verified">✓</span>
                      )}
                    </div>

                    <p>
                      <span className="following-shop__dot" />

                      {shop.statusName || "Đang hoạt động"}
                    </p>
                  </div>
                </button>

                <button
                  type="button"
                  className="following-shop__unfollow"
                  disabled={processingId === shop.shopId}
                  onClick={() => handleUnfollow(shop.shopId)}
                >
                  {processingId === shop.shopId ? (
                    <LoadingSpinner size="small" inline variant="light" />
                  ) : (
                    "✓ Đang theo dõi"
                  )}
                </button>
              </article>
            ))}
          </section>
        )}
      </div>
    </main>
  );
}

export default FollowingShopsPage;
