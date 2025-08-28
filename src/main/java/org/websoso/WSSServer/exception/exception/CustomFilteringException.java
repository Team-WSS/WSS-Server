package org.websoso.WSSServer.exception.exception;

import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomFilteringError;

public class CustomFilteringException extends AbstractCustomException {

    public CustomFilteringException(CustomFilteringError customFilteringError, String message) {
        super(customFilteringError, message);
    }
}
