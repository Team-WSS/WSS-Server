package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.PopularFeed;

@Repository
public interface PopularFeedRepository extends JpaRepository<PopularFeed, Long> {

    Boolean existsByFeed(Feed feed);

    List<PopularFeed> findTop9ByOrderByPopularFeedIdDesc();
}
