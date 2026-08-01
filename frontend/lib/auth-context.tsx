'use client';

import { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { useRouter } from 'next/navigation';
import { api, extractErrorMessage } from './api';

interface AuthContextValue {
  username: string | null;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [username, setUsername] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const storedUsername = window.localStorage.getItem('mci_username');
    const storedToken = window.localStorage.getItem('mci_token');
    if (storedUsername && storedToken) {
      setUsername(storedUsername);
    }
    setIsLoading(false);
  }, []);

  async function login(usernameInput: string, password: string) {
    try {
      const { data } = await api.post('/api/auth/login', { username: usernameInput, password });
      window.localStorage.setItem('mci_token', data.token);
      window.localStorage.setItem('mci_username', data.username);
      setUsername(data.username);
      router.push('/search');
    } catch (err) {
      throw new Error(extractErrorMessage(err));
    }
  }

  async function register(usernameInput: string, password: string) {
    try {
      const { data } = await api.post('/api/auth/register', { username: usernameInput, password });
      window.localStorage.setItem('mci_token', data.token);
      window.localStorage.setItem('mci_username', data.username);
      setUsername(data.username);
      router.push('/search');
    } catch (err) {
      throw new Error(extractErrorMessage(err));
    }
  }

  function logout() {
    window.localStorage.removeItem('mci_token');
    window.localStorage.removeItem('mci_username');
    setUsername(null);
    router.push('/login');
  }

  return (
    <AuthContext.Provider value={{ username, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
