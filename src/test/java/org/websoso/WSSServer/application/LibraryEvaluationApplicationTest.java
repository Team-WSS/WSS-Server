package org.websoso.WSSServer.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;
import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserNovelError.USER_NOVEL_NOT_FOUND;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.domain.common.ReadStatus;
import org.websoso.WSSServer.dto.userNovel.UserNovelUpdateRequest;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.exception.exception.CustomUserNovelException;
import org.websoso.WSSServer.library.domain.AttractivePoint;
import org.websoso.WSSServer.library.domain.Keyword;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.service.AttractivePointService;
import org.websoso.WSSServer.library.service.KeywordService;
import org.websoso.WSSServer.library.service.LibraryService;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.domain.NovelStatisticsContribution;
import org.websoso.WSSServer.novel.service.NovelServiceImpl;
import org.websoso.WSSServer.novel.service.NovelStatisticsService;
import org.websoso.WSSServer.user.domain.User;

@ExtendWith(MockitoExtension.class)
class LibraryEvaluationApplicationTest {

    private static final long NOVEL_ID = 1L;
    private static final UserNovelUpdateRequest UPDATE_REQUEST = new UserNovelUpdateRequest(
            4.0f, ReadStatus.WATCHED, null, null, List.of(), List.of());

    @InjectMocks
    private LibraryEvaluationApplication application;

    @Mock
    private NovelServiceImpl novelService;

    @Mock
    private LibraryService libraryService;

    @Mock
    private NovelStatisticsService novelStatisticsService;

    @Mock
    private AttractivePointService attractivePointService;

    @Mock
    private KeywordService keywordService;

    @Mock
    private User user;

    @Mock
    private Novel novel;

    @Mock
    private AttractivePoint attractivePoint;

    @Mock
    private Keyword keyword;

    @DisplayName("평가 수정 시 소설 존재 검증 후 서재 수정과 통계, 매력 포인트, 키워드 갱신을 위임한다")
    @Test
    void updatesEvaluationAfterValidatingNovel() {
        UserNovel userNovel = UserNovel.create(null, 0.0f, null, null, user, novel);
        UserNovelUpdateRequest request = new UserNovelUpdateRequest(
                4.0f, ReadStatus.WATCHED, null, null, List.of("몰입감"), List.of(1));
        given(libraryService.getLibraryForUpdateOrException(user, NOVEL_ID)).willReturn(userNovel);
        willAnswer(invocation -> {
            userNovel.updateUserNovel(4.0f, ReadStatus.WATCHED, null, null);
            return null;
        }).given(libraryService).updateEvaluation(userNovel, 4.0f, ReadStatus.WATCHED, null, null);
        given(attractivePointService.getAttractivePointByString("몰입감")).willReturn(attractivePoint);
        given(keywordService.getKeywordOrException(1)).willReturn(keyword);

        application.updateEvaluation(user, NOVEL_ID, request);

        then(novelService).should().getNovelOrException(NOVEL_ID);
        then(libraryService).should().updateEvaluation(userNovel, 4.0f, ReadStatus.WATCHED, null, null);
        then(novelStatisticsService).should().updateByDelta(
                NOVEL_ID,
                NovelStatisticsContribution.EMPTY,
                new NovelStatisticsContribution(new BigDecimal("4.0"), 1L, 1L)
        );
        then(attractivePointService).should().createUserNovelAttractivePoint(userNovel, attractivePoint);
        then(keywordService).should().createNovelKeyword(userNovel, keyword);
    }

    @DisplayName("관심 등록된 작품의 평가 삭제 시 소설 존재 검증 후 평가만 지우고 매력 포인트와 키워드를 삭제한다")
    @Test
    void deletesEvaluationOnlyWhenNovelIsStillInterested() {
        UserNovel userNovel = UserNovel.create(ReadStatus.WATCHED, 4.0f, null, null, user, novel);
        userNovel.markAsInterested();
        given(libraryService.getLibraryForUpdateOrException(user, NOVEL_ID)).willReturn(userNovel);
        willAnswer(invocation -> {
            userNovel.deleteEvaluation();
            return null;
        }).given(libraryService).deleteEvaluation(userNovel);

        application.deleteEvaluation(user, NOVEL_ID);

        then(novelService).should().getNovelOrException(NOVEL_ID);
        then(libraryService).should().deleteEvaluation(userNovel);
        then(libraryService).should(never()).delete(userNovel);
        then(novelStatisticsService).should().updateByDelta(
                NOVEL_ID,
                new NovelStatisticsContribution(new BigDecimal("4.0"), 1L, 1L),
                new NovelStatisticsContribution(BigDecimal.ZERO, 0L, 1L)
        );
        then(attractivePointService).should().deleteUserNovelAttractivePoints(userNovel.getUserNovelAttractivePoints());
        then(keywordService).should().deleteUserNovelKeywords(userNovel.getUserNovelKeywords());
    }

