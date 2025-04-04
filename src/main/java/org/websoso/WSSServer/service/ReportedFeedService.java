package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.exception.error.CustomFeedError.ALREADY_REPORTED_FEED;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.ReportedFeed;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.exception.exception.CustomFeedException;
import org.websoso.WSSServer.repository.ReportedFeedRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportedFeedService {

    private final ReportedFeedRepository reportedFeedRepository;

    public void createReportedFeed(Feed feed, User user, ReportedType reportedType) {
        if (reportedFeedRepository.existsByFeedAndUserAndReportedType(feed, user, reportedType)) {
            throw new CustomFeedException(ALREADY_REPORTED_FEED, "feed has already been reported by the user");
        }

        reportedFeedRepository.save(ReportedFeed.create(feed, user, reportedType));
    }

    public int getReportedCountByReportedType(Feed feed, ReportedType reportedType) {
        return reportedFeedRepository.countByFeedAndReportedType(feed, reportedType);
    }

}
