'use client';

import { SearchResult } from '@/lib/types';
import PlayButton from './PlayButton';

function formatDuration(ms: number | null) {
  if (!ms) return '—';
  const totalSeconds = Math.round(ms / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${minutes}:${seconds.toString().padStart(2, '0')}`;
}

export default function SongCard({
  result,
  onSave,
  isSaved,
  isSaving
}: {
  result: SearchResult;
  onSave: (r: SearchResult) => void;
  isSaved: boolean;
  isSaving: boolean;
}) {
  return (
    <div className="card flex flex-col sm:flex-row gap-3 sm:gap-4 items-start sm:items-center">
      <div className="flex items-center gap-3 w-full sm:w-auto">
        <div className="relative flex-shrink-0 group">
          {result.artworkUrl ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img src={result.artworkUrl} alt={result.title} className="w-14 h-14 sm:w-16 sm:h-16 rounded border-2 border-retro-ink dark:border-stone-700 object-cover" />
          ) : (
            <div className="w-14 h-14 sm:w-16 sm:h-16 rounded border-2 border-retro-ink dark:border-stone-700 bg-retro-paper dark:bg-stone-800 flex items-center justify-center text-[10px] sm:text-xs font-mono text-retro-muted dark:text-stone-400">
              NO ART
            </div>
          )}
          {result.previewUrl && (
            <div className="absolute inset-0 flex items-center justify-center bg-stone-900/40 rounded border-2 border-transparent group-hover:opacity-100 opacity-0 transition-opacity">
              <PlayButton track={result} size="md" />
            </div>
          )}
        </div>
        <div className="flex-1 min-w-0 sm:hidden">
          <p className="font-bold text-retro-ink dark:text-stone-100 font-sans text-sm truncate tracking-tight">{result.title}</p>
          <p className="text-xs font-mono text-stone-600 dark:text-stone-400 truncate mt-0.5">{result.artistName}</p>
        </div>
      </div>

      <div className="flex-1 min-w-0 w-full sm:w-auto">
        <div className="hidden sm:block">
          <p className="font-bold text-retro-ink dark:text-stone-100 font-sans text-base truncate tracking-tight">{result.title}</p>
          <p className="text-xs font-mono text-stone-600 dark:text-stone-400 truncate mt-0.5">{result.artistName}</p>
        </div>
        <div className="flex flex-wrap gap-1.5 sm:gap-2 mt-1 sm:mt-2 text-[11px] font-mono">
          {result.genre && (
            <span className="bg-retro-paper dark:bg-stone-800 px-1.5 py-0.5 rounded border border-stone-300 dark:border-stone-700 text-stone-700 dark:text-stone-300">
              {result.genre}
            </span>
          )}
          <span className="bg-stone-100 dark:bg-stone-800 px-1.5 py-0.5 rounded border border-stone-300 dark:border-stone-700 text-stone-600 dark:text-stone-400">
            ⏱ {formatDuration(result.durationMillis)}
          </span>
          {result.releaseDate && (
            <span className="bg-stone-100 dark:bg-stone-800 px-1.5 py-0.5 rounded border border-stone-300 dark:border-stone-700 text-stone-600 dark:text-stone-400">
              📅 {result.releaseDate.slice(0, 4)}
            </span>
          )}
        </div>
      </div>

      <button
        onClick={() => onSave(result)}
        disabled={isSaved || isSaving}
        className="btn-primary text-xs py-2 px-4 w-full sm:w-auto flex-shrink-0 mt-1 sm:mt-0"
      >
        {isSaved ? '✓ Saved' : isSaving ? 'Saving…' : '+ Save'}
      </button>
    </div>
  );
}

