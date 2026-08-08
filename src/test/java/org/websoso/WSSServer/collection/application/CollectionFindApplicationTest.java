package org.websoso.WSSServer.collection.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.websoso.WSSServer.collection.application.CollectionFindApplication.NOVEL_PREVIEW_SIZE;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.COLLECTION_NOT_FOUND;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_COLLECTION_CURSOR;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_COLLECTION_PAGE_SIZE;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.PRIVATE_COLLECTION_ACCESS;
import static org.websoso.WSSServer.user.exception.CustomBlockError.BLOCKED_USER_ACCESS;

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
import org.websoso.WSSServer.collection.controller.dto.CollectionGetResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionNovelGetResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionNovelPreviewGetResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionPreviewGetResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionsGetResponse;
import org.websoso.WSSServer.collection.domain.CollectionCursor;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.collection.repository.projection.CollectionDetailRow;
import org.websoso.WSSServer.collection.repository.projection.CollectionPreviewRow;
import org.websoso.WSSServer.collection.service.CollectionQueryService;
import org.websoso.WSSServer.domain.common.SortCriteria;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.exception.CustomBlockException;
import org.websoso.WSSServer.user.service.BlockService;
import org.websoso.WSSServer.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class CollectionFindApplicationTest {

    private static final long OWNER_ID = 1L;
    private static final long VISITOR_ID = 2L;
    private static final long COLLECTION_ID = 100L;
    private static final int SIZE = 2;

    private static final LocalDateTime FIRST_CREATED = LocalDateTime.of(2025, 1, 3, 0, 0);
    private static final LocalDateTime SECOND_CREATED = LocalDateTime.of(2025, 1, 2, 0, 0);
    private static final LocalDateTime THIRD_CREATED = LocalDateTime.of(2025, 1, 1, 0, 0);

    @InjectMocks
    private CollectionFindApplication application;

    @Mock
    private UserService userService;

    @Mock
    private BlockService blockService;

    @Mock
    private CollectionQueryService collectionQueryService;

    // 목록 조회: 공개 범위

    @DisplayName("본인 목록은 비공개 컬렉션까지 포함해 조회한다")
    @Test
    void ownerListIncludesPrivateCollections() {
        givenPage(List.of(row(11L, FIRST_CREATED)));

        application.getUserCollections(user(OWNER_ID), OWNER_ID, null, SIZE);

        then(collectionQueryService).should()
                .findCollectionPreviewRows(eq(OWNER_ID), eq(true), any(), anyInt());
        then(collectionQueryService).should().countVisibleCollections(OWNER_ID, true);
    }

    @DisplayName("다른 사용자의 목록은 공개 컬렉션만 조회한다")
    @Test
    void visitorListExcludesPrivateCollections() {
        givenPage(List.of(row(11L, FIRST_CREATED)));

        application.getUserCollections(user(VISITOR_ID), OWNER_ID, null, SIZE);

        then(collectionQueryService).should()
                .findCollectionPreviewRows(eq(OWNER_ID), eq(false), any(), anyInt());
        then(collectionQueryService).should().countVisibleCollections(OWNER_ID, false);
    }

    @DisplayName("존재하지 않는 사용자의 목록은 조회하지 않는다")
    @Test
    void listRejectsUnknownOwner() {
        willThrow(new IllegalStateException("user not found"))
                .given(userService).getUserOrException(OWNER_ID);

        assertThatThrownBy(() -> application.getUserCollections(user(VISITOR_ID), OWNER_ID, null, SIZE))
                .isInstanceOf(IllegalStateException.class);

        then(collectionQueryService).shouldHaveNoInteractions();
    }

    // 목록 조회: 차단

    @DisplayName("어느 방향이든 차단 관계면 목록을 조회하지 않는다")
    @Test
    void listRejectsBlockedRelation() {
        willThrow(new CustomBlockException(BLOCKED_USER_ACCESS, "blocked"))
                .given(blockService).validateNotBlocked(VISITOR_ID, OWNER_ID);

        assertThatThrownBy(() -> application.getUserCollections(user(VISITOR_ID), OWNER_ID, null, SIZE))
                .isInstanceOf(CustomBlockException.class)
                .extracting(exception -> ((CustomBlockException) exception).getICustomError())
                .isEqualTo(BLOCKED_USER_ACCESS);

        then(collectionQueryService).shouldHaveNoInteractions();
    }

    // 목록 조회: 커서 페이지네이션

    @DisplayName("요청 크기보다 하나 더 읽어 다음 페이지 존재 여부를 판단하고 초과분은 응답에 넣지 않는다")
    @Test
    void detectsNextPageWithoutLeakingExtraRow() {
        givenPage(List.of(row(11L, FIRST_CREATED), row(12L, SECOND_CREATED), row(13L, THIRD_CREATED)));

        CollectionsGetResponse response = application.getUserCollections(user(OWNER_ID), OWNER_ID, null, SIZE);

        then(collectionQueryService).should()
                .findCollectionPreviewRows(anyLong(), anyBoolean(), any(), eq(SIZE + 1));
        assertThat(response.hasNext()).isTrue();
        assertThat(response.collections()).extracting(CollectionPreviewGetResponse::collectionId)
                .containsExactly(11L, 12L);
    }

    @DisplayName("다음 커서는 이번 페이지 마지막 컬렉션의 생성 시점과 식별자를 담는다")
    @Test
    void nextCursorPointsToLastRowOfPage() {
        givenPage(List.of(row(11L, FIRST_CREATED), row(12L, SECOND_CREATED), row(13L, THIRD_CREATED)));

        CollectionsGetResponse response = application.getUserCollections(user(OWNER_ID), OWNER_ID, null, SIZE);

        CollectionCursor nextCursor = CollectionCursor.decode(response.nextCursor());
        assertThat(nextCursor.collectionId()).isEqualTo(12L);
        assertThat(nextCursor.createdDate()).isEqualTo(SECOND_CREATED);
    }

    @DisplayName("다음 페이지가 없으면 커서를 주지 않는다")
    @Test
    void omitsCursorOnLastPage() {
        givenPage(List.of(row(11L, FIRST_CREATED), row(12L, SECOND_CREATED)));

        CollectionsGetResponse response = application.getUserCollections(user(OWNER_ID), OWNER_ID, null, SIZE);

        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @DisplayName("컬렉션이 하나도 없으면 빈 목록과 커서 없음을 반환한다")
    @Test
    void returnsEmptyPage() {
        givenPage(List.of());

        CollectionsGetResponse response = application.getUserCollections(user(OWNER_ID), OWNER_ID, null, SIZE);

        assertThat(response.collections()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @DisplayName("받은 커서를 다음 조회 조건으로 그대로 넘겨 이미 본 컬렉션을 건너뛴다")
    @Test
    void passesReceivedCursorToQuery() {
        givenPage(List.of(row(13L, THIRD_CREATED)));
        String cursor = CollectionCursor.of(SECOND_CREATED, 12L).encode();

        application.getUserCollections(user(OWNER_ID), OWNER_ID, cursor, SIZE);

        ArgumentCaptor<CollectionCursor> captor = ArgumentCaptor.forClass(CollectionCursor.class);
        then(collectionQueryService).should()
                .findCollectionPreviewRows(anyLong(), anyBoolean(), captor.capture(), anyInt());
        assertThat(captor.getValue().collectionId()).isEqualTo(12L);
        assertThat(captor.getValue().createdDate()).isEqualTo(SECOND_CREATED);
    }

    @DisplayName("첫 페이지는 커서 없이 조회한다")
    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void firstPageQueriesWithoutCursor(String blankCursor) {
        givenPage(List.of(row(11L, FIRST_CREATED)));

        application.getUserCollections(user(OWNER_ID), OWNER_ID, blankCursor, SIZE);

        then(collectionQueryService).should()
                .findCollectionPreviewRows(anyLong(), anyBoolean(), eq(null), anyInt());
    }

    @DisplayName("이 API가 발급하지 않은 커서로는 조회하지 않는다")
    @Test
    void rejectsUnknownCursor() {
        assertThatThrownBy(() -> application.getUserCollections(user(OWNER_ID), OWNER_ID, "not-a-cursor", SIZE))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(INVALID_COLLECTION_CURSOR);

        then(collectionQueryService).shouldHaveNoInteractions();
    }

    @DisplayName("허용 범위를 벗어난 페이지 크기는 조회하기 전에 거부한다")
    @ParameterizedTest
    @ValueSource(ints = {0, -1, 101})
    void rejectsPageSizeOutOfRange(int size) {
        // 페이지 크기는 조회자를 확인하기 전에 검증하므로 사용자 정보를 준비할 필요가 없다.
        User viewer = mock(User.class);

        assertThatThrownBy(() -> application.getUserCollections(viewer, OWNER_ID, null, size))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(INVALID_COLLECTION_PAGE_SIZE);

        then(collectionQueryService).shouldHaveNoInteractions();
        then(userService).shouldHaveNoInteractions();
    }

    @DisplayName("마이페이지 미리보기 크기로도 같은 목록 API를 그대로 호출할 수 있다")
    @Test
    void supportsMyPagePreviewSize() {
        givenPage(List.of(row(11L, FIRST_CREATED), row(12L, SECOND_CREATED), row(13L, THIRD_CREATED)));
        given(collectionQueryService.countVisibleCollections(OWNER_ID, true)).willReturn(10L);

        CollectionsGetResponse response = application.getUserCollections(user(OWNER_ID), OWNER_ID, null, 3);

        assertThat(response.collections()).hasSize(3);
        assertThat(response.collectionsCount()).isEqualTo(10L);
    }

    // 목록 조회: 전체 개수와 미리보기

    @DisplayName("전체 개수는 페이지 크기가 아니라 조회 가능한 컬렉션 수를 그대로 반환한다")
    @Test
    void returnsTotalVisibleCount() {
        givenPage(List.of(row(11L, FIRST_CREATED)));
        given(collectionQueryService.countVisibleCollections(OWNER_ID, true)).willReturn(37L);

        CollectionsGetResponse response = application.getUserCollections(user(OWNER_ID), OWNER_ID, null, SIZE);

        assertThat(response.collectionsCount()).isEqualTo(37L);
    }

    @DisplayName("이번 페이지 컬렉션의 최근 추가 작품을 한 번의 조회로 가져와 카드에 채운다")
    @Test
    void fillsRecentNovelPreviewsWithSingleQuery() {
        givenPage(List.of(row(11L, FIRST_CREATED), row(12L, SECOND_CREATED)));
        given(collectionQueryService.findRecentNovelPreviews(List.of(11L, 12L), NOVEL_PREVIEW_SIZE))
                .willReturn(Map.of(11L, List.of(novelPreview(7L))));

        CollectionsGetResponse response = application.getUserCollections(user(OWNER_ID), OWNER_ID, null, SIZE);

        assertThat(response.collections().get(0).recentNovels())
                .extracting(CollectionNovelPreviewGetResponse::novelId)
                .containsExactly(7L);
        assertThat(response.collections().get(1).recentNovels()).isEmpty();
        then(collectionQueryService).should().findRecentNovelPreviews(List.of(11L, 12L), NOVEL_PREVIEW_SIZE);
    }

    @DisplayName("미리보기는 초과분 컬렉션까지 조회하지 않는다")
    @Test
    void doesNotPreviewTrimmedRow() {
        givenPage(List.of(row(11L, FIRST_CREATED), row(12L, SECOND_CREATED), row(13L, THIRD_CREATED)));

        application.getUserCollections(user(OWNER_ID), OWNER_ID, null, SIZE);

        then(collectionQueryService).should().findRecentNovelPreviews(List.of(11L, 12L), NOVEL_PREVIEW_SIZE);
    }

    @DisplayName("카드는 대표 작품과 포함 작품 수를 함께 제공한다")
    @Test
    void cardCarriesRepresentativeNovelAndCount() {
        givenPage(List.of(row(11L, FIRST_CREATED)));

        CollectionsGetResponse response = application.getUserCollections(user(OWNER_ID), OWNER_ID, null, SIZE);

        CollectionPreviewGetResponse card = response.collections().get(0);
        assertThat(card.representativeNovel().novelId()).isEqualTo(5L);
        assertThat(card.novelCount()).isEqualTo(12L);
    }

    // 상세 조회

    @DisplayName("소유자는 자신의 비공개 컬렉션 상세를 조회할 수 있다")
    @Test
    void ownerReadsPrivateCollection() {
        givenDetail(detailRow(false));
        givenNovels(List.of(novel(7L)));

        CollectionGetResponse response = application.getCollection(user(OWNER_ID), COLLECTION_ID, null);

        assertThat(response.isMyCollection()).isTrue();
        assertThat(response.isPublic()).isFalse();
        assertThat(response.novels()).extracting(CollectionNovelGetResponse::novelId).containsExactly(7L);
    }

    @DisplayName("다른 사용자는 비공개 컬렉션 상세를 조회할 수 없다")
    @Test
    void visitorCannotReadPrivateCollection() {
        givenDetail(detailRow(false));

        assertThatThrownBy(() -> application.getCollection(user(VISITOR_ID), COLLECTION_ID, null))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(PRIVATE_COLLECTION_ACCESS);

        then(collectionQueryService).should(never()).findCollectionNovels(anyLong(), any());
    }

    @DisplayName("비로그인 사용자는 공유 링크로 공개 컬렉션 상세를 조회할 수 있다")
    @Test
    void anonymousReadsPublicCollection() {
        givenDetail(detailRow(true));
        givenNovels(List.of(novel(7L)));

        CollectionGetResponse response = application.getCollection(null, COLLECTION_ID, null);

        assertThat(response.isMyCollection()).isFalse();
        assertThat(response.novels()).hasSize(1);
    }

    @DisplayName("비로그인 사용자는 비공개 컬렉션 상세를 조회할 수 없다")
    @Test
    void anonymousCannotReadPrivateCollection() {
        givenDetail(detailRow(false));

        assertThatThrownBy(() -> application.getCollection(null, COLLECTION_ID, null))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(PRIVATE_COLLECTION_ACCESS);
    }

    @DisplayName("비로그인 조회는 차단 관계가 없으므로 공개 상세 조회를 막지 않는다")
    @Test
    void anonymousDetailPassesBlockCheck() {
        givenDetail(detailRow(true));
        givenNovels(List.of());

        assertThatCode(() -> application.getCollection(null, COLLECTION_ID, null)).doesNotThrowAnyException();

        then(blockService).should().validateNotBlocked(null, OWNER_ID);
    }

    @DisplayName("어느 방향이든 차단 관계면 공개 컬렉션 상세도 조회할 수 없다")
    @Test
    void blockedVisitorCannotReadPublicCollection() {
        givenDetail(detailRow(true));
        willThrow(new CustomBlockException(BLOCKED_USER_ACCESS, "blocked"))
                .given(blockService).validateNotBlocked(VISITOR_ID, OWNER_ID);

        assertThatThrownBy(() -> application.getCollection(user(VISITOR_ID), COLLECTION_ID, null))
                .isInstanceOf(CustomBlockException.class)
                .extracting(exception -> ((CustomBlockException) exception).getICustomError())
                .isEqualTo(BLOCKED_USER_ACCESS);

        then(collectionQueryService).should(never()).findCollectionNovels(anyLong(), any());
    }

    @DisplayName("존재하지 않는 컬렉션 상세는 조회할 수 없다")
    @Test
    void detailRejectsUnknownCollection() {
        willThrow(new CustomCollectionException(COLLECTION_NOT_FOUND, "not found"))
                .given(collectionQueryService).getCollectionDetailRowOrException(COLLECTION_ID);

        assertThatThrownBy(() -> application.getCollection(user(OWNER_ID), COLLECTION_ID, null))
                .isInstanceOf(CustomCollectionException.class)
                .extracting(exception -> ((CustomCollectionException) exception).getICustomError())
                .isEqualTo(COLLECTION_NOT_FOUND);
    }

    @DisplayName("상세 작품은 요청한 추가 시점 정렬 기준으로 조회한다")
    @ParameterizedTest
    @ValueSource(strings = {"RECENT", "OLD"})
    void detailAppliesRequestedSortCriteria(String criteria) {
        SortCriteria sortCriteria = SortCriteria.of(criteria);
        givenDetail(detailRow(true));
        givenNovels(List.of());

        application.getCollection(user(OWNER_ID), COLLECTION_ID, sortCriteria);

        then(collectionQueryService).should().findCollectionNovels(COLLECTION_ID, sortCriteria);
    }

    @DisplayName("정렬 기준을 생략하면 기본 정렬로 조회한다")
    @Test
    void detailUsesDefaultSortWhenOmitted() {
        givenDetail(detailRow(true));
        givenNovels(List.of());

        application.getCollection(user(OWNER_ID), COLLECTION_ID, null);

        then(collectionQueryService).should().findCollectionNovels(COLLECTION_ID, null);
    }

    @DisplayName("상세는 작품 수를 조회한 작품 목록에서 그대로 계산한다")
    @Test
    void detailCountsNovelsFromLoadedList() {
        givenDetail(detailRow(true));
        givenNovels(List.of(novel(7L), novel(8L)));

        CollectionGetResponse response = application.getCollection(user(OWNER_ID), COLLECTION_ID, null);

        assertThat(response.novelCount()).isEqualTo(2);
    }

    private void givenPage(List<CollectionPreviewRow> rows) {
        given(collectionQueryService.findCollectionPreviewRows(anyLong(), anyBoolean(), any(), anyInt()))
                .willReturn(rows);
    }

    private void givenDetail(CollectionDetailRow detail) {
        given(collectionQueryService.getCollectionDetailRowOrException(COLLECTION_ID)).willReturn(detail);
    }

    private void givenNovels(List<CollectionNovelGetResponse> novels) {
        given(collectionQueryService.findCollectionNovels(eq(COLLECTION_ID), any())).willReturn(novels);
    }

    private CollectionPreviewRow row(Long collectionId, LocalDateTime createdDate) {
        return new CollectionPreviewRow(collectionId, "취향 저격 로판", "여주가 강한 로맨스 판타지", true,
                createdDate, 12L, 5L, "대표 작품", "https://image/5.png");
    }

    private CollectionDetailRow detailRow(boolean isPublic) {
        return new CollectionDetailRow(COLLECTION_ID, "취향 저격 로판", "여주가 강한 로맨스 판타지", isPublic,
                OWNER_ID, "웹소소", "https://image/avatar.png", 5L);
    }

    private CollectionNovelPreviewGetResponse novelPreview(Long novelId) {
        return new CollectionNovelPreviewGetResponse(novelId, "작품", "https://image/novel.png");
    }

    private CollectionNovelGetResponse novel(Long novelId) {
        return new CollectionNovelGetResponse(novelId, "작품", "작가", "https://image/novel.png", true, 4.5f, 10L);
    }

    private User user(Long userId) {
        User user = mock(User.class);
        given(user.getUserId()).willReturn(userId);
        return user;
    }
}
