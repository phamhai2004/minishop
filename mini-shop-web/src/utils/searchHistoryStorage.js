const SEARCH_HISTORY_KEY = "minishop_product_search_history";

const MAX_HISTORY_ITEMS = 10;

function normalizeKeyword(keyword) {
  return keyword.trim();
}

const searchHistoryStorage = {
  getAll() {
    const storedValue = localStorage.getItem(SEARCH_HISTORY_KEY);

    if (!storedValue) {
      return [];
    }

    try {
      const parsedValue = JSON.parse(storedValue);

      if (!Array.isArray(parsedValue)) {
        localStorage.removeItem(SEARCH_HISTORY_KEY);

        return [];
      }

      return parsedValue.filter(
        (item) => typeof item === "string" && item.trim() !== "",
      );
    } catch (error) {
      console.error("Không thể đọc lịch sử tìm kiếm:", error);

      localStorage.removeItem(SEARCH_HISTORY_KEY);

      return [];
    }
  },

  add(keyword) {
    const normalizedKeyword = normalizeKeyword(keyword);

    if (!normalizedKeyword) {
      return this.getAll();
    }

    const currentHistory = this.getAll();

    const nextHistory = [
      normalizedKeyword,
      ...currentHistory.filter(
        (item) =>
          item.toLocaleLowerCase("vi") !==
          normalizedKeyword.toLocaleLowerCase("vi"),
      ),
    ].slice(0, MAX_HISTORY_ITEMS);

    localStorage.setItem(SEARCH_HISTORY_KEY, JSON.stringify(nextHistory));

    return nextHistory;
  },

  remove(keyword) {
    const normalizedKeyword = normalizeKeyword(keyword);

    const nextHistory = this.getAll().filter(
      (item) =>
        item.toLocaleLowerCase("vi") !==
        normalizedKeyword.toLocaleLowerCase("vi"),
    );

    localStorage.setItem(SEARCH_HISTORY_KEY, JSON.stringify(nextHistory));

    return nextHistory;
  },

  clear() {
    localStorage.removeItem(SEARCH_HISTORY_KEY);
  },
};

export default searchHistoryStorage;
