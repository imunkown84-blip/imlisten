package com.immusic.controller;

import com.immusic.dto.library.CreateLibraryItemRequest;
import com.immusic.dto.library.LibraryItemResponse;
import com.immusic.dto.library.UpdateLibraryItemRequest;
import com.immusic.service.LibraryService;
import com.immusic.util.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;
    private final CurrentUser currentUser;

    @GetMapping
    public List<LibraryItemResponse> listLibrary() {
        return libraryService.findAll(currentUser.getUserId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LibraryItemResponse create(@Valid @RequestBody CreateLibraryItemRequest request) {
        return libraryService.create(currentUser.getUserId(), request);
    }

    @PutMapping("/{id}")
    public LibraryItemResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLibraryItemRequest request
    ) {
        return libraryService.update(currentUser.getUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        libraryService.delete(currentUser.getUserId(), id);
    }
}
