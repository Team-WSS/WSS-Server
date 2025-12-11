package org.websoso.WSSServer.auth.validator;

import org.websoso.WSSServer.user.domain.User;

public interface ResourceAuthorizationValidator {

    boolean hasPermission(Long resourceId, User user);

    Class<?> getResourceType();
}
