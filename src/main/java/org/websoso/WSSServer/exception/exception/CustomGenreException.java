package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomGenreError;

@Getter
public class CustomGenreException extends AbstractCustomException {
    public CustomGenreException(CustomGenreError customGenreError, String message) {
        super(customGenreError, message);
    }
}
