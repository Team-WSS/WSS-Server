package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.CustomUserNovelError;

@Getter
@AllArgsConstructor
public class CustomUserNovelException extends RuntimeException {

    public CustomUserNovelException(CustomUserNovelError customUserNovelError, String message) {
        super(message);
        this.customUserNovelError = customUserNovelError;
    }

    private CustomUserNovelError customUserNovelError;
}
