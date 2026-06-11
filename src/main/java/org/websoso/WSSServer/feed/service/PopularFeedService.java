package org.websoso.WSSServer.feed.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.domain.PopularFeed;
import org.websoso.WSSServer.feed.repository.PopularFeedRepository;

@Service
@RequiredArgsConstructor
public class PopularFeedService {

    private final PopularFeedRepository popularFeedRepository;

    public boolean existByFeed(Feed feed) {
        return popularFeedRepository.existsByFeed(feed);
    }

    public void create(Feed feed) {
        popularFeedRepository.save(PopularFeed.create(feed));
    }

}
