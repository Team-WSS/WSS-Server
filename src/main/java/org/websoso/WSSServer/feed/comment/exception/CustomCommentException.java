package org.websoso.WSSServer.feed.comment.exception;

import lombok.Getter;
import org.websoso.common.exception.AbstractCustomException;

@Getter
public class CustomCommentException extends AbstractCustomException {

    public CustomCommentException(CustomCommentError customCommentError, String message) {
        super(customCommentError, message);
    }
}
