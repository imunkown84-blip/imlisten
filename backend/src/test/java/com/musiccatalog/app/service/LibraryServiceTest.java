package com.musiccatalog.app.service;

import com.musiccatalog.app.dto.LibraryItemRequest;
import com.musiccatalog.app.dto.LibraryItemResponse;
import com.musiccatalog.app.exception.DuplicateResourceException;
import com.musiccatalog.app.exception.ResourceNotFoundException;
import com.musiccatalog.app.model.LibraryItem;
import com.musiccatalog.app.repository.LibraryItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

    @Mock
    private LibraryItemRepository repository;

    @InjectMocks
    private LibraryService libraryService;

    private LibraryItemRequest sampleRequest() {
        return new LibraryItemRequest(101L, "Yellow", "Coldplay", "Alternative", null, 269000L, "art.jpg", 5, "great song");
    }

    @Test
    void addToLibrary_rejectsDuplicateCatalogEntry() {
        when(repository.existsByUserIdAndAppleCatalogId(1L, 101L)).thenReturn(true);

        assertThatThrownBy(() -> libraryService.addToLibrary(1L, sampleRequest()))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void addToLibrary_savesNewItem() {
        when(repository.existsByUserIdAndAppleCatalogId(1L, 101L)).thenReturn(false);
        when(repository.save(any(LibraryItem.class))).thenAnswer(inv -> {
            LibraryItem item = inv.getArgument(0);
            item.setId(1L);
            return item;
        });

        LibraryItemResponse response = libraryService.addToLibrary(1L, sampleRequest());

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.artistName()).isEqualTo("Coldplay");
    }

    @Test
    void updateLibraryItem_throwsWhenNotOwnedByUser() {
        when(repository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> libraryService.updateLibraryItem(1L, 99L, sampleRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
