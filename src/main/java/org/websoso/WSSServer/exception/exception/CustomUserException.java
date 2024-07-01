package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomUserError;

@Getter
public class CustomUserException extends AbstractCustomException {

    public CustomUserException(CustomUserError customUserError, String message) {
        super(customUserError, message);
    }
}
