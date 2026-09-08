import { useState } from "react";
import { getSimilarProducts } from "../api/productApi";

export default function useSimilarProducts() {
  const [products, setProducts] = useState([]);

  const [loading, setLoading] = useState(false);

  const load = async (productId) => {
    setLoading(true);

    try {
      const data = await getSimilarProducts(productId);

      setProducts(data);
    } catch (e) {
      console.error(e);

      setProducts([]);
    } finally {
      setLoading(false);
    }
  };

  return {
    products,
    loading,
    load,
  };
}
