package org.websoso.WSSServer.exception.avatar.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.avatar.AvatarErrorCode;

@Getter
@AllArgsConstructor

public class AvatarNotFoundException extends RuntimeException {

    public AvatarNotFoundException(AvatarErrorCode avatarErrorCode, String message) {
        super(message);
        this.avatarErrorCode = avatarErrorCode;
    }

    private AvatarErrorCode avatarErrorCode;
}
