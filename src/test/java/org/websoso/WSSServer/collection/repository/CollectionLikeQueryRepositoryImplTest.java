package org.websoso.WSSServer.collection.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.websoso.WSSServer.collection.domain.CollectionLikeCursor;

/**
 * 실제 조회 결과와 인덱스 사용 여부는 DB 없이 확인할 수 없다. 여기서는 좋아요한 컬렉션 목록 조회가
 * 의도한 조건과 정렬, 읽는 행 수로 조립되는지를 확인한다.
 */
@SuppressWarnings("unchecked")
class CollectionLikeQueryRepositoryImplTest {

    private static final long VIEWER_ID = 1L;
    private static final List<Long> BLOCKED_USER_IDS = List.of(9L, 10L);
    private static final int LIMIT = 11;
    private static final LocalDateTime CURSOR_LIKED_DATE = LocalDateTime.of(2025, 1, 2, 3, 4, 5);

    private final JPAQueryFactory jpaQueryFactory = mock(JPAQueryFactory.class);
    private final CollectionLikeQueryRepositoryImpl repository =
            new CollectionLikeQueryRepositoryImpl(jpaQueryFactory);

    @DisplayName("목록은 좋아요한 시점 내림차순으로 읽고 좋아요 식별자로 순서를 확정한다")
    @Test
    void listOrdersByLikedDateThenLikeId() {
        JPAQuery<Object> query = givenSelectQuery(List.of());

        repository.findLikedCollectionRows(VIEWER_ID, List.of(), null, LIMIT);

        List<OrderSpecifier<?>> orders = capturedOrders(query);
        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getOrder()).isEqualTo(Order.DESC);
        assertThat(orders.get(0).getTarget()).hasToString("collectionLike.createdDate");
        assertThat(orders.get(1).getOrder()).isEqualTo(Order.DESC);
        assertThat(orders.get(1).getTarget()).hasToString("collectionLike.collectionLikeId");
    }

    @DisplayName("목록은 요청받은 행 수만큼만 읽는다")
    @Test
    void listReadsRequestedLimit() {
        JPAQuery<Object> query = givenSelectQuery(List.of());

        repository.findLikedCollectionRows(VIEWER_ID, List.of(), null, LIMIT);

        then(query).should().limit(LIMIT);
    }

    @DisplayName("목록은 조회자가 누른 좋아요만 읽는다")
    @Test
    void listReadsOnlyViewerLikes() {
        JPAQuery<Object> query = givenSelectQuery(List.of());

        repository.findLikedCollectionRows(VIEWER_ID, List.of(), null, LIMIT);

        assertThat(capturedConditions(query)).contains("collectionLike.userId = " + VIEWER_ID);
    }

    /**
     * 좋아요를 누른 뒤 컬렉션이 비공개로 바뀌어도 좋아요는 남아 있으므로, 지금 볼 수 있는지는 조회 시점에 가른다.
     * 본인이 만든 비공개 컬렉션은 그대로 노출하고 다른 사용자의 비공개 컬렉션만 숨긴다.
     */
    @DisplayName("공개 컬렉션과 본인이 만든 비공개 컬렉션만 읽는다")
    @Test
    void listKeepsOwnPrivateCollectionsOnly() {
        JPAQuery<Object> query = givenSelectQuery(List.of());

        repository.findLikedCollectionRows(VIEWER_ID, List.of(), null, LIMIT);

        assertThat(capturedConditions(query))
                .contains("collection.isPublic = true || collection.user.userId = " + VIEWER_ID);
    }

    @DisplayName("차단 관계인 사용자가 만든 컬렉션은 목록에서 뺀다")
    @Test
    void listExcludesCollectionsOwnedByBlockedUsers() {
        JPAQuery<Object> query = givenSelectQuery(List.of());

        repository.findLikedCollectionRows(VIEWER_ID, BLOCKED_USER_IDS, null, LIMIT);

        assertThat(capturedConditions(query)).contains("collection.user.userId not in [9, 10]");
    }

    @DisplayName("차단 관계인 사용자가 없으면 제외 조건을 넣지 않는다")
    @Test
    void listOmitsBlockConditionWhenNothingToExclude() {
        JPAQuery<Object> query = givenSelectQuery(List.of());

        repository.findLikedCollectionRows(VIEWER_ID, List.of(), null, LIMIT);

        assertThat(capturedConditions(query)).doesNotContain("not in", "!=");
    }

    @DisplayName("첫 페이지는 커서 조건 없이 읽는다")
    @Test
    void firstPageHasNoCursorCondition() {
        JPAQuery<Object> query = givenSelectQuery(List.of());

        repository.findLikedCollectionRows(VIEWER_ID, List.of(), null, LIMIT);

        assertThat(capturedConditions(query)).doesNotContain("createdDate <");
    }

    @DisplayName("커서 조건은 좋아요 시점과 좋아요 식별자를 함께 비교해 중복과 누락을 막는다")
    @Test
    void cursorComparesLikedDateAndLikeIdTogether() {
        JPAQuery<Object> query = givenSelectQuery(List.of());

        repository.findLikedCollectionRows(
                VIEWER_ID, List.of(), CollectionLikeCursor.of(CURSOR_LIKED_DATE, 42L), LIMIT);

        String conditions = capturedConditions(query);
        assertThat(conditions).contains("collectionLike.createdDate < " + CURSOR_LIKED_DATE);
        assertThat(conditions).contains("collectionLike.createdDate = " + CURSOR_LIKED_DATE);
        assertThat(conditions).contains("collectionLike.collectionLikeId < 42");
    }

    @DisplayName("전체 개수도 목록과 같은 공개 범위와 차단 조건으로 센다")
    @Test
    void countUsesSameVisibilityAsList() {
        JPAQuery<Object> query = givenSelectQuery(0L);

        repository.countLikedCollections(VIEWER_ID, BLOCKED_USER_IDS);

        String conditions = capturedConditions(query);
        assertThat(conditions).contains("collectionLike.userId = " + VIEWER_ID);
        assertThat(conditions).contains("collection.isPublic = true || collection.user.userId = " + VIEWER_ID);
        assertThat(conditions).contains("collection.user.userId not in [9, 10]");
    }

    @DisplayName("셀 결과가 없으면 전체 개수는 0이다")
    @Test
    void countReturnsZeroWhenNothingCounted() {
        givenSelectQuery((Object) null);

        assertThat(repository.countLikedCollections(VIEWER_ID, List.of())).isZero();
    }

    /**
     * 카드에 표시할 작품 수와 좋아요 수는 목록 쿼리 안의 서브 쿼리로 읽는다.
     * 컬렉션마다 따로 세면 목록 크기에 비례해 쿼리가 늘어난다.
     */
    @DisplayName("작품 수와 좋아요 수를 컬렉션마다 따로 조회하지 않는다")
    @Test
    void listCountsNovelsAndLikesInOneQuery() {
        givenSelectQuery(List.of());

        repository.findLikedCollectionRows(VIEWER_ID, List.of(), null, LIMIT);

        then(jpaQueryFactory).should(times(1)).select(any(Expression.class));
    }

    private JPAQuery<Object> givenSelectQuery(List<Object> rows) {
        JPAQuery<Object> query = newQuery();
        given(query.fetch()).willReturn(rows);
        return query;
    }

    private JPAQuery<Object> givenSelectQuery(Object singleResult) {
        JPAQuery<Object> query = newQuery();
        given(query.fetchOne()).willReturn(singleResult);
        return query;
    }

    private JPAQuery<Object> newQuery() {
        JPAQuery<Object> query = mock(JPAQuery.class, RETURNS_SELF);
        given(jpaQueryFactory.select(any(Expression.class))).willReturn(query);
        return query;
    }

    /**
     * {@code where}에 넘긴 조건을 문자열로 모아 준다. 비어 있는 조건은 QueryDSL이 무시하므로 함께 제외한다.
     */
    private String capturedConditions(JPAQuery<Object> query) {
        ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
        then(query).should().where(captor.capture());

        return Arrays.stream(captor.getValue())
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(" AND "));
    }

    private List<OrderSpecifier<?>> capturedOrders(JPAQuery<Object> query) {
        ArgumentCaptor<OrderSpecifier<?>[]> captor = ArgumentCaptor.forClass(OrderSpecifier[].class);
        then(query).should().orderBy(captor.capture());

        return List.of(captor.getValue());
    }
}
