package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.CustomUserError;

@Getter
@AllArgsConstructor
public class CustomUserException extends RuntimeException{

    public CustomUserException(CustomUserError customUserError, String message) {
        super(message);
        this.customUserError = customUserError;
    }

    private CustomUserError customUserError;
}
