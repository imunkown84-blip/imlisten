package com.musiccatalog.app.controller;

import com.musiccatalog.app.dto.LibraryItemRequest;
import com.musiccatalog.app.dto.LibraryItemResponse;
import com.musiccatalog.app.security.CurrentUser;
import com.musiccatalog.app.service.LibraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;
    private final CurrentUser currentUser;

    @GetMapping
    public ResponseEntity<List<LibraryItemResponse>> getLibrary() {
        return ResponseEntity.ok(libraryService.getLibrary(currentUser.id()));
    }

    @PostMapping
    public ResponseEntity<LibraryItemResponse> addToLibrary(@Valid @RequestBody LibraryItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryService.addToLibrary(currentUser.id(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LibraryItemResponse> updateLibraryItem(@PathVariable Long id,
                                                                   @Valid @RequestBody LibraryItemRequest request) {
        return ResponseEntity.ok(libraryService.updateLibraryItem(currentUser.id(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLibraryItem(@PathVariable Long id) {
        libraryService.deleteLibraryItem(currentUser.id(), id);
        return ResponseEntity.noContent().build();
    }
}
