'use client';

import { useState, FormEvent } from 'react';
import Link from 'next/link';
import { useAuth } from '@/lib/auth-context';

export default function LoginPage() {
  const { login } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await login(username, password);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="max-w-md mx-auto mt-4 sm:mt-12 px-2 sm:px-0">
      <div className="text-center mb-6 sm:mb-8">
        <div className="inline-block bg-retro-accent text-white px-3 py-1 rounded border-2 border-retro-ink dark:border-stone-900 font-mono text-xs font-bold mb-3 shadow-retro-sm">
          MUSIC CATALOG INSIGHTS
        </div>
        <h1 className="text-2xl sm:text-3xl font-bold font-sans text-retro-ink dark:text-stone-100">
          Welcome <span className="font-serif italic font-normal text-retro-accent">Back</span>
        </h1>
        <p className="text-stone-600 dark:text-stone-400 font-mono text-xs mt-1">Access your saved songs & analytics</p>
      </div>

      <form onSubmit={handleSubmit} className="card flex flex-col gap-4 sm:gap-5 p-4 sm:p-6 shadow-retro-lg">
        <div>
          <label className="text-xs font-mono font-bold text-stone-700 dark:text-stone-300 mb-1.5 block uppercase tracking-wider">
            Username
          </label>
          <input
            className="input text-sm"
            placeholder="e.g. musicfan"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div>
          <label className="text-xs font-mono font-bold text-stone-700 mb-1.5 block uppercase tracking-wider">
            Password
          </label>
          <input
            type="password"
            className="input text-sm"
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        {error && (
          <div className="p-3 bg-red-50 border-2 border-red-800 rounded text-red-800 font-mono text-xs shadow-retro-sm">
            ⚠️ {error}
          </div>
        )}
        <button type="submit" disabled={submitting} className="btn-primary w-full py-2.5 text-sm mt-1">
          {submitting ? 'Authenticating…' : 'Log In →'}
        </button>
      </form>

      <p className="text-center font-mono text-xs text-stone-600 mt-6">
        Don&apos;t have an account?{' '}
        <Link href="/register" className="text-retro-accent font-bold hover:underline">
          Create Account
        </Link>
      </p>
    </div>
  );

}
