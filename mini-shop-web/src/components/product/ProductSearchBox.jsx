import { useCallback, useEffect, useMemo, useRef, useState } from "react";

import productApi from "../../api/productApi";

import useDebouncedValue from "../../hooks/useDebouncedValue";
import useSpeechRecognition from "../../hooks/useSpeechRecognition";
import searchHistoryStorage from "../../utils/searchHistoryStorage";

import LoadingSpinner from "../common/LoadingSpinner";

import "./ProductSearchBox.css";

const MIN_SUGGESTION_LENGTH = 1;
const SUGGESTION_LIMIT = 8;
const SUGGESTION_DELAY = 300;

function ProductSearchBox({
  value = "",
  loading = false,
  placeholder = "Nhập tên sản phẩm...",
  compact = false,
  onSearch,
  onImageSearch,
}) {
  const [keyword, setKeyword] = useState(value);

  const [suggestions, setSuggestions] = useState([]);

  const [searchHistory, setSearchHistory] = useState(() =>
    searchHistoryStorage.getAll(),
  );

  const [suggestionLoading, setSuggestionLoading] = useState(false);

  const [showSearchPanel, setShowSearchPanel] = useState(false);

  const [activeSuggestionIndex, setActiveSuggestionIndex] = useState(-1);

  const searchContainerRef = useRef(null);

  const imageInputRef = useRef(null);

  useEffect(() => {
    setKeyword(value ?? "");
  }, [value]);

  const debouncedKeyword = useDebouncedValue(keyword.trim(), SUGGESTION_DELAY);

  const isShowingSuggestions = debouncedKeyword.length >= MIN_SUGGESTION_LENGTH;

  const panelItems = useMemo(
    () => (isShowingSuggestions ? suggestions : searchHistory),
    [isShowingSuggestions, suggestions, searchHistory],
  );

  useEffect(() => {
    if (debouncedKeyword.length < MIN_SUGGESTION_LENGTH) {
      setSuggestions([]);
      setSuggestionLoading(false);
      setActiveSuggestionIndex(-1);

      return undefined;
    }

    let active = true;

    const fetchSuggestions = async () => {
      try {
        setSuggestionLoading(true);

        const response = await productApi.getSuggestions(
          debouncedKeyword,
          SUGGESTION_LIMIT,
        );

        if (!active) {
          return;
        }

        const data = response.data?.data;

        setSuggestions(Array.isArray(data) ? data : []);

        setActiveSuggestionIndex(-1);
      } catch (error) {
        if (!active) {
          return;
        }

        console.error("Unable to load suggestions:", error);

        setSuggestions([]);
      } finally {
        if (active) {
          setSuggestionLoading(false);
        }
      }
    };

    void fetchSuggestions();

    return () => {
      active = false;
    };
  }, [debouncedKeyword]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        searchContainerRef.current &&
        !searchContainerRef.current.contains(event.target)
      ) {
        setShowSearchPanel(false);
        setActiveSuggestionIndex(-1);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  const submitSearch = useCallback(
    (override) => {
      const submittedKeyword = (override ?? keyword).trim();

      if (submittedKeyword) {
        setSearchHistory(searchHistoryStorage.add(submittedKeyword));
      }

      onSearch?.(submittedKeyword);

      setShowSearchPanel(false);
      setActiveSuggestionIndex(-1);
    },
    [keyword, onSearch],
  );

  const {
    isSupported: isVoiceSupported,

    isListening,
    transcript: voiceTranscript,

    interimTranscript,
    error: voiceError,

    startListening,
    stopListening,
    clearTranscript,
  } = useSpeechRecognition({
    language: "vi-VN",
    continuous: false,
    interimResults: true,
  });

  useEffect(() => {
    if (!voiceTranscript) {
      return;
    }

    setKeyword(voiceTranscript);

    submitSearch(voiceTranscript);

    clearTranscript();
  }, [voiceTranscript, submitSearch, clearTranscript]);

  const handleSubmit = (event) => {
    event.preventDefault();

    submitSearch();
  };

  const handleSelect = (item) => {
    setKeyword(item);

    submitSearch(item);
  };

  const handleKeyDown = (event) => {
    if (!showSearchPanel || panelItems.length === 0) {
      if (event.key === "Escape") {
        setShowSearchPanel(false);
      }

      return;
    }

    if (event.key === "ArrowDown") {
      event.preventDefault();

      setActiveSuggestionIndex((current) =>
        current >= panelItems.length - 1 ? 0 : current + 1,
      );

      return;
    }

    if (event.key === "ArrowUp") {
      event.preventDefault();

      setActiveSuggestionIndex((current) =>
        current <= 0 ? panelItems.length - 1 : current - 1,
      );

      return;
    }

    if (event.key === "Enter" && activeSuggestionIndex >= 0) {
      event.preventDefault();

      handleSelect(panelItems[activeSuggestionIndex]);

      return;
    }

    if (event.key === "Escape") {
      setShowSearchPanel(false);
      setActiveSuggestionIndex(-1);
    }
  };

  const handleClearKeyword = () => {
    setKeyword("");
    setSuggestions([]);
    setShowSearchPanel(true);
    setActiveSuggestionIndex(-1);
  };

  const handleClearHistory = () => {
    searchHistoryStorage.clear();

    setSearchHistory([]);
    setShowSearchPanel(false);
  };

  const handleRemoveHistory = (event, item) => {
    event.preventDefault();
    event.stopPropagation();

    setSearchHistory(searchHistoryStorage.remove(item));
  };

  const handleVoice = () => {
    setShowSearchPanel(false);

    if (isListening) {
      stopListening();
      return;
    }

    startListening();
  };

  const handleImageSelected = (event) => {
    const file = event.target.files?.[0];

    if (!file) {
      return;
    }

    onImageSearch?.(file);

    event.target.value = "";
  };

  const shouldShowSuggestionPanel =
    showSearchPanel &&
    isShowingSuggestions &&
    (suggestionLoading || suggestions.length > 0);

  const shouldShowHistoryPanel =
    showSearchPanel && !isShowingSuggestions && searchHistory.length > 0;

  const shouldShowPanel = shouldShowSuggestionPanel || shouldShowHistoryPanel;

  return (
    <div
      ref={searchContainerRef}
      className={`product-search-box ${
        compact ? "product-search-box--compact" : ""
      }`}
    >
      <div className="product-search-box__form">
        <span className="product-search-box__search-icon" aria-hidden="true">
          ⌕
        </span>

        <input
          type="search"
          value={isListening && interimTranscript ? interimTranscript : keyword}
          placeholder={isListening ? "Đang nghe..." : placeholder}
          autoComplete="off"
          disabled={loading || isListening}
          onChange={(event) => {
            setKeyword(event.target.value);
            setShowSearchPanel(true);
            setActiveSuggestionIndex(-1);
          }}
          onFocus={() => {
            setSearchHistory(searchHistoryStorage.getAll());

            setShowSearchPanel(true);
          }}
          onKeyDown={(event) => {
            if (event.key === "Enter" && activeSuggestionIndex < 0) {
              event.preventDefault();

              submitSearch();

              return;
            }

            handleKeyDown(event);
          }}
        />

        <div className="product-search-box__tools">
          {keyword && !isListening && (
            <button
              type="button"
              className="product-search-box__clear"
              onClick={handleClearKeyword}
              aria-label="Xóa từ khóa"
            >
              ×
            </button>
          )}

          {isVoiceSupported && (
            <button
              type="button"
              className={`product-search-box__voice ${
                isListening ? "is-listening" : ""
              }`}
              onClick={handleVoice}
              aria-label="Tìm kiếm bằng giọng nói"
            >
              {isListening ? "■" : "🎙️"}
            </button>
          )}

          {onImageSearch && (
            <>
              <button
                type="button"
                className="product-search-box__image"
                onClick={() => imageInputRef.current?.click()}
                aria-label="Tìm kiếm bằng hình ảnh"
              >
                📷
              </button>

              <input
                ref={imageInputRef}
                type="file"
                accept="image/*"
                hidden
                onChange={handleImageSelected}
              />
            </>
          )}
        </div>
      </div>

      {shouldShowPanel && (
        <div className="product-search-box__panel">
          {shouldShowHistoryPanel && (
            <div className="product-search-box__panel-header">
              <strong>Lịch sử tìm kiếm</strong>

              <button type="button" onClick={handleClearHistory}>
                Xóa tất cả
              </button>
            </div>
          )}

          {shouldShowSuggestionPanel && suggestionLoading ? (
            <div className="product-search-box__loading">
              <LoadingSpinner size="small" />
            </div>
          ) : (
            <div className="product-search-box__list">
              {panelItems.map((item, index) => (
                <div
                  key={`${item}-${index}`}
                  className={`product-search-box__item ${
                    activeSuggestionIndex === index ? "active" : ""
                  }`}
                >
                  <button
                    type="button"
                    className="product-search-box__select"
                    onMouseDown={(event) => {
                      event.preventDefault();

                      handleSelect(item);
                    }}
                  >
                    <span>{isShowingSuggestions ? "⌕" : "↶"}</span>

                    <span>{item}</span>
                  </button>

                  {!isShowingSuggestions && (
                    <button
                      type="button"
                      className="product-search-box__remove"
                      onMouseDown={(event) => handleRemoveHistory(event, item)}
                    >
                      ×
                    </button>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {voiceError && (
        <div className="product-search-box__voice-error">{voiceError}</div>
      )}
    </div>
  );
}

export default ProductSearchBox;
