import { useEffect, useState } from "react";

import { useNavigate } from "react-router-dom";

import categoryApi from "../../api/categoryApi";

import LoadingSpinner from "../common/LoadingSpinner";

import "./HomeCategories.css";

const CATEGORY_ICONS = [
  {
    keywords: ["ốp lưng", "ốp điện thoại"],
    icon: "📱",
  },
  {
    keywords: ["điện thoại", "smartphone"],
    icon: "📱",
  },
  {
    keywords: ["laptop", "máy tính"],
    icon: "💻",
  },
  {
    keywords: ["máy ảnh", "camera"],
    icon: "📷",
  },
  {
    keywords: ["giày", "dép"],
    icon: "👟",
  },
  {
    keywords: ["quần áo", "thời trang"],
    icon: "👕",
  },
  {
    keywords: ["đồng hồ"],
    icon: "⌚",
  },
  {
    keywords: ["túi", "ví"],
    icon: "👜",
  },
  {
    keywords: ["sức khỏe", "y tế"],
    icon: "💊",
  },
  {
    keywords: ["sách"],
    icon: "📚",
  },
  {
    keywords: ["mẹ", "bé"],
    icon: "👶",
  },
  {
    keywords: ["nhà cửa", "đời sống", "gia dụng"],
    icon: "🏠",
  },
  {
    keywords: ["thể thao", "du lịch"],
    icon: "⚽",
  },
  {
    keywords: ["ô tô", "xe máy", "xe đạp"],
    icon: "🛵",
  },
  {
    keywords: ["sắc đẹp", "mỹ phẩm", "làm đẹp"],
    icon: "💄",
  },
  {
    keywords: ["trang sức", "phụ kiện"],
    icon: "💍",
  },
];

function getCategoryIcon(categoryName) {
  const normalizedName = String(categoryName ?? "")
    .trim()
    .toLocaleLowerCase("vi-VN");

  const matchedCategory = CATEGORY_ICONS.find(({ keywords }) =>
    keywords.some((keyword) => normalizedName.includes(keyword)),
  );

  return matchedCategory?.icon ?? "material-symbols:category-rounded";
}

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
                {getCategoryIcon(category.name)}
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
