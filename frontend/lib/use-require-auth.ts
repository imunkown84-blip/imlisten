'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from './auth-context';

export function useRequireAuth() {
  const { username, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && !username) {
      router.push('/login');
    }
  }, [isLoading, username, router]);

  return { username, isLoading };
}
