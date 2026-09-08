import { forwardRef, useCallback, useImperativeHandle } from "react";
import { motion, useAnimate } from "motion/react";

const LetterRIcon = forwardRef(
  (
    { size = 24, color = "currentColor", strokeWidth = 2, className = "" },
    ref,
  ) => {
    const [scope, animate] = useAnimate();

    const start = useCallback(() => {
      animate(
        ".r-leg",
        {
          rotate: [0, 14, 0],
          x: [0, 2, 0],
        },
        { duration: 0.35 },
      );
    }, [animate]);

    const stop = useCallback(() => {
      animate(".r-leg", { rotate: 0, x: 0 }, { duration: 0.2 });
    }, [animate]);

    useImperativeHandle(ref, () => ({
      startAnimation: start,
      stopAnimation: stop,
    }));

    return (
      <motion.svg
        ref={scope}
        xmlns="http://www.w3.org/2000/svg"
        width={size}
        height={size}
        viewBox="0 0 24 24"
        fill="none"
        stroke={color}
        strokeWidth={strokeWidth}
        strokeLinecap="round"
        strokeLinejoin="round"
        className={className}
        onHoverStart={start}
        onHoverEnd={stop}
      >
        <motion.path d="M7 20V4h5.5a4 4 0 010 8H7" />

        <motion.path
          className="r-leg"
          d="M12 12l5 8"
          style={{
            transformOrigin: "12px 12px",
          }}
        />
      </motion.svg>
    );
  },
);

LetterRIcon.displayName = "LetterRIcon";
export default LetterRIcon;
