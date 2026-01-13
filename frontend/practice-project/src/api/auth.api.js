import api from "./axios";

export const registerUser = (data) =>
  api.post("/auth-service/auth/register", data);

export const loginUser = (data) =>
  api.post("/auth-service/auth/login", data);

export const refreshToken = () =>
  api.post("/auth-service/auth/refresh", null, {
    headers: {
      "X-Refresh-Token": localStorage.getItem("refreshToken"),
      "X-User-Id": localStorage.getItem("userId"),
    },
  });

export const logoutUser = () =>
  api.post("/auth-service/auth/logout", null, {
    headers: {
      "X-Refresh-Token": localStorage.getItem("refreshToken"),
      "X-User-Id": localStorage.getItem("userId"),
    },
  });
