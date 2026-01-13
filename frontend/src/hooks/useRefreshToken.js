import axios from "../api/axios";
import useAuth from "./useAuth";

const useRefreshToken = () => {
  const { setAuth, auth } = useAuth();

  const refresh = async () => {
    const response = await axios.post(
      "/auth-service/auth/refresh",
      {}, // no body
      {
        withCredentials: true,
      }
    );

    // Backend returns AuthResponse in the body, not headers
    const newAccessToken = response?.data?.accessToken;

    if (!newAccessToken) {
      throw new Error("No access token in refresh response");
    }

    setAuth(prev => ({
      ...prev,
      accessToken: newAccessToken,
    }));

    return newAccessToken;
  };

  return refresh;
};

export default useRefreshToken;
