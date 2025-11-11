package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.application.SearchNovelApplication;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.novel.FilteredNovelsGetResponse;
import org.websoso.WSSServer.dto.novel.NovelGetResponseBasic;
import org.websoso.WSSServer.dto.novel.NovelGetResponseFeedTab;
import org.websoso.WSSServer.dto.novel.NovelGetResponseInfoTab;
import org.websoso.WSSServer.dto.novel.SearchedNovelsGetResponse;
import org.websoso.WSSServer.dto.popularNovel.PopularNovelsGetResponse;
import org.websoso.WSSServer.dto.userNovel.TasteNovelsGetResponse;
import org.websoso.WSSServer.feed.service.FeedService;
import org.websoso.WSSServer.novel.service.NovelService;

@RestController
@RequestMapping("/novels")
@RequiredArgsConstructor
public class NovelController {

    private final NovelService novelService;
    private final FeedService feedService;
    private final SearchNovelApplication searchNovelApplication;

    /**
     * 검색어를 사용해서 소설을 찾는다.
     *
     * @param query 검색할 작품명 or 작가명
     * @param page  페이지 네이션 페이지
     * @param size  페이지 네이션 사이즈
     * @return SearchedNovelsGetResponse
     */
    @GetMapping
    public ResponseEntity<SearchedNovelsGetResponse> searchNovels(@RequestParam(required = false) String query,
                                                                  @RequestParam int page,
                                                                  @RequestParam int size) {
        return ResponseEntity
                .status(OK)
                .body(searchNovelApplication.searchNovels(query, page, size));
    }

    @GetMapping("/filtered")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FilteredNovelsGetResponse> getFilteredNovels(
            @RequestParam(required = false) List<String> genres,
            @RequestParam(required = false) Boolean isCompleted,
            @RequestParam(required = false) Float novelRating,
            @RequestParam(required = false) List<Integer> keywordIds,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity
                .status(OK)
                .body(searchNovelApplication.getFilteredNovels(genres, isCompleted, novelRating, keywordIds, page,
                        size));
    }

    @GetMapping("/popular")
    public ResponseEntity<PopularNovelsGetResponse> getTodayPopularNovels(@AuthenticationPrincipal User user) {
        //TODO 차단 관계에 있는 유저의 피드글 처리
        return ResponseEntity
                .status(OK)
                .body(novelService.getTodayPopularNovels());
    }

    @GetMapping("/taste")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TasteNovelsGetResponse> getTasteNovels(@AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(OK)
                .body(novelService.getTasteNovels(user));
    }

    @GetMapping("/{novelId}")
    public ResponseEntity<NovelGetResponseBasic> getNovelInfoBasic(@AuthenticationPrincipal User user,
                                                                   @PathVariable Long novelId) {
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
    public ResponseEntity<NovelGetResponseFeedTab> getFeedsByNovel(@AuthenticationPrincipal User user,
                                                                   @PathVariable Long novelId,
                                                                   @RequestParam("lastFeedId") Long lastFeedId,
                                                                   @RequestParam("size") int size) {
        return ResponseEntity
                .status(OK)
                .body(feedService.getFeedsByNovel(user, novelId, lastFeedId, size));
    }

    @PostMapping("/{novelId}/is-interest")
    @PreAuthorize("isAuthenticated() and @authorizationService.validate(#novelId, #user, T(org.websoso.WSSServer.novel.domain.Novel))")
    public ResponseEntity<Void> registerAsInterest(@AuthenticationPrincipal User user,
                                                   @PathVariable("novelId") Long novelId) {
        novelService.registerAsInterest(user, novelId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }

    @DeleteMapping("/{novelId}/is-interest")
    @PreAuthorize("isAuthenticated() and @authorizationService.validate(#novelId, #user, T(org.websoso.WSSServer.library.domain.UserNovel))")
    public ResponseEntity<Void> unregisterAsInterest(@AuthenticationPrincipal User user,
                                                     @PathVariable("novelId") Long novelId) {
        novelService.unregisterAsInterest(user, novelId);
        return ResponseEntity
                .status(NO_CONTENT)
                .build();
    }
}
