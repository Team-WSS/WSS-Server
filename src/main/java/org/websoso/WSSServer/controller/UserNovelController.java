package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.domain.Novel;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.userNovel.UserNovelCreateRequest;
import org.websoso.WSSServer.dto.userNovel.UserNovelGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelUpdateRequest;
import org.websoso.WSSServer.service.NovelService;
import org.websoso.WSSServer.service.UserNovelService;
import org.websoso.WSSServer.service.UserService;

@RequestMapping("/user-novels")
@RestController
@RequiredArgsConstructor
public class UserNovelController {

    private final UserService userService;
    private final NovelService novelService;
    private final UserNovelService userNovelService;

    @GetMapping("/{novelId}")
    public ResponseEntity<UserNovelGetResponse> getEvaluation(@AuthenticationPrincipal User user,
                                                              @PathVariable Long novelId) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        Novel novel = novelService.getNovelOrException(novelId);

        return ResponseEntity
                .status(OK)
                .body(userNovelService.getEvaluation(user, novel));
    }

    @PostMapping
    public ResponseEntity<Void> createEvaluation(@AuthenticationPrincipal User user,
                                                 @Valid @RequestBody UserNovelCreateRequest request) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        userNovelService.createEvaluation(user, request);

        return ResponseEntity
                .status(CREATED)
                .build();
    }

    @PutMapping("/{novelId}")
    public ResponseEntity<Void> updateEvaluation(@AuthenticationPrincipal User user, @PathVariable Long novelId,
                                                 @Valid @RequestBody UserNovelUpdateRequest request) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        Novel novel = novelService.getNovelOrException(novelId);
        userNovelService.updateEvaluation(user, novel, request);

        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @DeleteMapping("/{novelId}")
    public ResponseEntity<Void> deleteEvaluation(@AuthenticationPrincipal User user, @PathVariable Long novelId) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        Novel novel = novelService.getNovelOrException(novelId);
        userNovelService.deleteEvaluation(user, novel);

        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

}
