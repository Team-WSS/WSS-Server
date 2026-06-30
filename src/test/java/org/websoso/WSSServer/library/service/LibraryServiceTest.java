package org.websoso.WSSServer.library.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.domain.common.ReadStatus;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.repository.UserNovelRepository;
import org.websoso.WSSServer.library.repository.projection.NovelInterestCount;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.user.domain.User;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

    private static final long NOVEL_ID = 1L;

    @InjectMocks
    private LibraryService libraryService;

    @Mock
    private UserNovelRepository userNovelRepository;

    @Mock
    private User user;

    @Mock
    private Novel novel;

    @DisplayName("평점과 상태 변경 시 기존값과 변경값의 차이만 작품 통계에 반영한다")
    @Test
    void updatesStatisticsByDelta() {
        given(novel.getNovelId()).willReturn(NOVEL_ID);
        UserNovel library = UserNovel.create(ReadStatus.QUIT, 2.0f, null, null, user, novel);

        libraryService.updateEvaluation(library, 4.0f, ReadStatus.WATCHING, null, null);

        then(userNovelRepository).should().flush();
        then(userNovelRepository).should()
                .updateNovelStatisticsByDelta(NOVEL_ID, new BigDecimal("2.0"), 0L, 1L);
    }

    @DisplayName("평점이 새로 생기면 평점 합과 평가 수를 각각 증가시킨다")
    @Test
    void addsRatingStatistics() {
        given(novel.getNovelId()).willReturn(NOVEL_ID);
        UserNovel library = UserNovel.create(ReadStatus.WATCHING, 0.0f, null, null, user, novel);

        libraryService.updateEvaluation(library, 3.5f, ReadStatus.WATCHING, null, null);

        then(userNovelRepository).should()
                .updateNovelStatisticsByDelta(NOVEL_ID, new BigDecimal("3.5"), 1L, 0L);
    }

    @DisplayName("통계에 영향 없는 변경은 작품 통계 UPDATE를 실행하지 않는다")
    @Test
    void skipsStatisticsUpdateWhenDeltaIsZero() {
        UserNovel library = UserNovel.create(ReadStatus.WATCHED, 4.0f, null, null, user, novel);

        libraryService.updateEvaluation(
                library,
                4.0f,
                ReadStatus.WATCHED,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 2)
        );

        then(userNovelRepository).should(never()).flush();
        then(userNovelRepository).should(never())
                .updateNovelStatisticsByDelta(anyLong(), any(BigDecimal.class), anyLong(), anyLong());
    }

    @DisplayName("서재 데이터 삭제 시 해당 데이터의 통계 기여분만 차감한다")
    @Test
    void subtractsStatisticsWhenDeletingLibrary() {
        given(novel.getNovelId()).willReturn(NOVEL_ID);
        UserNovel library = UserNovel.create(ReadStatus.WATCHED, 4.5f, null, null, user, novel);

        libraryService.delete(library);

        then(userNovelRepository).should().delete(library);
        then(userNovelRepository).should()
                .updateNovelStatisticsByDelta(NOVEL_ID, new BigDecimal("-4.5"), -1L, -1L);
    }

    @DisplayName("관심 서재 데이터가 새로 삽입되면 인기도만 증가시킨다")
    @Test
    void incrementsPopularityWhenInterestIsInserted() {
        given(user.getUserId()).willReturn(10L);
        given(novel.getNovelId()).willReturn(NOVEL_ID);
        given(userNovelRepository.insertInterestIfAbsent(
                10L,
                NOVEL_ID,
                UserNovel.DEFAULT_RATING,
                UserNovel.DEFAULT_STATUS
        )).willReturn(1);

        UserNovel library = UserNovel.create(null, 0.0f, null, null, user, novel);
        library.markAsInterested();
        given(userNovelRepository.findByNovelIdAndUserForUpdate(NOVEL_ID, user))
                .willReturn(Optional.of(library));

        libraryService.registerInterest(user, novel);

        then(userNovelRepository).should()
                .updateNovelStatisticsByDelta(NOVEL_ID, BigDecimal.ZERO, 0L, 1L);
    }

    @DisplayName("작품별 관심 수를 한 번의 집계 조회 결과로 반환한다")
    @Test
    void getsInterestCountsByNovelIds() {
        List<Long> novelIds = List.of(1L, 2L);
        given(userNovelRepository.findInterestCountsByNovelIds(novelIds))
                .willReturn(List.of(
                        new NovelInterestCount(1L, 3L),
                        new NovelInterestCount(2L, 5L)
                ));

        Map<Long, Long> result = libraryService.getInterestCountsByNovelIds(novelIds);

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                1L, 3L,
                2L, 5L
        ));
    }
}
