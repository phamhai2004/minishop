const STORAGE_KEY = "minishop_pending_product_action";

export const PENDING_ACTION_TYPES = {
  BUY_NOW: "BUY_NOW",
  ADD_TO_CART: "ADD_TO_CART",
};

export function savePendingProductAction(
  type,
  productId,
  variantId = null,
  quantity = 1,
) {
  sessionStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      type,
      productId,
      variantId,
      quantity,
    }),
  );
}

export function getPendingProductAction() {
  const storedValue = sessionStorage.getItem(STORAGE_KEY);

  if (!storedValue) {
    return null;
  }

  try {
    const action = JSON.parse(storedValue);

    if (!action?.type || !action?.productId) {
      return null;
    }

    return action;
  } catch (error) {
    console.error("Không thể đọc pending product action:", error);

    return null;
  }
}

export function clearPendingProductAction() {
  sessionStorage.removeItem(STORAGE_KEY);
}
