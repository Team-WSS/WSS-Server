package org.websoso.WSSServer.collection.repository;

import static org.websoso.WSSServer.collection.domain.QCollection.collection;
import static org.websoso.WSSServer.collection.domain.QCollectionLike.collectionLike;
import static org.websoso.WSSServer.collection.domain.QCollectionNovel.collectionNovel;
import static org.websoso.WSSServer.novel.domain.QNovel.novel;
import static org.websoso.WSSServer.user.domain.QAvatarProfile.avatarProfile;
import static org.websoso.WSSServer.user.domain.QUser.user;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.collection.domain.CollectionCursor;
import org.websoso.WSSServer.collection.domain.QCollectionNovel;
import org.websoso.WSSServer.collection.repository.projection.CollectionDetailRow;
import org.websoso.WSSServer.collection.repository.projection.CollectionNovelPreviewRow;
import org.websoso.WSSServer.collection.repository.projection.CollectionNovelRow;
import org.websoso.WSSServer.collection.repository.projection.CollectionPreviewRow;
import org.websoso.WSSServer.domain.common.SortCriteria;
import org.websoso.WSSServer.novel.domain.QNovel;

@Repository
@RequiredArgsConstructor
public class CollectionQueryRepositoryImpl implements CollectionQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<CollectionPreviewRow> findCollectionPreviewRows(Long ownerId, boolean includePrivate,
                                                                CollectionCursor cursor, int limit) {
        QNovel representativeNovel = new QNovel("representativeNovel");

        return jpaQueryFactory
                .select(Projections.constructor(
                        CollectionPreviewRow.class,
                        collection.collectionId,
                        collection.name,
                        collection.description,
                        collection.isPublic,
                        collection.createdDate,
                        novelCount(),
                        representativeNovel.novelId,
                        representativeNovel.title,
                        representativeNovel.novelImage,
                        representativeNovel.author
                ))
                .from(collection)
                .leftJoin(representativeNovel)
                .on(collection.representativeNovelId.eq(representativeNovel.novelId))
                .where(
                        collection.user.userId.eq(ownerId),
                        onlyPublic(includePrivate),
                        afterCursor(cursor)
                )
                .orderBy(collection.createdDate.desc(), collection.collectionId.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public long countVisibleCollections(Long ownerId, boolean includePrivate) {
        Long count = jpaQueryFactory
                .select(collection.collectionId.count())
                .from(collection)
                .where(
                        collection.user.userId.eq(ownerId),
                        onlyPublic(includePrivate)
                )
                .fetchOne();

        return count == null ? 0L : count;
    }

    @Override
    public List<CollectionNovelPreviewRow> findRecentNovelPreviewRows(List<Long> collectionIds, int previewSize) {
        if (collectionIds.isEmpty()) {
            return List.of();
        }

        return jpaQueryFactory
                .select(Projections.constructor(
                        CollectionNovelPreviewRow.class,
                        collectionNovel.collection.collectionId,
                        novel.novelId,
                        novel.title,
                        novel.novelImage,
                        novel.author
                ))
                .from(collectionNovel)
                .join(collectionNovel.novel, novel)
                .where(
                        collectionNovel.collection.collectionId.in(collectionIds),
                        withinPreview(previewSize)
                )
                .orderBy(
                        collectionNovel.collection.collectionId.asc(),
                        collectionNovel.displayOrder.asc()
                )
                .fetch();
    }

    @Override
    public Optional<CollectionDetailRow> findCollectionDetailRow(Long collectionId) {
        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.constructor(
                        CollectionDetailRow.class,
                        collection.collectionId,
                        collection.name,
                        collection.description,
                        collection.isPublic,
                        user.userId,
                        user.nickname,
                        avatarProfile.avatarProfileImage,
                        collection.representativeNovelId,
                        likeCount()
                ))
                .from(collection)
                .join(collection.user, user)
                .join(avatarProfile).on(user.avatarProfileId.eq(avatarProfile.avatarProfileId))
                .where(collection.collectionId.eq(collectionId))
                .fetchOne());
    }

    @Override
    public List<CollectionNovelRow> findCollectionNovelRows(Long collectionId, SortCriteria sortCriteria) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        CollectionNovelRow.class,
                        novel.novelId,
                        novel.title,
                        novel.novelImage,
                        novel.author
                ))
                .from(collectionNovel)
                .join(collectionNovel.novel, novel)
                .where(collectionNovel.collection.collectionId.eq(collectionId))
                .orderBy(displayOrder(sortCriteria))
                .fetch();
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
     * 컬렉션이 받은 좋아요 수. 상세 쿼리 안의 서브 쿼리이므로 상세를 그리는 쿼리 수가 늘지 않는다.
     * 컬렉션이 비공개로 바뀌어도 좋아요 데이터를 지우지 않으므로 이 값은 공개 여부와 무관하게 유지된다.
     */
    private JPQLQuery<Long> likeCount() {
        return JPAExpressions
                .select(collectionLike.collectionLikeId.count())
                .from(collectionLike)
                .where(collectionLike.collection.collectionId.eq(collection.collectionId));
    }

    /**
     * 본인 조회가 아니면 공개 컬렉션만 보여 준다.
     */
    private BooleanExpression onlyPublic(boolean includePrivate) {
        if (includePrivate) {
            return null;
        }

        return collection.isPublic.isTrue();
    }

    /**
     * 커서보다 뒤에 오는 컬렉션만 고른다. 정렬이 (생성 시점 내림차순, 식별자 내림차순)이므로
     * 생성 시점이 더 이르거나, 생성 시점이 같으면 식별자가 더 작은 컬렉션이 다음 페이지다.
     * 커서 값 자체를 조건에 넣으므로 커서로 쓰던 컬렉션이 삭제돼도 페이지가 어긋나지 않는다.
     */
    private BooleanExpression afterCursor(CollectionCursor cursor) {
        if (cursor == null) {
            return null;
        }

        return collection.createdDate.lt(cursor.createdDate())
                .or(collection.createdDate.eq(cursor.createdDate())
                        .and(collection.collectionId.lt(cursor.collectionId())));
    }

    /**
     * 컬렉션마다 표시 순서 앞쪽 {@code previewSize}개만 남긴다.
     * <p>
     * 표시 순서는 컬렉션 안에서 {@code 0}부터 연속하므로(컬렉션 작품 엔티티가 보장하는 불변식이다)
     * 컬렉션별 상위 N개가 {@code displayOrder < previewSize} 하나로 정확히 표현된다. 윈도 함수도,
     * 자기보다 앞선 행을 세는 상관 서브 쿼리도 필요 없고 인덱스 {@code (collection_id, display_order)}를
     * 그대로 탄다. 컬렉션마다 미리보기를 따로 조회하지 않으므로 목록 크기에 비례해 쿼리가 늘지 않고,
     * 컬렉션당 최대 {@code previewSize}행만 돌아온다.
     */
    private BooleanExpression withinPreview(int previewSize) {
        return collectionNovel.displayOrder.lt(previewSize);
    }

    /**
     * 포함 작품의 순서는 클라이언트가 저장해 둔 표시 순서 하나로 정해진다. 앞쪽이 최신·우선이므로
     * 최신순은 오름차순이고, 오래된순은 그 역순이다. 표시 순서는 컬렉션 안에서 중복되지 않으므로
     * 보조 정렬 기준이 없어도 순서가 결정적이다.
     */
    private OrderSpecifier<?> displayOrder(SortCriteria sortCriteria) {
        if (sortCriteria != null && sortCriteria.isOld()) {
            return new OrderSpecifier<>(Order.DESC, collectionNovel.displayOrder);
        }

        return new OrderSpecifier<>(Order.ASC, collectionNovel.displayOrder);
    }
}
