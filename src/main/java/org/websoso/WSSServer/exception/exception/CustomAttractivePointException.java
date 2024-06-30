package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.AttractivePointErrorCode;

@Getter
@AllArgsConstructor
public class CustomAttractivePointException extends RuntimeException {

    public CustomAttractivePointException(AttractivePointErrorCode attractivePointErrorCode, String message) {
        super(message);
        this.attractivePointErrorCode = attractivePointErrorCode;
    }

    private AttractivePointErrorCode attractivePointErrorCode;
}
