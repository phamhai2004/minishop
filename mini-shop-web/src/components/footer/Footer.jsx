import "./Footer.css";

function Footer() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="site-footer">
      <div className="site-footer__top">
        <div className="site-footer__container">
          <div className="site-footer__column">
            <h3>DỊCH VỤ KHÁCH HÀNG</h3>
            <a href="/help">Trung tâm trợ giúp</a>
            <a href="/orders">Theo dõi đơn hàng</a>
            <a href="/returns">Đổi trả & Hoàn tiền</a>
            <a href="/shipping">Chính sách giao hàng</a>
            <a href="/contact">Liên hệ với chúng tôi</a>
          </div>

          <div className="site-footer__column">
            <h3>VỀ HAIR</h3>
            <a href="/about">Về chúng tôi</a>
            <a href="/products">Sản phẩm</a>
            <a href="/flash-sale">Flash Sale</a>
            <a href="/vouchers">Voucher</a>
            <a href="/seller/register">Đăng ký bán hàng</a>
          </div>

          <div className="site-footer__column">
            <h3>THANH TOÁN</h3>
            <div className="site-footer__payment-grid">
              <span>VISA</span>
              <span>MC</span>
              <span>JCB</span>
              <span>COD</span>
              <span>QR</span>
              <span>ATM</span>
            </div>
            <h3 className="site-footer__subheading">VẬN CHUYỂN</h3>
            <div className="site-footer__shipping-grid">
              <span>FAST</span>
              <span>EXPRESS</span>
              <span>STANDARD</span>
            </div>
          </div>

          <div className="site-footer__column">
            <h3>THEO DÕI CHÚNG TÔI</h3>
            <a href="#" aria-label="Facebook">
              <iconify-icon icon="lucide:facebook" />
              Facebook
            </a>
            <a href="#" aria-label="Instagram">
              <iconify-icon icon="lucide:instagram" />
              Instagram
            </a>
            <a href="#" aria-label="GitHub">
              <iconify-icon icon="lucide:github" />
              GitHub
            </a>
          </div>

          <div className="site-footer__column site-footer__column--app">
            <h3>ỨNG DỤNG HAIR</h3>
            <div className="site-footer__app-content">
              <div className="site-footer__qr" aria-label="QR tải ứng dụng">
                <span>HAIR</span>
                <i />
                <i />
                <i />
                <i />
              </div>
              <div className="site-footer__stores">
                <span>
                  <iconify-icon icon="lucide:smartphone" />
                  App Store
                </span>
                <span>
                  <iconify-icon icon="lucide:play" />
                  Google Play
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="site-footer__middle">
        <div className="site-footer__container site-footer__container--middle">
          <div className="site-footer__legal-links">
            <a href="/privacy">CHÍNH SÁCH BẢO MẬT</a>
            <a href="/terms">QUY CHẾ HOẠT ĐỘNG</a>
            <a href="/shipping-policy">CHÍNH SÁCH VẬN CHUYỂN</a>
            <a href="/refund-policy">CHÍNH SÁCH TRẢ HÀNG VÀ HOÀN TIỀN</a>
          </div>

          <div className="site-footer__badges">
            <div className="site-footer__badge">
              ✓
              <span>
                ĐÃ ĐĂNG KÝ
                <br />
                HAIR
              </span>
            </div>
            <div className="site-footer__badge">
              ✓
              <span>
                ĐÃ ĐĂNG KÝ
                <br />
                THƯƠNG MẠI
              </span>
            </div>
            <div className="site-footer__badge site-footer__badge--shield">
              ★
            </div>
          </div>
        </div>
      </div>

      <div className="site-footer__bottom">
        <div className="site-footer__container site-footer__container--bottom">
          <p>© {currentYear} Hair. Tất cả các quyền được bảo lưu.</p>
          <p>HAIR — Nền tảng mua sắm trực tuyến hiện đại.</p>
          <p>Việt Nam · An toàn · Nhanh chóng · Tiện lợi</p>
        </div>
      </div>
    </footer>
  );
}

export default Footer;
