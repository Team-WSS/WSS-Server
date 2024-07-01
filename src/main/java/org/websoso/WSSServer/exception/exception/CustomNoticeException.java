package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.CustomNoticeError;

@Getter
@AllArgsConstructor
public class CustomNoticeException extends RuntimeException {

    public CustomNoticeException(CustomNoticeError customNoticeError, String message) {
        super(message);
        this.customNoticeError = customNoticeError;
    }

    private CustomNoticeError customNoticeError;
}
