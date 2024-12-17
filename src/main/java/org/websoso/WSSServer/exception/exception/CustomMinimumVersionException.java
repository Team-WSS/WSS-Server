package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomMinimumVersionError;

@Getter
public class CustomMinimumVersionException extends AbstractCustomException {

    public CustomMinimumVersionException(CustomMinimumVersionError customMinimumVersionError, String message) {
        super(customMinimumVersionError, message);
    }
}
