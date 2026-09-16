package org.websoso.WSSServer.collection.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.websoso.WSSServer.collection.application.CollectionFindApplication.NOVEL_PREVIEW_SIZE;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_COLLECTION_CURSOR;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_COLLECTION_PAGE_SIZE;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.collection.controller.dto.CollectionNovelSummaryGetResponse;
import org.websoso.WSSServer.collection.controller.dto.LikedCollectionPreviewGetResponse;
import org.websoso.WSSServer.collection.controller.dto.LikedCollectionsGetResponse;
import org.websoso.WSSServer.collection.domain.CollectionLikeCursor;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.collection.repository.projection.LikedCollectionRow;
import org.websoso.WSSServer.collection.service.CollectionLikeQueryService;
import org.websoso.WSSServer.collection.service.CollectionQueryService;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.BlockService;

@ExtendWith(MockitoExtension.class)
class CollectionLikeFindApplicationTest {

    private static final long VIEWER_ID = 1L;
    private static final long BLOCKED_USER_ID = 9L;
    private static final int SIZE = 2;

    private static final LocalDateTime FIRST_LIKED = LocalDateTime.of(2025, 1, 3, 0, 0);
    private static final LocalDateTime SECOND_LIKED = LocalDateTime.of(2025, 1, 2, 0, 0);
    private static final LocalDateTime THIRD_LIKED = LocalDateTime.of(2025, 1, 1, 0, 0);

    @InjectMocks
    private CollectionLikeFindApplication application;

    @Mock
    private BlockService blockService;

    @Mock
    private CollectionLikeQueryService collectionLikeQueryService;

    @Mock
    private CollectionQueryService collectionQueryService;

    // 페이지 크기

    @DisplayName("허용 범위를 벗어난 페이지 크기는 조회 전에 거부한다")
    @ParameterizedTest
    @ValueSource(ints = {0, -1, 101})
    void rejectsPageSizeOutOfRange(int size) {
        // 페이지 크기는 조회자를 읽기 전에 검증하므로 사용자 정보가 필요 없다.
        assertThatThrownBy(() -> application.getLikedCollections(mock(User.class), null, size))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(INVALID_COLLECTION_PAGE_SIZE);

        then(collectionLikeQueryService).should(never())
                .findLikedCollectionRows(anyLong(), anyList(), any(), anyInt());
    }

    @DisplayName("허용 범위의 페이지 크기는 그대로 조회한다")
    @ParameterizedTest
    @ValueSource(ints = {1, 100})
    void acceptsPageSizeInRange(int size) {
        givenPage(List.of());

        assertThatCode(() -> application.getLikedCollections(user(), null, size)).doesNotThrowAnyException();
    }

    // 커서 페이지네이션

    @DisplayName("다음 페이지가 있는지 알기 위해 요청 크기보다 하나 더 읽는다")
    @Test
    void readsOneMoreThanRequested() {
        givenPage(List.of());

        application.getLikedCollections(user(), null, SIZE);

        then(collectionLikeQueryService).should()
                .findLikedCollectionRows(eq(VIEWER_ID), anyList(), eq(null), eq(SIZE + 1));
    }

    @DisplayName("더 읽은 초과분은 응답에 넣지 않고 다음 페이지가 있다고만 알린다")
    @Test
    void excludesExtraRowFromPage() {
        givenPage(List.of(row(11L, FIRST_LIKED), row(12L, SECOND_LIKED), row(13L, THIRD_LIKED)));

        LikedCollectionsGetResponse response = application.getLikedCollections(user(), null, SIZE);

        assertThat(response.hasNext()).isTrue();
        assertThat(response.collections()).hasSize(SIZE);
        assertThat(response.collections())
                .extracting(LikedCollectionPreviewGetResponse::collectionId)
                .containsExactly(11L, 12L);
    }

    @DisplayName("초과분은 미리보기 조회에서도 뺀다")
    @Test
    void excludesExtraRowFromPreviewQuery() {
        givenPage(List.of(row(11L, FIRST_LIKED), row(12L, SECOND_LIKED), row(13L, THIRD_LIKED)));

        application.getLikedCollections(user(), null, SIZE);

        then(collectionQueryService).should().findRecentNovelPreviews(List.of(11L, 12L), NOVEL_PREVIEW_SIZE);
    }

    @DisplayName("다음 페이지가 있으면 마지막 행의 좋아요 시점과 좋아요 식별자로 커서를 만든다")
    @Test
    void buildsCursorFromLastRowOfPage() {
        givenPage(List.of(row(11L, FIRST_LIKED), row(12L, SECOND_LIKED), row(13L, THIRD_LIKED)));

        LikedCollectionsGetResponse response = application.getLikedCollections(user(), null, SIZE);

        CollectionLikeCursor cursor = CollectionLikeCursor.decode(response.nextCursor());
        assertThat(cursor.likedDate()).isEqualTo(SECOND_LIKED);
        assertThat(cursor.collectionLikeId()).isEqualTo(likeIdOf(12L));
    }

