package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.NovelErrorCode;

@Getter
@AllArgsConstructor
public class CustomNovelException extends RuntimeException {

    public CustomNovelException(NovelErrorCode novelErrorCode, String message) {
        super(message);
        this.novelErrorCode = novelErrorCode;
    }

    private NovelErrorCode novelErrorCode;
}
