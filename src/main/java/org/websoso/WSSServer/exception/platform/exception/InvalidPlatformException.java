package org.websoso.WSSServer.exception.platform.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.platform.PlatformErrorCode;

@Getter
@AllArgsConstructor
public class InvalidPlatformException extends RuntimeException {

    public InvalidPlatformException(PlatformErrorCode platformErrorCode, String message) {
        super(message);
        this.platformErrorCode = platformErrorCode;
    }

    private PlatformErrorCode platformErrorCode;
}
