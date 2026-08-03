package com.immusic.repository;

import com.immusic.entity.LibraryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LibraryItemRepository extends JpaRepository<LibraryItem, UUID> {

    Page<LibraryItem> findByUserId(UUID userId, Pageable pageable);

    Page<LibraryItem> findByUserIdAndGenreIgnoreCase(UUID userId, String genre, Pageable pageable);

    Optional<LibraryItem> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndAppleCatalogId(UUID userId, Long appleCatalogId);
}
