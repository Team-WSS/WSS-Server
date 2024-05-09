package org.websoso.WSSServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Feed;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {
}
