package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomImageError;

@Getter
public class CustomImageException extends AbstractCustomException {

    public CustomImageException(CustomImageError customImageError, String message) {
        super(customImageError, message);
    }
}
