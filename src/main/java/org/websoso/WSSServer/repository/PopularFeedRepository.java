package org.websoso.WSSServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.PopularFeed;

public interface PopularFeedRepository extends JpaRepository<PopularFeed, Long> {

    Boolean existsByFeed(Feed feed);
}
