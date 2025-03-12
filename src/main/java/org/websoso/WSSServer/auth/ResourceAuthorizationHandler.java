package org.websoso.WSSServer.auth;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.auth.validator.ResourceAuthorizationValidator;

@Component
public class ResourceAuthorizationHandler {

    private final Map<Class<?>, ResourceAuthorizationValidator> validatorMap = new HashMap<>();

}
