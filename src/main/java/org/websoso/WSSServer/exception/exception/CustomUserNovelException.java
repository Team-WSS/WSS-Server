package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomUserNovelError;

@Getter
public class CustomUserNovelException extends AbstractCustomException {

    public CustomUserNovelException(CustomUserNovelError customUserNovelError, String message) {
        super(customUserNovelError, message);
    }
}
