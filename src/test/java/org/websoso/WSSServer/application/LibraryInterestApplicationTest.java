package org.websoso.WSSServer.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.USER_NOVEL_NOT_FOUND;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.exception.exception.CustomUserNovelException;
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
        given(libraryService.getLibraryForUpdateOrException(user, NOVEL_ID)).willReturn(library);
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

    @DisplayName("관심 해제 시 소설이 없으면 서재를 조회하지 않고 NOVEL_NOT_FOUND 예외를 던진다")
    @Test
    void throwsNovelNotFoundWhenUnregisteringInterestOfMissingNovel() {
        given(novelService.getNovelOrException(NOVEL_ID))
                .willThrow(new CustomNovelException(NOVEL_NOT_FOUND, "novel with the given id is not found"));

        assertThatThrownBy(() -> application.unregisterAsInterest(user, NOVEL_ID))
                .isInstanceOf(CustomNovelException.class)
                .extracting(throwable -> ((CustomNovelException) throwable).getICustomError())
                .isEqualTo(NOVEL_NOT_FOUND);

        then(libraryService).shouldHaveNoInteractions();
    }

    @DisplayName("관심 해제 시 사용자의 서재에 작품이 없으면 USER_NOVEL_NOT_FOUND 예외를 던진다")
    @Test
    void throwsUserNovelNotFoundWhenUnregisteringInterestOfUnregisteredNovel() {
        given(libraryService.getLibraryForUpdateOrException(user, NOVEL_ID))
                .willThrow(new CustomUserNovelException(USER_NOVEL_NOT_FOUND,
                        "user novel with the given user and novel is not found"));

        assertThatThrownBy(() -> application.unregisterAsInterest(user, NOVEL_ID))
                .isInstanceOf(CustomUserNovelException.class)
                .extracting(throwable -> ((CustomUserNovelException) throwable).getICustomError())
                .isEqualTo(USER_NOVEL_NOT_FOUND);

        then(novelStatisticsService).shouldHaveNoInteractions();
    }
}
