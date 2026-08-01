package com.musiccatalog.app.service;

import com.musiccatalog.app.dto.LibraryItemRequest;
import com.musiccatalog.app.dto.LibraryItemResponse;
import com.musiccatalog.app.exception.DuplicateResourceException;
import com.musiccatalog.app.exception.ResourceNotFoundException;
import com.musiccatalog.app.model.LibraryItem;
import com.musiccatalog.app.repository.LibraryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final LibraryItemRepository repository;

    public List<LibraryItemResponse> getLibrary(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public LibraryItemResponse addToLibrary(Long userId, LibraryItemRequest request) {
        if (repository.existsByUserIdAndAppleCatalogId(userId, request.appleCatalogId())) {
            throw new DuplicateResourceException("This track is already saved in your library");
        }
        LibraryItem item = LibraryItem.builder()
                .userId(userId)
                .appleCatalogId(request.appleCatalogId())
                .title(request.title())
                .artistName(request.artistName())
                .genre(request.genre())
                .releaseDate(request.releaseDate())
                .durationMillis(request.durationMillis())
                .artworkUrl(request.artworkUrl())
                .userRating(request.userRating())
                .userNotes(request.userNotes())
                .build();
        return toResponse(repository.save(item));
    }

    @Transactional
    public LibraryItemResponse updateLibraryItem(Long userId, Long id, LibraryItemRequest request) {
        LibraryItem item = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Library item not found: " + id));

        item.setTitle(request.title());
        item.setArtistName(request.artistName());
        item.setGenre(request.genre());
        item.setReleaseDate(request.releaseDate());
        item.setDurationMillis(request.durationMillis());
        item.setArtworkUrl(request.artworkUrl());
        item.setUserRating(request.userRating());
        item.setUserNotes(request.userNotes());

        return toResponse(repository.save(item));
    }

    @Transactional
    public void deleteLibraryItem(Long userId, Long id) {
        LibraryItem item = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Library item not found: " + id));
        repository.delete(item);
    }

    private LibraryItemResponse toResponse(LibraryItem item) {
        return new LibraryItemResponse(
                item.getId(),
                item.getAppleCatalogId(),
                item.getTitle(),
                item.getArtistName(),
                item.getGenre(),
                item.getReleaseDate(),
                item.getDurationMillis(),
                item.getArtworkUrl(),
                item.getUserRating(),
                item.getUserNotes(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
