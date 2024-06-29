package org.websoso.WSSServer.exception.block.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.block.BlockErrorCode;

@Getter
@AllArgsConstructor
public class AdminBlockRestrictionException extends RuntimeException {

    public AdminBlockRestrictionException(BlockErrorCode blockErrorCode, String message) {
        super(message);
        this.blockErrorCode = blockErrorCode;
    }

    private BlockErrorCode blockErrorCode;
}
