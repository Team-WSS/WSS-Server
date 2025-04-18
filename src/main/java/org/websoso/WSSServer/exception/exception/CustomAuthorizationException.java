package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomAuthorizationError;

@Getter
public class CustomAuthorizationException extends AbstractCustomException {
    public CustomAuthorizationException(CustomAuthorizationError customAuthorizationError, String message) {
        super(customAuthorizationError, message);
    }
}
