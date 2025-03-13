package org.websoso.WSSServer.auth.validator;

import org.springframework.stereotype.Component;
import org.websoso.WSSServer.domain.User;

@Component
public class FeedAuthorizationValidator implements ResourceAuthorizationValidator {

    @Override
    public boolean hasPermission(Long resourceId, User user) {
        return false;
    }

    @Override
    public Class<?> getResourceType() {
        return null;
    }
}
