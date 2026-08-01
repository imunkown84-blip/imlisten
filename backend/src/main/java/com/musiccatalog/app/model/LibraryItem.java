package com.musiccatalog.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * A single song saved into a user's personal library.
 * Only user-curated data lives here — the public catalog (iTunes) is never persisted
 * beyond what the user explicitly chooses to save.
 */
@Entity
@Table(name = "library_items", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "apple_catalog_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "apple_catalog_id", nullable = false)
    private Long appleCatalogId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "artist_name", nullable = false, length = 300)
    private String artistName;

    @Column(length = 150)
    private String genre;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    /** Track duration in milliseconds, as returned by the iTunes API (trackTimeMillis). */
    @Column(name = "duration_millis")
    private Long durationMillis;

    @Column(name = "artwork_url", length = 1000)
    private String artworkUrl;

    @Column(name = "user_rating")
    private Integer userRating;

    @Column(name = "user_notes", length = 2000)
    private String userNotes;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
