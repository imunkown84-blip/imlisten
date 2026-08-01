import type { Metadata } from 'next';
import './globals.css';
import { AuthProvider } from '@/lib/auth-context';
import { ThemeProvider } from '@/lib/theme-context';
import { AudioPlayerProvider } from '@/components/AudioPlayerContext';
import Navbar from '@/components/Navbar';
import MiniPlayer from '@/components/MiniPlayer';

export const metadata: Metadata = {
  title: 'Music Catalog Insights',
  description: 'Search songs, build your library, and get AI-powered insights.'
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link href="https://fonts.googleapis.com/css2?family=Instrument+Serif:ital@0;1&family=Space+Grotesk:wght@400;500;600;700&family=Space+Mono:ital,wght@0,400;0,700;1,400&display=swap" rel="stylesheet" />
      </head>
      <body className="font-sans antialiased bg-retro-bg dark:bg-stone-950 text-retro-ink dark:text-stone-100 selection:bg-retro-accent selection:text-white min-h-screen transition-colors duration-200">
        <ThemeProvider>
          <AuthProvider>
            <AudioPlayerProvider>
              <Navbar />
              <main className="max-w-5xl mx-auto px-3 sm:px-4 py-4 sm:py-8 pb-32 sm:pb-36">{children}</main>
              <MiniPlayer />
            </AudioPlayerProvider>
          </AuthProvider>
        </ThemeProvider>
      </body>
    </html>
  );
}


