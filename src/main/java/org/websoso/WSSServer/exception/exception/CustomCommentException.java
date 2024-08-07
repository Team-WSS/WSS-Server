package org.websoso.WSSServer.exception.exception;

import lombok.Getter;
import org.websoso.WSSServer.exception.common.AbstractCustomException;
import org.websoso.WSSServer.exception.error.CustomCommentError;

@Getter
public class CustomCommentException extends AbstractCustomException {

    public CustomCommentException(CustomCommentError customCommentError, String message) {
        super(customCommentError, message);
    }
}
