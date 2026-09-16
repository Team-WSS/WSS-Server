package org.websoso.WSSServer.collection.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.collection.domain.CollectionLike;

@Repository
public interface CollectionLikeRepository extends JpaRepository<CollectionLike, Long>,
        CollectionLikeCustomRepository {

    boolean existsByUserIdAndCollectionCollectionId(Long userId, Long collectionId);

    long countByCollectionCollectionId(Long collectionId);
}
