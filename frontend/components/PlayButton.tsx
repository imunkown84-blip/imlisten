'use client';

import { useAudioPlayer, AudioTrack } from './AudioPlayerContext';

export default function PlayButton({
  track,
  size = 'md'
}: {
  track: { previewUrl: string | null; title: string; artistName: string; artworkUrl: string | null };
  size?: 'sm' | 'md';
}) {
  const { toggle, isCurrentTrack, isPlaying } = useAudioPlayer();

  if (!track.previewUrl) return null;

  const audioTrack: AudioTrack = {
    previewUrl: track.previewUrl,
    title: track.title,
    artistName: track.artistName,
    artworkUrl: track.artworkUrl,
  };

  const active = isCurrentTrack(track.previewUrl) && isPlaying;
  const sizeClass = size === 'sm' ? 'play-btn-sm' : 'play-btn-md';

  return (
    <button
      onClick={(e) => { e.stopPropagation(); toggle(audioTrack); }}
      className={`play-btn ${sizeClass} ${active ? 'play-btn-active' : ''}`}
      aria-label={active ? 'Pause' : 'Play preview'}
      title={active ? 'Pause' : 'Play 30s preview'}
    >
      {active ? (
        <svg width="100%" height="100%" viewBox="0 0 24 24" fill="currentColor">
          <rect x="6" y="5" width="4" height="14" rx="1" />
          <rect x="14" y="5" width="4" height="14" rx="1" />
        </svg>
      ) : (
        <svg width="100%" height="100%" viewBox="0 0 24 24" fill="currentColor">
          <path d="M8 5.14v14.72a1 1 0 001.5.86l11-7.36a1 1 0 000-1.72l-11-7.36A1 1 0 008 5.14z" />
        </svg>
      )}
    </button>
  );
}
