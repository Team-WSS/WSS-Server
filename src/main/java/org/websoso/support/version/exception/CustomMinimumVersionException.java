package org.websoso.support.version.exception;

import lombok.Getter;
import org.websoso.common.exception.AbstractCustomException;

@Getter
public class CustomMinimumVersionException extends AbstractCustomException {

    public CustomMinimumVersionException(CustomMinimumVersionError customMinimumVersionError, String message) {
        super(customMinimumVersionError, message);
    }
}
