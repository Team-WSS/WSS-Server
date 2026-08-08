package org.websoso.WSSServer.collection.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.collection.application.CollectionManagementApplication;
import org.websoso.WSSServer.collection.controller.dto.CollectionCreateRequest;
import org.websoso.WSSServer.collection.controller.dto.CollectionCreateResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionUpdateRequest;
import org.websoso.WSSServer.user.domain.User;

@RequestMapping
@RestController
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionManagementApplication collectionManagementApplication;

    @PostMapping("/collections")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CollectionCreateResponse> createCollection(@AuthenticationPrincipal User user,
                                                                     @Valid @RequestBody CollectionCreateRequest request) {
        return ResponseEntity
                .status(CREATED)
                .body(collectionManagementApplication.create(user, request));
    }

    @PutMapping("/collections/{collectionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateCollection(@AuthenticationPrincipal User user,
                                                 @PathVariable("collectionId") Long collectionId,
                                                 @Valid @RequestBody CollectionUpdateRequest request) {
        collectionManagementApplication.update(user, collectionId, request);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @DeleteMapping("/collections/{collectionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteCollection(@AuthenticationPrincipal User user,
                                                 @PathVariable("collectionId") Long collectionId) {
        collectionManagementApplication.delete(user, collectionId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }
}
