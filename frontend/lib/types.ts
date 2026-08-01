export interface SearchResult {
  appleCatalogId: number;
  title: string;
  artistName: string;
  collectionName: string | null;
  genre: string | null;
  releaseDate: string | null;
  durationMillis: number | null;
  artworkUrl: string | null;
  trackPrice: number | null;
  previewUrl: string | null;
}

export interface LibraryItem {
  id: number;
  appleCatalogId: number;
  title: string;
  artistName: string;
  genre: string | null;
  releaseDate: string | null;
  durationMillis: number | null;
  artworkUrl: string | null;
  userRating: number | null;
  userNotes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface AnalyticsResponse {
  totalTracks: number;
  averageRating: number;
  averageDurationSeconds: number;
  tracksByGenre: Record<string, number>;
  tracksByArtist: Record<string, number>;
  tracksByReleaseYear: Record<string, number>;
  ratingDistribution: Record<string, number>;
  durationHistogram: { bucketLabel: string; count: number }[];
}

export interface RecommendedTrack {
  appleCatalogId: number;
  title: string;
  artistName: string;
  genre: string | null;
  artworkUrl: string | null;
  previewUrl: string | null;
  reason: string;
  score: number;
}

export interface RecommendationResponse {
  basedOn: string;
  recommendations: RecommendedTrack[];
}

export interface ApiErrorBody {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details?: string[];
}
