package org.websoso.WSSServer.exception.userNovel.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.userNovel.UserNovelErrorCode;

@Getter
@AllArgsConstructor
public class CustomUserNovelException extends RuntimeException {

    public CustomUserNovelException(UserNovelErrorCode userNovelErrorCode, String message) {
        super(message);
        this.userNovelErrorCode = userNovelErrorCode;
    }

    private UserNovelErrorCode userNovelErrorCode;
}
