'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useAuth } from '@/lib/auth-context';
import { useTheme } from '@/lib/theme-context';

const links = [
  { href: '/search', label: 'Search' },
  { href: '/library', label: 'My Library' },
  { href: '/analytics', label: 'Analytics' }
];

export default function Navbar() {
  const pathname = usePathname();
  const { username, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();

  if (!username) return null;

  return (
    <nav className="bg-retro-card dark:bg-stone-900 border-b-2 border-retro-ink dark:border-stone-700 sticky top-0 z-40 transition-colors">
      <div className="max-w-5xl mx-auto px-4 flex items-center justify-between h-16">
        <div className="flex items-center gap-6">
          <Link href="/search" className="font-bold text-lg flex items-center gap-2 text-retro-ink dark:text-stone-100 tracking-tight font-sans">
            <span className="bg-retro-accent text-white px-2 py-0.5 rounded border border-retro-ink dark:border-stone-900 text-sm font-mono shadow-retro-sm">
              MCI
            </span>
            <span>Catalog<span className="font-serif italic font-normal text-retro-accent">Insights</span></span>
          </Link>
          <div className="flex gap-1.5">
            {links.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                className={`px-3 py-1.5 rounded text-xs font-mono font-bold transition-all border-2 ${
                  pathname === link.href
                    ? 'bg-retro-ink dark:bg-stone-100 text-retro-bg dark:text-stone-950 border-retro-ink dark:border-stone-100 shadow-retro-sm'
                    : 'bg-transparent text-stone-700 dark:text-stone-300 border-transparent hover:border-retro-ink dark:hover:border-stone-600 hover:bg-retro-paper dark:hover:bg-stone-800'
                }`}
              >
                {link.label}
              </Link>
            ))}
          </div>
        </div>
        <div className="flex items-center gap-2.5">
          <button
            onClick={toggleTheme}
            className="p-1.5 rounded border-2 border-retro-ink dark:border-stone-700 bg-retro-paper dark:bg-stone-800 text-retro-ink dark:text-stone-100 font-mono text-xs shadow-retro-sm hover:translate-x-[-1px] hover:translate-y-[-1px] transition-all"
            title={`Switch to ${theme === 'light' ? 'Dark' : 'Light'} Mode`}
            aria-label="Toggle theme"
          >
            {theme === 'light' ? '🌙 Dark' : '☀️ Light'}
          </button>
          <span className="text-xs font-mono bg-retro-paper dark:bg-stone-800 px-2.5 py-1.5 rounded border border-stone-400 dark:border-stone-700 text-stone-700 dark:text-stone-300">
            ● {username}
          </span>
          <button onClick={logout} className="btn-secondary text-xs py-1 px-3">
            Log out
          </button>
        </div>
      </div>
    </nav>
  );
}


