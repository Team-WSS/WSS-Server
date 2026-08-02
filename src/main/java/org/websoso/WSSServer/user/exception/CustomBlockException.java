package org.websoso.WSSServer.user.exception;

import lombok.Getter;
import org.websoso.common.exception.AbstractCustomException;

@Getter
public class CustomBlockException extends AbstractCustomException {

    public CustomBlockException(CustomBlockError customBlockError, String message) {
        super(customBlockError, message);
    }
}
