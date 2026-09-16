package org.websoso.WSSServer.collection.repository;

import java.util.List;
import org.websoso.WSSServer.collection.domain.CollectionLikeCursor;
import org.websoso.WSSServer.collection.repository.projection.LikedCollectionRow;

/**
 * 좋아요한 컬렉션 목록 조회 전용 쿼리. 엔티티를 다루는 {@link CollectionLikeCustomRepository}와 분리하고
 * 결과를 projection 레코드로 읽는다.
 */
public interface CollectionLikeQueryRepository {

    /**
     * 조회자가 좋아요한 컬렉션 중 지금 볼 수 있는 컬렉션을 좋아요한 시점 최신순으로 읽는다.
     *
     * @param blockedUserIds 조회자와 어느 방향이든 차단 관계인 사용자 PK. 이 사용자들의 컬렉션은 제외한다.
     */
    List<LikedCollectionRow> findLikedCollectionRows(Long viewerId, List<Long> blockedUserIds,
                                                     CollectionLikeCursor cursor, int limit);

    /**
     * 페이지 크기와 무관하게, 조회자가 좋아요한 컬렉션 중 지금 볼 수 있는 전체 개수를 센다.
     */
    long countLikedCollections(Long viewerId, List<Long> blockedUserIds);
}
