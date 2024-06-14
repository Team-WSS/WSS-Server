package org.websoso.WSSServer.exception.comment.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.comment.CommentErrorCode;

@Getter
@AllArgsConstructor
public class InvalidCommentException extends RuntimeException {

    public InvalidCommentException(CommentErrorCode commentErrorCode, String message) {
        super(message);
        this.commentErrorCode = commentErrorCode;
    }

    private CommentErrorCode commentErrorCode;

}
