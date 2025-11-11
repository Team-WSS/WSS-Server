package org.websoso.WSSServer.auth.validator;

import static org.websoso.WSSServer.exception.error.CustomFeedError.BLOCKED_USER_ACCESS;
import static org.websoso.WSSServer.exception.error.CustomFeedError.FEED_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomFeedError.HIDDEN_FEED_ACCESS;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.exception.exception.CustomFeedException;
import org.websoso.WSSServer.feed.repository.FeedRepository;
import org.websoso.WSSServer.service.BlockService;

@Component
@RequiredArgsConstructor
public class FeedAccessValidator {

    private final FeedRepository feedRepository;
    private final BlockService blockService;

    public boolean canAccess(Long feedId, User user) {
        Feed feed = getFeedOrException(feedId);

        if (feed.getUser().equals(user)) {
            return true;
        }

        if (feed.getIsHidden() || !feed.isVisibleTo(user.getUserId())) {
            throw new CustomFeedException(HIDDEN_FEED_ACCESS, "Cannot access hidden feed.");
        }

        if (blockService.isBlocked(user.getUserId(), feed.getWriterId())) {
            throw new CustomFeedException(BLOCKED_USER_ACCESS,
                    "cannot access this feed because either you or the feed author has blocked the other.");
        }

        return true;
    }

    private Feed getFeedOrException(Long feedId) {
        return feedRepository.findById(feedId)
                .orElseThrow(() -> new CustomFeedException(FEED_NOT_FOUND, "feed with the given id was not found"));
    }
}
