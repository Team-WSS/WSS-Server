package org.websoso.WSSServer.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.user.domain.User;

/**
 * @deprecated 리소스 접근 검증은 각 도메인의 Application 또는 Service 계층에서 처리한다.
 */
@Deprecated
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final ResourceAuthorizationHandler resourceAuthorizationHandler;

    public boolean validate(Long resourceId, User user, Class<?> resourceType) {
        return resourceAuthorizationHandler.authorizeResourceAccess(resourceId, user, resourceType);
    }
}