    @DisplayName("관심 등록되지 않은 작품의 평가 삭제 시 소설 존재 검증 후 서재에서 제거하고 통계 기여분을 비운다")
    @Test
    void deletesLibraryWhenNovelIsNotInterested() {
        UserNovel userNovel = UserNovel.create(ReadStatus.WATCHED, 4.0f, null, null, user, novel);
        given(libraryService.getLibraryForUpdateOrException(user, NOVEL_ID)).willReturn(userNovel);

        application.deleteEvaluation(user, NOVEL_ID);

        then(novelService).should().getNovelOrException(NOVEL_ID);
        then(libraryService).should().delete(userNovel);
        then(libraryService).should(never()).deleteEvaluation(userNovel);
        then(novelStatisticsService).should().updateByDelta(
                NOVEL_ID,
                new NovelStatisticsContribution(new BigDecimal("4.0"), 1L, 1L),
                NovelStatisticsContribution.EMPTY
        );
        then(attractivePointService).shouldHaveNoInteractions();
        then(keywordService).shouldHaveNoInteractions();
    }

    @DisplayName("평가 수정 시 소설이 없으면 서재를 조회하지 않고 NOVEL_NOT_FOUND 예외를 던진다")
    @Test
    void throwsNovelNotFoundWhenUpdatingEvaluationOfMissingNovel() {
        given(novelService.getNovelOrException(NOVEL_ID))
                .willThrow(new CustomNovelException(NOVEL_NOT_FOUND, "novel with the given id is not found"));

        assertThatThrownBy(() -> application.updateEvaluation(user, NOVEL_ID, UPDATE_REQUEST))
                .isInstanceOf(CustomNovelException.class)
                .extracting(throwable -> ((CustomNovelException) throwable).getICustomError())
                .isEqualTo(NOVEL_NOT_FOUND);

        then(libraryService).shouldHaveNoInteractions();
    }

    @DisplayName("평가 수정 시 사용자의 서재에 작품이 없으면 USER_NOVEL_NOT_FOUND 예외를 던진다")
    @Test
    void throwsUserNovelNotFoundWhenUpdatingEvaluationOfUnregisteredNovel() {
        given(libraryService.getLibraryForUpdateOrException(user, NOVEL_ID))
                .willThrow(new CustomUserNovelException(USER_NOVEL_NOT_FOUND,
                        "user novel with the given user and novel is not found"));

        assertThatThrownBy(() -> application.updateEvaluation(user, NOVEL_ID, UPDATE_REQUEST))
                .isInstanceOf(CustomUserNovelException.class)
                .extracting(throwable -> ((CustomUserNovelException) throwable).getICustomError())
                .isEqualTo(USER_NOVEL_NOT_FOUND);

        then(novelStatisticsService).shouldHaveNoInteractions();
    }

    @DisplayName("평가 삭제 시 소설이 없으면 서재를 조회하지 않고 NOVEL_NOT_FOUND 예외를 던진다")
    @Test
    void throwsNovelNotFoundWhenDeletingEvaluationOfMissingNovel() {
        given(novelService.getNovelOrException(NOVEL_ID))
                .willThrow(new CustomNovelException(NOVEL_NOT_FOUND, "novel with the given id is not found"));

        assertThatThrownBy(() -> application.deleteEvaluation(user, NOVEL_ID))
                .isInstanceOf(CustomNovelException.class)
                .extracting(throwable -> ((CustomNovelException) throwable).getICustomError())
                .isEqualTo(NOVEL_NOT_FOUND);

        then(libraryService).shouldHaveNoInteractions();
    }

    @DisplayName("평가 삭제 시 사용자의 서재에 작품이 없으면 USER_NOVEL_NOT_FOUND 예외를 던진다")
    @Test
    void throwsUserNovelNotFoundWhenDeletingEvaluationOfUnregisteredNovel() {
        given(libraryService.getLibraryForUpdateOrException(user, NOVEL_ID))
                .willThrow(new CustomUserNovelException(USER_NOVEL_NOT_FOUND,
                        "user novel with the given user and novel is not found"));

        assertThatThrownBy(() -> application.deleteEvaluation(user, NOVEL_ID))
                .isInstanceOf(CustomUserNovelException.class)
                .extracting(throwable -> ((CustomUserNovelException) throwable).getICustomError())
                .isEqualTo(USER_NOVEL_NOT_FOUND);

        then(novelStatisticsService).shouldHaveNoInteractions();
    }
}
