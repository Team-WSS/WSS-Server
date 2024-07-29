package org.websoso.WSSServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.ReportedFeed;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.ReportedType;

@Repository
public interface ReportedFeedRepository extends JpaRepository<ReportedFeed, Long> {

    Boolean existsByFeedAndUser(Feed feed, User user);

    Integer countByFeedAndReportedType(Feed feed, ReportedType reportedType);

}
