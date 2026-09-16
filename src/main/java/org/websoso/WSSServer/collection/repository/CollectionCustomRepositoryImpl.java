package org.websoso.WSSServer.collection.repository;

import static org.websoso.WSSServer.collection.domain.QCollection.collection;
import static org.websoso.WSSServer.collection.domain.QCollectionNovel.collectionNovel;
import static org.websoso.WSSServer.novel.domain.QNovel.novel;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.collection.domain.Collection;

@Repository
@RequiredArgsConstructor
public class CollectionCustomRepositoryImpl implements CollectionCustomRepository {

    /**
     * 탈퇴한 사용자를 대신하는 알 수 없는 사용자 PK. 피드·댓글 작성자 익명화와 같은 값을 사용한다.
     */
    private static final long UNKNOWN_USER_ID = -1L;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Collection> findByIdForUpdate(Long collectionId) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(collection)
                .where(collection.collectionId.eq(collectionId))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne());
    }

    @Override
    public Optional<Collection> findByIdWithNovels(Long collectionId) {
        // 포함 작품을 fetch join하면 컬렉션 하나가 작품 수만큼의 행으로 돌아오므로
        // 중복을 제거한 결과의 첫 원소를 사용한다.
        return jpaQueryFactory
                .selectFrom(collection)
                .distinct()
                .leftJoin(collection.collectionNovels, collectionNovel).fetchJoin()
                .leftJoin(collectionNovel.novel, novel).fetchJoin()
                .where(collection.collectionId.eq(collectionId))
                .fetch()
                .stream()
                .findFirst();
    }

    @Override
    public void updateOwnerToUnknown(Long userId) {
        jpaQueryFactory
                .update(collection)
                .set(collection.user.userId, UNKNOWN_USER_ID)
                .where(collection.user.userId.eq(userId))
                .execute();
    }
}
