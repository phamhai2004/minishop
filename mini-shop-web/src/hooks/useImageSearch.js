import { useState } from "react";
import { apiSearchByImage } from "../api/productApi";

export default function useImageSearch() {
  const [loading, setLoading] = useState(false);

  const searchByImage = async (file) => {
    setLoading(true);

    try {
      return await apiSearchByImage(file);
    } finally {
      setLoading(false);
    }
  };

  return {
    loading,
    searchByImage,
  };
}
