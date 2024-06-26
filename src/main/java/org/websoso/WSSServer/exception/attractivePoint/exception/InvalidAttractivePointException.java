package org.websoso.WSSServer.exception.attractivePoint.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.attractivePoint.AttractivePointErrorCode;

@Getter
@AllArgsConstructor
public class InvalidAttractivePointException extends RuntimeException {

    public InvalidAttractivePointException(AttractivePointErrorCode attractivePointErrorCode, String message) {
        super(message);
        this.attractivePointErrorCode = attractivePointErrorCode;
    }

    private AttractivePointErrorCode attractivePointErrorCode;
}
