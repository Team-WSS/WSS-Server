package org.websoso.WSSServer.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.domain.User;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final ResourceAuthorizationHandler resourceAuthorizationHandler;

    public boolean validate(Long resourceId, Long userId, Class<?> resourceType) {
        return resourceAuthorizationHandler.authorizeResourceAccess(resourceId, user, resourceType);
    }
}
