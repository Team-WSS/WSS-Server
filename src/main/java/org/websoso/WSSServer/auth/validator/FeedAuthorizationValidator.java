package org.websoso.WSSServer.auth.validator;

import static org.websoso.WSSServer.exception.error.CustomFeedError.FEED_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserError.INVALID_AUTHORIZED;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.exception.exception.CustomFeedException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.feed.repository.FeedRepository;

@Component
@RequiredArgsConstructor
public class FeedAuthorizationValidator implements ResourceAuthorizationValidator {

    private final FeedRepository feedRepository;

    @Override
    public boolean hasPermission(Long feedId, User user) {
        Feed feed = getFeedOrException(feedId);

        if (!feed.isMine(user.getUserId())) {
            throw new CustomUserException(INVALID_AUTHORIZED,
                    "User with ID " + user.getUserId() + " is not the owner of feed " + feed.getFeedId());
        }
        return true;
    }

    private Feed getFeedOrException(Long feedId) {
        return feedRepository.findById(feedId)
                .orElseThrow(() -> new CustomFeedException(FEED_NOT_FOUND, "feed with the given id was not found"));
    }

    @Override
    public Class<?> getResourceType() {
        return Feed.class;
    }
}
