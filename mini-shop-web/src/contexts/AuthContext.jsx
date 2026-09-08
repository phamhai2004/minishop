import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";

import authApi from "../api/authApi";
import LoadingSpinner from "../components/common/LoadingSpinner";

import { AUTH_CHANGED_EVENT, notifyAuthChanged } from "../utils/authEvents";

import tokenStorage from "../utils/tokenStorage";

const AuthContext = createContext(null);
const USER_STORAGE_KEY = "minishop_user";

export function AuthProvider({ children }) {
  const [accessToken, setAccessToken] = useState(null);
  const [currentUser, setCurrentUser] = useState(() => tokenStorage.getUser());
  const [authReady, setAuthReady] = useState(false);
  const restoreStartedRef = useRef(false);
  const synchronizeAuthentication = useCallback(() => {
    setAccessToken(tokenStorage.getAccessToken());

    setCurrentUser(tokenStorage.getUser());
  }, []);

  useEffect(() => {
    if (restoreStartedRef.current) {
      return;
    }

    restoreStartedRef.current = true;

    const restoreSession = async () => {
      const refreshToken = tokenStorage.getRefreshToken();

      const storedUser = tokenStorage.getUser();

      if (!refreshToken) {
        tokenStorage.clearAuthData();

        setAccessToken(null);
        setCurrentUser(null);
        setAuthReady(true);

        return;
      }

      if (!storedUser || storedUser.userId == null || !storedUser.role) {
        tokenStorage.clearAuthData();

        setAccessToken(null);
        setCurrentUser(null);
        setAuthReady(true);

        return;
      }

      try {
        const response = await authApi.refresh(refreshToken);
        const apiResponse = response.data;
        const loginData = apiResponse?.data;

        if (!apiResponse?.success) {
          throw new Error(
            apiResponse?.message || "Không thể khôi phục phiên đăng nhập.",
          );
        }

        if (!loginData?.accessToken) {
          throw new Error("Backend không trả về accessToken mới.");
        }

        const nextRefreshToken = loginData.refreshToken || refreshToken;

        tokenStorage.setAccessToken(loginData.accessToken);

        tokenStorage.setRefreshToken(nextRefreshToken);

        setAccessToken(loginData.accessToken);

        setCurrentUser(storedUser);
      } catch (error) {
        console.error("Không thể khôi phục phiên đăng nhập:", error);

        if (error.response) {
          tokenStorage.clearAuthData();

          setAccessToken(null);
          setCurrentUser(null);
        } else {
          setAccessToken(tokenStorage.getAccessToken());
          setCurrentUser(storedUser);
        }
      } finally {
        setAuthReady(true);
      }
    };

    void restoreSession();
  }, []);

  useEffect(() => {
    const handleAuthChanged = () => {
      synchronizeAuthentication();
    };

    const handleStorageChanged = (event) => {
      if (
        event.key === "minishop_access_token" ||
        event.key === "minishop_refresh_token" ||
        event.key === USER_STORAGE_KEY
      ) {
        synchronizeAuthentication();
      }
    };

    window.addEventListener(AUTH_CHANGED_EVENT, handleAuthChanged);

    window.addEventListener("storage", handleStorageChanged);

    return () => {
      window.removeEventListener(AUTH_CHANGED_EVENT, handleAuthChanged);

      window.removeEventListener("storage", handleStorageChanged);
    };
  }, [synchronizeAuthentication]);

  const login = useCallback((loginData) => {
    tokenStorage.saveLoginData(loginData);

    setAccessToken(loginData.accessToken);

    setCurrentUser({
      userId: loginData.userId,

      fullName: loginData.fullName ?? "",

      email: loginData.email,

      role: loginData.role,
    });

    notifyAuthChanged();
  }, []);

  const updateCurrentUser = useCallback((updatedUser) => {
    if (!updatedUser) {
      return;
    }

    setCurrentUser((previousUser) => {
      const normalizedUser = {
        ...previousUser,
        ...updatedUser,

        userId: updatedUser.userId ?? updatedUser.id ?? previousUser?.userId,

        role: updatedUser.role ?? previousUser?.role,
      };

      tokenStorage.setUser(normalizedUser);

      return normalizedUser;
    });
  }, []);

  const logout = useCallback(async () => {
    try {
      const refreshToken = tokenStorage.getRefreshToken();

      if (refreshToken) {
        await authApi.logout(refreshToken);
      }
    } catch (error) {
      console.error("Logout API failed:", error);
    } finally {
      tokenStorage.clearAuthData();

      setAccessToken(null);
      setCurrentUser(null);

      notifyAuthChanged();
    }
  }, []);

  const value = useMemo(
    () => ({
      accessToken,
      currentUser,

      isAuthenticated: Boolean(accessToken),

      authReady,

      login,
      logout,
      updateCurrentUser,
    }),
    [accessToken, currentUser, authReady, login, logout, updateCurrentUser],
  );

  if (!authReady) {
    return (
      <div
        style={{
          minHeight: "100vh",
          display: "grid",
          placeItems: "center",
        }}
      >
        <LoadingSpinner size="large" />
      </div>
    );
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth phải được sử dụng bên trong AuthProvider.");
  }

  return context;
}
