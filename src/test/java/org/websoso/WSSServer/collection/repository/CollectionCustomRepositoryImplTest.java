package org.websoso.WSSServer.collection.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.websoso.WSSServer.collection.domain.QCollection.collection;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.websoso.WSSServer.collection.domain.Collection;

/**
 * 실제 잠금 동작과 fetch join 결과, 벌크 갱신이 실제로 바꾼 행 수는 DB 없이는 확인할 수 없다.
 * 여기서는 각 쿼리가 의도한 잠금 모드와 조건으로 조립되는지, 조회 전용 쿼리가 잠금을 걸지 않는지를 확인한다.
 */
@SuppressWarnings("unchecked")
class CollectionCustomRepositoryImplTest {

    private static final long COLLECTION_ID = 100L;
    private static final long USER_ID = 1L;
    private static final long UNKNOWN_USER_ID = -1L;

    private final JPAQueryFactory jpaQueryFactory = mock(JPAQueryFactory.class);
    private final CollectionCustomRepositoryImpl repository = new CollectionCustomRepositoryImpl(jpaQueryFactory);

    @DisplayName("수정·삭제 대상 조회는 쓰기 잠금을 건다")
    @Test
    void mutationLookupSetsPessimisticWrite() {
        JPAQuery<Collection> query = givenQuery();
        Collection found = mock(Collection.class);
        given(query.fetchOne()).willReturn(found);

        assertThat(repository.findByIdForUpdate(COLLECTION_ID)).contains(found);

        then(query).should().setLockMode(LockModeType.PESSIMISTIC_WRITE);
    }

    @DisplayName("수정·삭제 대상 조회는 컬렉션 루트만 읽고 포함 작품을 조인하지 않는다")
    @Test
    void mutationLookupDoesNotJoinNovels() {
        JPAQuery<Collection> query = givenQuery();
        given(query.fetchOne()).willReturn(mock(Collection.class));

        repository.findByIdForUpdate(COLLECTION_ID);

        then(jpaQueryFactory).should().selectFrom(collection);
        then(query).should(never()).fetchJoin();
        then(query).should(never()).distinct();
    }

    @DisplayName("수정·삭제 대상이 없으면 빈 값을 반환한다")
    @Test
    void mutationLookupReturnsEmptyWhenMissing() {
        JPAQuery<Collection> query = givenQuery();
        given(query.fetchOne()).willReturn(null);

        assertThat(repository.findByIdForUpdate(COLLECTION_ID)).isEmpty();
    }

    @DisplayName("포함 작품까지 읽는 조회는 잠금을 걸지 않고 fetch join으로 한 번에 가져온다")
    @Test
    void readLookupFetchesNovelsWithoutLock() {
        JPAQuery<Collection> query = givenQuery();
        Collection found = mock(Collection.class);
        given(query.fetch()).willReturn(List.of(found));

        assertThat(repository.findByIdWithNovels(COLLECTION_ID)).contains(found);

        then(query).should(never()).setLockMode(any());
        then(query).should().distinct();
        then(query).should(times(2)).fetchJoin();
    }

    @DisplayName("포함 작품까지 읽는 조회는 fetch join 중복 행에서 컬렉션 하나만 돌려준다")
    @Test
    void readLookupDeduplicatesJoinedRows() {
        JPAQuery<Collection> query = givenQuery();
        Collection found = mock(Collection.class);
        given(query.fetch()).willReturn(List.of(found, found, found));

        assertThat(repository.findByIdWithNovels(COLLECTION_ID)).contains(found);
    }

    @DisplayName("조회 결과가 없으면 빈 값을 반환한다")
    @Test
    void readLookupReturnsEmptyWhenMissing() {
        JPAQuery<Collection> query = givenQuery();
        given(query.fetch()).willReturn(List.of());

        assertThat(repository.findByIdWithNovels(COLLECTION_ID)).isEmpty();
    }

    @DisplayName("탈퇴 정리는 소유자 컬럼만 갱신하는 벌크 update를 실행한다")
    @Test
    void withdrawalCleanupExecutesOwnerUpdate() {
        JPAUpdateClause update = givenUpdateClause();

        repository.updateOwnerToUnknown(USER_ID);

        then(jpaQueryFactory).should().update(collection);
        then(update).should().set(collection.user.userId, UNKNOWN_USER_ID);
        then(update).should().where(collection.user.userId.eq(USER_ID));
        then(update).should().execute();
    }

    @DisplayName("탈퇴 정리는 컬렉션을 조회하거나 삭제하지 않는다")
    @Test
    void withdrawalCleanupNeitherLoadsNorDeletesCollections() {
        givenUpdateClause();

        repository.updateOwnerToUnknown(USER_ID);

        then(jpaQueryFactory).should(never()).selectFrom(collection);
        then(jpaQueryFactory).should(never()).delete(collection);
    }

    private JPAQuery<Collection> givenQuery() {
        JPAQuery<Collection> query = mock(JPAQuery.class, RETURNS_SELF);
        given(jpaQueryFactory.selectFrom(collection)).willReturn(query);
        return query;
    }

    private JPAUpdateClause givenUpdateClause() {
        JPAUpdateClause update = mock(JPAUpdateClause.class, RETURNS_SELF);
        given(jpaQueryFactory.update(collection)).willReturn(update);
        return update;
    }
}
