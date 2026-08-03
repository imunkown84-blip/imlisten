package com.immusic.service;

import com.immusic.dto.library.CreateLibraryItemRequest;
import com.immusic.dto.library.LibraryItemResponse;
import com.immusic.dto.library.UpdateLibraryItemRequest;
import com.immusic.entity.AppUser;
import com.immusic.entity.LibraryItem;
import com.immusic.exception.DuplicateResourceException;
import com.immusic.exception.ResourceNotFoundException;
import com.immusic.repository.AppUserRepository;
import com.immusic.repository.LibraryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final LibraryItemRepository libraryItemRepository;
    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public Page<LibraryItemResponse> listLibrary(UUID userId, String genre, Pageable pageable) {
        Page<LibraryItem> page = (genre != null && !genre.isBlank())
                ? libraryItemRepository.findByUserIdAndGenreIgnoreCase(userId, genre.trim(), pageable)
                : libraryItemRepository.findByUserId(userId, pageable);

        return page.map(this::toResponse);
    }

    @Transactional
    public LibraryItemResponse create(UUID userId, CreateLibraryItemRequest request) {
        if (libraryItemRepository.existsByUserIdAndAppleCatalogId(userId, request.getAppleCatalogId())) {
            throw new DuplicateResourceException("Album already exists in library");
        }

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LibraryItem item = LibraryItem.builder()
                .user(user)
                .appleCatalogId(request.getAppleCatalogId())
                .title(request.getTitle())
                .artistName(request.getArtistName())
                .genre(request.getGenre())
                .releaseDate(request.getReleaseDate())
                .trackCount(request.getTrackCount())
                .artworkUrl(request.getArtworkUrl())
                .userRating(request.getUserRating())
                .userNotes(request.getUserNotes())
                .build();

        return toResponse(libraryItemRepository.save(item));
    }

    @Transactional
    public LibraryItemResponse update(UUID userId, UUID itemId, UpdateLibraryItemRequest request) {
        LibraryItem item = libraryItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Library item not found"));

        if (request.getUserRating() != null) {
            item.setUserRating(request.getUserRating());
        }
        if (request.getUserNotes() != null) {
            item.setUserNotes(request.getUserNotes());
        }

        return toResponse(libraryItemRepository.save(item));
    }

    @Transactional
    public void delete(UUID userId, UUID itemId) {
        LibraryItem item = libraryItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Library item not found"));
        libraryItemRepository.delete(item);
    }

    private LibraryItemResponse toResponse(LibraryItem item) {
        return LibraryItemResponse.builder()
                .id(item.getId())
                .appleCatalogId(item.getAppleCatalogId())
                .title(item.getTitle())
                .artistName(item.getArtistName())
                .genre(item.getGenre())
                .releaseDate(item.getReleaseDate())
                .trackCount(item.getTrackCount())
                .artworkUrl(item.getArtworkUrl())
                .userRating(item.getUserRating())
                .userNotes(item.getUserNotes())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
