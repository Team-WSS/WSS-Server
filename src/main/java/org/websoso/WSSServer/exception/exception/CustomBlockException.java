package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.CustomBlockError;

@Getter
@AllArgsConstructor
public class CustomBlockException extends RuntimeException{

    public CustomBlockException(CustomBlockError customBlockError, String message) {
        super(message);
        this.customBlockError = customBlockError;
    }

    private CustomBlockError customBlockError;
}
