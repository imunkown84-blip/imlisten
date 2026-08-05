import axios, { AxiosError } from 'axios';

export const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8082';

export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000,
  headers: { 'Content-Type': 'application/json' }
});

// Attach the JWT (if present) to every outgoing request.
api.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const token = window.localStorage.getItem('mci_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

// On a 401, the token is invalid/expired — clear it and bounce to login.
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401 && typeof window !== 'undefined') {
      window.localStorage.removeItem('mci_token');
      window.localStorage.removeItem('mci_username');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export function extractErrorMessage(err: unknown): string {
  if (axios.isAxiosError(err)) {
    if (err.code === 'ECONNABORTED' || err.message?.includes('timeout')) {
      if (API_BASE_URL.includes('localhost')) {
        return 'Server response timed out. Please check if the local backend server is running on port 8082.';
      }
      return 'Server response timed out. The backend server (Render free tier) may be cold-starting. Please wait ~30 seconds and try again.';
    }
    if (err.code === 'ERR_NETWORK') {
      if (API_BASE_URL.includes('localhost')) {
        return 'Cannot connect to backend server. Please verify backend is running on port 8082.';
      }
      return `Cannot connect to backend server at ${API_BASE_URL}. Please verify backend URL and CORS settings.`;
    }
    const data = err.response?.data as { message?: string; details?: string[] } | undefined;
    if (data?.details?.length) return data.details.join(', ');
    if (data?.message) return data.message;
    if (err.message) return err.message;
  }
  return 'Something went wrong. Please try again.';
}
