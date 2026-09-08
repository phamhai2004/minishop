import { Link, useLocation, useNavigate } from "react-router-dom";

function ForbiddenPage() {
  const location = useLocation();
  const navigate = useNavigate();

  const previousPath = location.state?.from?.pathname;

  const handleGoBack = () => {
    navigate(-1);
  };

  return (
    <main>
      <h1>403 - Forbidden</h1>

      <p>Bạn đã đăng nhập nhưng không có quyền truy cập trang này.</p>

      {previousPath && (
        <p>
          Đường dẫn bị từ chối: <strong>{previousPath}</strong>
        </p>
      )}

      <button type="button" onClick={handleGoBack}>
        Quay lại
      </button>

      <p>
        <Link to="/">Về trang chủ</Link>
      </p>
    </main>
  );
}

export default ForbiddenPage;
