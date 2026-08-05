package org.websoso.WSSServer.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.GenrePreference;
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
}
