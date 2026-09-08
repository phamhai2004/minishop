import { authenticatedClient, publicClient } from "./httpClients";

import tokenStorage from "../utils/tokenStorage";

import { notifyAuthChanged } from "../utils/authEvents";

let refreshPromise = null;

function isAuthenticationEndpoint(url = "") {
  return url.includes("/auth/login") || url.includes("/auth/refresh");
}

function validateRefreshResponse(response) {
  const apiResponse = response.data;
  const loginData = apiResponse?.data;

  if (!apiResponse?.success) {
    throw new Error(apiResponse?.message ?? "Làm mới token không thành công.");
  }

  if (!loginData?.accessToken) {
    throw new Error("Backend không trả về accessToken mới.");
  }

  if (!loginData?.refreshToken) {
    throw new Error("Backend không trả về refreshToken mới.");
  }

  return loginData;
}

async function requestNewAccessToken() {
  const refreshToken = tokenStorage.getRefreshToken();

  if (!refreshToken) {
    throw new Error("Không tìm thấy refresh token.");
  }

  if (!refreshPromise) {
    refreshPromise = publicClient
      .post("/auth/refresh", {
        refreshToken,
      })
      .then((response) => {
        const loginData = validateRefreshResponse(response);

        tokenStorage.saveLoginData(loginData);

        notifyAuthChanged();

        return loginData.accessToken;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }

  return refreshPromise;
}

authenticatedClient.interceptors.request.use(
  (config) => {
    const accessToken = tokenStorage.getAccessToken();

    if (accessToken) {
      config.headers = config.headers ?? {};

      config.headers.Authorization = `Bearer ${accessToken}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

authenticatedClient.interceptors.response.use(
  (response) => response,

  async (error) => {
    const originalRequest = error.config;
    const status = error.response?.status;
    const requestUrl = originalRequest?.url ?? "";

    const cannotRefresh =
      !originalRequest ||
      status !== 401 ||
      originalRequest._retry ||
      isAuthenticationEndpoint(requestUrl);

    if (cannotRefresh) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      const newAccessToken = await requestNewAccessToken();

      originalRequest.headers = originalRequest.headers ?? {};

      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

      return authenticatedClient(originalRequest);
    } catch (refreshError) {
      tokenStorage.clearAuthData();

      notifyAuthChanged();

      return Promise.reject(refreshError);
    }
  },
);

export default authenticatedClient;
