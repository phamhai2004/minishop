import { forwardRef, useCallback, useImperativeHandle } from "react";
import { motion, useAnimate } from "motion/react";

const LetterHIcon = forwardRef(
  (
    { size = 24, color = "currentColor", strokeWidth = 2, className = "" },
    ref,
  ) => {
    const [scope, animate] = useAnimate();

    const start = useCallback(() => {
      animate(
        ".h-group",
        { x: [0, 2, -2, 1.5, -1, 0] },
        { duration: 0.45, ease: "easeOut" },
      );
    }, [animate]);

    const stop = useCallback(() => {
      animate(".h-group", { x: 0 }, { duration: 0.2 });
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
        <motion.g className="h-group">
          <motion.path d="M7 4v16" />
          <motion.path d="M17 4v16" />
          <motion.path d="M7 12h10" />
        </motion.g>
      </motion.svg>
    );
  },
);

LetterHIcon.displayName = "LetterHIcon";
export default LetterHIcon;
