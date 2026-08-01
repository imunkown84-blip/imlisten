'use client';

import { useState } from 'react';
import { LibraryItem } from '@/lib/types';
import StarRating from './StarRating';

export default function LibraryCard({
  item,
  onUpdate,
  onDelete
}: {
  item: LibraryItem;
  onUpdate: (id: number, rating: number | null, notes: string | null) => Promise<void>;
  onDelete: (id: number) => void;
}) {
  const [notes, setNotes] = useState(item.userNotes ?? '');
  const [rating, setRating] = useState(item.userRating);
  const [saving, setSaving] = useState(false);

  async function handleBlurSave() {
    if (notes === (item.userNotes ?? '') && rating === item.userRating) return;
    setSaving(true);
    try {
      await onUpdate(item.id, rating, notes || null);
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="card flex flex-col sm:flex-row gap-3 sm:gap-4 items-start">
      <div className="flex items-center gap-3 w-full sm:w-auto justify-between sm:justify-start">
        <div className="flex items-center gap-3 min-w-0">
          {item.artworkUrl ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img src={item.artworkUrl} alt={item.title} className="w-14 h-14 sm:w-16 sm:h-16 rounded border-2 border-retro-ink dark:border-stone-700 object-cover flex-shrink-0" />
          ) : (
            <div className="w-14 h-14 sm:w-16 sm:h-16 rounded border-2 border-retro-ink dark:border-stone-700 bg-retro-paper dark:bg-stone-800 flex items-center justify-center text-[10px] sm:text-xs font-mono text-retro-muted dark:text-stone-400 flex-shrink-0">
              NO ART
            </div>
          )}
          <div className="min-w-0 sm:hidden">
            <p className="font-bold text-retro-ink dark:text-stone-100 font-sans text-sm truncate tracking-tight">{item.title}</p>
            <p className="text-xs font-mono text-stone-600 dark:text-stone-400 truncate mt-0.5">{item.artistName}</p>
          </div>
        </div>

        <button
          onClick={() => onDelete(item.id)}
          className="text-xs font-mono text-stone-400 hover:text-red-500 transition-colors py-1 px-2 border border-transparent hover:border-red-800 hover:bg-red-950/30 rounded flex-shrink-0 sm:hidden"
          aria-label="Remove from library"
        >
          ✕
        </button>
      </div>

      <div className="flex-1 min-w-0 w-full">
        <div className="hidden sm:flex items-start justify-between gap-2">
          <div className="min-w-0">
            <p className="font-bold text-retro-ink dark:text-stone-100 font-sans text-base truncate tracking-tight">{item.title}</p>
            <p className="text-xs font-mono text-stone-600 dark:text-stone-400 truncate mt-0.5">{item.artistName}</p>
          </div>
          <button
            onClick={() => onDelete(item.id)}
            className="text-xs font-mono text-stone-400 hover:text-red-500 transition-colors py-1 px-2 border border-transparent hover:border-red-800 hover:bg-red-950/30 rounded flex-shrink-0"
            aria-label="Remove from library"
          >
            ✕ Remove
          </button>
        </div>
        <div className="flex flex-wrap gap-1.5 sm:gap-2 mt-1 sm:mt-1.5 text-[11px] font-mono">
          {item.genre && (
            <span className="bg-retro-paper dark:bg-stone-800 px-1.5 py-0.5 rounded border border-stone-300 dark:border-stone-700 text-stone-700 dark:text-stone-300">
              {item.genre}
            </span>
          )}
          {item.releaseDate && (
            <span className="bg-stone-100 dark:bg-stone-800 px-1.5 py-0.5 rounded border border-stone-300 dark:border-stone-700 text-stone-600 dark:text-stone-400">
              📅 {item.releaseDate.slice(0, 4)}
            </span>
          )}
        </div>
        <div className="mt-2.5 sm:mt-3 flex items-center gap-3">
          <StarRating
            value={rating}
            onChange={(r) => {
              setRating(r);
              onUpdate(item.id, r, notes || null);
            }}
          />
          {saving && <span className="text-xs font-mono text-stone-400">Saving…</span>}
        </div>
        <input
          className="input mt-2 text-xs"
          placeholder="✍️ Add a private note…"
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          onBlur={handleBlurSave}
        />
      </div>
    </div>
  );

}