    @DisplayName("마지막 페이지에서는 커서를 주지 않는다")
    @Test
    void omitsCursorOnLastPage() {
        givenPage(List.of(row(11L, FIRST_LIKED)));

        LikedCollectionsGetResponse response = application.getLikedCollections(user(), null, SIZE);

        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @DisplayName("좋아요한 컬렉션이 없으면 빈 목록과 커서 없는 응답을 준다")
    @Test
    void returnsEmptyPage() {
        givenPage(List.of());

        LikedCollectionsGetResponse response = application.getLikedCollections(user(), null, SIZE);

        assertThat(response.collections()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @DisplayName("받은 커서를 복원해 다음 페이지 조회 조건으로 넘긴다")
    @Test
    void passesDecodedCursorToQuery() {
        givenPage(List.of());
        String cursor = CollectionLikeCursor.of(SECOND_LIKED, 42L).encode();

        application.getLikedCollections(user(), cursor, SIZE);

        ArgumentCaptor<CollectionLikeCursor> captor = ArgumentCaptor.forClass(CollectionLikeCursor.class);
        then(collectionLikeQueryService).should()
                .findLikedCollectionRows(eq(VIEWER_ID), anyList(), captor.capture(), anyInt());
        assertThat(captor.getValue().likedDate()).isEqualTo(SECOND_LIKED);
        assertThat(captor.getValue().collectionLikeId()).isEqualTo(42L);
    }

    @DisplayName("서버가 발급하지 않은 커서는 조회 전에 거부한다")
    @Test
    void rejectsCursorNotIssuedByApi() {
        assertThatThrownBy(() -> application.getLikedCollections(user(), "not-a-cursor", SIZE))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(INVALID_COLLECTION_CURSOR);

        then(collectionLikeQueryService).should(never())
                .findLikedCollectionRows(anyLong(), anyList(), any(), anyInt());
    }

    // 공개 범위와 차단

    @DisplayName("차단 관계인 사용자를 조회해 목록 쿼리에 넘긴다")
    @Test
    void passesBlockedUsersToQuery() {
        given(blockService.findBlockRelationUserIds(VIEWER_ID)).willReturn(List.of(BLOCKED_USER_ID));
        givenPage(List.of());

        application.getLikedCollections(user(), null, SIZE);

        then(collectionLikeQueryService).should()
                .findLikedCollectionRows(eq(VIEWER_ID), eq(List.of(BLOCKED_USER_ID)), any(), anyInt());
    }

    @DisplayName("전체 개수도 목록과 같은 차단 조건으로 센다")
    @Test
    void countsWithSameBlockCondition() {
        given(blockService.findBlockRelationUserIds(VIEWER_ID)).willReturn(List.of(BLOCKED_USER_ID));
        givenPage(List.of());

        application.getLikedCollections(user(), null, SIZE);

        then(collectionLikeQueryService).should().countLikedCollections(VIEWER_ID, List.of(BLOCKED_USER_ID));
    }

    @DisplayName("전체 개수는 페이지 크기와 무관하다")
    @Test
    void countIsIndependentOfPageSize() {
        givenPage(List.of(row(11L, FIRST_LIKED)));
        given(collectionLikeQueryService.countLikedCollections(anyLong(), anyList())).willReturn(12L);

        LikedCollectionsGetResponse response = application.getLikedCollections(user(), null, SIZE);

        assertThat(response.collectionsCount()).isEqualTo(12L);
        assertThat(response.collections()).hasSize(1);
    }

    // 카드

    @DisplayName("카드는 컬렉션이 받은 좋아요 수를 담는다")
    @Test
    void cardCarriesLikeCount() {
        givenPage(List.of(row(11L, FIRST_LIKED)));

        LikedCollectionsGetResponse response = application.getLikedCollections(user(), null, SIZE);

        assertThat(response.collections().get(0).likeCount()).isEqualTo(24L);
    }

    @DisplayName("카드는 컬렉션별 표시 순서 앞쪽 미리보기 작품을 채운다")
    @Test
    void cardCarriesRecentNovelPreview() {
        givenPage(List.of(row(11L, FIRST_LIKED)));
        given(collectionQueryService.findRecentNovelPreviews(anyList(), anyInt()))
                .willReturn(Map.of(11L, List.of(novelSummary(7L))));

        LikedCollectionsGetResponse response = application.getLikedCollections(user(), null, SIZE);

        assertThat(response.collections().get(0).recentNovels()).containsExactly(novelSummary(7L));
    }

    @DisplayName("미리보기가 없는 컬렉션의 카드는 빈 배열을 담는다")
    @Test
    void cardCarriesEmptyPreviewWhenNothingFound() {
        givenPage(List.of(row(11L, FIRST_LIKED)));

        LikedCollectionsGetResponse response = application.getLikedCollections(user(), null, SIZE);

        assertThat(response.collections().get(0).recentNovels()).isEmpty();
    }

    private void givenPage(List<LikedCollectionRow> rows) {
        given(collectionLikeQueryService.findLikedCollectionRows(anyLong(), anyList(), any(), anyInt()))
                .willReturn(rows);
    }

    private LikedCollectionRow row(Long collectionId, LocalDateTime likedDate) {
        return new LikedCollectionRow(likeIdOf(collectionId), likedDate, collectionId, "취향 저격 로판",
                "여주가 강한 로맨스 판타지", true, 12L, 24L, 5L, "대표 작품", "https://image/5.png", "대표 작가");
    }

    /**
     * 커서는 컬렉션이 아니라 좋아요 행을 가리키므로, 컬렉션과 좋아요의 식별자가 섞이지 않도록 다른 값을 쓴다.
     */
    private long likeIdOf(Long collectionId) {
        return collectionId + 1000L;
    }

    private CollectionNovelSummaryGetResponse novelSummary(Long novelId) {
        return new CollectionNovelSummaryGetResponse(novelId, "작품", "https://image/novel.png", "작가");
    }

    private User user() {
        User user = mock(User.class);
        given(user.getUserId()).willReturn(VIEWER_ID);
        return user;
    }
}
