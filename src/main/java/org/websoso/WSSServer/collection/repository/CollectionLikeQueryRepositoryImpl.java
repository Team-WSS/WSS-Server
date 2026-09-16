package org.websoso.WSSServer.collection.repository;

import static org.websoso.WSSServer.collection.domain.QCollection.collection;
import static org.websoso.WSSServer.collection.domain.QCollectionLike.collectionLike;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.collection.domain.CollectionLikeCursor;
import org.websoso.WSSServer.collection.domain.QCollectionLike;
import org.websoso.WSSServer.collection.domain.QCollectionNovel;
import org.websoso.WSSServer.collection.repository.projection.LikedCollectionRow;
import org.websoso.WSSServer.novel.domain.QNovel;

@Repository
@RequiredArgsConstructor
public class CollectionLikeQueryRepositoryImpl implements CollectionLikeQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<LikedCollectionRow> findLikedCollectionRows(Long viewerId, List<Long> blockedUserIds,
                                                            CollectionLikeCursor cursor, int limit) {
        QNovel representativeNovel = new QNovel("representativeNovel");

        return jpaQueryFactory
                .select(Projections.constructor(
                        LikedCollectionRow.class,
                        collectionLike.collectionLikeId,
                        collectionLike.createdDate,
                        collection.collectionId,
                        collection.name,
                        collection.description,
                        collection.isPublic,
                        novelCount(),
                        likeCount(),
                        representativeNovel.novelId,
                        representativeNovel.title,
                        representativeNovel.novelImage,
                        representativeNovel.author
                ))
                .from(collectionLike)
                .join(collectionLike.collection, collection)
                .leftJoin(representativeNovel)
                .on(collection.representativeNovelId.eq(representativeNovel.novelId))
                .where(
                        collectionLike.userId.eq(viewerId),
                        visibleToViewer(viewerId),
                        notOwnedByBlockedUser(blockedUserIds),
                        afterCursor(cursor)
                )
                .orderBy(collectionLike.createdDate.desc(), collectionLike.collectionLikeId.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public long countLikedCollections(Long viewerId, List<Long> blockedUserIds) {
        Long count = jpaQueryFactory
                .select(collectionLike.collectionLikeId.count())
                .from(collectionLike)
                .join(collectionLike.collection, collection)
                .where(
                        collectionLike.userId.eq(viewerId),
                        visibleToViewer(viewerId),
                        notOwnedByBlockedUser(blockedUserIds)
                )
                .fetchOne();

        return count == null ? 0L : count;
    }

    /**
     * 카드에 표시할 포함 작품 수. 목록 쿼리 안의 서브 쿼리이므로 컬렉션 수만큼 쿼리가 늘지 않는다.
     */
    private JPQLQuery<Long> novelCount() {
        QCollectionNovel novelCountSub = new QCollectionNovel("novelCountSub");

        return JPAExpressions
                .select(novelCountSub.collectionNovelId.count())
                .from(novelCountSub)
                .where(novelCountSub.collection.collectionId.eq(collection.collectionId));
    }

    /**
     * 카드에 표시할 좋아요 수. 조회자 본인의 좋아요만이 아니라 그 컬렉션이 받은 전체 좋아요 수다.
     * 컬렉션이 비공개로 바뀌어도 좋아요 데이터를 지우지 않으므로 이 값은 공개 여부와 무관하게 유지된다.
     */
    private JPQLQuery<Long> likeCount() {
        QCollectionLike likeCountSub = new QCollectionLike("likeCountSub");

        return JPAExpressions
                .select(likeCountSub.collectionLikeId.count())
                .from(likeCountSub)
                .where(likeCountSub.collection.collectionId.eq(collection.collectionId));
    }

    /**
     * 본인이 만든 비공개 컬렉션은 본인 목록에 그대로 노출하고, 다른 사용자의 비공개 컬렉션만 숨긴다.
     * 좋아요를 누른 뒤 컬렉션이 비공개로 바뀌어도 좋아요 자체는 남아 있으므로 목록 조회 시점에 걸러 낸다.
     */
    private BooleanExpression visibleToViewer(Long viewerId) {
        return collection.isPublic.isTrue().or(collection.user.userId.eq(viewerId));
    }

    /**
     * 어느 방향이든 차단 관계인 사용자가 만든 컬렉션은 숨긴다. 목록 전체를 거부하는 사용자별 컬렉션 목록과 달리
     * 여러 사용자의 컬렉션이 섞이는 목록이므로 해당 컬렉션만 걸러 낸다.
     */
    private BooleanExpression notOwnedByBlockedUser(List<Long> blockedUserIds) {
        if (blockedUserIds == null || blockedUserIds.isEmpty()) {
            return null;
        }

        return collection.user.userId.notIn(blockedUserIds);
    }

    /**
     * 커서보다 뒤에 오는 좋아요만 고른다. 정렬이 (좋아요 시점 내림차순, 좋아요 식별자 내림차순)이므로
     * 좋아요 시점이 더 이르거나, 같으면 식별자가 더 작은 좋아요가 다음 페이지다.
     * 커서 값 자체를 조건에 넣으므로 커서로 쓰던 좋아요가 취소돼도 페이지가 어긋나지 않는다.
     */
    private BooleanExpression afterCursor(CollectionLikeCursor cursor) {
        if (cursor == null) {
            return null;
        }

        return collectionLike.createdDate.lt(cursor.likedDate())
                .or(collectionLike.createdDate.eq(cursor.likedDate())
                        .and(collectionLike.collectionLikeId.lt(cursor.collectionLikeId())));
    }
}
