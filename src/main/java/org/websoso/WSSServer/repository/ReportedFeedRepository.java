package org.websoso.WSSServer.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.ReportedFeed;

@Repository
public interface ReportedFeedRepository extends JpaRepository<ReportedFeed, Long> {

    Optional<ReportedFeed> findByFeed(Feed feed);
}
