package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.novel.FilteredNovelsGetResponse;
import org.websoso.WSSServer.dto.novel.NovelGetResponseBasic;
import org.websoso.WSSServer.dto.novel.NovelGetResponseInfoTab;
import org.websoso.WSSServer.service.NovelService;
import org.websoso.WSSServer.service.UserService;

@RestController
@RequestMapping("/novels")
@RequiredArgsConstructor
public class NovelController {

    private final NovelService novelService;
    private final UserService userService;

    @GetMapping("/{novelId}")
    public ResponseEntity<NovelGetResponseBasic> getNovelInfoBasic(Principal principal, @PathVariable Long novelId) {
        if (principal == null) {
            return ResponseEntity
                    .status(OK)
                    .body(novelService.getNovelInfoBasic(null, novelId));
        }
        return ResponseEntity
                .status(OK)
                .body(novelService.getNovelInfoBasic(userService.getUserOrException(Long.valueOf(principal.getName())),
                        novelId));
    }

    @GetMapping("/{novelId}/info")
    public ResponseEntity<NovelGetResponseInfoTab> getNovelInfoInfoTab(@PathVariable Long novelId) {
        return ResponseEntity
                .status(OK)
                .body(novelService.getNovelInfoInfoTab(novelId));
    }

    @PostMapping("/{novelId}/is-interest")
    public ResponseEntity<Void> registerAsInterest(Principal principal,
                                                   @PathVariable("novelId") Long novelId) {

        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        novelService.registerAsInterest(user, novelId);

        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @DeleteMapping("/{novelId}/is-interest")
    public ResponseEntity<Void> unregisterAsInterest(Principal principal,
                                                     @PathVariable("novelId") Long novelId) {

        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        novelService.unregisterAsInterest(user, novelId);

        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @GetMapping("/filtered")
    public ResponseEntity<FilteredNovelsGetResponse> getFilteredNovels(
            @RequestParam(required = false) List<String> genre,
            @RequestParam(required = false) Boolean isCompleted,
            @RequestParam(required = false) Float novelRating,
            @RequestParam(required = false) List<Integer> keywordIds,
            @RequestParam Long lastNovelId,
            @RequestParam int size) {

        return ResponseEntity
                .status(OK)
                .body(novelService.getFilteredNovels(genre, isCompleted, novelRating, keywordIds, lastNovelId, size));
    }

}
