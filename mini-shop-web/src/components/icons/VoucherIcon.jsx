import { forwardRef, useImperativeHandle, useRef } from "react";
import { useAnimate } from "motion/react";

const VoucherIcon = forwardRef(function VoucherIcon(
  { size = 24, strokeWidth = 1.8, className = "", ...props },
  ref,
) {
  const [scope, animate] = useAnimate();
  const isAnimating = useRef(false);

  const startAnimation = async () => {
    if (isAnimating.current) return;

    isAnimating.current = true;

    await Promise.all([
      animate(
        ".voucher-body",
        {
          rotate: [0, -2, 2, -1, 0],
          scale: [1, 1.04, 1.04, 1.02, 1],
        },
        {
          duration: 0.5,
          ease: "easeInOut",
        },
      ),

      animate(
        ".voucher-percent",
        {
          scale: [1, 1.15, 1],
          rotate: [0, 8, -8, 0],
        },
        {
          duration: 0.45,
          ease: "easeInOut",
        },
      ),

      animate(
        ".voucher-dot",
        {
          scale: [1, 1.3, 1],
        },
        {
          duration: 0.35,
          ease: "easeInOut",
          delay: 0.05,
        },
      ),
    ]);

    isAnimating.current = false;
  };

  const stopAnimation = () => {
    animate(
      ".voucher-body",
      {
        rotate: 0,
        scale: 1,
      },
      {
        duration: 0.2,
      },
    );

    animate(
      ".voucher-percent",
      {
        scale: 1,
        rotate: 0,
      },
      {
        duration: 0.2,
      },
    );

    animate(
      ".voucher-dot",
      {
        scale: 1,
      },
      {
        duration: 0.2,
      },
    );

    isAnimating.current = false;
  };

  useImperativeHandle(ref, () => ({
    startAnimation,
    stopAnimation,
  }));

  return (
    <svg
      ref={scope}
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
      {...props}
      onMouseEnter={startAnimation}
      onMouseLeave={stopAnimation}
      onFocus={startAnimation}
      onBlur={stopAnimation}
      aria-hidden="true"
    >
      {/* Voucher body */}
      <g className="voucher-body">
        <path
          d="M20 12.5V17C20 18.1 19.1 19 18 19H6C4.9 19 4 18.1 4 17V15.5C5.1 15.5 6 14.6 6 13.5C6 12.4 5.1 11.5 4 11.5V7C4 5.9 4.9 5 6 5H18C19.1 5 20 5.9 20 7V11.5C18.9 11.5 18 12.4 18 13.5C18 14.6 18.9 15.5 20 15.5"
          stroke="currentColor"
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          strokeLinejoin="round"
        />

        {/* Perforated line */}
        <path
          d="M9 5V19"
          stroke="currentColor"
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          strokeDasharray="1.5 2"
        />
      </g>

      {/* Percentage symbol */}
      <g
        className="voucher-percent"
        style={{
          transformOrigin: "14.5px 12px",
        }}
      >
        <path
          d="M13 10L17 14"
          stroke="currentColor"
          strokeWidth={strokeWidth}
          strokeLinecap="round"
        />

        <circle
          className="voucher-dot"
          cx="13"
          cy="10"
          r="1"
          fill="currentColor"
        />

        <circle
          className="voucher-dot"
          cx="17"
          cy="14"
          r="1"
          fill="currentColor"
        />
      </g>
    </svg>
  );
});

export default VoucherIcon;
