package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomAppleLoginError;

@Getter
public class CustomAppleLoginException extends AbstractCustomException {

    public CustomAppleLoginException(CustomAppleLoginError customAppleLoginError, String message) {
        super(customAppleLoginError, message);
    }
}
