package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomAttractivePointError;

@Getter
public class CustomAttractivePointException extends AbstractCustomException {

    public CustomAttractivePointException(CustomAttractivePointError customAttractivePointError, String message) {
        super(customAttractivePointError, message);
    }
