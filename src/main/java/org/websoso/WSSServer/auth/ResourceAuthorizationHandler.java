package org.websoso.WSSServer.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.auth.validator.ResourceAuthorizationValidator;
import org.websoso.WSSServer.domain.User;

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
                .orElseThrow(RuntimeException::new)
                .hasPermission(resourceId, user);
    }
}
