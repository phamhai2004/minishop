import { useEffect, useState } from "react";
import recommendationApi from "../api/recommendationApi";

export default function useRecommendations(enabled = true) {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(enabled);

  useEffect(() => {
    if (!enabled) {
      setProducts([]);
      setLoading(false);
      return;
    }

    load();
  }, [enabled]);

  async function load() {
    try {
      setLoading(true);

      const result = await recommendationApi.getMyRecommendations();

      setProducts(result);
    } catch (e) {
      if (e.response?.status !== 401) {
        console.error(e);
      }

      setProducts([]);
    } finally {
      setLoading(false);
    }
  }

  return {
    products,
    loading,
  };
}
