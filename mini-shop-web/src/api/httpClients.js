import axios from "axios";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const defaultConfig = {
  baseURL: API_BASE_URL,

  timeout: 65000,
};

export const publicClient = axios.create(defaultConfig);
export const authenticatedClient = axios.create(defaultConfig);

const RETRY_DELAYS = [2000, 4000, 6000];

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const dispatchBackendEvent = (eventName, detail = {}) => {
  if (typeof window === "undefined") {
    return;
  }

  window.dispatchEvent(
    new CustomEvent(eventName, {
      detail,
    }),
  );
};

const shouldRetry = (error) => {
  const config = error.config;

  if (!config) {
    return false;
  }

  const method = (config.method || "get").toLowerCase();

  if (method !== "get") {
    return false;
  }

  if (!error.response) {
    return true;
  }

  const status = error.response.status;

  return status === 502 || status === 503 || status === 504;
};

const attachRetryInterceptor = (client) => {
  client.interceptors.response.use(
    (response) => {
      dispatchBackendEvent("backend:ready");

      return response;
    },

    async (error) => {
      const config = error.config;

      if (!config || !shouldRetry(error)) {
        return Promise.reject(error);
      }

      const retryCount = config.__retryCount || 0;

      if (retryCount >= RETRY_DELAYS.length) {
        dispatchBackendEvent("backend:unavailable");

        return Promise.reject(error);
      }

      const delay = RETRY_DELAYS[retryCount];

      config.__retryCount = retryCount + 1;

      dispatchBackendEvent("backend:waking", {
        attempt: retryCount + 1,
        maxAttempts: RETRY_DELAYS.length,
        delay,
      });

      console.warn(
        `[API] Backend chưa sẵn sàng. Retry ${
          retryCount + 1
        }/${RETRY_DELAYS.length} sau ${delay / 1000} giây...`,
      );

      await sleep(delay);

      return client.request(config);
    },
  );
};

attachRetryInterceptor(publicClient);
attachRetryInterceptor(authenticatedClient);

export { API_BASE_URL };
