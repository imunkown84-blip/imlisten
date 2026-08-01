'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/auth-context';
import LoadingSpinner from '@/components/LoadingSpinner';

export default function HomePage() {
  const { username, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (isLoading) return;
    router.push(username ? '/search' : '/login');
  }, [isLoading, username, router]);

  return <LoadingSpinner />;
}
