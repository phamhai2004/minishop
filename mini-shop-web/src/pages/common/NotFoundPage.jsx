import { Link } from "react-router-dom";

import "./NotFoundPage.css";

function NotFoundPage() {
  return (
    <main className="not-found-page">
      <section className="not-found-page__card">
        <div
          className="not-found-page__glow not-found-page__glow--purple"
          aria-hidden="true"
        />

        <div
          className="not-found-page__glow not-found-page__glow--orange"
          aria-hidden="true"
        />

        <div className="not-found-page__content">
          <h1 className="not-found-page__code">404</h1>

          <h2 className="not-found-page__title">Trang không tồn tại</h2>

          <Link to="/" className="not-found-page__home">
            <span aria-hidden="true">←</span>
            Quay về trang chủ
          </Link>
        </div>
      </section>
    </main>
  );
}

export default NotFoundPage;
