package com.immusic.dto.library;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLibraryItemRequest {

    @NotNull
    private Long appleCatalogId;

    @NotBlank
    @Size(max = 500)
    private String title;

    @NotBlank
    @Size(max = 500)
    private String artistName;

    @Size(max = 255)
    private String genre;

    private LocalDate releaseDate;

    private Integer trackCount;

    private String artworkUrl;

    @Min(1)
    @Max(5)
    private Short userRating;

    private String userNotes;
}
