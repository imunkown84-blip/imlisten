package com.musiccatalog.app.service;

import com.musiccatalog.app.dto.AnalyticsResponse;
import com.musiccatalog.app.model.LibraryItem;
import com.musiccatalog.app.repository.LibraryItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private LibraryItemRepository repository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void computesAggregatesAcrossGenresArtistsAndYears() {
        List<LibraryItem> items = List.of(
                LibraryItem.builder().userId(1L).appleCatalogId(1L).title("A").artistName("Coldplay")
                        .genre("Alternative").releaseDate(LocalDate.of(2020, 1, 1))
                        .durationMillis(200_000L).userRating(5).build(),
                LibraryItem.builder().userId(1L).appleCatalogId(2L).title("B").artistName("Coldplay")
                        .genre("Alternative").releaseDate(LocalDate.of(2020, 6, 1))
                        .durationMillis(180_000L).userRating(3).build(),
                LibraryItem.builder().userId(1L).appleCatalogId(3L).title("C").artistName("Adele")
                        .genre("Pop").releaseDate(LocalDate.of(2019, 1, 1))
                        .durationMillis(220_000L).userRating(null).build()
        );
        when(repository.findByUserId(1L)).thenReturn(items);

        AnalyticsResponse result = analyticsService.getAnalytics(1L);

        assertThat(result.totalTracks()).isEqualTo(3);
        assertThat(result.tracksByGenre()).containsEntry("Alternative", 2L).containsEntry("Pop", 1L);
        assertThat(result.tracksByArtist()).containsEntry("Coldplay", 2L);
        assertThat(result.tracksByReleaseYear()).containsEntry(2020, 2L).containsEntry(2019, 1L);
        assertThat(result.averageRating()).isEqualTo(4.0); // (5+3)/2, unrated excluded
    }

    @Test
    void returnsZeroedResponseForEmptyLibrary() {
        when(repository.findByUserId(1L)).thenReturn(List.of());

        AnalyticsResponse result = analyticsService.getAnalytics(1L);

        assertThat(result.totalTracks()).isZero();
        assertThat(result.averageRating()).isZero();
        assertThat(result.tracksByGenre()).isEmpty();
    }
}
