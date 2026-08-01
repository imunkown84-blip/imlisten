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
      <div className="flex flex-col sm:flex-row sm:items-end justify-between mb-6 pb-4 border-b-2 border-retro-ink gap-2">
        <div>
          <h1 className="text-3xl font-bold font-sans tracking-tight text-retro-ink">
            Library <span className="font-serif italic font-normal text-retro-accent">Analytics</span>
          </h1>
          <p className="text-stone-600 font-mono text-xs mt-1">
            Data Insights & AI Recommendation Engine
          </p>
        </div>
        <div className="font-mono text-xs bg-retro-paper px-3 py-1 rounded border border-stone-300 text-stone-700 self-start sm:self-auto">
          📊 Active Profile
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
        <div className="card">
          <p className="text-xs font-mono font-bold text-stone-500 uppercase tracking-wider">Total Tracks</p>
          <p className="text-3xl font-bold font-mono text-retro-ink mt-1">{analytics.totalTracks}</p>
        </div>
        <div className="card">
          <p className="text-xs font-mono font-bold text-stone-500 uppercase tracking-wider">Average Rating</p>
          <p className="text-3xl font-bold font-mono text-retro-accent mt-1">★ {analytics.averageRating.toFixed(1)} <span className="text-sm font-normal text-stone-500">/ 5</span></p>
        </div>
        <div className="card">
          <p className="text-xs font-mono font-bold text-stone-500 uppercase tracking-wider">Avg Length</p>
          <p className="text-3xl font-bold font-mono text-retro-ink mt-1">
            {Math.floor(analytics.averageDurationSeconds / 60)}:
            {Math.round(analytics.averageDurationSeconds % 60)
              .toString()
              .padStart(2, '0')}
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card">
          <p className="font-bold font-mono text-xs uppercase tracking-wider text-stone-700 mb-4 pb-2 border-b border-stone-200">
            📊 Tracks by Genre
          </p>
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={genreData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e7e5e4" vertical={false} />
              <XAxis dataKey="name" tick={{ fontSize: 11, fontFamily: 'Space Mono' }} />
              <YAxis allowDecimals={false} tick={{ fontSize: 11, fontFamily: 'Space Mono' }} />
              <Tooltip contentStyle={{ backgroundColor: '#fffdf9', borderColor: '#1c1917', borderRadius: '6px', fontFamily: 'Space Mono', fontSize: '12px' }} />
              <Bar dataKey="value" fill="#c85a32" radius={[3, 3, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="card">
          <p className="font-bold font-mono text-xs uppercase tracking-wider text-stone-700 mb-4 pb-2 border-b border-stone-200">
            🎤 Top Artists
          </p>
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={artistData} layout="vertical" margin={{ left: 20 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e7e5e4" horizontal={false} />
              <XAxis type="number" allowDecimals={false} tick={{ fontSize: 11, fontFamily: 'Space Mono' }} />
              <YAxis type="category" dataKey="name" width={100} tick={{ fontSize: 11, fontFamily: 'Space Mono' }} />
              <Tooltip contentStyle={{ backgroundColor: '#fffdf9', borderColor: '#1c1917', borderRadius: '6px', fontFamily: 'Space Mono', fontSize: '12px' }} />
              <Bar dataKey="value" fill="#57534e" radius={[0, 3, 3, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="card">
          <p className="font-bold font-mono text-xs uppercase tracking-wider text-stone-700 mb-4 pb-2 border-b border-stone-200">
            📅 Timeline Distribution
          </p>
          <ResponsiveContainer width="100%" height={240}>
            <LineChart data={yearData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e7e5e4" vertical={false} />
              <XAxis dataKey="year" tick={{ fontSize: 11, fontFamily: 'Space Mono' }} />
              <YAxis allowDecimals={false} tick={{ fontSize: 11, fontFamily: 'Space Mono' }} />
              <Tooltip contentStyle={{ backgroundColor: '#fffdf9', borderColor: '#1c1917', borderRadius: '6px', fontFamily: 'Space Mono', fontSize: '12px' }} />
              <Line type="monotone" dataKey="value" stroke="#d97706" strokeWidth={2} dot={{ r: 4, fill: '#1c1917' }} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className="card">
          <p className="font-bold font-mono text-xs uppercase tracking-wider text-stone-700 mb-4 pb-2 border-b border-stone-200">
            ⭐ Rating Distribution
          </p>
          <ResponsiveContainer width="100%" height={240}>
            <PieChart>
              <Pie data={ratingData} dataKey="value" nameKey="name" innerRadius={45} outerRadius={85} paddingAngle={3}>
                {ratingData.map((_, i) => (
                  <Cell key={i} fill={COLORS[i % COLORS.length]} stroke="#1c1917" strokeWidth={1} />
                ))}
              </Pie>
              <Legend wrapperStyle={{ fontFamily: 'Space Mono', fontSize: '11px' }} />
              <Tooltip contentStyle={{ backgroundColor: '#fffdf9', borderColor: '#1c1917', borderRadius: '6px', fontFamily: 'Space Mono', fontSize: '12px' }} />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="card lg:col-span-2">
          <p className="font-bold font-mono text-xs uppercase tracking-wider text-stone-700 mb-4 pb-2 border-b border-stone-200">
            ⏱ Track Length Histogram
          </p>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={durationData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e7e5e4" vertical={false} />
              <XAxis dataKey="name" tick={{ fontSize: 11, fontFamily: 'Space Mono' }} />
              <YAxis allowDecimals={false} tick={{ fontSize: 11, fontFamily: 'Space Mono' }} />
              <Tooltip contentStyle={{ backgroundColor: '#fffdf9', borderColor: '#1c1917', borderRadius: '6px', fontFamily: 'Space Mono', fontSize: '12px' }} />
              <Bar dataKey="value" fill="#c85a32" radius={[3, 3, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="card mt-8">
        <div className="flex items-center justify-between pb-3 mb-4 border-b-2 border-retro-ink">
          <div>
            <h2 className="text-xl font-bold font-sans text-retro-ink">
              🤖 AI <span className="font-serif italic font-normal text-retro-accent">Recommendations</span>
            </h2>
            {recs && <p className="text-xs font-mono text-stone-500 mt-0.5">Profile context: {recs.basedOn}</p>}
          </div>
          <span className="text-xs font-mono bg-retro-accent text-white px-2.5 py-1 rounded border border-retro-ink shadow-retro-sm">
            SMART MATCH
          </span>
        </div>

        {recsLoading && <LoadingSpinner label="Analyzing taste matrix & recommendations…" />}

        {!recsLoading && recs && recs.recommendations.length === 0 && (
          <EmptyState title="No recommendations yet" subtitle="Save a few more songs to build your personalized taste model." />
        )}

        {!recsLoading && recs && recs.recommendations.length > 0 && (
          <div className="grid sm:grid-cols-2 gap-3.5">
            {recs.recommendations.map((r) => (
              <div key={r.appleCatalogId} className="flex gap-3 items-center border-2 border-stone-300 rounded-lg p-3 bg-retro-paper/50 hover:border-retro-ink transition-all">
                <div className="relative flex-shrink-0 group">
                  {r.artworkUrl ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img src={r.artworkUrl} alt={r.title} className="w-12 h-12 rounded border border-retro-ink object-cover" />
                  ) : (
                    <div className="w-12 h-12 rounded border border-retro-ink bg-stone-200" />
                  )}
                  {r.previewUrl && (
                    <div className="absolute inset-0 flex items-center justify-center bg-stone-900/40 rounded opacity-0 group-hover:opacity-100 transition-opacity">
                      <PlayButton track={r} size="sm" />
                    </div>
                  )}
                </div>
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-bold font-sans text-retro-ink truncate">{r.title}</p>
                  <p className="text-xs font-mono text-stone-600 truncate">{r.artistName}</p>
                  <p className="text-[11px] font-mono text-retro-accent font-semibold mt-1">✨ {r.reason}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

