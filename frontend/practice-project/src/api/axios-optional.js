// import axios from "axios";
// import { getAccessToken, setAccessToken, clearAccessToken } from "../auth/tokenStore";
// import { refreshToken } from "./auth.api";

// const api = axios.create({
//   baseURL: "http://localhost:8765",
// });

// // Request interceptor: attach access token
// api.interceptors.request.use((config) => {
//   const token = getAccessToken();
//   if (token) config.headers["Authorization"] = `Bearer ${token}`;
//   return config;
// });

// // Response interceptor: handle 401 (token expired)
// api.interceptors.response.use(
//   (response) => response,
//   async (error) => {
//     const originalRequest = error.config;
//     if (error.response?.status === 401 && !originalRequest._retry) {
//       originalRequest._retry = true;
//       try {
//         const newAccessToken = await refreshToken(); // call refresh API
//         setAccessToken(newAccessToken);
//         originalRequest.headers["Authorization"] = `Bearer ${newAccessToken}`;
//         return api(originalRequest);
//       } catch (err) {
//         clearAccessToken();
//         return Promise.reject(err);
//       }
//     }
//     return Promise.reject(error);
//   }
// );

// export default api;


import axios from "axios";

export default axios.create({
  baseURL: "http://localhost:8765",
});