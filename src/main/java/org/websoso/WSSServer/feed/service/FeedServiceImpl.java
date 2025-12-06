package org.websoso.WSSServer.feed.service;

import static org.websoso.WSSServer.exception.error.CustomFeedError.FEED_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.exception.exception.CustomFeedException;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.repository.FeedRepository;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl {

    private final FeedRepository feedRepository;

    @Transactional(readOnly = true)
    public Feed getFeedOrException(Long feedId) {
        return feedRepository.findById(feedId)
                .orElseThrow(() -> new CustomFeedException(FEED_NOT_FOUND, "feed with the given id was not found"));
    }
}
