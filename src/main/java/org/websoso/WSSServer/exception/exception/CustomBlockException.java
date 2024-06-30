package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.BlockErrorCode;

@Getter
@AllArgsConstructor
public class CustomBlockException extends RuntimeException{

    public CustomBlockException(BlockErrorCode blockErrorCode, String message) {
        super(message);
        this.blockErrorCode = blockErrorCode;
    }

    private BlockErrorCode blockErrorCode;
}
