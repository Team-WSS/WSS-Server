package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomNotificationTypeError;

@Getter
public class CustomNotificationTypeException extends AbstractCustomException {

    public CustomNotificationTypeException(CustomNotificationTypeError customNotificationTypeError, String message) {
        super(customNotificationTypeError, message);
    }
}
