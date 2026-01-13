// import { useEffect } from "react"
// import { useAuth } from "../context/AuthContext"
// import { authService } from "../services/authService"

// // Hook to automatically refresh token before it expires
// export const useTokenRefresh = () => {
//   const { setTokens } = useAuth()

//   useEffect(() => {
//     // Token expires in 15 minutes (900 seconds)
//     // Refresh after 12 minutes (720 seconds)
//     const refreshInterval = setInterval(async () => {
//       try {
//         const response = await authService.refresh()
//         setTokens(response.accessToken, response.refreshToken)
//       } catch (error) {
//         console.error("Token refresh failed:", error)
//         // Token refresh failed, user will need to login again
//       }
//     }, 720000) // 12 minutes

//     return () => clearInterval(refreshInterval)
//   }, [setTokens])
// }

import axios from "../api/axios";
import useAuth from "./useAuth";

const useTokenRefresh = () => {
  const { setAuth } = useAuth();
  const refresh = async () => {
    const response = await axios.get("/auth/refresh", {
      withCredentials: true,
    });
    setAuth((prev) => {
      console.log(JSON.stringify(prev));
      console.log("response from refresh: " + response.data.accessToken);
      return { ...prev, accessToken: response.data.accessToken }; // overwrite accessToken with new one
    });
    return response.data.accessToken;
  };
  return refresh;
};

export default useTokenRefresh;
