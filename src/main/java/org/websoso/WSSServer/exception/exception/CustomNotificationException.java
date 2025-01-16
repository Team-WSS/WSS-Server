package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomNotificationError;

@Getter
public class CustomNotificationException extends AbstractCustomException {

    public CustomNotificationException(CustomNotificationError customNotificationError, String message) {
        super(customNotificationError, message);
    }
}
