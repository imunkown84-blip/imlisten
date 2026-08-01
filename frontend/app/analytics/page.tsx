'use client';

import { useEffect, useState, useCallback } from 'react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend, LineChart, Line
} from 'recharts';
import { useRequireAuth } from '@/lib/use-require-auth';
import { api, extractErrorMessage } from '@/lib/api';
import { AnalyticsResponse, RecommendationResponse } from '@/lib/types';
import LoadingSpinner from '@/components/LoadingSpinner';
import EmptyState from '@/components/EmptyState';
import PlayButton from '@/components/PlayButton';

const COLORS = ['#c85a32', '#d97706', '#059669', '#2563eb', '#7c3aed', '#b91c1c'];

function toChartData(record: Record<string, number>) {
  return Object.entries(record).map(([name, value]) => ({ name, value }));
}

export default function AnalyticsPage() {
  const { username, isLoading: authLoading } = useRequireAuth();
  const [analytics, setAnalytics] = useState<AnalyticsResponse | null>(null);
  const [recs, setRecs] = useState<RecommendationResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [recsLoading, setRecsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadAnalytics = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await api.get<AnalyticsResponse>('/api/analytics');
      setAnalytics(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  const loadRecommendations = useCallback(async () => {
    setRecsLoading(true);
    try {
      const { data } = await api.get<RecommendationResponse>('/api/recommendations');
      setRecs(data);
    } catch {
      // recommendations are a bonus panel — fail quietly
    } finally {
      setRecsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (username) {
      loadAnalytics();
      loadRecommendations();
    }
  }, [username, loadAnalytics, loadRecommendations]);

  if (authLoading || !username) return <LoadingSpinner />;
  if (loading) return <LoadingSpinner />;
  if (error) return <p className="text-sm font-mono text-red-600 bg-red-50 p-3 rounded border-2 border-red-800">{error}</p>;
  if (!analytics) return null;

  if (analytics.totalTracks === 0) {
    return (
      <EmptyState
        title="No catalog analytics yet"
        subtitle="Save a few songs to your library to generate your taste profile and charts."
      />
    );
  }

  const genreData = toChartData(analytics.tracksByGenre);
  const artistData = toChartData(analytics.tracksByArtist);
  const yearData = Object.entries(analytics.tracksByReleaseYear)
    .map(([year, value]) => ({ year, value }))
    .sort((a, b) => Number(a.year) - Number(b.year));
  const ratingData = toChartData(analytics.ratingDistribution);
  const durationData = analytics.durationHistogram.map((b) => ({ name: b.bucketLabel, value: b.count }));

  return (
    <div>
      <div className="flex flex-col sm:flex-row sm:items-end justify-between mb-6 pb-4 border-b-2 border-retro-ink dark:border-stone-700 gap-2">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold font-sans tracking-tight text-retro-ink dark:text-stone-100">
            Library <span className="font-serif italic font-normal text-retro-accent">Analytics</span>
          </h1>
          <p className="text-stone-600 dark:text-stone-400 font-mono text-xs mt-1">
            Data Insights & AI Recommendation Engine
          </p>
        </div>
        <div className="font-mono text-xs bg-retro-paper dark:bg-stone-800 px-3 py-1 rounded border border-stone-300 dark:border-stone-700 text-stone-700 dark:text-stone-300 self-start sm:self-auto">
          📊 Active Profile
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 sm:gap-4 mb-6">
        <div className="card">
          <p className="text-xs font-mono font-bold text-stone-500 dark:text-stone-400 uppercase tracking-wider">Total Tracks</p>
          <p className="text-2xl sm:text-3xl font-bold font-mono text-retro-ink dark:text-stone-100 mt-1">{analytics.totalTracks}</p>
        </div>
        <div className="card">
          <p className="text-xs font-mono font-bold text-stone-500 dark:text-stone-400 uppercase tracking-wider">Average Rating</p>
          <p className="text-2xl sm:text-3xl font-bold font-mono text-retro-accent mt-1">★ {analytics.averageRating.toFixed(1)} <span className="text-sm font-normal text-stone-500 dark:text-stone-400">/ 5</span></p>
        </div>
        <div className="card">
          <p className="text-xs font-mono font-bold text-stone-500 dark:text-stone-400 uppercase tracking-wider">Avg Length</p>
          <p className="text-2xl sm:text-3xl font-bold font-mono text-retro-ink dark:text-stone-100 mt-1">
            {Math.floor(analytics.averageDurationSeconds / 60)}:
            {Math.round(analytics.averageDurationSeconds % 60)
              .toString()
              .padStart(2, '0')}
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 sm:gap-6">
        <div className="card">
          <p className="font-bold font-mono text-xs uppercase tracking-wider text-stone-700 dark:text-stone-300 mb-4 pb-2 border-b border-stone-200 dark:border-stone-800">
            📊 Tracks by Genre
          </p>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={genreData} margin={{ top: 5, right: 5, left: -20, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e7e5e4" vertical={false} />
              <XAxis dataKey="name" tick={{ fontSize: 10, fontFamily: 'Space Mono' }} />
              <YAxis allowDecimals={false} tick={{ fontSize: 10, fontFamily: 'Space Mono' }} />
              <Tooltip contentStyle={{ backgroundColor: '#fffdf9', borderColor: '#1c1917', borderRadius: '6px', fontFamily: 'Space Mono', fontSize: '12px' }} />
              <Bar dataKey="value" fill="#c85a32" radius={[3, 3, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="card">
          <p className="font-bold font-mono text-xs uppercase tracking-wider text-stone-700 dark:text-stone-300 mb-4 pb-2 border-b border-stone-200 dark:border-stone-800">
            🎤 Top Artists
          </p>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={artistData} layout="vertical" margin={{ top: 5, right: 10, left: 0, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e7e5e4" horizontal={false} />
              <XAxis type="number" allowDecimals={false} tick={{ fontSize: 10, fontFamily: 'Space Mono' }} />
              <YAxis type="category" dataKey="name" width={80} tick={{ fontSize: 10, fontFamily: 'Space Mono' }} />
              <Tooltip contentStyle={{ backgroundColor: '#fffdf9', borderColor: '#1c1917', borderRadius: '6px', fontFamily: 'Space Mono', fontSize: '12px' }} />
              <Bar dataKey="value" fill="#57534e" radius={[0, 3, 3, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="card">
          <p className="font-bold font-mono text-xs uppercase tracking-wider text-stone-700 dark:text-stone-300 mb-4 pb-2 border-b border-stone-200 dark:border-stone-800">
            📅 Timeline Distribution
          </p>
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={yearData} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e7e5e4" vertical={false} />
              <XAxis dataKey="year" tick={{ fontSize: 10, fontFamily: 'Space Mono' }} />
              <YAxis allowDecimals={false} tick={{ fontSize: 10, fontFamily: 'Space Mono' }} />
              <Tooltip contentStyle={{ backgroundColor: '#fffdf9', borderColor: '#1c1917', borderRadius: '6px', fontFamily: 'Space Mono', fontSize: '12px' }} />
              <Line type="monotone" dataKey="value" stroke="#d97706" strokeWidth={2} dot={{ r: 4, fill: '#1c1917' }} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className="card">
          <p className="font-bold font-mono text-xs uppercase tracking-wider text-stone-700 dark:text-stone-300 mb-4 pb-2 border-b border-stone-200 dark:border-stone-800">
            ⭐ Rating Distribution
          </p>
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie data={ratingData} dataKey="value" nameKey="name" innerRadius={40} outerRadius={75} paddingAngle={3}>
                {ratingData.map((_, i) => (
                  <Cell key={i} fill={COLORS[i % COLORS.length]} stroke="#1c1917" strokeWidth={1} />
                ))}
              </Pie>
              <Legend wrapperStyle={{ fontFamily: 'Space Mono', fontSize: '10px' }} />
              <Tooltip contentStyle={{ backgroundColor: '#fffdf9', borderColor: '#1c1917', borderRadius: '6px', fontFamily: 'Space Mono', fontSize: '12px' }} />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="card lg:col-span-2">
          <p className="font-bold font-mono text-xs uppercase tracking-wider text-stone-700 dark:text-stone-300 mb-4 pb-2 border-b border-stone-200 dark:border-stone-800">
            ⏱ Track Length Histogram
          </p>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={durationData} margin={{ top: 5, right: 5, left: -20, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e7e5e4" vertical={false} />
              <XAxis dataKey="name" tick={{ fontSize: 10, fontFamily: 'Space Mono' }} />
              <YAxis allowDecimals={false} tick={{ fontSize: 10, fontFamily: 'Space Mono' }} />
              <Tooltip contentStyle={{ backgroundColor: '#fffdf9', borderColor: '#1c1917', borderRadius: '6px', fontFamily: 'Space Mono', fontSize: '12px' }} />
              <Bar dataKey="value" fill="#c85a32" radius={[3, 3, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="card mt-6 sm:mt-8">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-3 mb-4 border-b-2 border-retro-ink dark:border-stone-700">
          <div>
            <h2 className="text-lg sm:text-xl font-bold font-sans text-retro-ink dark:text-stone-100">
              🤖 AI <span className="font-serif italic font-normal text-retro-accent">Recommendations</span>
            </h2>
            {recs && <p className="text-xs font-mono text-stone-500 dark:text-stone-400 mt-0.5">{recs.basedOn}</p>}
          </div>
          <span className="text-xs font-mono bg-retro-accent text-white px-2.5 py-1 rounded border border-retro-ink dark:border-stone-900 shadow-retro-sm self-start sm:self-auto">
            SMART MATCH
          </span>
        </div>

        {recsLoading && <LoadingSpinner label="Analyzing taste matrix & recommendations…" />}

        {!recsLoading && recs && recs.recommendations.length === 0 && (
          <EmptyState title="No recommendations yet" subtitle="Save a few more songs to build your personalized taste model." />
        )}

        {!recsLoading && recs && recs.recommendations.length > 0 && (
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-3.5">
            {recs.recommendations.map((r) => (
              <div key={r.appleCatalogId} className="flex gap-3 items-center border-2 border-stone-300 dark:border-stone-700 rounded-lg p-3 bg-retro-paper/50 dark:bg-stone-800/50 hover:border-retro-ink dark:hover:border-stone-500 transition-all">
                <div className="relative flex-shrink-0 group">
                  {r.artworkUrl ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img src={r.artworkUrl} alt={r.title} className="w-12 h-12 rounded border border-retro-ink dark:border-stone-700 object-cover" />
                  ) : (
                    <div className="w-12 h-12 rounded border border-retro-ink dark:border-stone-700 bg-stone-200 dark:bg-stone-800" />
                  )}
                  {r.previewUrl && (
                    <div className="absolute inset-0 flex items-center justify-center bg-stone-900/40 rounded opacity-0 group-hover:opacity-100 transition-opacity">
                      <PlayButton track={r} size="sm" />
                    </div>
                  )}
                </div>
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-bold font-sans text-retro-ink dark:text-stone-100 truncate">{r.title}</p>
                  <p className="text-xs font-mono text-stone-600 dark:text-stone-400 truncate">{r.artistName}</p>
                  <p className="text-[11px] font-mono text-retro-accent font-semibold mt-1 truncate">✨ {r.reason}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

