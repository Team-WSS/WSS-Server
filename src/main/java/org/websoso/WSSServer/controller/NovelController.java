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
import org.websoso.WSSServer.dto.novel.NovelGetResponseFeedTab;
import org.websoso.WSSServer.dto.novel.NovelGetResponseInfoTab;
import org.websoso.WSSServer.dto.novel.SearchedNovelsGetResponse;
import org.websoso.WSSServer.dto.popularNovel.PopularNovelsGetResponse;
import org.websoso.WSSServer.dto.userNovel.TasteNovelsGetResponse;
import org.websoso.WSSServer.service.FeedService;
import org.websoso.WSSServer.service.NovelService;
import org.websoso.WSSServer.service.UserService;

@RestController
@RequestMapping("/novels")
@RequiredArgsConstructor
public class NovelController {

    private final NovelService novelService;
    private final UserService userService;
    private final FeedService feedService;

    @GetMapping("/{novelId}")
    public ResponseEntity<NovelGetResponseBasic> getNovelInfoBasic(Principal principal,
                                                                   @PathVariable Long novelId) {
        User user = principal == null
                ? null
                : userService.getUserOrException(Long.valueOf(principal.getName()));

        return ResponseEntity
                .status(OK)
                .body(novelService.getNovelInfoBasic(user, novelId));
    }

    @GetMapping("/{novelId}/info")
    public ResponseEntity<NovelGetResponseInfoTab> getNovelInfoInfoTab(@PathVariable Long novelId) {
        return ResponseEntity
                .status(OK)
                .body(novelService.getNovelInfoInfoTab(novelId));
    }

    @GetMapping("/{novelId}/feeds")
    public ResponseEntity<NovelGetResponseFeedTab> getFeedsByNovel(Principal principal,
                                                                   @PathVariable Long novelId,
                                                                   @RequestParam("lastFeedId") Long lastFeedId,
                                                                   @RequestParam("size") int size) {
        User user = principal == null
                ? null
                : userService.getUserOrException(Long.valueOf(principal.getName()));

        return ResponseEntity
                .status(OK)
                .body(feedService.getFeedsByNovel(user, novelId, lastFeedId, size));
    }

    @GetMapping
    public ResponseEntity<SearchedNovelsGetResponse> searchNovels(@RequestParam(required = false) String query,
                                                                  @RequestParam int page,
                                                                  @RequestParam int size) {
        return ResponseEntity
                .status(OK)
                .body(novelService.searchNovels(query, page, size));
    }

    @GetMapping("/filtered")
    public ResponseEntity<FilteredNovelsGetResponse> getFilteredNovels(
            @RequestParam(required = false) List<String> genres,
            @RequestParam(required = false) Boolean isCompleted,
            @RequestParam(required = false) Float novelRating,
            @RequestParam(required = false) List<Integer> keywordIds,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity
                .status(OK)
                .body(novelService.getFilteredNovels(genres, isCompleted, novelRating, keywordIds, page, size));
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

    @GetMapping("/popular")
    public ResponseEntity<PopularNovelsGetResponse> getTodayPopularNovels(Principal principal) {
        //TODO 차단 관계에 있는 유저의 피드글 처리
        return ResponseEntity
                .status(OK)
                .body(novelService.getTodayPopularNovels());
    }

    @GetMapping("/taste")
    public ResponseEntity<TasteNovelsGetResponse> getTasteNovels(Principal principal) {
        User user = userService.getUserOrException(Long.valueOf(principal.getName()));
        return ResponseEntity
                .status(OK)
                .body(novelService.getTasteNovels(user));
    }
}
