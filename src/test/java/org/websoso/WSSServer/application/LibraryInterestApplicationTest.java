package org.websoso.WSSServer.application;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.service.LibraryService;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.domain.NovelStatisticsContribution;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;
import org.websoso.WSSServer.novel.service.NovelStatisticsService;
import org.websoso.WSSServer.user.domain.User;

@ExtendWith(MockitoExtension.class)
class LibraryInterestApplicationTest {

    private static final long NOVEL_ID = 1L;

    @InjectMocks
    private LibraryInterestApplication application;

    @Mock
    private NovelServiceImpl novelService;

    @Mock
    private LibraryService libraryService;

    @Mock
    private NovelStatisticsService novelStatisticsService;

    @Mock
    private User user;

    @Mock
    private Novel novel;

    @DisplayName("관심 등록 전후의 UserNovel 통계 기여분을 작품 통계 서비스에 전달한다")
    @Test
    void registersInterestAndUpdatesStatistics() {
        UserNovel library = UserNovel.create(null, 0.0f, null, null, user, novel);
        given(novelService.getNovelOrException(NOVEL_ID)).willReturn(novel);
        given(libraryService.getOrCreateLibraryForInterest(user, novel)).willReturn(library);
        willAnswer(invocation -> {
            library.markAsInterested();
            return null;
        }).given(libraryService).registerInterest(library);

        application.registerAsInterest(user, NOVEL_ID);

        then(novelStatisticsService).should().updateByDelta(
                NOVEL_ID,
                NovelStatisticsContribution.EMPTY,
                new NovelStatisticsContribution(BigDecimal.ZERO, 0L, 1L)
        );
    }

    @DisplayName("관심 해제 전후의 UserNovel 통계 기여분을 작품 통계 서비스에 전달한다")
    @Test
    void unregistersInterestAndUpdatesStatistics() {
        UserNovel library = UserNovel.create(null, 0.0f, null, null, user, novel);
        library.markAsInterested();
        given(libraryService.getLibraryForUpdateOrNull(user, NOVEL_ID)).willReturn(library);
        willAnswer(invocation -> {
            library.unmarkAsInterested();
            return null;
        }).given(libraryService).unregisterInterest(library);

        application.unregisterAsInterest(user, NOVEL_ID);

        then(novelStatisticsService).should().updateByDelta(
                NOVEL_ID,
                new NovelStatisticsContribution(BigDecimal.ZERO, 0L, 1L),
                NovelStatisticsContribution.EMPTY
        );
    }
}
