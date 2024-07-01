package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.CustomAvatarError;

@Getter
@AllArgsConstructor
public class CustomAvatarException extends RuntimeException {

    public CustomAvatarException(CustomAvatarError customAvatarError, String message) {
        super(message);
        this.customAvatarError = customAvatarError;
    }

    private CustomAvatarError customAvatarError;
}
