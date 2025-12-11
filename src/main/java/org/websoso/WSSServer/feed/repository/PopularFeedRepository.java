package org.websoso.WSSServer.feed.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.domain.PopularFeed;

@Repository
public interface PopularFeedRepository extends JpaRepository<PopularFeed, Long>, PopularFeedCustomRepository {

    Boolean existsByFeed(Feed feed);

    List<PopularFeed> findTop9ByOrderByPopularFeedIdDesc();
}
