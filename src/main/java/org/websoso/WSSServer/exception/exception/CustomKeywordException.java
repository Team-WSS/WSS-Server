package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.CustomKeywordError;

@Getter
@AllArgsConstructor
public class CustomKeywordException extends RuntimeException {

    public CustomKeywordException(CustomKeywordError customKeywordError, String message) {
        super(message);
        this.customKeywordError = customKeywordError;
    }

    private CustomKeywordError customKeywordError;
}
