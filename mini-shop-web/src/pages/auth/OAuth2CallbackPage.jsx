import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

import { useAuth } from "../../contexts/AuthContext";
import LoadingSpinner from "../../components/common/LoadingSpinner";

import {
  PENDING_ACTION_TYPES,
  getPendingProductAction,
  clearPendingProductAction,
} from "../../utils/pendingProductAction";

function OAuth2CallbackPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const { login } = useAuth();

  useEffect(() => {
    const accessToken = searchParams.get("accessToken");
    const refreshToken = searchParams.get("refreshToken");
    const userId = searchParams.get("userId");
    const fullName = searchParams.get("fullName");
    const email = searchParams.get("email");
    const role = searchParams.get("role");

    if (!accessToken || !refreshToken || !userId || !email || !role) {
      console.error("Google OAuth callback thiếu thông tin đăng nhập.");

      navigate("/login", {
        replace: true,
        state: {
          message: "Đăng nhập Google thất bại.",
        },
      });

      return;
    }

    try {
      login({
        accessToken,
        refreshToken,
        userId: Number(userId),
        fullName: fullName ?? "",
        email,
        role,
      });

      const pendingAction = getPendingProductAction();

      if (pendingAction) {
        clearPendingProductAction();

        if (pendingAction.type === PENDING_ACTION_TYPES.BUY_NOW) {
          navigate("/checkout", {
            replace: true,
            state: {
              type: "BUY_NOW",
              items: [
                {
                  productId: pendingAction.productId,
                  variantId: pendingAction.variantId ?? null,
                  quantity: pendingAction.quantity ?? 1,
                },
              ],
            },
          });

          return;
        }

        if (pendingAction.type === PENDING_ACTION_TYPES.ADD_TO_CART) {
          console.log("ADD_TO_CART", {
            productId: pendingAction.productId,
            variantId: pendingAction.variantId,
          });

          navigate("/products", {
            replace: true,
          });

          return;
        }
      }

      navigate("/", {
        replace: true,
      });
    } catch (error) {
      console.error("Google login failed:", error);

      navigate("/login", {
        replace: true,
        state: {
          message: "Không thể hoàn tất đăng nhập Google.",
        },
      });
    }
  }, [login, navigate, searchParams]);

  return (
    <section
      style={{
        minHeight: "100vh",
        display: "grid",
        placeItems: "center",
      }}
    >
      <LoadingSpinner size="large" />
    </section>
  );
}

export default OAuth2CallbackPage;
