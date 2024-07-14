package org.websoso.WSSServer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.PopularFeed;
import org.websoso.WSSServer.repository.PopularFeedRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class PopularFeedService {

    private final PopularFeedRepository popularFeedRepository;

    public void createPopularFeed(Feed feed) {
        if (!popularFeedRepository.existsByFeed(feed)) {
            popularFeedRepository.save(PopularFeed.create(feed));
        }
    }

}
