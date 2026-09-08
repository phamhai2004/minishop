import CategoryManagement from "../../../components/admin/category/CategoryManagement";

import "./CategoryManagementPage.css";

function CategoryManagementPage() {
  return (
    <main className="category-management-page">
      <div className="category-management-page__header">
        <h1>Danh mục</h1>

        <p>Quản lý các danh mục sản phẩm của Hair.</p>
      </div>

      <CategoryManagement />
    </main>
  );
}

export default CategoryManagementPage;
