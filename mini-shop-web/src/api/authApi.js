import { publicClient } from "./httpClients";

const authApi = {
  login(loginRequest) {
    return publicClient.post("/auth/login", loginRequest);
  },

  register(registerRequest) {
    return publicClient.post("/users/register", registerRequest);
  },

  requestRegistrationEmail(email) {
    return publicClient.post("/users/register/request-email", {
      email,
    });
  },

  verifyRegistrationEmail(token) {
    return publicClient.get("/users/register/verify", {
      params: { token },
    });
  },

  verifyCurrentUserEmail(token) {
    return publicClient.get("/users/email/verify", {
      params: { token },
    });
  },

  resendCurrentUserEmailVerification(token) {
    return publicClient.post("/users/email/resend", null, {
      params: { token },
    });
  },

  refresh(refreshToken) {
    return publicClient.post("/auth/refresh", {
      refreshToken,
    });
  },

  logout(refreshToken) {
    return publicClient.post("/auth/logout", {
      refreshToken,
    });
  },
};

export default authApi;
