import { useEffect, useState } from "react";

import { Link, useParams } from "react-router-dom";

import categoryApi from "../../api/categoryApi";

import CategoryShops from "../../components/category/CategoryShops";
import CategoryProducts from "../../components/category/CategoryProducts";
import LoadingSpinner from "../../components/common/LoadingSpinner";

import "./CategoryPage.css";

function CategoryPage() {
  const { categoryId } = useParams();

  const [category, setCategory] = useState(null);

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    const loadCategory = async () => {
      try {
        setLoading(true);
        setError("");

        const response = await categoryApi.getById(categoryId);

        if (cancelled) {
          return;
        }

        setCategory(response.data?.data ?? null);
      } catch (err) {
        console.error("Unable to load category:", err);

        if (!cancelled) {
          setError(err.response?.data?.message ?? "Không thể tải danh mục.");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    void loadCategory();

    return () => {
      cancelled = true;
    };
  }, [categoryId]);

  if (loading) {
    return (
      <main className="category-page">
        <LoadingSpinner size="large" />
      </main>
    );
  }

  if (error || !category) {
    return (
      <main className="category-page">
        <p className="category-page__error">
          {error || "Không tìm thấy danh mục."}
        </p>

        <Link to="/">← Về trang chủ</Link>
      </main>
    );
  }

  return (
    <main className="category-page">
      <div className="category-page__breadcrumb">
        <Link to="/">Trang chủ</Link>

        <span>›</span>

        <strong>{category.name}</strong>
      </div>

      <header className="category-page__header">
        <div>
          <span className="category-page__eyebrow">DANH MỤC</span>

          <h1>{category.name}</h1>
        </div>
      </header>

      <CategoryShops categoryId={category.id} categoryName={category.name} />

      <CategoryProducts categoryId={category.id} categoryName={category.name} />
    </main>
  );
}

export default CategoryPage;
