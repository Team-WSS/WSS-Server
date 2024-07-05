package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomNovelError;

@Getter
public class CustomNovelException extends AbstractCustomException {

    public CustomNovelException(CustomNovelError customNovelError, String message) {
        super(customNovelError, message);
    }
}
