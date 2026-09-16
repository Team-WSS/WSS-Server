package org.websoso.WSSServer.collection.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.collection.domain.CollectionLikeCursor;
import org.websoso.WSSServer.collection.repository.CollectionLikeQueryRepository;
import org.websoso.WSSServer.collection.repository.projection.LikedCollectionRow;

/**
 * 좋아요한 컬렉션 목록의 조회 전용 Repository 접근을 캡슐화한다.
 * <p>
 * 유스케이스 전체의 트랜잭션 경계는 {@code CollectionLikeFindApplication}이 소유한다. 이 클래스의 메서드는
 * 모두 조회 전용이므로 {@code readOnly} 속성을 명시하되, 기본 전파 속성을 사용하므로 Application이 연
 * 조회 트랜잭션이 있으면 거기에 참여한다.
 */
@Service
@RequiredArgsConstructor
public class CollectionLikeQueryService {

    private final CollectionLikeQueryRepository collectionLikeQueryRepository;

    @Transactional(readOnly = true)
    public List<LikedCollectionRow> findLikedCollectionRows(Long viewerId, List<Long> blockedUserIds,
                                                            CollectionLikeCursor cursor, int limit) {
        return collectionLikeQueryRepository.findLikedCollectionRows(viewerId, blockedUserIds, cursor, limit);
    }

    @Transactional(readOnly = true)
    public long countLikedCollections(Long viewerId, List<Long> blockedUserIds) {
        return collectionLikeQueryRepository.countLikedCollections(viewerId, blockedUserIds);
    }
}
