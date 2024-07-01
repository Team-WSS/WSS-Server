package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomAvatarError;

@Getter
public class CustomAvatarException extends AbstractCustomException {

    public CustomAvatarException(CustomAvatarError customAvatarError, String message) {
        super(customAvatarError, message);
    }
}
