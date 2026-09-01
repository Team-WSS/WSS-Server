package org.websoso.WSSServer.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.GenrePreference;
import org.websoso.WSSServer.dto.novel.SearchedNovelsResponse;
import org.websoso.WSSServer.dto.userNovel.TasteNovelGetResponse;
import org.websoso.WSSServer.dto.userNovel.TasteNovelsGetResponse;
import org.websoso.WSSServer.feed.feed.repository.FeedRepository;
import org.websoso.WSSServer.library.service.AttractivePointService;
import org.websoso.WSSServer.library.service.KeywordService;
import org.websoso.WSSServer.library.service.LibraryService;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.domain.NovelStatistics;
import org.websoso.WSSServer.novel.service.GenreServiceImpl;
import org.websoso.WSSServer.novel.service.KeywordServiceImpl;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;
import org.websoso.WSSServer.novel.service.PopularNovelService;
import org.websoso.WSSServer.recentsearch.event.NovelSearchedEvent;
import org.websoso.WSSServer.repository.GenrePreferenceRepository;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.repository.AvatarProfileRepository;
import org.websoso.WSSServer.user.service.BlockService;

/**
 * 작품 검색 애플리케이션의 조회 조합 로직을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class SearchNovelApplicationTest {

    @InjectMocks
    private SearchNovelApplication searchNovelApplication;

    @Mock
    private NovelServiceImpl novelService;

    @Mock
    private PopularNovelService popularNovelService;

    @Mock
    private GenreServiceImpl genreService;

    @Mock
    private AttractivePointService attractivePointService;

    @Mock
    private KeywordService libraryKeywordService;

    @Mock
    private KeywordServiceImpl keywordService;

    @Mock
    private LibraryService libraryService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private BlockService blockService;

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private GenrePreferenceRepository genrePreferenceRepository;

    @Mock
    private AvatarProfileRepository avatarProfileRepository;

    @Mock
    private User user;

    @Mock
    private GenrePreference genrePreference;

    @Mock
    private Genre genre;

    @Mock
    private Novel novel;

    @Mock
    private NovelStatistics novelStatistics;

    // 추천 작품의 관심 수와 평점 통계를 컬렉션 로딩 없이 응답에 반영하는지 검증한다.
    @DisplayName("취향 추천 작품의 통계를 일괄 조회 결과로 반환한다")
    @Test
    void getsTasteNovelsWithAggregatedStatistics() {
        given(genrePreferenceRepository.findByUser(user)).willReturn(List.of(genrePreference));
        given(genrePreference.getGenre()).willReturn(genre);
        given(libraryService.getTasteNovels(List.of(genre))).willReturn(List.of(novel));
        given(novel.getNovelId()).willReturn(1L);
        given(novel.getTitle()).willReturn("작품명");
        given(novel.getAuthor()).willReturn("작가명");
        given(novel.getNovelImage()).willReturn("https://example.com/novel.png");
        given(novel.getNovelStatistics()).willReturn(novelStatistics);
        given(novelStatistics.getAverageRating()).willReturn(new BigDecimal("4.250"));
        given(novelStatistics.getRatingCount()).willReturn(8L);
        given(libraryService.getInterestCountsByNovelIds(List.of(1L))).willReturn(Map.of(1L, 3L));

        TasteNovelsGetResponse response = searchNovelApplication.getTasteNovels(user);

        assertThat(response.tasteNovels()).containsExactly(new TasteNovelGetResponse(
                1L,
                "작품명",
                "작가명",
                "https://example.com/novel.png",
                3L,
                4.25f,
                8L
        ));
        then(libraryService).should().getInterestCountsByNovelIds(List.of(1L));
        then(novel).should(never()).getUserNovels();
    }

    /**
     * 최근 검색어 저장은 검색 결과와 별개의 부수 효과다. 검색 화면이 아닌 곳(작품 연결, 컬렉션 담기 등)에서
     * 같은 검색 API를 쓰면 사용자가 검색한 적 없는 단어가 최근 검색어로 남는다.
     * 요청이 저장을 원하는지(recordRecentSearch)를 기존 저장 조건과 함께 검증한다.
     */
    @Nested
    @DisplayName("작품 검색의 최근 검색어 저장")
    class SearchNovelsRecentSearch {

        private static final String QUERY = "재혼 황후";
        private static final String SANITIZED_QUERY = "재혼황후";
        private static final long USER_ID = 42L;

        @DisplayName("저장을 요청한 로그인 사용자의 검색은 정규화된 검색어로 저장 이벤트를 발행한다")
        @Test
        void publishesNovelSearchedEventWhenRecordRecentSearchIsTrue() {
            givenLoginUser();
            givenSearchResult();

            searchNovelApplication.searchNovels(user, QUERY, 0, 10, true);

            then(eventPublisher).should().publishEvent(new NovelSearchedEvent(USER_ID, SANITIZED_QUERY));
        }

        // 검색 결과가 없어도 사용자가 검색한 사실은 남아야 하므로 기존처럼 저장한다.
        @DisplayName("검색 결과가 없어도 저장을 요청하면 저장 이벤트를 발행한다")
        @Test
        void publishesNovelSearchedEventWhenNoNovelMatches() {
            givenLoginUser();
            givenEmptySearchResult();

            searchNovelApplication.searchNovels(user, QUERY, 0, 10, true);

            then(eventPublisher).should().publishEvent(new NovelSearchedEvent(USER_ID, SANITIZED_QUERY));
        }

        @DisplayName("저장을 요청하지 않은 검색은 로그인 사용자여도 저장 이벤트를 발행하지 않는다")
        @Test
        void doesNotPublishNovelSearchedEventWhenRecordRecentSearchIsFalse() {
            givenSearchResult();

            searchNovelApplication.searchNovels(user, QUERY, 0, 10, false);

            then(eventPublisher).should(never()).publishEvent(any());
        }

        // 저장 여부와 무관하게 검색 결과·전체 개수·다음 페이지 여부는 그대로여야 한다.
        @DisplayName("저장을 요청하지 않아도 검색 결과와 페이지 정보는 그대로 반환한다")
        @Test
        void returnsSameSearchResultWhenRecordRecentSearchIsFalse() {
            givenSearchResult();

            SearchedNovelsResponse response = searchNovelApplication.searchNovels(user, QUERY, 0, 10, false);

            assertThat(response.resultCount()).isEqualTo(1L);
            assertThat(response.isLoadable()).isFalse();
            assertThat(response.novels()).hasSize(1);
            assertThat(response.novels().get(0).novelId()).isEqualTo(1L);
        }

        @DisplayName("비로그인 검색은 저장을 요청해도 저장 이벤트를 발행하지 않는다")
        @Test
        void doesNotPublishNovelSearchedEventForAnonymousUser() {
            givenSearchResult();

            searchNovelApplication.searchNovels(null, QUERY, 0, 10, true);

            then(eventPublisher).should(never()).publishEvent(any());
        }

        @DisplayName("정규화 후 빈 검색어는 저장을 요청해도 저장 이벤트를 발행하지 않는다")
        @Test
        void doesNotPublishNovelSearchedEventForBlankQuery() {
            searchNovelApplication.searchNovels(user, "   !!!   ", 0, 10, true);

            then(eventPublisher).should(never()).publishEvent(any());
        }

        private void givenLoginUser() {
            given(user.getUserId()).willReturn(USER_ID);
        }

        private void givenSearchResult() {
            given(novel.getNovelId()).willReturn(1L);
            given(novel.getTitle()).willReturn("작품명");
            given(novel.getAuthor()).willReturn("작가명");
            given(novel.getNovelImage()).willReturn("https://example.com/novel.png");
            given(novel.getNovelStatistics()).willReturn(novelStatistics);
            given(novelStatistics.getAverageRating()).willReturn(new BigDecimal("4.250"));
            given(novelStatistics.getRatingCount()).willReturn(8L);
            given(libraryService.getInterestCountsByNovelIds(List.of(1L))).willReturn(Map.of(1L, 3L));
            givenSearchPage(new PageImpl<>(List.of(novel), PageRequest.of(0, 10), 1));
        }

        private void givenEmptySearchResult() {
            given(libraryService.getInterestCountsByNovelIds(List.of())).willReturn(Map.of());
            givenSearchPage(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));
        }

        private void givenSearchPage(Page<Novel> page) {
            given(novelService.searchNovels(PageRequest.of(0, 10), SANITIZED_QUERY)).willReturn(page);
        }
    }
}
