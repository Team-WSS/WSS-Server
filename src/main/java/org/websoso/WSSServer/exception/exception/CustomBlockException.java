package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomBlockError;

@Getter
public class CustomBlockException extends AbstractCustomException {

    public CustomBlockException(CustomBlockError customBlockError, String message) {
        super(customBlockError, message);
    }
}
