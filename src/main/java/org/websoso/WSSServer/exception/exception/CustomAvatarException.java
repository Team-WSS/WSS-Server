package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.AvatarErrorCode;

@Getter
@AllArgsConstructor
public class CustomAvatarException extends RuntimeException {

    public CustomAvatarException(AvatarErrorCode avatarErrorCode, String message) {
        super(message);
        this.avatarErrorCode = avatarErrorCode;
    }

    private AvatarErrorCode avatarErrorCode;
}
