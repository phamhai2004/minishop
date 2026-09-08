import { Link } from "react-router-dom";

import "./HomeBanner.css";

function HomeBanner() {
  return (
    <section className="home-banner" aria-label="Khuyến mãi nổi bật">
      <Link to="/products" className="home-banner__main">
        <div className="home-banner__main-content">
          <span className="home-banner__label">HAIR SALE</span>

          <h1>Siêu hội mua sắm</h1>

          <strong>Săn deal cực chất</strong>

          <p>Hàng ngàn sản phẩm đang chờ bạn khám phá</p>

          <span className="home-banner__cta">Mua ngay →</span>
        </div>

        <div className="home-banner__symbol" aria-hidden="true">
          🛍️
        </div>
      </Link>

      <div className="home-banner__side">
        <Link
          to="/products"
          className="home-banner__secondary home-banner__secondary--orange"
        >
          <span>DEAL HOT</span>

          <strong>Mua sắm thả ga</strong>

          <small>Giá tốt mỗi ngày</small>
        </Link>

        <Link
          to="/customer/vouchers"
          className="home-banner__secondary home-banner__secondary--purple"
        >
          <span>VOUCHER</span>

          <strong>Săn mã giảm giá</strong>

          <small>Tiết kiệm hơn mỗi đơn</small>
        </Link>
      </div>
    </section>
  );
}

export default HomeBanner;
