package org.websoso.WSSServer.collection.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 좋아요 등록은 중복을 DB가 흡수하는 upsert 한 문장으로, 취소와 컬렉션 삭제 정리는 지울 행을 엔티티로
 * 읽지 않는 한 번의 벌크 삭제로 끝낸다. 실제 실행 결과는 DB 없이 확인할 수 없으므로,
 * 여기서는 의도한 문장과 조건으로 조립되는지를 확인한다.
 */
class CollectionLikeCustomRepositoryImplTest {

    private static final long USER_ID = 1L;
    private static final long COLLECTION_ID = 100L;

    private final JPAQueryFactory jpaQueryFactory = mock(JPAQueryFactory.class);
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final CollectionLikeCustomRepositoryImpl repository =
            new CollectionLikeCustomRepositoryImpl(jpaQueryFactory, jdbcTemplate);

    // 등록

    /**
     * 테이블명과 컬럼명은 14.3절의 `collection_like` 정의와 정확히 같아야 한다.
     * JPQL과 달리 이 문장은 매핑을 거치지 않으므로, 이름이 어긋나도 애플리케이션이 뜰 때는 알 수 없다.
     */
    @DisplayName("좋아요 등록은 collection_like의 사용자·컬렉션과 생성·수정 시각을 넣는다")
    @Test
    void upsertLikeInsertsIntoCollectionLike() {
        repository.upsertLike(USER_ID, COLLECTION_ID);

        assertThat(normalizedSql())
                .isEqualTo("INSERT INTO collection_like (user_id, collection_id, created_date, modified_date) "
                        + "VALUES (?, ?, NOW(6), NOW(6)) "
                        + "ON DUPLICATE KEY UPDATE collection_like_id = collection_like_id");
    }

    @DisplayName("좋아요 등록은 사용자, 컬렉션 순서로 값을 바인딩한다")
    @Test
    void upsertLikeBindsUserBeforeCollection() {
        repository.upsertLike(USER_ID, COLLECTION_ID);

        assertThat(capturedArguments()).containsExactly(USER_ID, COLLECTION_ID);
    }

    /**
     * 중복이면 PK를 자기 자신으로 대입하는 no-op이 실행된다. 기존 행의 `created_date`를 건드리지 않으므로
     * 같은 요청을 반복해도 좋아요한 컬렉션 목록의 순서가 흔들리지 않는다.
     */
    @DisplayName("중복 좋아요는 기존 행의 시각을 바꾸지 않는 no-op으로 흡수한다")
    @Test
    void upsertLikeKeepsExistingRowUntouched() {
        repository.upsertLike(USER_ID, COLLECTION_ID);

        String sql = normalizedSql();
        assertThat(sql).contains("ON DUPLICATE KEY UPDATE collection_like_id = collection_like_id");
        assertThat(sql).doesNotContain("created_date =");
        assertThat(sql).doesNotContain("modified_date =");
    }

    /**
     * `INSERT IGNORE`는 유니크 위반뿐 아니라 외래 키 위반까지 경고로 낮춰 삼킨다.
     * 사라진 컬렉션에 대한 좋아요가 조용히 성공으로 끝나므로 404로 알릴 수 없게 된다.
     */
    @DisplayName("다른 무결성 오류까지 숨기는 INSERT IGNORE를 쓰지 않는다")
    @Test
    void upsertLikeDoesNotUseInsertIgnore() {
        repository.upsertLike(USER_ID, COLLECTION_ID);

        assertThat(normalizedSql().toUpperCase()).doesNotContain("INSERT IGNORE");
    }

    /**
     * 갱신 행 수는 커넥터 설정(`CLIENT_FOUND_ROWS`)에 따라 중복에서 0이 되기도 1이 되기도 한다.
     * 그 값을 결과로 내보내면 호출하는 쪽이 설정에 따라 달라지는 값을 판단 근거로 삼게 된다.
     */
    @DisplayName("좋아요 등록은 갱신 행 수를 결과로 내보내지 않는다")
    @Test
    void upsertLikeReturnsNothing() throws NoSuchMethodException {
        assertThat(CollectionLikeCustomRepository.class
                .getMethod("upsertLike", Long.class, Long.class)
                .getReturnType())
                .isEqualTo(void.class);
    }

    // 취소·정리

    @DisplayName("좋아요 취소는 사용자와 컬렉션을 함께 조건으로 건다")
    @Test
    void deleteLikeFiltersByUserAndCollection() {
        JPADeleteClause clause = givenDeleteClause(1L);

        repository.deleteLike(USER_ID, COLLECTION_ID);

        String conditions = capturedConditions(clause);
        assertThat(conditions).contains("collectionLike.userId = " + USER_ID);
        assertThat(conditions).contains("collectionLike.collection.collectionId = " + COLLECTION_ID);
    }

    @DisplayName("좋아요 취소는 지운 행 수를 그대로 돌려준다")
    @Test
    void deleteLikeReturnsDeletedRowCount() {
        givenDeleteClause(1L);

        assertThat(repository.deleteLike(USER_ID, COLLECTION_ID)).isEqualTo(1L);
    }

    @DisplayName("이미 취소된 좋아요는 지울 행이 없다")
    @Test
    void deleteLikeReturnsZeroWhenNothingToDelete() {
        givenDeleteClause(0L);

        assertThat(repository.deleteLike(USER_ID, COLLECTION_ID)).isZero();
    }

    @DisplayName("컬렉션 정리는 그 컬렉션의 좋아요만 조건으로 걸어 한 번에 지운다")
    @Test
    void deleteAllByCollectionIdFiltersByCollectionOnly() {
        JPADeleteClause clause = givenDeleteClause(3L);

        assertThat(repository.deleteAllByCollectionId(COLLECTION_ID)).isEqualTo(3L);

        assertThat(capturedCondition(clause))
                .isEqualTo("collectionLike.collection.collectionId = " + COLLECTION_ID);
    }

    private String normalizedSql() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        then(jdbcTemplate).should().update(captor.capture(), any(Object[].class));

        return captor.getValue().replaceAll("\\s+", " ").trim();
    }

    private Object[] capturedArguments() {
        ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);
        then(jdbcTemplate).should().update(any(String.class), captor.capture());

        return captor.getValue();
    }

    private JPADeleteClause givenDeleteClause(long deletedRows) {
        JPADeleteClause clause = mock(JPADeleteClause.class, RETURNS_SELF);
        given(jpaQueryFactory.delete(any(EntityPath.class))).willReturn(clause);
        given(clause.execute()).willReturn(deletedRows);
        return clause;
    }

    private String capturedConditions(JPADeleteClause clause) {
        ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
        then(clause).should().where(captor.capture());

        return Arrays.stream(captor.getValue())
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(" AND "));
    }

    /**
     * QueryDSL은 조건이 하나뿐인 {@code where}에 가변 인자가 아닌 단일 인자 메서드를 쓰므로 따로 확인한다.
     */
    private String capturedCondition(JPADeleteClause clause) {
        ArgumentCaptor<Predicate> captor = ArgumentCaptor.forClass(Predicate.class);
        then(clause).should().where(captor.capture());

        return captor.getValue().toString();
    }
}
