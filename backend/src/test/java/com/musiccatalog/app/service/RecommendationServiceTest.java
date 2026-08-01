package com.musiccatalog.app.service;

import com.musiccatalog.app.dto.RecommendationResponse;
import com.musiccatalog.app.dto.SearchResultDto;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private LibraryItemRepository repository;

    @Mock
    private ItunesService itunesService;

    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    void returnsEmptyRecommendationsForEmptyLibrary() {
        when(repository.findByUserId(1L)).thenReturn(List.of());

        RecommendationResponse response = recommendationService.recommend(1L);

        assertThat(response.basedOn()).contains("your library is empty");
        assertThat(response.recommendations()).isEmpty();
    }

    @Test
    void generatesRecommendationsBasedOnUserTasteProfileAndFiltersAlreadySavedTracks() {
        List<LibraryItem> library = List.of(
                LibraryItem.builder().userId(1L).appleCatalogId(100L).title("Existing Track").artistName("Coldplay")
                        .genre("Alternative").releaseDate(LocalDate.of(2020, 1, 1))
                        .durationMillis(200_000L).userRating(5).build()
        );

        SearchResultDto alreadySavedTrack = new SearchResultDto(
                100L, "Existing Track", "Coldplay", "Album A", "Alternative",
                "2020-01-01", 200000L, "http://art/1.jpg", 1.29, "http://preview/1.mp3"
        );

        SearchResultDto newRecommendedTrack = new SearchResultDto(
                200L, "Yellow", "Coldplay", "Parachutes", "Alternative",
                "2000-07-10", 269000L, "http://art/2.jpg", 1.29, "http://preview/2.mp3"
        );

        when(repository.findByUserId(1L)).thenReturn(library);
        when(itunesService.searchTopByTerm(eq("Alternative"), anyInt())).thenReturn(List.of(alreadySavedTrack, newRecommendedTrack));
        when(itunesService.searchTopByTerm(eq("Coldplay"), anyInt())).thenReturn(List.of(newRecommendedTrack));

        RecommendationResponse response = recommendationService.recommend(1L);

        assertThat(response.basedOn()).contains("Alternative").contains("Coldplay");
        assertThat(response.recommendations()).hasSize(1);

        RecommendationResponse.RecommendedTrack track = response.recommendations().get(0);
        assertThat(track.appleCatalogId()).isEqualTo(200L);
        assertThat(track.title()).isEqualTo("Yellow");
        assertThat(track.artistName()).isEqualTo("Coldplay");
        assertThat(track.reason()).contains("Coldplay");
    }
}
