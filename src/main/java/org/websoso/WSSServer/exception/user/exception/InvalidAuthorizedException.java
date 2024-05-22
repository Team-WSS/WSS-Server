package org.websoso.WSSServer.exception.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.user.UserErrorCode;

@Getter
@AllArgsConstructor
public class InvalidAuthorizedException extends RuntimeException {

    public InvalidAuthorizedException(UserErrorCode userErrorCode, String message) {
        super(message);
        this.userErrorCode = userErrorCode;
    }

    private UserErrorCode userErrorCode;
}

