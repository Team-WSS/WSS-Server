package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.application.LibraryEvaluationApplication;
import org.websoso.WSSServer.library.service.LibraryService;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.userNovel.UserNovelCreateRequest;
import org.websoso.WSSServer.dto.userNovel.UserNovelGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelUpdateRequest;
import org.websoso.WSSServer.novel.service.NovelService;
import org.websoso.WSSServer.library.service.UserNovelService;

@RequestMapping("/user-novels")
@RestController
@RequiredArgsConstructor
public class UserNovelController {

    private final NovelService novelService;
    private final UserNovelService userNovelService;
    private final LibraryEvaluationApplication libraryEvaluationApplication;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> createEvaluation(@AuthenticationPrincipal User user,
                                                 @Valid @RequestBody UserNovelCreateRequest request) {
        libraryEvaluationApplication.createEvaluation(user, request);
        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @GetMapping("/{novelId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserNovelGetResponse> getEvaluation(@AuthenticationPrincipal User user,
                                                              @PathVariable Long novelId) {
        Novel novel = novelService.getNovelOrException(novelId);
        return ResponseEntity
                .status(OK)
                .body(userNovelService.getEvaluation(user, novel));
    }

    @PutMapping("/{novelId}")
    @PreAuthorize("isAuthenticated() and @authorizationService.validate(#novelId, #user, T(org.websoso.WSSServer.library.domain.UserNovel))")
    public ResponseEntity<Void> updateEvaluation(@AuthenticationPrincipal User user,
                                                 @PathVariable Long novelId,
                                                 @Valid @RequestBody UserNovelUpdateRequest request) {
        userNovelService.updateEvaluation(user, novelId, request);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @DeleteMapping("/{novelId}")
    @PreAuthorize("isAuthenticated() and @authorizationService.validate(#novelId, #user, T(org.websoso.WSSServer.library.domain.UserNovel))")
    public ResponseEntity<Void> deleteEvaluation(@AuthenticationPrincipal User user,
                                                 @PathVariable Long novelId) {
        userNovelService.deleteEvaluation(user, novelId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }
}
