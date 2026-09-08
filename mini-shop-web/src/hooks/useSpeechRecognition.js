import { useCallback, useEffect, useRef, useState } from "react";

function getSpeechRecognitionConstructor() {
  if (typeof window === "undefined") {
    return null;
  }

  return window.SpeechRecognition ?? window.webkitSpeechRecognition ?? null;
}

function translateSpeechError(errorCode) {
  switch (errorCode) {
    case "not-allowed":
    case "service-not-allowed":
      return "Bạn chưa cấp quyền sử dụng microphone.";

    case "audio-capture":
      return "Không tìm thấy microphone khả dụng.";

    case "no-speech":
      return "Không nghe thấy giọng nói. Hãy thử nói lại.";

    case "network":
      return "Không thể kết nối dịch vụ nhận dạng giọng nói.";

    case "language-not-supported":
      return "Trình duyệt không hỗ trợ nhận dạng tiếng Việt.";

    case "aborted":
      return "";

    default:
      return "Không thể nhận dạng giọng nói. Vui lòng thử lại.";
  }
}

function useSpeechRecognition({
  language = "vi-VN",
  continuous = false,
  interimResults = false,
} = {}) {
  const recognitionRef = useRef(null);

  const [isSupported, setIsSupported] = useState(false);

  const [isListening, setIsListening] = useState(false);

  const [transcript, setTranscript] = useState("");

  const [interimTranscript, setInterimTranscript] = useState("");

  const [error, setError] = useState("");

  useEffect(() => {
    const SpeechRecognitionConstructor = getSpeechRecognitionConstructor();

    if (!SpeechRecognitionConstructor) {
      setIsSupported(false);
      return undefined;
    }

    setIsSupported(true);

    const recognition = new SpeechRecognitionConstructor();

    recognition.lang = language;
    recognition.continuous = continuous;
    recognition.interimResults = interimResults;

    recognition.maxAlternatives = 1;

    recognition.onstart = () => {
      setIsListening(true);
      setError("");
      setInterimTranscript("");
    };

    recognition.onresult = (event) => {
      let finalText = "";
      let temporaryText = "";

      for (
        let index = event.resultIndex;
        index < event.results.length;
        index += 1
      ) {
        const result = event.results[index];

        const recognizedText = result[0]?.transcript ?? "";

        if (result.isFinal) {
          finalText += recognizedText;
        } else {
          temporaryText += recognizedText;
        }
      }

      if (temporaryText) {
        setInterimTranscript(temporaryText.trim());
      }

      if (finalText) {
        setTranscript(finalText.trim());
        setInterimTranscript("");
      }
    };

    recognition.onerror = (event) => {
      setError(translateSpeechError(event.error));

      setIsListening(false);
    };

    recognition.onend = () => {
      setIsListening(false);
      setInterimTranscript("");
    };

    recognitionRef.current = recognition;

    return () => {
      recognition.onstart = null;
      recognition.onresult = null;
      recognition.onerror = null;
      recognition.onend = null;

      try {
        recognition.abort();
      } catch {
        // Recognition có thể chưa được khởi động.
      }

      recognitionRef.current = null;
    };
  }, [continuous, interimResults, language]);

  const startListening = useCallback(() => {
    if (!recognitionRef.current) {
      setError("Trình duyệt không hỗ trợ tìm kiếm bằng giọng nói.");

      return;
    }

    if (isListening) {
      return;
    }

    setTranscript("");
    setInterimTranscript("");
    setError("");

    try {
      recognitionRef.current.start();
    } catch (startError) {
      console.error("Unable to start speech recognition:", startError);

      setError("Microphone đang được sử dụng hoặc chưa sẵn sàng.");
    }
  }, [isListening]);

  const stopListening = useCallback(() => {
    if (!recognitionRef.current) {
      return;
    }

    try {
      recognitionRef.current.stop();
    } catch {
      // Không cần xử lý nếu recognition đã dừng.
    }
  }, []);

  const abortListening = useCallback(() => {
    if (!recognitionRef.current) {
      return;
    }

    try {
      recognitionRef.current.abort();
    } catch {
      // Không cần xử lý nếu recognition chưa chạy.
    }

    setIsListening(false);
    setInterimTranscript("");
  }, []);

  const clearTranscript = useCallback(() => {
    setTranscript("");
    setInterimTranscript("");
    setError("");
  }, []);

  return {
    isSupported,
    isListening,
    transcript,
    interimTranscript,
    error,
    startListening,
    stopListening,
    abortListening,
    clearTranscript,
  };
}

export default useSpeechRecognition;
