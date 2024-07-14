package org.websoso.WSSServer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.ReportedFeed;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.repository.ReportedFeedRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportedFeedService {

    private final ReportedFeedRepository reportedFeedRepository;

    public void createReportedFeed(Feed feed, User user) {
        ReportedFeed reportedFeed = reportedFeedRepository.findByFeed(feed)
                .orElse(reportedFeedRepository.save(ReportedFeed.create(feed)));

        reportedFeed.incrementSpoilerCount();
    }

}
