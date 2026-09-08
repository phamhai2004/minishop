import { useEffect, useState } from "react";

import { useNavigate } from "react-router-dom";

import categoryApi from "../../api/categoryApi";

import LoadingSpinner from "../common/LoadingSpinner";

import "./HomeCategories.css";

function HomeCategories() {
  const navigate = useNavigate();

  const [categories, setCategories] = useState([]);

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    const loadCategories = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await categoryApi.getAll();

        const data = response.data?.data;

        if (cancelled) {
          return;
        }

        setCategories(Array.isArray(data) ? data : []);
      } catch (err) {
        console.error("Unable to load categories:", err);

        if (!cancelled) {
          setError(err.response?.data?.message ?? "Không thể tải danh mục.");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    void loadCategories();

    return () => {
      cancelled = true;
    };
  }, []);

  const handleCategoryClick = (categoryId) => {
    navigate(`/categories/${categoryId}`);
  };

  return (
    <section className="home-section home-categories">
      <div className="home-section__heading">
        <h2>DANH MỤC</h2>
      </div>

      {loading ? (
        <div className="home-section__state">
          <LoadingSpinner size="medium" />
        </div>
      ) : error ? (
        <p className="home-section__error">{error}</p>
      ) : (
        <div className="home-categories__grid">
          {categories.map((category) => (
            <button
              key={category.id}
              type="button"
              className="home-category"
              onClick={() => handleCategoryClick(category.id)}
            >
              <span className="home-category__icon" aria-hidden="true">
                {category.name?.trim()?.charAt(0)?.toUpperCase() || "C"}
              </span>

              <span className="home-category__name">{category.name}</span>
            </button>
          ))}
        </div>
      )}
    </section>
  );
}

export default HomeCategories;
