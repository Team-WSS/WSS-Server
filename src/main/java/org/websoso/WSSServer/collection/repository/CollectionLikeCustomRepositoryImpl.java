package org.websoso.WSSServer.collection.repository;

import static org.websoso.WSSServer.collection.domain.QCollectionLike.collectionLike;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CollectionLikeCustomRepositoryImpl implements CollectionLikeCustomRepository {

    /**
     * 좋아요 등록 upsert. 테이블명·컬럼명·유니크 제약조건은 14.3절의 `collection_like` 정의와 같아야 한다.
     * <p>
     * 중복이면 {@code ON DUPLICATE KEY UPDATE}가 PK를 자기 자신으로 대입하는 no-op을 실행한다.
     * 기존 행의 {@code created_date}와 {@code modified_date}를 건드리지 않으므로 좋아요 시점이 보존된다.
     * <p>
     * {@code INSERT IGNORE}를 쓰지 않는다. 그 구문은 유니크 위반뿐 아니라 외래 키 위반을 포함한 다른
     * 무결성 오류까지 경고로 낮춰 삼키므로, 사라진 컬렉션에 대한 좋아요가 조용히 성공으로 끝난다.
     * <p>
     * 시각은 애플리케이션이 아니라 DB의 {@code NOW(6)}로 채운다. 감사 필드를 채우는
     * {@code AuditingEntityListener}는 영속성 컨텍스트를 거치는 저장에만 동작하기 때문이다.
     */
    private static final String UPSERT_LIKE_SQL = """
            INSERT INTO collection_like (user_id, collection_id, created_date, modified_date)
            VALUES (?, ?, NOW(6), NOW(6))
            ON DUPLICATE KEY UPDATE collection_like_id = collection_like_id
            """;

    private final JPAQueryFactory jpaQueryFactory;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void upsertLike(Long userId, Long collectionId) {
        jdbcTemplate.update(UPSERT_LIKE_SQL, userId, collectionId);
    }

    @Override
    public long deleteLike(Long userId, Long collectionId) {
        return jpaQueryFactory
                .delete(collectionLike)
                .where(
                        collectionLike.userId.eq(userId),
                        collectionLike.collection.collectionId.eq(collectionId)
                )
                .execute();
    }

    @Override
    public long deleteAllByCollectionId(Long collectionId) {
        return jpaQueryFactory
                .delete(collectionLike)
                .where(collectionLike.collection.collectionId.eq(collectionId))
                .execute();
    }
}
