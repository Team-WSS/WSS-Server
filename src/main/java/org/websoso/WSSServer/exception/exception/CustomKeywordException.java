package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.common.exception.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomKeywordError;

@Getter
public class CustomKeywordException extends AbstractCustomException {

    public CustomKeywordException(CustomKeywordError customKeywordError, String message) {
        super(customKeywordError, message);
    }
}
