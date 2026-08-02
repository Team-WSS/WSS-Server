package org.websoso.WSSServer.auth.validator;

import org.websoso.WSSServer.user.domain.User;

/**
 * @deprecated 리소스 접근 검증은 각 도메인의 Application 또는 Service 계층에서 처리한다.
 */
@Deprecated
public interface ResourceAuthorizationValidator {

    boolean hasPermission(Long resourceId, User user);

    Class<?> getResourceType();
}
