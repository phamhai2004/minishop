import ProductOptionManagement from "../../../components/admin/ProductOptionManagement";

import "./ProductOptionManagementPage.css";

function ProductOptionManagementPage() {
  return (
    <main className="product-option-management-page">
      <div className="product-option-management-page__header">
        <h1>Phân loại sản phẩm</h1>

        <p>Quản lý các phân loại dùng cho biến thể sản phẩm của Hair.</p>
      </div>

      <ProductOptionManagement />
    </main>
  );
}

export default ProductOptionManagementPage;
