# 🎵 Music Catalog Insights Platform

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot 3.3](https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen.svg?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Next.js 14](https://img.shields.io/badge/Next.js-14.2-black.svg?style=for-the-badge&logo=next.js)](https://nextjs.org/)
[![React 18](https://img.shields.io/badge/React-18.3-61DAFB.svg?style=for-the-badge&logo=react)](https://reactjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg?style=for-the-badge&logo=postgresql)](https://www.postgresql.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.4-38B2AC.svg?style=for-the-badge&logo=tailwind-css)](https://tailwindcss.com/)

A modern full-stack music catalog management, analytics, and recommendation platform. Search the public iTunes track catalog, curate a personal song library with custom ratings and notes, visualize interactive catalog analytics, and receive personalized content-based AI track recommendations.

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Domain & Entity Choice](#-domain--entity-choice)
- [Project Architecture](#-project-architecture)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
- [Database Schema](#-database-schema)
- [REST API Reference](#-rest-api-reference)
- [Analytics Dashboard](#-analytics-dashboard)
- [AI Recommendation Algorithm](#-ai-recommendation-algorithm)
- [Technical Trade-offs & Decisions](#-technical-trade-offs--decisions)
- [Deployment](#-deployment)
- [License](#-license)

---

## ✨ Features

- 🔍 **Live iTunes Catalog Search**: Search millions of public tracks instantly using a proxy-backed iTunes API with no rate-limit hurdles or API keys required.
- 📚 **Personal Song Library**: Save favorite tracks, assign ratings (1–5 stars), record custom review notes, and manage your library seamlessly.
- 🔐 **JWT Authentication & Security**: Secure user registration and login powered by Spring Security, stateless JWT tokens, and BCrypt password hashing.
- 📊 **Interactive Visual Analytics**: Comprehensive dashboard powered by Recharts offering 5 analytical visualizations over your personal library.
- 🤖 **Algorithmic AI Recommendation Engine**: Transparent, content-based taste profiling algorithm that searches candidate tracks and ranks them based on user affinity.
- 🎵 **Integrated Audio Preview**: Listen to 30-second high-quality audio previews directly from search results or personal library cards.

---

## 🛠️ Tech Stack

### **Backend**
- **Framework**: Spring Boot 3.3.2
- **Language**: Java 17
- **Security**: Spring Security 6 (Stateless JWT via `jjwt 0.12.6`, BCrypt Hashing)
- **Persistence**: Spring Data JPA / Hibernate
- **Database**: PostgreSQL (Production) / H2 (In-memory zero-config dev mode)
- **Validation**: `jakarta.validation` annotations with unified error handling
- **Build Tool**: Apache Maven

### **Frontend**
- **Framework**: Next.js 14.2 (App Router)
- **Language**: TypeScript / React 18
- **Styling**: Tailwind CSS, PostCSS, Autoprefixer
- **Data Visualization**: Recharts (Bar, Line, Donut, Histograms)
- **HTTP Client**: Axios with interlocked Bearer Token interceptors

### **External APIs**
- **Catalog Source**: [iTunes Search API](https://itunes.apple.com/search) (Public REST API, no key required)

---

## 🎯 Domain & Entity Choice

The platform explicitly standardizes on **Songs (Tracks)** as the core domain entity instead of Albums or Artists.

### **Why Tracks?**
1. **Atomic Precision**: Songs are the natural atomic unit of listening behavior. Track-level ratings and personal notes provide significantly higher utility than coarse per-album ratings.
2. **Numeric Data for Analytics**: Attributes like `duration_millis` (`trackTimeMillis`) provide rich, continuous numeric data needed to drive distribution histograms and quantitative metrics.
3. **Finer Taste Signal**: A song-level preference graph (Genre + Artist affinity weighted by personal track rating) produces vastly superior recommendations compared to broader artist-level or album-level aggregation.

---

## 📁 Project Architecture

```
music-catalog-insights/
├── backend/                        # Spring Boot REST API
│   ├── pom.xml                     # Maven configuration & dependencies
│   └── src/
│       ├── main/
│       │   ├── java/com/musiccatalog/app/
│       │   │   ├── config/         # Security & CORS configuration
│       │   │   ├── controller/     # REST Endpoints (Auth, Library, Search, Analytics, Recs)
│       │   │   ├── dto/            # Request & Response Data Transfer Objects
│       │   │   ├── exception/      # Centralized Global Exception Handler
│       │   │   ├── external/       # iTunes API Integration Proxy Client
│       │   │   ├── model/          # JPA Entities (User, LibraryItem)
│       │   │   ├── repository/     # Spring Data Repositories
│       │   │   ├── security/       # JWT Token Provider & Auth Filters
│       │   │   └── service/        # Business Logic & Recommendation Engine
│       │   └── resources/          # application.yml & DB profiles
│       └── test/                   # Unit & Integration Tests
│
└── frontend/                       # Next.js 14 App Router Frontend
    ├── app/                        # Pages & Navigation Routes (Search, Library, Analytics, Recs)
    ├── components/                 # UI Components (SongCard, LibraryCard, AnalyticsCharts, MiniPlayer)
    ├── lib/                        # API Client, Auth Utilities, & Type Definitions
    ├── package.json                # Dependencies & scripts
    └── tailwind.config.js          # Tailwind theme configuration
```

---

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK)**: Version 17 or higher
- **Node.js**: Version 18.x or higher
- **Maven**: Version 3.8+ (or Maven wrapper)
- **PostgreSQL**: (Optional) Version 14+ if not using in-memory H2

---

### Backend Setup

1. **Navigate to the backend directory**:
   ```bash
   cd backend
   ```

2. **Option A — Quick Start (Zero-Setup with In-Memory H2 DB)**:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

3. **Option B — Running with PostgreSQL**:
   ```bash
   # Create PostgreSQL Database
   createdb music_catalog

   # Configure Environment Variables
   export DB_URL=jdbc:postgresql://localhost:5432/music_catalog
   export DB_USERNAME=postgres
   export DB_PASSWORD=postgres
   export JWT_SECRET=$(openssl rand -base64 48)

   # Run Application
   mvn spring-boot:run
   ```

4. **Running Tests**:
   ```bash
   mvn test
   ```

> ℹ️ The backend API server will start on **`http://localhost:8080`**.

---

### Frontend Setup

1. **Navigate to the frontend directory**:
   ```bash
   cd frontend
   ```

2. **Environment Configuration**:
   ```bash
   cp .env.local.example .env.local
   ```
   *By default, `.env.local` points to `http://localhost:8080`.*

3. **Install Dependencies**:
   ```bash
   npm install
   ```

4. **Start Development Server**:
   ```bash
   npm run dev
   ```

> ℹ️ The frontend application will start on **`http://localhost:3000`**.

---

## 🗄️ Database Schema

The database strictly persists **only items explicitly saved by the user**. The public iTunes catalog remains external and unpersisted to avoid data redundancy.

### `users`
Represents user accounts.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | Primary Key, Auto-increment | Unique user identifier |
| `username` | `VARCHAR(60)` | Unique, Not Null | Account username |
| `password_hash` | `VARCHAR` | Not Null | BCrypt-hashed password |
| `created_at` | `TIMESTAMP` | Not Null | Account creation timestamp |

---

### `library_items`
Represents tracks saved into a user's personal collection.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | Primary Key, Auto-increment | Internal library item ID |
| `user_id` | `BIGINT` | Foreign Key (`users.id`) | Owner user scoping |
| `apple_catalog_id` | `BIGINT` | Not Null | iTunes `trackId` (Unique per `user_id` + `apple_catalog_id`) |
| `title` | `VARCHAR` | Not Null | Song title |
| `artist_name` | `VARCHAR` | Not Null | Artist / Performer name |
| `genre` | `VARCHAR` | Not Null | Primary music genre |
| `release_date` | `DATE` | Nullable | Original track release date |
| `duration_millis` | `BIGINT` | Nullable | Track length in ms (powers histogram analytics) |
| `artwork_url` | `VARCHAR` | Nullable | Track album artwork URL |
| `user_rating` | `INTEGER` | Check (1–5), Nullable | User star rating (1 to 5) |
| `user_notes` | `VARCHAR(2000)`| Nullable | Custom user notes / review |
| `created_at` | `TIMESTAMP` | Not Null | Timestamp saved to library |
| `updated_at` | `TIMESTAMP` | Not Null | Timestamp last modified |

> 💡 **Relational (PostgreSQL) vs. NoSQL Rationale**:
> - The data structure is strictly uniform and relational (One User $\rightarrow$ Many Library Items).
> - SQL composite unique key constraint (`user_id`, `apple_catalog_id`) prevents duplicate song saves at the engine level.
> - SQL aggregate functions (`GROUP BY`, `COUNT`, `AVG`, `BUCKET`) natively power the analytics dashboard efficiently without client-side memory overhead.

---

## 📡 REST API Reference

All endpoints except `/api/auth/**` require an `Authorization: Bearer <JWT_TOKEN>` header.

### Authentication

| Method | Endpoint | Description | Request Body / Params |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register a new user | `{ "username": "...", "password": "..." }` |
| `POST` | `/api/auth/login` | Authenticate user & get JWT | `{ "username": "...", "password": "..." }` |

### Music Catalog & Library

| Method | Endpoint | Description | Request Body / Params |
|---|---|---|---|
| `GET` | `/api/search` | Search iTunes catalog | Query params: `query`, `type` (default: song), `limit` |
| `GET` | `/api/library` | Retrieve user saved library | Optional filtering/sorting query params |
| `POST` | `/api/library` | Save a song to user library | `{ "appleCatalogId": 123, "title": "...", ... }` |
| `PUT` | `/api/library/{id}` | Update rating or notes | `{ "userRating": 5, "userNotes": "Favorite track" }` |
| `DELETE` | `/api/library/{id}` | Remove song from library | Path variable: `id` |

### Analytics & AI

| Method | Endpoint | Description | Response Details |
|---|---|---|---|
| `GET` | `/api/analytics` | Aggregate metrics for dashboard | Returns distribution JSON for all 5 chart modules |
| `GET` | `/api/recommendations` | Get personalized track recs | List of 12 candidate tracks with `reason` strings |

### Error Handling & Validation

Validation is strictly enforced via `jakarta.validation` annotations on incoming request DTOs. Unhandled or validation exceptions are caught globally in `GlobalExceptionHandler`, producing standard HTTP error responses:

```json
{
  "timestamp": "2026-08-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for request DTO",
  "path": "/api/library",
  "details": {
    "userRating": "Rating must be between 1 and 5"
  }
}
```

---

## 📊 Analytics Dashboard

Driven by a single optimized aggregate endpoint (`GET /api/analytics`), the dashboard presents 5 visual metrics rendered with Recharts:

1. 📊 **Genre Distribution (Bar Chart)**: Visualizes song count breakdown across music genres.
2. 🎤 **Top Artists (Horizontal Bar Chart)**: Displays the top 10 most saved artists in the collection.
3. 📈 **Release Timeline (Line Chart)**: Tracks historical release year distribution of saved music.
4. 🍩 **Rating Breakdown (Donut Chart)**: Shares distribution of 1-star through 5-star rated tracks.
5. ⏱️ **Track Length Buckets (Histogram)**: Categorizes track durations into standard length buckets (`<2m`, `2-3m`, `3-4m`, `4-5m`, `5m+`).

---

## 🤖 AI Recommendation Algorithm

The platform features an **explainable, deterministic, content-based recommendation engine** operating in 4 stages:

```
[User Library] ➔ [1. Taste Profiling] ➔ [2. Catalog Candidate Search] ➔ [3. Affinity Scoring] ➔ [4. Top Recommendations]
```

1. **Taste Profiling**: Analyzes saved tracks to calculate genre & artist affinity vectors, heavily weighted by user ratings (e.g., a 5-star song contributes 5x more weight than a 1-star track).
2. **Candidate Fetching**: Queries the iTunes Search API using top affinity genres and artists as seed terms to fetch candidate songs.
3. **Affinity Scoring**: Scores candidate tracks against the user's taste profile (giving higher weight to artist matches over general genre matches) while filtering out already saved songs.
4. **Transparent Explanations**: Selects the top 12 tracks and attaches a human-readable explanation string for each recommendation (e.g., *"Recommended because you highly rated Coldplay tracks in Alternative"*).

---

## ⚖️ Technical Trade-offs & Decisions

| Decision | Approach | Rationale & Production Considerations |
|---|---|---|
| **JWT Storage** | Saved in `localStorage` | Prioritized dev speed for demo scope. Production applications should utilize HTTP-only, secure, SameSite cookies to mitigate XSS vulnerabilities. |
| **Token Expiry** | 24-hour expiration (`app.jwt.expiration-ms`) | Keeps auth stateless and simple without refresh token rotation overhead. |
| **Catalog Proxy Caching** | Direct pass-through proxy | iTunes Search API rate limits are extremely generous. For high-scale production, a Redis cache layer for popular search queries would be introduced. |
| **Database Migrations** | `spring.jpa.hibernate.ddl-auto=update` | Enables rapid iteration during development. Production deployments should transition to versioned migration tools like Flyway or Liquibase. |
| **Recommender Model** | Algorithmic Content-Based | Zero API cost, zero external latency, 100% deterministic, reproducible, and fully explainable without third-party LLM dependencies. |

---

## 🌐 Deployment Guide

### Part 1: Deploying Backend & Database on Render

#### Step 1: Create PostgreSQL Database on Render
1. Log in to [Render Dashboard](https://dashboard.render.com/).
2. Click **New +** $\rightarrow$ **PostgreSQL**.
3. Name your database (e.g., `music-catalog-db`).
4. Select your preferred region and tier (Free tier available).
5. Click **Create Database**.
6. Once provisioned, note down the following connection details from the Info page:
   - **Internal Database URL** (e.g., `postgres://user:password@dpg-xxx/music_catalog`)
   - **Host**, **Database**, **User**, **Password**, and **Port** (5432).

#### Step 2: Deploy Spring Boot Web Service on Render
1. Click **New +** $\rightarrow$ **Web Service**.
2. Connect your GitHub repository (`imunkown84-blip/imlisten`).
3. Select **Docker** as the Runtime (Render auto-detects `backend/Dockerfile`).
4. Set **Root Directory** to `backend`.
5. Under **Environment Variables**, add:
   - `DB_URL`: `jdbc:postgresql://<HOSTNAME>:5432/<DATABASE_NAME>` *(Prepend `jdbc:` to the Postgres connection string)*
   - `DB_USERNAME`: `<POSTGRES_USER>`
   - `DB_PASSWORD`: `<POSTGRES_PASSWORD>`
   - `JWT_SECRET`: Generate a random secret string (e.g., `openssl rand -base64 48`)
   - `CORS_ORIGINS`: `https://<YOUR_VERCEL_APP_NAME>.vercel.app` *(Update this after deploying frontend)*
6. Click **Create Web Service**.
7. Copy your deployed backend URL (e.g., `https://music-catalog-backend.onrender.com`).

---

### Part 2: Deploying Frontend on Vercel

#### Step 1: Import Project into Vercel
1. Log in to [Vercel Dashboard](https://vercel.com/dashboard).
2. Click **Add New...** $\rightarrow$ **Project**.
3. Import your GitHub repository (`imunkown84-blip/imlisten`).

#### Step 2: Configure Project Settings
1. **Framework Preset**: Next.js (Auto-detected).
2. **Root Directory**: Click **Edit** and set it to `frontend`.
3. Expand **Environment Variables** and add:
   - `NEXT_PUBLIC_API_BASE_URL`: `https://<YOUR_RENDER_BACKEND_URL>.onrender.com`
4. Click **Deploy**.

#### Step 3: Connect Backend CORS
1. Go back to your Render Backend Web Service $\rightarrow$ **Environment Variables**.
2. Update `CORS_ORIGINS` to match your exact Vercel frontend URL (e.g., `https://music-catalog-frontend.vercel.app`).
3. Save changes — Render will automatically redeploy the backend with the new CORS origin allowed!

---

## 📝 License

This project is open source and available under the [MIT License](LICENSE).