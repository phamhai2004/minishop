import { useEffect, useRef, useState } from "react";

import { useAuth } from "../../contexts/AuthContext";

import ROLES from "../constants/roles";
import HeartIcon from "../icons/HeartIcon";
import AuthRequiredModal from "../auth/AuthRequiredModal";
import LoadingSpinner from "../common/LoadingSpinner";

import wishlistApi from "../../api/wishlistApi";

import "./WishlistButton.css";

function formatLikeCount(value) {
  const count = Number(value) || 0;

  if (count < 1000) {
    return String(count);
  }

  if (count < 1_000_000) {
    const thousands = count / 1000;

    return `${Number(thousands.toFixed(thousands >= 10 ? 0 : 1))}k`;
  }

  const millions = count / 1_000_000;

  return `${Number(millions.toFixed(millions >= 10 ? 0 : 1))}tr`;
}

function WishlistButton({ productId }) {
  const { currentUser, isAuthenticated } = useAuth();
  const heartRef = useRef(null);
  const [liked, setLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [checking, setChecking] = useState(false);
  const [showAuthModal, setShowAuthModal] = useState(false);
  const isCustomer = currentUser?.role === ROLES.CUSTOMER;

  useEffect(() => {
    if (!isAuthenticated || !isCustomer || !productId) {
      setLiked(false);
      return;
    }

    let cancelled = false;

    const checkWishlist = async () => {
      try {
        setChecking(true);

        const response = await wishlistApi.getMyWishlist();

        const items = response.data?.data;

        if (cancelled) {
          return;
        }

        const exists =
          Array.isArray(items) &&
          items.some((item) => Number(item.productId) === Number(productId));

        setLiked(exists);
      } catch (error) {
        console.error("Unable to check wishlist:", error);
      } finally {
        if (!cancelled) {
          setChecking(false);
        }
      }
    };

    void checkWishlist();

    return () => {
      cancelled = true;
    };
  }, [productId, isAuthenticated, isCustomer]);

  useEffect(() => {
    if (!productId) {
      setLikeCount(0);
      return;
    }

    let cancelled = false;

    const fetchLikeCount = async () => {
      try {
        const response = await wishlistApi.getCount(productId);

        if (cancelled) {
          return;
        }

        const count = Number(response.data?.data ?? 0);

        setLikeCount(Number.isFinite(count) ? count : 0);
      } catch (error) {
        console.error("Unable to load wishlist count:", error);

        if (!cancelled) {
          setLikeCount(0);
        }
      }
    };

    void fetchLikeCount();

    return () => {
      cancelled = true;
    };
  }, [productId]);

  const handleToggle = async () => {
    if (!isAuthenticated) {
      setShowAuthModal(true);
      return;
    }

    if (!isCustomer) {
      return;
    }

    if (loading || checking) {
      return;
    }

    try {
      setLoading(true);

      if (liked) {
        await wishlistApi.remove(productId);

        setLiked(false);

        setLikeCount((current) => Math.max(0, current - 1));
      } else {
        await wishlistApi.add(productId);

        setLiked(true);

        setLikeCount((current) => current + 1);

        heartRef.current?.startAnimation?.();
      }

      window.dispatchEvent(
        new CustomEvent("wishlist:changed", {
          detail: {
            productId,
            liked: !liked,
          },
        }),
      );
    } catch (error) {
      console.error("Wishlist action failed:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <button
        type="button"
        className={`wishlist-button ${liked ? "wishlist-button--liked" : ""}`}
        onClick={handleToggle}
        disabled={loading || checking || (isAuthenticated && !isCustomer)}
      >
        <HeartIcon
          ref={heartRef}
          size={26}
          strokeWidth={1.8}
          filled={liked}
          className="wishlist-button__icon"
        />

        <span className="wishlist-button__label">
          {loading ? (
            <LoadingSpinner size="small" inline />
          ) : liked ? (
            "Đã thích"
          ) : (
            "Yêu thích"
          )}

          {!loading && (
            <span className="wishlist-button__count">
              {" "}
              ({formatLikeCount(likeCount)})
            </span>
          )}
        </span>
      </button>

      <AuthRequiredModal
        isOpen={showAuthModal}
        onClose={() => setShowAuthModal(false)}
      />
    </>
  );
}

export default WishlistButton;
