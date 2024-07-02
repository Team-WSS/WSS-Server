package org.websoso.WSSServer.exception.userNovel.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.userNovel.UserNovelErrorCode;

@Getter
@AllArgsConstructor
public class InvalidUserNovelException extends RuntimeException {

    public InvalidUserNovelException(UserNovelErrorCode userNovelErrorCode, String message) {
        super(message);
        this.userNovelErrorCode = userNovelErrorCode;
    }

    private UserNovelErrorCode userNovelErrorCode;
}
