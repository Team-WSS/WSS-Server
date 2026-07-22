package org.websoso.WSSServer.feed.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.feed.report.domain.ReportedFeed;
import org.websoso.WSSServer.domain.common.ReportedType;

@Repository
public interface ReportedFeedRepository extends JpaRepository<ReportedFeed, Long> {

    int countByFeedAndReportedType(Feed feed, ReportedType reportedType);

}
