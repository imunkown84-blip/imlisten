'use client';

import { useEffect, useState, useCallback } from 'react';
import { useRequireAuth } from '@/lib/use-require-auth';
import { api, extractErrorMessage } from '@/lib/api';
import { LibraryItem } from '@/lib/types';
import LibraryCard from '@/components/LibraryCard';
import LoadingSpinner from '@/components/LoadingSpinner';
import EmptyState from '@/components/EmptyState';

export default function LibraryPage() {
  const { username, isLoading: authLoading } = useRequireAuth();
  const [items, setItems] = useState<LibraryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadLibrary = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await api.get<LibraryItem[]>('/api/library');
      setItems(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (username) loadLibrary();
  }, [username, loadLibrary]);

  async function handleUpdate(id: number, rating: number | null, notes: string | null) {
    const item = items.find((i) => i.id === id);
    if (!item) return;
    try {
      const { data } = await api.put<LibraryItem>(`/api/library/${id}`, {
        appleCatalogId: item.appleCatalogId,
        title: item.title,
        artistName: item.artistName,
        genre: item.genre,
        releaseDate: item.releaseDate,
        durationMillis: item.durationMillis,
        artworkUrl: item.artworkUrl,
        userRating: rating,
        userNotes: notes
      });
      setItems((prev) => prev.map((i) => (i.id === id ? data : i)));
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  }

  async function handleDelete(id: number) {
    const prevItems = items;
    setItems((prev) => prev.filter((i) => i.id !== id));
    try {
      await api.delete(`/api/library/${id}`);
    } catch (err) {
      setItems(prevItems);
      setError(extractErrorMessage(err));
    }
  }

  if (authLoading || !username) return <LoadingSpinner />;

  return (
    <div>
      <div className="flex flex-col sm:flex-row sm:items-end justify-between mb-6 pb-4 border-b-2 border-retro-ink dark:border-stone-700 gap-2">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold font-sans tracking-tight text-retro-ink dark:text-stone-100">
            My <span className="font-serif italic font-normal text-retro-accent">Library</span>
          </h1>
          <p className="text-stone-600 dark:text-stone-400 font-mono text-xs mt-1">
            Personal Saved Collection & Notes
          </p>
        </div>
        <div className="font-mono text-xs bg-retro-paper dark:bg-stone-800 px-3 py-1 rounded border border-stone-300 dark:border-stone-700 text-stone-700 dark:text-stone-300 self-start sm:self-auto">
          📀 {items.length} {items.length === 1 ? 'Track' : 'Tracks'} Saved
        </div>
      </div>

      {error && (
        <div className="p-3 mb-6 bg-red-50 dark:bg-red-950/40 border-2 border-red-800 rounded text-red-800 dark:text-red-300 font-mono text-xs shadow-retro-sm">
          ⚠️ {error}
        </div>
      )}


      {loading && <LoadingSpinner />}

      {!loading && items.length === 0 && (
        <EmptyState title="Your library is empty" subtitle="Search for songs and save them to build your collection." />
      )}

      {!loading && items.length > 0 && (
        <div className="grid gap-3">
          {items.map((item) => (
            <LibraryCard key={item.id} item={item} onUpdate={handleUpdate} onDelete={handleDelete} />
          ))}
        </div>
      )}
    </div>
  );
}
