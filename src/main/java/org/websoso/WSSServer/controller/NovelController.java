package org.websoso.WSSServer.controller;

import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

@RestController
@RequestMapping("/novels")
@RequiredArgsConstructor
public class NovelController {

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

    /**
     * 상세 탐색 API
     *
     * @param genres      장르 리스트
     * @param isCompleted 완결 여부
     * @param novelRating 소설 평점
     * @param keywordIds  키워드 리스트
     * @param page        페이지
     * @param size        사이즈
     * @return FilteredNovelsGetResponse
     */
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

    /**
     * 작품정보 조회 API (기본)
     *
     * @param user    로그인 유저 객체
     * @param novelId 소설 ID
     * @return NovelGetResponseBasic
     */
    @GetMapping("/{novelId}")
    public ResponseEntity<NovelGetResponseBasic> getNovelInfoBasic(@AuthenticationPrincipal User user,
                                                                   @PathVariable Long novelId) {
        return ResponseEntity
                .status(OK)
                .body(searchNovelApplication.getNovelInfoBasic(user, novelId));
    }

    /**
     * 작품정보 조회 API (정보탭)
     *
     * @param novelId 소설 ID
     * @return NovelGetResponseInfoTab
     */
    @GetMapping("/{novelId}/info")
    public ResponseEntity<NovelGetResponseInfoTab> getNovelInfoInfoTab(@PathVariable Long novelId) {
        return ResponseEntity
                .status(OK)
                .body(searchNovelApplication.getNovelInfoInfoTab(novelId));
    }

    /**
     * 인기있는 소설 조회 API (오늘의 발견)
     *
     * @param user 로그인 유저 객체
     * @return PopularNovelsGetResponse
     */
    @GetMapping("/popular")
    public ResponseEntity<PopularNovelsGetResponse> getTodayPopularNovels(@AuthenticationPrincipal User user) {
        //TODO 차단 관계에 있는 유저의 피드글 처리
        return ResponseEntity
                .status(OK)
                .body(searchNovelApplication.getTodayPopularNovels());
    }

    /**
     * 취향에 해당하는 웹소설 조회 API (취향 추천 작품 조회)
     *
     * @param user 로그인 유저 객체
     * @return TasteNovelsGetResponse
     */
    @GetMapping("/taste")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TasteNovelsGetResponse> getTasteNovels(@AuthenticationPrincipal User user) {
        return ResponseEntity
                .status(OK)
                .body(searchNovelApplication.getTasteNovels(user));
    }

    // TODO: Feed Controller로 이동해야함
    @GetMapping("/{novelId}/feeds")
    public ResponseEntity<NovelGetResponseFeedTab> getFeedsByNovel(@AuthenticationPrincipal User user,
                                                                   @PathVariable Long novelId,
                                                                   @RequestParam("lastFeedId") Long lastFeedId,
                                                                   @RequestParam("size") int size) {
        return ResponseEntity
                .status(OK)
                .body(feedService.getFeedsByNovel(user, novelId, lastFeedId, size));
    }
}
