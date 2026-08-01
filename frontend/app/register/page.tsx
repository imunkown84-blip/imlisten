'use client';

import { useState, FormEvent } from 'react';
import Link from 'next/link';
import { useAuth } from '@/lib/auth-context';

export default function RegisterPage() {
  const { register } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await register(username, password);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="max-w-md mx-auto mt-12">
      <div className="text-center mb-8">
        <div className="inline-block bg-retro-ink text-retro-bg px-3 py-1 rounded border-2 border-retro-ink font-mono text-xs font-bold mb-3 shadow-retro-sm">
          NEW USER REGISTRATION
        </div>
        <h1 className="text-3xl font-bold font-sans text-retro-ink">
          Create <span className="font-serif italic font-normal text-retro-accent">Account</span>
        </h1>
        <p className="text-stone-600 font-mono text-xs mt-1">Start building your song library & analytics profile</p>
      </div>

      <form onSubmit={handleSubmit} className="card flex flex-col gap-5 p-6 shadow-retro-lg">
        <div>
          <label className="text-xs font-mono font-bold text-stone-700 mb-1.5 block uppercase tracking-wider">
            Choose Username
          </label>
          <input
            className="input text-sm"
            placeholder="e.g. vinyl_lover"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            minLength={3}
            maxLength={30}
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
            placeholder="Minimum 6 characters"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            minLength={6}
            required
          />
          <p className="text-[11px] font-mono text-stone-500 mt-1">🔒 Stored securely using BCrypt hashing</p>
        </div>
        {error && (
          <div className="p-3 bg-red-50 border-2 border-red-800 rounded text-red-800 font-mono text-xs shadow-retro-sm">
            ⚠️ {error}
          </div>
        )}
        <button type="submit" disabled={submitting} className="btn-primary w-full py-2.5 text-sm mt-1">
          {submitting ? 'Creating account…' : 'Register Account →'}
        </button>
      </form>

      <p className="text-center font-mono text-xs text-stone-600 mt-6">
        Already registered?{' '}
        <Link href="/login" className="text-retro-accent font-bold hover:underline">
          Log In Here
        </Link>
      </p>
    </div>
  );

}
