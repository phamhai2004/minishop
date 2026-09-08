import "./Pagination.css";

function createPageNumbers(currentPage, totalPages) {
  if (totalPages <= 1) {
    return [];
  }

  const visiblePages = 5;

  let startPage = Math.max(currentPage - Math.floor(visiblePages / 2), 0);

  let endPage = Math.min(startPage + visiblePages - 1, totalPages - 1);

  startPage = Math.max(endPage - visiblePages + 1, 0);

  const pageNumbers = [];

  for (let page = startPage; page <= endPage; page += 1) {
    pageNumbers.push(page);
  }

  return pageNumbers;
}

function Pagination({ currentPage, totalPages, onPageChange }) {
  const pageNumbers = createPageNumbers(currentPage, totalPages);

  if (totalPages <= 1) {
    return null;
  }

  const handlePrevious = () => {
    if (currentPage > 0) {
      onPageChange(currentPage - 1);
    }
  };

  const handleNext = () => {
    if (currentPage < totalPages - 1) {
      onPageChange(currentPage + 1);
    }
  };

  return (
    <nav className="pagination" aria-label="Phân trang sản phẩm">
      <button
        type="button"
        className="pagination__button"
        onClick={handlePrevious}
        disabled={currentPage === 0}
      >
        ← Trước
      </button>

      <div className="pagination__pages">
        {pageNumbers.map((page) => {
          const isActive = page === currentPage;

          return (
            <button
              key={page}
              type="button"
              className={
                isActive
                  ? "pagination__page pagination__page--active"
                  : "pagination__page"
              }
              onClick={() => onPageChange(page)}
              aria-current={isActive ? "page" : undefined}
              aria-label={`Trang ${page + 1}`}
            >
              {page + 1}
            </button>
          );
        })}
      </div>

      <button
        type="button"
        className="pagination__button"
        onClick={handleNext}
        disabled={currentPage >= totalPages - 1}
      >
        Sau →
      </button>
    </nav>
  );
}

export default Pagination;
