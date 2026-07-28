package org.websoso.WSSServer.auth;

import static org.websoso.WSSServer.exception.error.CustomAuthorizationError.UNSUPPORTED_RESOURCE_TYPE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.auth.validator.ResourceAuthorizationValidator;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.exception.exception.CustomAuthorizationException;

/**
 * @deprecated 리소스 접근 검증은 각 도메인의 Application 또는 Service 계층에서 처리한다.
 */
@Deprecated
@Component
public class ResourceAuthorizationHandler {

    private final Map<Class<?>, ResourceAuthorizationValidator> validatorMap = new HashMap<>();

    @Autowired
    public ResourceAuthorizationHandler(List<ResourceAuthorizationValidator> validators) {
        for (ResourceAuthorizationValidator validator : validators) {
            validatorMap.put(validator.getResourceType(), validator);
        }
    }

    public boolean authorizeResourceAccess(Long resourceId, User user, Class<?> resourceType) {
        return Optional.ofNullable(validatorMap.get(resourceType))
                .orElseThrow(() -> new CustomAuthorizationException(
                        UNSUPPORTED_RESOURCE_TYPE, "Unsupported resource type for authorization"))
                .hasPermission(resourceId, user);
    }
}
