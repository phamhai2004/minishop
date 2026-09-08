const ACCESS_TOKEN_KEY = "minishop_access_token";

const REFRESH_TOKEN_KEY = "minishop_refresh_token";

const USER_KEY = "minishop_user";

const tokenStorage = {
  getAccessToken() {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  },

  setAccessToken(accessToken) {
    if (!accessToken) {
      return;
    }

    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  },

  removeAccessToken() {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
  },

  getRefreshToken() {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  },

  setRefreshToken(refreshToken) {
    if (!refreshToken) {
      return;
    }

    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  },

  removeRefreshToken() {
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  },

  getUser() {
    const storedUser = localStorage.getItem(USER_KEY);

    if (!storedUser) {
      return null;
    }

    try {
      return JSON.parse(storedUser);
    } catch (error) {
      console.error("Không thể đọc user trong localStorage:", error);

      localStorage.removeItem(USER_KEY);

      return null;
    }
  },

  setUser(user) {
    if (!user) {
      return;
    }

    localStorage.setItem(USER_KEY, JSON.stringify(user));
  },

  removeUser() {
    localStorage.removeItem(USER_KEY);
  },

  saveLoginData(loginData) {
    const { accessToken, refreshToken, userId, fullName, email, role } =
      loginData;

    if (!accessToken) {
      throw new Error("LoginResponse không có accessToken.");
    }

    if (!refreshToken) {
      throw new Error("LoginResponse không có refreshToken.");
    }

    const previousUser = this.getUser();

    const currentUser = {
      userId: userId ?? previousUser?.userId ?? null,

      fullName: fullName ?? previousUser?.fullName ?? "",

      email: email ?? previousUser?.email ?? null,

      role: role ?? previousUser?.role ?? null,
    };

    this.setAccessToken(accessToken);

    this.setRefreshToken(refreshToken);

    if (currentUser.userId != null || currentUser.email || currentUser.role) {
      this.setUser(currentUser);
    }
  },

  clearAuthData() {
    this.removeAccessToken();
    this.removeRefreshToken();
    this.removeUser();
  },
};

export default tokenStorage;
