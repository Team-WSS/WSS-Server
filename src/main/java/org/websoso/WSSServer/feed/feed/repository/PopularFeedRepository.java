package org.websoso.WSSServer.feed.feed.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.feed.feed.domain.PopularFeed;

@Repository
public interface PopularFeedRepository extends JpaRepository<PopularFeed, Long> {

    Boolean existsByFeed(Feed feed);

}
