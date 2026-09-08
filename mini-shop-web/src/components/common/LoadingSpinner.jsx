import "./LoadingSpinner.css";

function LoadingSpinner({
  size = "medium",
  text = "",
  overlay = false,
  inline = false,
  variant = "brand",
  className = "",
}) {
  const classes = [
    "loading-spinner",
    `loading-spinner--${size}`,
    `loading-spinner--${variant}`,
    overlay ? "loading-spinner--overlay" : "",
    inline ? "loading-spinner--inline" : "",
    className,
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <div
      className={classes}
      role="status"
      aria-live="polite"
      aria-label={text || "Đang tải"}
    >
      <span className="loading-spinner__ring" aria-hidden="true" />

      {text && <span className="loading-spinner__text">{text}</span>}
    </div>
  );
}

export default LoadingSpinner;
