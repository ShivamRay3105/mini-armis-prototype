import { axiosPrivate } from "../api/axios";
import { useEffect, useRef } from "react";
import useRefreshToken from "./useRefreshToken";
import useAuth from "./useAuth";

const useAxiosPrivate = () => {
  const refresh = useRefreshToken();
  const { auth } = useAuth();
  const refreshTokenPromiseRef = useRef(null);

  useEffect(() => {
    const requestIntercept = axiosPrivate.interceptors.request.use(
      (config) => {
        if (!config.headers["Authorization"]) {
          config.headers["Authorization"] = `Bearer ${auth?.accessToken}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );
    const responseIntercept = axiosPrivate.interceptors.response.use(
      (response) => response,
      async (error) => {
        const prevRequest = error?.config;
        
        // Ignore aborted requests (CanceledError)
        if (error.code === 'ERR_CANCELED' || error.name === 'CanceledError') {
          return Promise.reject(error);
        }

        if (error?.response?.status === 401 && !prevRequest?._retry) {
          prevRequest._retry = true;

          // Prevent multiple simultaneous refresh calls
          if (!refreshTokenPromiseRef.current) {
            refreshTokenPromiseRef.current = refresh();
          }

          try {
            const newAccessToken = await refreshTokenPromiseRef.current;
            prevRequest.headers["Authorization"] = `Bearer ${newAccessToken}`;
            refreshTokenPromiseRef.current = null; // Reset after successful refresh
            return axiosPrivate(prevRequest);
          } catch (refreshError) {
            refreshTokenPromiseRef.current = null; // Reset on error
            return Promise.reject(refreshError);
          }
        }
        return Promise.reject(error);
      }
    );
    return () => {
      axiosPrivate.interceptors.request.eject(requestIntercept);
      axiosPrivate.interceptors.response.eject(responseIntercept);
    };
  }, [auth, refresh]);
  return axiosPrivate;
};

export default useAxiosPrivate;
