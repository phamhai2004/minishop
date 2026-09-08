import { useEffect, useRef } from "react";

function useInfiniteScroll({ enabled, hasNextPage, loading, onLoadMore }) {
  const sentinelRef = useRef(null);

  useEffect(() => {
    if (!enabled || !hasNextPage || loading) {
      return undefined;
    }

    const sentinelElement = sentinelRef.current;

    if (!sentinelElement) {
      return undefined;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        const firstEntry = entries[0];

        if (firstEntry.isIntersecting && hasNextPage && !loading) {
          onLoadMore();
        }
      },
      {
        root: null,
        rootMargin: "250px 0px",
        threshold: 0,
      },
    );

    observer.observe(sentinelElement);

    return () => {
      observer.disconnect();
    };
  }, [enabled, hasNextPage, loading, onLoadMore]);

  return sentinelRef;
}

export default useInfiniteScroll;
