package org.websoso.WSSServer.feed.feed.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.feed.feed.domain.PopularFeed;
import org.websoso.WSSServer.feed.feed.repository.PopularFeedRepository;

@Service
@RequiredArgsConstructor
public class PopularFeedService {

    private final PopularFeedRepository popularFeedRepository;

    @Transactional
    public void create(Feed feed) {
        popularFeedRepository.save(PopularFeed.create(feed));
    }

    @Transactional(readOnly = true)
    public boolean existByFeed(Feed feed) {
        return popularFeedRepository.existsByFeed(feed);
    }

}
