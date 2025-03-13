package org.websoso.WSSServer.auth.validator;

import static org.websoso.WSSServer.exception.error.CustomFeedError.FEED_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserError.INVALID_AUTHORIZED;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.exception.exception.CustomFeedException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.repository.FeedRepository;

@Component
@RequiredArgsConstructor
public class FeedAuthorizationValidator implements ResourceAuthorizationValidator {

    private final FeedRepository feedRepository;

    @Override
    public boolean hasPermission(Long feedId, User user) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new CustomFeedException(FEED_NOT_FOUND, "feed with the given id was not found"));

        if (!feed.getUser().getUserId()
                .equals(user.getUserId())) {
            throw new CustomUserException(INVALID_AUTHORIZED,
                    "User with ID " + user.getUserId() + " is not the owner of feed " + feed.getFeedId());
        }
        return true;
    }

    @Override
    public Class<?> getResourceType() {
        return Feed.class;
    }
}
