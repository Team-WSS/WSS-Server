package org.websoso.WSSServer.collection.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.websoso.WSSServer.collection.domain.QCollectionNovel.collectionNovel;
import static org.websoso.WSSServer.domain.common.SortCriteria.OLD;
import static org.websoso.WSSServer.domain.common.SortCriteria.RECENT;
import static org.websoso.WSSServer.novel.domain.QNovel.novel;
import static org.websoso.WSSServer.user.domain.QAvatarProfile.avatarProfile;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
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
import org.websoso.WSSServer.collection.domain.CollectionCursor;
import org.websoso.WSSServer.collection.repository.projection.CollectionDetailRow;
import org.websoso.WSSServer.domain.common.SortCriteria;

/**
 * 실제 조회 결과와 인덱스 사용 여부는 DB 없이 확인할 수 없다. 여기서는 각 조회가 의도한 조건과 정렬,
 * 읽는 행 수로 조립되는지, 그리고 컬렉션 수에 비례해 쿼리를 반복하지 않는지를 확인한다.
 */
@SuppressWarnings("unchecked")
class CollectionQueryRepositoryImplTest {

    private static final long OWNER_ID = 1L;
    private static final long COLLECTION_ID = 100L;
    private static final int LIMIT = 11;
    private static final int PREVIEW_SIZE = 5;
    private static final LocalDateTime CURSOR_CREATED_DATE = LocalDateTime.of(2025, 1, 2, 3, 4, 5);

    private final JPAQueryFactory jpaQueryFactory = mock(JPAQueryFactory.class);
    private final CollectionQueryRepositoryImpl repository = new CollectionQueryRepositoryImpl(jpaQueryFactory);

