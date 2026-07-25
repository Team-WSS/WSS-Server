package org.websoso.WSSServer.user.exception;

public class DuplicateBlockException extends CustomBlockException {

    public DuplicateBlockException(CustomBlockError customBlockError, String message) {
        super(customBlockError, message);
    }
}
