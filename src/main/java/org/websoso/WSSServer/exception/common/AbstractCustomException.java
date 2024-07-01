package org.websoso.WSSServer.exception.common;

import lombok.Getter;

@Getter
public abstract class AbstractCustomException extends RuntimeException {

    private final ICustomError iCustomError;

    public AbstractCustomException(ICustomError iCustomError, String message) {
        super(message);
        this.iCustomError = iCustomError;
    }
}
