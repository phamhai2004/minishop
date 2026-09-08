import "./HomePagination.css";

function HomePagination({ page, totalPages, onChange }) {
  if (totalPages <= 1) {
    return null;
  }

  const start = Math.max(0, page - 2);

  const end = Math.min(totalPages, start + 5);

  const pages = [];

  for (let index = start; index < end; index += 1) {
    pages.push(index);
  }

  return (
    <nav className="home-pagination" aria-label="Phân trang">
      <button
        type="button"
        disabled={page === 0}
        onClick={() => onChange(page - 1)}
      >
        ‹
      </button>

      {pages.map((pageNumber) => (
        <button
          key={pageNumber}
          type="button"
          className={pageNumber === page ? "active" : ""}
          onClick={() => onChange(pageNumber)}
        >
          {pageNumber + 1}
        </button>
      ))}

      <button
        type="button"
        disabled={page + 1 >= totalPages}
        onClick={() => onChange(page + 1)}
      >
        ›
      </button>
    </nav>
  );
}

export default HomePagination;
