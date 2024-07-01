package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.CustomAttractivePointError;

@Getter
@AllArgsConstructor
public class CustomAttractivePointException extends RuntimeException {

    public CustomAttractivePointException(CustomAttractivePointError customAttractivePointError, String message) {
        super(message);
        this.customAttractivePointError = customAttractivePointError;
    }

    private CustomAttractivePointError customAttractivePointError;
}
