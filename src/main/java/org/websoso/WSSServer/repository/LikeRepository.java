package org.websoso.WSSServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.Like;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    void deleteByUserIdAndFeed(Long userId, Feed feed);

    boolean existsByUserIdAndFeed(Long userId, Feed feed);
}
