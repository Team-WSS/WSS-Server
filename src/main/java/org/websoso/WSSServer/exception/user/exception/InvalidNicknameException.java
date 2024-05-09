package org.websoso.WSSServer.exception.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.user.UserErrorCode;

@Getter
@AllArgsConstructor
public class InvalidNicknameException extends RuntimeException {

    public InvalidNicknameException(UserErrorCode userErrorCode, String message) {
        super(message);
        this.userErrorCode = userErrorCode;
    }

    private UserErrorCode userErrorCode;
}

