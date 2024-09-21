package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomAuthError;

@Getter
public class CustomAuthException extends AbstractCustomException {

    public CustomAuthException(CustomAuthError customAuthError, String message) {
        super(customAuthError, message);
    }
}
