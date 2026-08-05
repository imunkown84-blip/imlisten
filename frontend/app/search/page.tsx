'use client';

import { useEffect, useState, useCallback } from 'react';
import { useRequireAuth } from '@/lib/use-require-auth';
import { api, extractErrorMessage } from '@/lib/api';
import { SearchResult, LibraryItem } from '@/lib/types';
import SongCard from '@/components/SongCard';
import LoadingSpinner from '@/components/LoadingSpinner';
import EmptyState from '@/components/EmptyState';

const DEBOUNCE_MS = 700;
const MIN_QUERY_LENGTH = 2;

export default function SearchPage() {
  const { username, isLoading: authLoading } = useRequireAuth();
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SearchResult[]>([]);
  const [savedIds, setSavedIds] = useState<Set<number>>(new Set());
  const [savingId, setSavingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [hasSearched, setHasSearched] = useState(false);

  const loadLibraryIds = useCallback(async () => {
    try {
      const { data } = await api.get<LibraryItem[]>('/api/library');
      setSavedIds(new Set(data.map((i) => i.appleCatalogId)));
    } catch {
      // non-fatal — save button will just retry duplicate detection server-side
    }
  }, []);

  useEffect(() => {
    if (username) loadLibraryIds();
  }, [username, loadLibraryIds]);

  useEffect(() => {
    const trimmed = query.trim();
    if (!trimmed) {
      setResults([]);
      setHasSearched(false);
      setError(null);
      return;
    }
    if (trimmed.length < MIN_QUERY_LENGTH) {
      setResults([]);
      setHasSearched(false);
      return;
    }
    const timer = setTimeout(async () => {
      setLoading(true);
      setError(null);
      try {
        const { data } = await api.get<any>('/api/search', {
          params: { query: trimmed, type: 'song', limit: 25 }
        });
        setResults(Array.isArray(data) ? data : (data?.results || []));
        setHasSearched(true);
      } catch (err: any) {
        const status = err?.response?.status;
        if (status === 429) {
          setError('Search is temporarily busy — please wait a moment and try again.');
        } else {
          setError(extractErrorMessage(err));
        }
      } finally {
        setLoading(false);
      }
    }, DEBOUNCE_MS);
    return () => clearTimeout(timer);
  }, [query]);

  async function handleSave(result: SearchResult) {
    setSavingId(result.appleCatalogId);
    try {
      await api.post('/api/library', {
        appleCatalogId: result.appleCatalogId,
        title: result.title,
        artistName: result.artistName,
        genre: result.genre,
        releaseDate: result.releaseDate ? result.releaseDate.slice(0, 10) : null,
        durationMillis: result.durationMillis,
        artworkUrl: result.artworkUrl
      });
      setSavedIds((prev) => new Set(prev).add(result.appleCatalogId));
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSavingId(null);
    }
  }

  if (authLoading || !username) return <LoadingSpinner />;

  return (
    <div>
      <div className="flex flex-col sm:flex-row sm:items-end justify-between mb-6 pb-4 border-b-2 border-retro-ink dark:border-stone-700 gap-2">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold font-sans tracking-tight text-retro-ink dark:text-stone-100">
            Search <span className="font-serif italic font-normal text-retro-accent">Catalog</span>
          </h1>
          <p className="text-stone-600 dark:text-stone-400 font-mono text-xs mt-1">
            Powered by iTunes Search API
          </p>
        </div>
        <div className="font-mono text-xs bg-retro-paper dark:bg-stone-800 px-3 py-1 rounded border border-stone-300 dark:border-stone-700 text-stone-700 dark:text-stone-300 self-start sm:self-auto">
          🔍 Query Mode
        </div>
      </div>

      <div className="relative mb-8">
        <input
          className="input text-base py-3 px-4 shadow-retro-sm"
          placeholder="Type title, artist, or song keyword… (e.g. “Shape of You”)"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          autoFocus
        />
        {query && (
          <button
            onClick={() => setQuery('')}
            className="absolute right-3 top-1/2 -translate-y-1/2 font-mono text-xs text-stone-400 hover:text-retro-ink dark:hover:text-stone-100 py-1 px-2"
          >
            CLEAR
          </button>
        )}
      </div>

      {error && (
        <div className="p-3 mb-6 bg-red-50 dark:bg-red-950/40 border-2 border-red-800 rounded text-red-800 dark:text-red-300 font-mono text-xs shadow-retro-sm">
          ⚠️ {error}
        </div>
      )}



      {loading && <LoadingSpinner label="Searching…" />}

      {!loading && hasSearched && results.length === 0 && (
        <EmptyState title="No results" subtitle="Try a different search term." />
      )}

      {!loading && !hasSearched && (
        <EmptyState title="Search the catalog" subtitle="Start typing above to find songs to add to your library." />
      )}

      {!loading && results.length > 0 && (
        <div className="grid gap-3">
          {results.map((r) => (
            <SongCard
              key={r.appleCatalogId}
              result={r}
              onSave={handleSave}
              isSaved={savedIds.has(r.appleCatalogId)}
              isSaving={savingId === r.appleCatalogId}
            />
          ))}
        </div>
      )}
    </div>
  );
}
