package org.websoso.WSSServer.feed.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.domain.ReportedFeed;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.domain.common.ReportedType;

@Repository
public interface ReportedFeedRepository extends JpaRepository<ReportedFeed, Long> {

    Boolean existsByFeedAndUserAndReportedType(Feed feed, User user, ReportedType reportedType);

    Integer countByFeedAndReportedType(Feed feed, ReportedType reportedType);

}
