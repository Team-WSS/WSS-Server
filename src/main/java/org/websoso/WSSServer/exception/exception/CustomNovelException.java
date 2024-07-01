package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.CustomNovelError;

@Getter
@AllArgsConstructor
public class CustomNovelException extends RuntimeException {

    public CustomNovelException(CustomNovelError customNovelError, String message) {
        super(message);
        this.customNovelError = customNovelError;
    }

    private CustomNovelError customNovelError;
}