    @DisplayName("목록은 최초 생성 시점 내림차순으로 읽고 식별자로 순서를 확정한다")
    @Test
    void listOrdersByCreatedDateThenId() {
        JPAQuery<Object> query = givenSelectQuery(List.of());

        repository.findCollectionPreviewRows(OWNER_ID, true, null, LIMIT);

        List<OrderSpecifier<?>> orders = capturedOrders(query);
        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getOrder()).isEqualTo(Order.DESC);
        assertThat(orders.get(0).getTarget()).hasToString("collection.createdDate");
        assertThat(orders.get(1).getOrder()).isEqualTo(Order.DESC);
        assertThat(orders.get(1).getTarget()).hasToString("collection.collectionId");
    }

    @DisplayName("목록은 요청받은 행 수만큼만 읽는다")
    @Test
    void listReadsRequestedLimit() {
        JPAQuery<Object> query = givenSelectQuery(List.of());

        repository.findCollectionPreviewRows(OWNER_ID, true, null, LIMIT);

        then(query).should().limit(LIMIT);
    }

    @DisplayName("본인 목록은 공개 여부로 거르지 않는다")
    @Test
    void listKeepsPrivateCollectionsForOwner() {
        JPAQuery<Object> query = givenSelectQuery(List.of());

        repository.findCollectionPreviewRows(OWNER_ID, true, null, LIMIT);

        assertThat(capturedConditions(query)).doesNotContain("isPublic");
    }

    @DisplayName("다른 사용자의 목록은 공개 컬렉션만 읽는다")
    @Test
    void listFiltersPrivateCollectionsForVisitor() {
        JPAQuery<Object> query = givenSelectQuery(List.of());

        repository.findCollectionPreviewRows(OWNER_ID, false, null, LIMIT);

        assertThat(capturedConditions(query)).contains("collection.isPublic = true");
    }

    @DisplayName("목록은 항상 요청한 사용자의 컬렉션만 읽는다")
    @Test
    void listFiltersByOwner() {
        JPAQuery<Object> query = givenSelectQuery(List.of());

        repository.findCollectionPreviewRows(OWNER_ID, true, null, LIMIT);

        assertThat(capturedConditions(query)).contains("collection.user.userId = " + OWNER_ID);
    }

    @DisplayName("첫 페이지는 커서 조건 없이 읽는다")
    @Test
    void firstPageHasNoCursorCondition() {
        JPAQuery<Object> query = givenSelectQuery(List.of());

        repository.findCollectionPreviewRows(OWNER_ID, true, null, LIMIT);

        assertThat(capturedConditions(query)).doesNotContain("createdDate <");
    }

    @DisplayName("커서 조건은 생성 시점과 식별자를 함께 비교해 중복과 누락을 막는다")
    @Test
    void cursorComparesCreatedDateAndIdTogether() {
        JPAQuery<Object> query = givenSelectQuery(List.of());

        repository.findCollectionPreviewRows(OWNER_ID, true, CollectionCursor.of(CURSOR_CREATED_DATE, 42L), LIMIT);

        String conditions = capturedConditions(query);
        assertThat(conditions).contains("collection.createdDate < " + CURSOR_CREATED_DATE);
        assertThat(conditions).contains("collection.createdDate = " + CURSOR_CREATED_DATE);
        assertThat(conditions).contains("collection.collectionId < 42");
    }

    @DisplayName("전체 개수도 목록과 같은 공개 범위로 센다")
    @Test
    void countUsesSameVisibilityAsList() {
        JPAQuery<Object> query = givenSelectQuery(0L);

        repository.countVisibleCollections(OWNER_ID, false);

        assertThat(capturedConditions(query)).contains("collection.isPublic = true");
    }

    @DisplayName("전체 개수 조회는 페이지 크기와 무관하므로 행 수를 제한하지 않는다")
    @Test
    void countDoesNotLimitRows() {
        JPAQuery<Object> query = givenSelectQuery(3L);

        assertThat(repository.countVisibleCollections(OWNER_ID, true)).isEqualTo(3L);

        then(query).should(never()).limit(anyLong());
    }

    @DisplayName("셀 컬렉션이 없으면 0을 반환한다")
    @Test
    void countReturnsZeroWhenNoRows() {
        givenSelectQuery((Object) null);

        assertThat(repository.countVisibleCollections(OWNER_ID, true)).isZero();
    }

    @DisplayName("미리보기는 여러 컬렉션을 한 번의 쿼리로 읽는다")
    @Test
    void previewReadsAllCollectionsInOneQuery() {
        JPAQuery<Object> query = givenSelectQuery(List.of());

        repository.findRecentNovelPreviewRows(List.of(11L, 12L, 13L), PREVIEW_SIZE);

        then(jpaQueryFactory).should().select(any(Expression.class));
        assertThat(capturedConditions(query)).contains("collectionNovel.collection.collectionId in [11, 12, 13]");
    }

    @DisplayName("미리보기는 컬렉션마다 최근 추가 작품 상위 N개만 읽는다")
    @Test
    void previewKeepsOnlyRecentNovelsPerCollection() {
        JPAQuery<Object> query = givenSelectQuery(List.of());

        repository.findRecentNovelPreviewRows(List.of(11L), PREVIEW_SIZE);

        assertThat(capturedConditions(query)).contains("< " + PREVIEW_SIZE);
    }

    @DisplayName("미리보기는 컬렉션 안에서 최근 추가 순으로 읽는다")
    @Test
    void previewOrdersByRecentlyAdded() {
        JPAQuery<Object> query = givenSelectQuery(List.of());

        repository.findRecentNovelPreviewRows(List.of(11L), PREVIEW_SIZE);

        List<OrderSpecifier<?>> orders = capturedOrders(query);
        assertThat(orders).hasSize(3);
        assertThat(orders.get(1).getOrder()).isEqualTo(Order.DESC);
        assertThat(orders.get(1).getTarget()).hasToString("collectionNovel.createdDate");
        assertThat(orders.get(2).getOrder()).isEqualTo(Order.DESC);
        assertThat(orders.get(2).getTarget()).hasToString("collectionNovel.collectionNovelId");
    }

    @DisplayName("미리보기할 컬렉션이 없으면 쿼리를 실행하지 않는다")
    @Test
    void previewSkipsQueryWhenNoCollections() {
        assertThat(repository.findRecentNovelPreviewRows(List.of(), PREVIEW_SIZE)).isEmpty();

        then(jpaQueryFactory).shouldHaveNoInteractions();
    }

    @DisplayName("상세는 해당 컬렉션 한 건만 읽는다")
    @Test
    void detailReadsSingleCollection() {
        JPAQuery<Object> query = givenSelectQuery(detailRow());

        repository.findCollectionDetailRow(COLLECTION_ID);

        assertThat(capturedCondition(query)).contains("collection.collectionId = " + COLLECTION_ID);
        then(query).should().fetchOne();
    }

    @DisplayName("상세 대상이 없으면 빈 값을 반환한다")
    @Test
    void detailReturnsEmptyWhenMissing() {
        givenSelectQuery((Object) null);

        assertThat(repository.findCollectionDetailRow(COLLECTION_ID)).isEmpty();
    }

    /**
     * 아바타는 도메인상 필수이므로 outer join으로 읽으면 안 된다. outer join은 아바타가 없는 사용자를 정상으로
     * 취급해 응답의 {@code owner.avatarImage}를 조용히 비우고, 그 상태를 클라이언트가 처리해야 할 값으로 만든다.
     */
    @DisplayName("상세는 소유자 아바타를 inner join으로 읽는다")
    @Test
    void detailJoinsAvatarProfileWithInnerJoin() {
        JPAQuery<Object> query = givenSelectQuery(detailRow());

        repository.findCollectionDetailRow(COLLECTION_ID);

        then(query).should().join(avatarProfile);
        then(query).should(never()).leftJoin(any(EntityPath.class));
    }

    @DisplayName("상세 작품은 작품 통계를 조인하지 않는다")
    @Test
    void detailNovelsDoNotJoinStatistics() {
        JPAQuery<Object> query = givenSelectQuery(List.of());

        repository.findCollectionNovelRows(COLLECTION_ID, RECENT);

        then(query).should().join(collectionNovel.novel, novel);
        then(query).should(never()).leftJoin(any(EntityPath.class), any(Path.class));
    }

    @DisplayName("상세 작품은 기본적으로 추가된 시점 최신순으로 읽는다")
    @Test
    void detailNovelsDefaultToNewestFirst() {
        assertAddedOrder(RECENT, Order.DESC);
    }

    @DisplayName("정렬 기준을 생략해도 추가된 시점 최신순으로 읽는다")
    @Test
    void detailNovelsFallBackToNewestFirst() {
        assertAddedOrder(null, Order.DESC);
    }

    @DisplayName("오래된순을 요청하면 추가된 시점 오름차순으로 읽는다")
    @Test
    void detailNovelsSupportOldestFirst() {
        assertAddedOrder(OLD, Order.ASC);
    }

    private void assertAddedOrder(SortCriteria sortCriteria, Order expected) {
        JPAQuery<Object> query = givenSelectQuery(List.of());

        repository.findCollectionNovelRows(COLLECTION_ID, sortCriteria);

        List<OrderSpecifier<?>> orders = capturedOrders(query);
        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getOrder()).isEqualTo(expected);
        assertThat(orders.get(0).getTarget()).hasToString("collectionNovel.createdDate");
        assertThat(orders.get(1).getOrder()).isEqualTo(expected);
        assertThat(orders.get(1).getTarget()).hasToString("collectionNovel.collectionNovelId");
        assertThat(capturedCondition(query)).contains("collectionNovel.collection.collectionId = " + COLLECTION_ID);
    }

    private CollectionDetailRow detailRow() {
        return new CollectionDetailRow(COLLECTION_ID, "취향 저격 로판", null, true, OWNER_ID, "웹소소",
                "https://image/avatar.png", 5L);
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

    /**
     * QueryDSL은 조건이 하나뿐인 {@code where}에 가변 인자가 아닌 단일 인자 메서드를 쓰므로 따로 확인한다.
     */
    private String capturedCondition(JPAQuery<Object> query) {
        ArgumentCaptor<Predicate> captor = ArgumentCaptor.forClass(Predicate.class);
        then(query).should().where(captor.capture());

        return captor.getValue().toString();
    }

    private List<OrderSpecifier<?>> capturedOrders(JPAQuery<Object> query) {
        ArgumentCaptor<OrderSpecifier<?>[]> captor = ArgumentCaptor.forClass(OrderSpecifier[].class);
        then(query).should().orderBy(captor.capture());

        return List.of(captor.getValue());
    }
}
