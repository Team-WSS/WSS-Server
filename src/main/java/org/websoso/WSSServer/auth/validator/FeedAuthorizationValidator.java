package org.websoso.WSSServer.auth.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.repository.FeedRepository;

@Component
@RequiredArgsConstructor
public class FeedAuthorizationValidator implements ResourceAuthorizationValidator {

    private final FeedRepository feedRepository;

    @Override
    public boolean hasPermission(Long resourceId, User user) {
        return false;
    }

    @Override
    public Class<?> getResourceType() {
        return Feed.class;
    }
}
