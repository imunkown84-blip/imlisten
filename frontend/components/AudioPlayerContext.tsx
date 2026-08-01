'use client';

import { createContext, useContext, useState, useRef, useCallback, useEffect, ReactNode } from 'react';

export interface AudioTrack {
  previewUrl: string;
  title: string;
  artistName: string;
  artworkUrl: string | null;
}

interface AudioPlayerContextValue {
  currentTrack: AudioTrack | null;
  isPlaying: boolean;
  progress: number;          // 0–1
  duration: number;          // seconds
  play: (track: AudioTrack) => void;
  pause: () => void;
  toggle: (track: AudioTrack) => void;
  seek: (fraction: number) => void;
  isCurrentTrack: (previewUrl: string) => boolean;
}

const AudioPlayerContext = createContext<AudioPlayerContextValue | undefined>(undefined);

export function AudioPlayerProvider({ children }: { children: ReactNode }) {
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const [currentTrack, setCurrentTrack] = useState<AudioTrack | null>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [progress, setProgress] = useState(0);
  const [duration, setDuration] = useState(0);

  // Lazily create the audio element (avoids SSR issues)
  useEffect(() => {
    audioRef.current = new Audio();
    audioRef.current.volume = 0.8;

    const audio = audioRef.current;

    const onTimeUpdate = () => {
      if (audio.duration) {
        setProgress(audio.currentTime / audio.duration);
      }
    };
    const onLoadedMetadata = () => setDuration(audio.duration);
    const onEnded = () => { setIsPlaying(false); setProgress(0); };
    const onPlay = () => setIsPlaying(true);
    const onPause = () => setIsPlaying(false);

    audio.addEventListener('timeupdate', onTimeUpdate);
    audio.addEventListener('loadedmetadata', onLoadedMetadata);
    audio.addEventListener('ended', onEnded);
    audio.addEventListener('play', onPlay);
    audio.addEventListener('pause', onPause);

    return () => {
      audio.removeEventListener('timeupdate', onTimeUpdate);
      audio.removeEventListener('loadedmetadata', onLoadedMetadata);
      audio.removeEventListener('ended', onEnded);
      audio.removeEventListener('play', onPlay);
      audio.removeEventListener('pause', onPause);
      audio.pause();
      audio.src = '';
    };
  }, []);

  const play = useCallback((track: AudioTrack) => {
    const audio = audioRef.current;
    if (!audio) return;

    if (currentTrack?.previewUrl !== track.previewUrl) {
      audio.src = track.previewUrl;
      audio.load();
      setCurrentTrack(track);
      setProgress(0);
    }
    audio.play().catch(() => { /* autoplay may be blocked */ });
  }, [currentTrack]);

  const pause = useCallback(() => {
    audioRef.current?.pause();
  }, []);

  const toggle = useCallback((track: AudioTrack) => {
    if (currentTrack?.previewUrl === track.previewUrl && isPlaying) {
      pause();
    } else {
      play(track);
    }
  }, [currentTrack, isPlaying, play, pause]);

  const seek = useCallback((fraction: number) => {
    const audio = audioRef.current;
    if (audio && audio.duration) {
      audio.currentTime = fraction * audio.duration;
    }
  }, []);

  const isCurrentTrack = useCallback((previewUrl: string) => {
    return currentTrack?.previewUrl === previewUrl;
  }, [currentTrack]);

  return (
    <AudioPlayerContext.Provider value={{ currentTrack, isPlaying, progress, duration, play, pause, toggle, seek, isCurrentTrack }}>
      {children}
    </AudioPlayerContext.Provider>
  );
}

export function useAudioPlayer() {
  const ctx = useContext(AudioPlayerContext);
  if (!ctx) throw new Error('useAudioPlayer must be used within AudioPlayerProvider');
  return ctx;
}
