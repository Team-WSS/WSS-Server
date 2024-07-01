package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomNoticeError;

@Getter
public class CustomNoticeException extends AbstractCustomException {

    public CustomNoticeException(CustomNoticeError customNoticeError, String message) {
        super(customNoticeError, message);
    }
}
