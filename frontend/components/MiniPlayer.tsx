'use client';

import { useAudioPlayer } from './AudioPlayerContext';

function formatTime(sec: number) {
  if (!sec || !isFinite(sec)) return '0:00';
  const m = Math.floor(sec / 60);
  const s = Math.floor(sec % 60);
  return `${m}:${s.toString().padStart(2, '0')}`;
}

export default function MiniPlayer() {
  const { currentTrack, isPlaying, progress, duration, pause, play, seek } = useAudioPlayer();

  if (!currentTrack) return null;

  function handleProgressClick(e: React.MouseEvent<HTMLDivElement>) {
    const rect = e.currentTarget.getBoundingClientRect();
    const fraction = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
    seek(fraction);
  }

  return (
    <div className="mini-player">
      <div className="mini-player-inner">
        {/* Artwork */}
        {currentTrack.artworkUrl ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img
            src={currentTrack.artworkUrl}
            alt={currentTrack.title}
            className="mini-player-artwork"
          />
        ) : (
          <div className="mini-player-artwork mini-player-artwork-placeholder" />
        )}

        {/* Track info */}
        <div className="mini-player-info">
          <p className="mini-player-title">{currentTrack.title}</p>
          <p className="mini-player-artist">{currentTrack.artistName}</p>
        </div>

        {/* Play/Pause */}
        <button
          onClick={() => isPlaying ? pause() : play(currentTrack)}
          className="mini-player-btn"
          aria-label={isPlaying ? 'Pause' : 'Play'}
        >
          {isPlaying ? (
            <svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
              <rect x="6" y="5" width="4" height="14" rx="1" />
              <rect x="14" y="5" width="4" height="14" rx="1" />
            </svg>
          ) : (
            <svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
              <path d="M8 5.14v14.72a1 1 0 001.5.86l11-7.36a1 1 0 000-1.72l-11-7.36A1 1 0 008 5.14z" />
            </svg>
          )}
        </button>

        {/* Time */}
        <span className="mini-player-time">
          {formatTime(progress * duration)} / {formatTime(duration)}
        </span>

        {/* Progress bar */}
        <div className="mini-player-progress" onClick={handleProgressClick}>
          <div className="mini-player-progress-fill" style={{ width: `${progress * 100}%` }} />
        </div>
      </div>
    </div>
  );
}
